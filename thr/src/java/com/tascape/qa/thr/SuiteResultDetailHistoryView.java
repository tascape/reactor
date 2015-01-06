package com.tascape.qa.thr;

import com.tascape.qa.th.db.DbHandler.Suite_Result;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
@Named
@RequestScoped
public class SuiteResultDetailHistoryView implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(SuiteResultDetailHistoryView.class);

    private static final long serialVersionUID = 1L;

    private long startTime = System.currentTimeMillis() - 5184000000L; // two months

    private long stopTime = System.currentTimeMillis() + 86400000L; // one day

    private int numberOfEntries = 100;

    private boolean invisibleIncluded = false;

    private String suiteName = "";

    @Inject
    private MySqlBaseBean db;

    private List<Map<String, Object>> suitesResult;

    private List<Map<String, Map<String, Object>>> suiteHistoryDetail = new ArrayList<>();

    @PostConstruct
    public void init() {
        this.getParameters();

        try {
            this.suitesResult = this.db.getSuitesResult(this.startTime, this.stopTime, this.numberOfEntries,
                this.suiteName, this.invisibleIncluded);
            List<String> suiteResultIds = new ArrayList<>();
            this.suitesResult.stream().forEach(row
                -> suiteResultIds.add(row.get(Suite_Result.SUITE_RESULT_ID.name()) + ""));
            List<Map<String, Object>> testsResult = this.db.getTestsResult(suiteResultIds);

            for (Map<String, Object> tr : testsResult) {
                String suite = tr.get("SUITE_CLASS") + "";
                String clazz = tr.get("TEST_CLASS") + "";
                String method = tr.get("TEST_METHOD") + "";
                String data = tr.get("TEST_DATA") + "";
                String sid = tr.get("SUITE_RESULT") + "";
                String result = tr.get("EXECUTION_RESULT") + "";

                boolean toAdd = true;
                for (Map<String, Map<String, Object>> testHistory : this.suiteHistoryDetail) {
                    if (testHistory.get("SUITE_CLASS").equals(suite)
                        && testHistory.get("TEST_CLASS").equals(clazz)
                        && testHistory.get("TEST_METHOD").equals(method)
                        && testHistory.get("TEST_DATA").equals(data)) {
                        if (testHistory.get(sid) == null) {
                            testHistory.put(sid, tr);
                            toAdd = false;
                            break;
                        }
                    }
                }
                if (toAdd) {
                    Map<String, Map<String, Object>> t = this.wrapTest(tr);
                    this.suiteHistoryDetail.add(t);
                }

            }
        } catch (NamingException | SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Map<String, Map<String, Object>> wrapTest(Map<String, Object> test) {
        Map<String, Map<String, Object>> t = new HashMap<>();
//        t.put("SUITE_CLASS", test.get("SUITE_CLASS"));
//        t.put("_suite_class", StringUtils.substringAfterLast(test.get("SUITE_CLASS") + "", "."));
//        t.put("TEST_CLASS", test.get("TEST_CLASS"));
//        t.put("TEST_METHOD", test.get("TEST_METHOD"));
//        t.put("TEST_DATA", test.get("TEST_DATA"));
//        t.put("EXECUTION_RESULT", test.get("EXECUTION_RESULT"));
//        t.put(test.get("SUITE_RESULT") + "", test);
        return t;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    public boolean isInvisibleIncluded() {
        return invisibleIncluded;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public void setNumberOfEntries(int numberOfEntries) {
        this.numberOfEntries = numberOfEntries;
    }

    public void setInvisibleIncluded(boolean invisibleIncluded) {
        this.invisibleIncluded = invisibleIncluded;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public List<Map<String, Object>> getSuitesResult() {
        return suitesResult;
    }

    private void getParameters() {
        Map<String, String> map = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        LOG.trace("request parameters {}", map);
        String v = map.get("start");
        if (v != null) {
            this.startTime = Long.parseLong(v);
        }
        v = map.get("stop");
        if (v != null) {
            this.stopTime = Long.parseLong(v);
        }
        v = map.get("number");
        if (v != null) {
            this.numberOfEntries = Integer.parseInt(v);
        }
        v = map.get("invisible");
        if (v != null) {
            this.invisibleIncluded = Boolean.parseBoolean(v);
        }
        v = map.get("suite");
        if (v != null) {
            this.suiteName = v;
        }
    }
}
