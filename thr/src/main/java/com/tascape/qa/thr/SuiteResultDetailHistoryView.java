package com.tascape.qa.thr;

import com.tascape.qa.th.db.SuiteResult;
import com.tascape.qa.th.db.TestCase;
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
import org.apache.commons.lang3.StringUtils;
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

    private String jobName = "";

    @Inject
    private MySqlBaseBean db;

    private List<Map<String, Object>> suitesResult;

    private final List<TestCase> testCases = new ArrayList<>();

    private final List<Map<String, Map<String, Object>>> suiteHistoryDetail = new ArrayList<>();

    @PostConstruct
    public void init() {
        this.getParameters();

        try {
            this.suitesResult = this.db.getSuitesResult(this.startTime, this.stopTime, this.numberOfEntries,
                    this.suiteName, this.jobName, this.invisibleIncluded);

            for (Map<String, Object> suiteResult : this.suitesResult) {
                String srid = suiteResult.get(SuiteResult.SUITE_RESULT_ID).toString();

                List<Map<String, Object>> testsResult = this.db.getTestsResult(srid);
                for (Map<String, Object> testResult : testsResult) {

                    boolean toAddOneRow = true;
                    for (Map<String, Map<String, Object>> testHistory : this.suiteHistoryDetail) {
                        Map<String, Object> tr = testHistory.get(srid);
                        if (tr != null) {
                            continue;
                        }

                        TestCase testCase = new TestCase(testResult);
                        TestCase tc = new TestCase(testHistory.get("TEST_CASE"));
                        if (testCase.equals(tc)) {
                            MySqlBaseBean.setLogUrl(testResult);
                            testHistory.put(srid, testResult);
                            toAddOneRow = false;
                            break;
                        }
                    }

                    if (toAddOneRow) {
                        Map<String, Map<String, Object>> testHistory = new HashMap<>();
                        testResult.put("_TEST_CLASS",
                                StringUtils.substringAfterLast(testResult.get("TEST_CLASS").toString(), "."));
                        testHistory.put("TEST_CASE", testResult);
                        MySqlBaseBean.setLogUrl(testResult);
                        testHistory.put(srid, testResult);
                        this.suiteHistoryDetail.add(testHistory); // add one row
                    }
                }
            }
        }
        catch (NamingException | SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<TestCase> getTestCases() {
        return testCases;
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

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
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

    public List<Map<String, Map<String, Object>>> getSuiteHistoryDetail() {
        return suiteHistoryDetail;
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
