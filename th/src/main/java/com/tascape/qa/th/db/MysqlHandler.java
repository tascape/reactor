package com.tascape.qa.th.db;

import com.tascape.qa.th.ExecutionResult;
import com.tascape.qa.th.TestSuite;
import com.tascape.qa.th.Utils;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class MysqlHandler extends DbHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MysqlHandler.class);

    private static final String DB_DRIVER = "com.mysql.jdbc.Driver";

    private static final String DB_HOST = CONFIG.getDatabaseHost();

    private static final String DB_SCHEMA = CONFIG.getDatabaseSchema();

    private static final String DB_USER = CONFIG.getDatabaseUser();

    private static final String DB_PASS = CONFIG.getDatabasePass();

    static {
        try {
            Class.forName(DB_DRIVER).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("Cannot load database driver: " + DB_DRIVER, ex);
        }
    }

    private BoneCP connPool;

    @Override
    protected void init() throws Exception {
        BoneCPConfig connPoolConfig = new BoneCPConfig();
        connPoolConfig.setJdbcUrl("jdbc:mysql://" + DB_HOST + "/" + DB_SCHEMA);
        connPoolConfig.setUsername(DB_USER);
        connPoolConfig.setPassword(DB_PASS);
        connPoolConfig.setMaxConnectionAgeInSeconds(600);
        connPoolConfig.setDefaultAutoCommit(true);
        connPoolConfig.setIdleConnectionTestPeriodInSeconds(30);
        connPoolConfig.setConnectionTestStatement("SELECT 1");
        this.connPool = new BoneCP(connPoolConfig);

        // todo: shutdown pool
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return connPool.getConnection();
    }

    @Override
    public void queueSuiteExecution(TestSuite suite, String execId) throws SQLException {
        LOG.info("Queue test suite {} for execution with execution id {}", suite.getName(), execId);
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

    @Override
    public boolean queueTestSuite(TestSuite suite, String execId) throws SQLException {
        LOG.info("Queueing test suite result with execution id {} ", execId);
        final String sql = "SELECT * FROM " + TABLES.suite_result.name() + " WHERE "
                + Suite_Result.SUITE_RESULT_ID.name() + " = ?";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            stmt.setMaxRows(1);
            stmt.setString(1, execId);
            ResultSet rs = stmt.executeQuery();
            if (rs.first()) {
                LOG.debug("Suite execution {} is already queued ", execId);
                return false;

            } else {
                Long time = System.currentTimeMillis();
                LOG.debug("Queueing suite execution {}", execId);
                rs.moveToInsertRow();

                rs.updateString(Suite_Result.SUITE_RESULT_ID.name(), execId);
                rs.updateString(Suite_Result.SUITE_NAME.name(), "");
                rs.updateString(Suite_Result.JOB_NAME.name(), CONFIG.getJobName());
                rs.updateInt(Suite_Result.JOB_BUILD_NUMBER.name(), CONFIG.getJobBuildNumber());
                rs.updateString(Suite_Result.JOB_BUILD_URL.name(), CONFIG.getJobBuildUrl());
                rs.updateLong(Suite_Result.START_TIME.name(), time);
                rs.updateLong(Suite_Result.STOP_TIME.name(), time);
                rs.updateString(Suite_Result.EXECUTION_RESULT.name(), ExecutionResult.QUEUED.name());
                rs.updateInt(Suite_Result.NUMBER_OF_TESTS.name(), suite.getTests().size());
                rs.updateInt(Suite_Result.NUMBER_OF_FAILURE.name(), suite.getTests().size());
                rs.updateNString(Suite_Result.PRODUCT_UNDER_TEST.name(), CONFIG.getProdUnderTest());

                rs.insertRow();
                rs.last();
                rs.updateRow();
                return true;
            }
        }
    }

    @Override
    protected int getTestCaseId(TestCase test) throws SQLException {
        LOG.info("Query for id of test case {} ", test.format());
        final String sql = "SELECT * FROM " + TABLES.test_case.name() + " WHERE "
                + Test_Case.SUITE_CLASS + " = ? AND "
                + Test_Case.TEST_CLASS + " = ? AND "
                + Test_Case.TEST_METHOD + " = ? AND "
                + Test_Case.TEST_DATA_INFO + " = ? AND "
                + Test_Case.TEST_DATA + " = ?";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, test.getSuiteClass());
            stmt.setString(2, test.getTestClass());
            stmt.setString(3, test.getTestMethod());
            stmt.setString(4, test.getTestDataInfo());
            stmt.setString(5, test.getTestData());
            stmt.setMaxRows(1);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.moveToInsertRow();
                rs.updateString(Test_Case.SUITE_CLASS.name(), test.getSuiteClass());
                rs.updateString(Test_Case.TEST_CLASS.name(), test.getTestClass());
                rs.updateString(Test_Case.TEST_METHOD.name(), test.getTestMethod());
                rs.updateString(Test_Case.TEST_DATA_INFO.name(), test.getTestDataInfo());
                rs.updateString(Test_Case.TEST_DATA.name(), test.getTestData());

                rs.insertRow();
                rs.last();
                rs.updateRow();
            }
            return rs.getInt(Test_Case.TEST_CASE_ID.name());
        }
    }

    @Override
    protected void queueTestCaseResults(String execId, List<TestCase> tests) throws SQLException {
        LOG.info("Queue {} test case result(s) with execution id {} ", tests.size(), execId);
        final String sql = "SELECT * FROM " + TABLES.test_result.name() + " WHERE "
                + Test_Result.SUITE_RESULT + " = ?";
        Map<String, Integer> idMap = this.getTestCaseIds(tests);

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            stmt.setMaxRows(1);
            stmt.setString(1, execId);

            ResultSet rs = stmt.executeQuery();
            boolean autoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                int index = 0;
                for (TestCase test : tests) {
                    index++;
                    rs.moveToInsertRow();

                    Integer tcid = idMap.get(test.format());
                    if (tcid == null) {
                        tcid = this.getTestCaseId(test);
                    }

                    rs.updateString(Test_Result.TEST_RESULT_ID.name(), Utils.getUniqueId("r"));
                    rs.updateString(Test_Result.SUITE_RESULT.name(), execId);
                    rs.updateInt(Test_Result.TEST_CASE_ID.name(), tcid);
                    rs.updateString(Test_Result.EXECUTION_RESULT.name(), ExecutionResult.QUEUED.name());
                    rs.updateLong(Test_Result.START_TIME.name(), System.currentTimeMillis());
                    rs.updateLong(Test_Result.STOP_TIME.name(), System.currentTimeMillis());
                    rs.updateString(Test_Result.TEST_STATION.name(), "?");
                    rs.updateString(Test_Result.LOG_DIR.name(), "?");

                    rs.insertRow();
                    rs.last();
                    rs.updateRow();
                    if (index % 100 == 0) {
                        conn.commit();
                    }
                }
                conn.commit();
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    @Override
    public void updateTestExecutionResult(TestResult tcr) throws SQLException {
        LOG.info("Update test result {} ({}) to {}", tcr.getId(), tcr.getTestCase().format(),
                tcr.getExecutionResult().result());
        final String sql = "SELECT tr.* FROM " + TABLES.test_result.name() + " tr INNER JOIN " + TABLES.test_case.name()
                + " tc WHERE tr.TEST_CASE_ID=tc.TEST_CASE_ID AND "
                + Test_Result.TEST_RESULT_ID.name() + " = ?;";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
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

    @Override
    public void updateSuiteExecutionResult(String execId) throws SQLException {
        LOG.info("Update test suite execution result with execution id {}", execId);
        String lock = "testharness." + execId;

        Connection conn = this.getConnection();
        try {
            if (!this.acquireExecutionLock(conn, lock)) {
                throw new SQLException("Cannot acquire lock of name " + lock);
            }

            int total = 0, fail = 0;
            final String sql1 = "SELECT " + Test_Result.EXECUTION_RESULT.name() + " FROM "
                    + TABLES.test_result.name() + " WHERE " + Test_Result.SUITE_RESULT.name() + " = ?;";
            try (PreparedStatement stmt = this.getConnection().prepareStatement(sql1,
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                stmt.setString(1, execId);
                stmt.setFetchSize(Integer.MIN_VALUE);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    total++;
                    String result = rs.getString(Test_Result.EXECUTION_RESULT.name());
                    if (!result.equals(ExecutionResult.PASS.name()) && !result.endsWith("/0")) {
                        fail++;
                    }
                }
            }

            final String sql2 = "SELECT * FROM " + TABLES.suite_result.name()
                    + " WHERE " + Suite_Result.SUITE_RESULT_ID.name() + " = ?;";
            try (PreparedStatement stmt = this.getConnection().prepareStatement(sql2,
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                stmt.setString(1, execId);
                ResultSet rs = stmt.executeQuery();
                if (rs.first()) {
                    rs.updateInt(Suite_Result.NUMBER_OF_TESTS.name(), total);
                    rs.updateInt(Suite_Result.NUMBER_OF_FAILURE.name(), fail);
                    rs.updateString(Suite_Result.EXECUTION_RESULT.name(), fail == 0 ? "PASS" : "FAIL");
                    rs.updateLong(Suite_Result.STOP_TIME.name(), System.currentTimeMillis());
                    rs.updateRow();
                }
            }
        } finally {
            try {
                this.releaseExecutionLock(conn, lock);
            } finally {
                conn.close();
            }
        }
    }

    @Override
    protected boolean acquireExecutionLock(Connection conn, String lock) throws SQLException {
        final String sqlLock = String.format("SELECT GET_LOCK('%s', 1200);", lock); // 20 minutes
        LOG.debug("Acquire lock {}", lock);
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlLock)) {
            if (rs.next() && "1".equals(rs.getString(1))) {
                LOG.trace("{} is locked", lock);
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean releaseExecutionLock(Connection conn, String lock) throws SQLException {
        final String sqlRelease = String.format("SELECT RELEASE_LOCK('%s');", lock);
        LOG.debug("Release lock {}", lock);
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlRelease)) {
            if (rs.next() && "1".equals(rs.getString(1))) {
                LOG.trace("{} is released", lock);
            } else {
                return false;
            }
        } finally {
            conn.close();
        }
        return true;
    }

    public static void main(String[] args) throws SQLException {
        MysqlHandler db = new MysqlHandler();
        TestCase tc = new TestCase();
        tc.setSuiteClass("a");
        LOG.debug("test case id = {}", db.getTestCaseId(tc));
    }
}
