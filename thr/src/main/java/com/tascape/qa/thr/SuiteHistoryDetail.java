package com.tascape.qa.thr;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.naming.NamingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong
 */
@ManagedBean(name = "suiteHistoryDetail", eager = false)
@RequestScoped
public class SuiteHistoryDetail extends ResultBase {
    private static final Logger LOG = LoggerFactory.getLogger(SuiteHistoryDetail.class);

    @ManagedProperty(value = "#{param.suiteids}")
    private String suiteIds;

    private final Map<String, String> suiteIdsDisplay = new HashMap<>();

    @ManagedProperty(value = "#{param.failonly}")
    private String failOnly;

    private List<Map<String, Object>> suiteHistory;

    private List<Map<String, Object>> suiteHistoryDetailList;

    private Map<String, Map<String, Object>> suiteMap;

    public void loadSuiteHistoryDetail() throws SQLException, NamingException {
        String[] ids = this.suiteIds.split(",");
        String[] sids = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            sids[i] = "\"" + ids[i] + "\"";
        }
        String[] sidsDiaplay = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            sidsDiaplay[i] = StringUtils.right(ids[i], 8);
            this.suiteIdsDisplay.put(ids[i], sidsDiaplay[i]);
        }
        {
            try (Connection conn = this.getConnection()) {
                String sql = "SELECT * FROM test_result tr INNER JOIN test_case tc "
                        + "WHERE tr.TEST_CASE_ID = tc.TEST_CASE_ID AND SUITE_RESULT IN ("
                        + StringUtils.join(sids, ",")
                        + ") ORDER BY SUITE_CLASS, TEST_CLASS, TEST_METHOD, TEST_DATA_INFO;";
                LOG.trace(sql);
                Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(Integer.MIN_VALUE);
                ResultSet rs = stmt.executeQuery(sql);
                this.suiteHistoryDetailList = this.dumpResultSetToList(rs);
            }
        }
        this.reorder();
        {
            try (Connection conn = this.getConnection()) {
                String sql = "SELECT SUITE_RESULT_ID, START_TIME FROM suite_result WHERE SUITE_RESULT_ID IN ("
                        + StringUtils.join(sids, ",") + ");";
                LOG.trace(sql);
                Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(Integer.MIN_VALUE);
                ResultSet rs = stmt.executeQuery(sql);
                this.suiteMap = this.dumpResultSetToMap(rs, "SUITE_RESULT_ID");
            }
        }
    }

    public Map<String, String> getSuiteIdsDisplay() {
        return suiteIdsDisplay;
    }

    private void reorder() {
        this.suiteHistory = new ArrayList<>();
        int index = 1;
        for (Map<String, Object> test : this.suiteHistoryDetailList) {
            test.put("_log_url", this.getLogUrl(test.get("TEST_STATION") + "", test.get("LOG_DIR") + ""));

            String suite = test.get("SUITE_CLASS") + "";
            String clazz = test.get("TEST_CLASS") + "";
            String method = test.get("TEST_METHOD") + "";
            String data = test.get("TEST_DATA") + "";
            String sid = test.get("SUITE_RESULT") + "";
            String result = test.get("EXECUTION_RESULT") + "";

            boolean toAdd = true;
            for (Map<String, Object> testHistory : this.suiteHistory) {
                if (testHistory.get("SUITE_CLASS").equals(suite)
                        && testHistory.get("TEST_CLASS").equals(clazz)
                        && testHistory.get("TEST_METHOD").equals(method)
                        && testHistory.get("TEST_DATA").equals(data)) {
                    if (testHistory.get(sid) == null) {
                        testHistory.put(sid, test);
                        if (!testHistory.get("EXECUTION_RESULT").equals(result)) {
                            testHistory.put("_row_result", false);
                        }
                        toAdd = false;
                        break;
                    }
                }
            }
            if (toAdd) {
                Map<String, Object> t = this.wrapTest(test);
                t.put("_row_index", index);
                t.put("_row_result", true);
                this.suiteHistory.add(t);
                LOG.trace("add {} - {}", index, t);
                index++;
            }
        }

        if (Boolean.parseBoolean(this.failOnly)) {
            List<Map<String, Object>> failedTests = new ArrayList<>();
            String[] ids = StringUtils.split(suiteIds, ",");
            index = 1;
            for (Map<String, Object> testHistory : this.suiteHistory) {
                for (String id : ids) {
                    Map<String, Object> test = (Map<String, Object>) testHistory.get(id);
                    if (test == null) {
                        continue;
                    }
                    String result = test.get("EXECUTION_RESULT") + "";
                    if (!result.equals("PASS") && !result.endsWith("/0")) {
                        testHistory.put("_row_index", index++);
                        failedTests.add(testHistory);
                        break;
                    }
                }
            }
            this.suiteHistory = failedTests;
        }
    }

    private Map<String, Object> wrapTest(Map<String, Object> test) {
        Map<String, Object> t = new HashMap<>();
        t.put("SUITE_CLASS", test.get("SUITE_CLASS"));
        t.put("_suite_class", StringUtils.substringAfterLast(test.get("SUITE_CLASS") + "", "."));
        t.put("TEST_CLASS", test.get("TEST_CLASS"));
        t.put("TEST_METHOD", test.get("TEST_METHOD"));
        t.put("TEST_DATA", test.get("TEST_DATA"));
        t.put("EXECUTION_RESULT", test.get("EXECUTION_RESULT"));
        t.put(test.get("SUITE_RESULT") + "", test);
        return t;
    }

    public String getSuiteIds() {
        return suiteIds;
    }

    public void setSuiteIds(String suiteIds) {
        this.suiteIds = suiteIds;
    }

    public List<Map<String, Object>> getSuiteHistory() {
        return suiteHistory;
    }

    public void setSuiteHistory(List<Map<String, Object>> suiteHistory) {
        this.suiteHistory = suiteHistory;
    }

    public String getFailOnly() {
        return failOnly;
    }

    public void setFailOnly(String failOnly) {
        this.failOnly = failOnly;
    }

    public Map<String, Map<String, Object>> getSuiteMap() {
        return suiteMap;
    }

    public void setSuiteMap(Map<String, Map<String, Object>> suiteMap) {
        this.suiteMap = suiteMap;
    }
}
