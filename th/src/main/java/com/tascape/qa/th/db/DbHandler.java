package com.tascape.qa.th.db;

import com.tascape.qa.th.ExecutionResult;
import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.TestSuite;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class DbHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DbHandler.class);

    static final SystemConfiguration CONFIG = SystemConfiguration.getInstance();

    public static final String SYSPROP_DATABASE_TYPE = "qa.th.db.type";

    public static final String SYSPROP_DATABASE_HOST = "qa.th.db.host";

    public static final String SYSPROP_DATABASE_SCHEMA = "qa.th.db.schema";

    public static final String SYSPROP_DATABASE_USER = "qa.th.db.user";

    public static final String SYSPROP_DATABASE_PASS = "qa.th.db.pass";

    public enum TABLES {
        suite_result,
        test_case,
        test_result,
        test_result_metric,
    }

    public enum Suite_Result {
        SUITE_RESULT_ID,
        SUITE_NAME,
        JOB_NAME,
        JOB_BUILD_NUMBER,
        JOB_BUILD_URL,
        EXECUTION_RESULT,
        START_TIME,
        STOP_TIME,
        NUMBER_OF_TESTS,
        NUMBER_OF_FAILURE,
        INVISIBLE_ENTRY,
        PRODUCT_UNDER_TEST,
    }

    public enum Test_Case {
        TEST_CASE_ID,
        SUITE_CLASS,
        TEST_CLASS,
        TEST_METHOD,
        TEST_DATA_INFO,
        TEST_DATA,
        TEST_ISSUES,
    }

    public enum Test_Result {
        TEST_RESULT_ID,
        SUITE_RESULT,
        TEST_CASE_ID,
        EXECUTION_RESULT,
        AUT,
        START_TIME,
        STOP_TIME,
        RETRY,
        TEST_STATION,
        LOG_DIR,
    }

    public enum Test_Result_Metric {
        TEST_RESULT_METRIC_ID,
        TEST_RESULT_ID,
        METRIC_GROUP,
        METRIC_NAME,
        METRIC_VALUE,
    }

    public static DbHandler getInstance() {
        String type = SystemConfiguration.getInstance().getDatabaseType();
        DbHandler dbh;
        switch (type) {
            case "h2":
                dbh = new H2Handler();
                break;
            case "postgresql":
                dbh = new PostgresqlHandler();
                break;
            case "mysql":
                dbh = new MysqlHandler();
                break;
            default:
                dbh = new H2Handler();
        }
        try {
            dbh.init();
        } catch (Exception ex) {
            throw new RuntimeException("Cannot connect to db", ex);
        }
        return dbh;
    }

    protected abstract void init() throws Exception;

    protected abstract Connection getConnection() throws SQLException;

    public SuiteResult getSuiteResult(String id) throws SQLException {
        LOG.info("Query for suite result with execution id {}", id);
        final String sql = "SELECT * FROM " + TABLES.suite_result.name() + " WHERE "
            + Suite_Result.SUITE_RESULT_ID.name() + " = ?";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setMaxRows(1);
            stmt.setString(1, id);

            ResultSet rs = stmt.executeQuery();
            SuiteResult tsr = new SuiteResult();
            if (rs.first()) {
                tsr.setId(rs.getString(Suite_Result.SUITE_RESULT_ID.name()));
                tsr.setJobName(rs.getString(Suite_Result.JOB_NAME.name()));
                tsr.setJobBuildNumber(rs.getInt(Suite_Result.JOB_BUILD_NUMBER.name()));
                tsr.setJobBuildUrl(rs.getString(Suite_Result.JOB_BUILD_URL.name()));
            } else {
                LOG.warn("no suite result with execution id {}", id);
            }
            return tsr;
        }
    }

    public void queueSuiteExecution(TestSuite suite, String execId) throws SQLException {
        LOG.info("Queue test suite for execution with execution id {}", execId);
        String lock = "testharness." + execId;
        try (Connection conn = this.getConnection()) {
            try {
                if (!this.acquireExecutionLock(conn, lock)) {
                    throw new SQLException("Cannot acquire lock of name " + lock);
                }

                if (this.queueTestSuite(suite, execId)) {
                    this.queueTestCaseResults(execId, suite.getTests());
                }
            } finally {
                this.releaseExecutionLock(conn, lock);
            }
        }
    }

    public abstract boolean queueTestSuite(TestSuite suite, String execId) throws SQLException;

    protected abstract int getTestCaseId(TestCase test) throws SQLException;

    protected Map<String, Integer> getTestCaseIds(List<TestCase> tests) throws SQLException {
        Set<String> suiteClasses = new HashSet<>();
        tests.stream().forEach((tc) -> {
            suiteClasses.add(tc.getSuiteClass());
        });

        Map<String, Integer> idMap = new HashMap<>();
        String sql = "SELECT * FROM " + TABLES.test_case + " WHERE ";
        String sql0 = "";
        for (String sc : suiteClasses) {
            sql0 += " OR " + Test_Case.SUITE_CLASS.name() + "='" + sc + "'";
        }
        sql += sql0.substring(4);
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            LOG.debug("{}", stmt.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TestCase tc = new TestCase();
                tc.setSuiteClass(rs.getString(Test_Case.SUITE_CLASS.name()));
                tc.setTestClass(rs.getString(Test_Case.TEST_CLASS.name()));
                tc.setTestMethod(rs.getString(Test_Case.TEST_METHOD.name()));
                tc.setTestDataInfo(rs.getString(Test_Case.TEST_DATA_INFO.name()));
                tc.setTestData(rs.getString(Test_Case.TEST_DATA.name()));
                idMap.put(tc.format(), rs.getInt(Test_Case.TEST_CASE_ID.name()));
            }
            LOG.debug("Found {} tests exist", idMap.size());
            return idMap;
        }
    }

    protected abstract void queueTestCaseResults(String execId, List<TestCase> tests) throws SQLException;

    public List<TestResult> getQueuedTestCaseResults(String execId, int limit) throws SQLException {
        LOG.info("Query database for all queued test cases");
        final String sql = "SELECT * FROM " + TABLES.test_result.name() + " tr "
            + "INNER JOIN " + TABLES.test_case.name() + " tc "
            + "ON tr.TEST_CASE_ID=tc.TEST_CASE_ID AND " + Test_Result.EXECUTION_RESULT.name() + " = ? "
            + "WHERE " + Test_Result.SUITE_RESULT.name() + " = ? "
            + "ORDER BY SUITE_CLASS, TEST_CLASS, TEST_METHOD, TEST_DATA_INFO "
            + "LIMIT ?;";
        List<TestResult> tcrs = new ArrayList<>();

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, ExecutionResult.QUEUED.name());
            stmt.setString(2, execId);
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TestResult tcr = new TestResult();
                tcr.setId(rs.getString(Test_Result.TEST_RESULT_ID.name()));
                tcr.setSuiteResult(execId);
                tcr.setStartTime(rs.getLong(Test_Result.START_TIME.name()));
                tcr.setStopTime(rs.getLong(Test_Result.STOP_TIME.name()));
                tcr.setRetry(rs.getInt(Test_Result.RETRY.name()));

                TestCase tc = new TestCase();
                tc.setSuiteClass(rs.getString(Test_Case.SUITE_CLASS.name()));
                tc.setTestClass(rs.getString(Test_Case.TEST_CLASS.name()));
                tc.setTestMethod(rs.getString(Test_Case.TEST_METHOD.name()));
                tc.setTestDataInfo(rs.getString(Test_Case.TEST_DATA_INFO.name()));
                tc.setTestData(rs.getString(Test_Case.TEST_DATA.name()));
                tcr.setTestCase(tc);

                tcr.setHost(rs.getString(Test_Result.TEST_STATION.name()));
                tcr.setLogDirectory(rs.getString(Test_Result.LOG_DIR.name()));
                tcrs.add(tcr);
            }
        }

        int num = tcrs.size();
        LOG.debug("Found {} test case{} in DB with QUEUED state", num, num > 1 ? "s" : "");
        return tcrs;
    }

    public boolean acquireTestCaseResult(TestResult tcr) throws SQLException {
        LOG.info("Acquire test case {}", tcr.getTestCase().format());
        final String sql = "SELECT * FROM " + TABLES.test_result.name() + " WHERE "
            + Test_Result.TEST_RESULT_ID.name() + " = ? LIMIT 1;";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, tcr.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String host = rs.getString(Test_Result.TEST_STATION.name());
                if (rs.getString(Test_Result.EXECUTION_RESULT.name()).equals(ExecutionResult.QUEUED.result())) {
                    LOG.debug("Found test case {} in DB with QUEUED state", tcr.getTestCase().format());
                    if (CONFIG.getHostName().equals(host)) {
                        LOG.debug("This test case Failed on current host, and was requeue. Skip...");
                        return false;
                    }
                    rs.updateString(Test_Result.EXECUTION_RESULT.name(), ExecutionResult.ACQUIRED.result());
                    rs.updateString(Test_Result.TEST_STATION.name(), CONFIG.getHostName());
                    rs.updateRow();
                    return true;
                } else {
                    LOG.debug("Test case {} was acquired by {}", tcr.getTestCase().format(), host);
                }
            }
        }
        return false;
    }

    public void updateTestExecutionResult(TestResult tcr) throws SQLException {
        LOG.info("Update test result {} ({}) to {}", tcr.getId(), tcr.getTestCase().format(),
            tcr.getExecutionResult().result());
        final String sql = "SELECT tr.* FROM " + TABLES.test_result.name() + " tr INNER JOIN " + TABLES.test_case.name()
            + " tc WHERE tr.TEST_CASE_ID=tc.TEST_CASE_ID AND "
            + Test_Result.TEST_RESULT_ID.name() + " = ?;";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, tcr.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.first()) {
// TODO: update test data into test case table
//                rs.updateString(Test_Case.TEST_DATA.name(), tcr.getTestCase().getTestData());
                rs.updateString(Test_Result.EXECUTION_RESULT.name(), tcr.getExecutionResult().result());
                rs.updateString(Test_Result.AUT.name(), tcr.getAut());
                rs.updateLong(Test_Result.START_TIME.name(), tcr.getStartTime());
                rs.updateLong(Test_Result.STOP_TIME.name(), tcr.getStopTime());
                rs.updateInt(Test_Result.RETRY.name(), tcr.getRetry());
                rs.updateString(Test_Result.TEST_STATION.name(), tcr.getHost());
                rs.updateString(Test_Result.LOG_DIR.name(), tcr.getLogDirectory());
                rs.updateRow();
            } else {
                LOG.warn("Cannot update test result");
            }
        }
    }

    public abstract void updateSuiteExecutionResult(String execId) throws SQLException;

    public void saveTestResultMetrics(String trid, List<TestResultMetric> resultMetrics) throws SQLException {
        final String sql = "SELECT * FROM " + TABLES.test_result_metric.name() + ";";
        try (Connection conn = this.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            stmt.setMaxRows(1);
            LOG.trace("save metric data {}", resultMetrics);
            ResultSet rs = stmt.executeQuery();

            for (TestResultMetric metric : resultMetrics) {
                rs.moveToInsertRow();

                rs.updateString(Test_Result_Metric.TEST_RESULT_ID.name(), trid);
                rs.updateString(Test_Result_Metric.METRIC_GROUP.name(), metric.getMetricGroup());
                rs.updateString(Test_Result_Metric.METRIC_NAME.name(), metric.getMetricName());
                rs.updateDouble(Test_Result_Metric.METRIC_VALUE.name(), metric.getMetricValue());

                rs.insertRow();
                rs.last();
                rs.updateRow();
            }
        }
    }

    protected int hash(String string) {
        char[] chars = string.toCharArray();
        int hash = 7;
        for (int i = 0; i < chars.length; i++) {
            hash = hash * 31 + chars[i];
        }
        return hash;
    }

    protected abstract boolean acquireExecutionLock(Connection conn, String lock) throws SQLException;

    protected abstract boolean releaseExecutionLock(Connection conn, String lock) throws SQLException;

    public static void main(String[] args) throws SQLException {
        DbHandler db = DbHandler.getInstance();
        TestCase tc = new TestCase();
        tc.setSuiteClass("a");
        LOG.debug("test case id = {}", db.getTestCaseId(tc));
    }
}
