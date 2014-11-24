package com.tascape.qa.th.db;

import com.tascape.qa.th.ExecutionResult;
import com.tascape.qa.th.TestSuite;
import com.tascape.qa.th.Utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public final class H2Handler extends DbHandler {
    private static final Logger LOG = LoggerFactory.getLogger(H2Handler.class);

    private static final String DB_DRIVER = "org.h2.Driver";

    static {
        try {
            Class.forName(DB_DRIVER).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("Cannot load database driver: " + DB_DRIVER, ex);
        }
    }

    private final String dbPath = CONFIG.getRootPath() + "/db/";

    private JdbcConnectionPool connPool;

    @Override
    public void init() throws Exception {
        File dir = new File(this.dbPath);
        if (dir.exists()) {
            FileUtils.cleanDirectory(dir);
        }
        this.connPool = JdbcConnectionPool.create("jdbc:h2:" + this.dbPath + CONFIG.getExecId(), "sa", "sa");
        connPool.setMaxConnections(10);
        try (Connection conn = this.getConnection()) {
            try {
                conn.prepareStatement("SELECT * FROM test_result WHERE 0;").executeQuery();
            } catch (SQLException ex) {
                LOG.warn("{}", ex.getMessage());
                this.initSchema();
            }
        }
    }

    @Override
    public boolean queueTestSuite(TestSuite suite, String execId) throws SQLException {
        LOG.info("Queueing test suite result with execution id {} ", execId);
        final String sql = "INSERT INTO " + DbHandler.TABLES.suite_result.name() + " ("
                + Suite_Result.SUITE_RESULT_ID.name() + ", "
                + Suite_Result.SUITE_NAME.name() + ", "
                + Suite_Result.JOB_NAME.name() + ", "
                + Suite_Result.JOB_BUILD_NUMBER.name() + ", "
                + Suite_Result.JOB_BUILD_URL.name() + ", "
                + Suite_Result.START_TIME.name() + ", "
                + Suite_Result.STOP_TIME.name() + ", "
                + Suite_Result.EXECUTION_RESULT.name() + ", "
                + Suite_Result.NUMBER_OF_TESTS.name() + ", "
                + Suite_Result.NUMBER_OF_FAILURE.name() + ", "
                + Suite_Result.PRODUCT_UNDER_TEST.name()
                + ") VALUES (?,?,?,?,?,?,?,?,?,?,?);";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            Long time = System.currentTimeMillis();
            stmt.setString(1, execId);
            stmt.setString(2, "");
            stmt.setString(3, CONFIG.getJobName());
            stmt.setInt(4, CONFIG.getJobBuildNumber());
            stmt.setString(5, CONFIG.getJobBuildUrl());
            stmt.setLong(6, time);
            stmt.setLong(7, time + 11);
            stmt.setString(8, ExecutionResult.QUEUED.name());
            stmt.setInt(9, suite.getTests().size());
            stmt.setInt(10, suite.getTests().size());
            stmt.setString(11, CONFIG.getProdUnderTest());
            LOG.debug("{}", stmt);
            int i = stmt.executeUpdate();
            return i == 1;
        }
    }

    @Override
    protected void queueTestCaseResults(String execId, List<TestCase> tests) throws SQLException {
        LOG.info("Queue {} test case result(s) with execution id {} ", tests.size(), execId);
        final String sql = "INSERT INTO " + TABLES.test_result.name() + " ("
                + Test_Result.TEST_RESULT_ID.name() + ", "
                + Test_Result.SUITE_RESULT.name() + ", "
                + Test_Result.TEST_CASE_ID.name() + ", "
                + Test_Result.EXECUTION_RESULT.name() + ", "
                + Test_Result.START_TIME.name() + ", "
                + Test_Result.STOP_TIME.name() + ", "
                + Test_Result.TEST_STATION.name() + ", "
                + Test_Result.LOG_DIR.name()
                + ") VALUES (?,?,?,?,?,?,?,?);";
        Map<String, Integer> idMap = this.getTestCaseIds(tests);

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            int index = 0;
            for (TestCase test : tests) {
                Integer tcid = idMap.get(test.format());
                if (tcid == null) {
                    tcid = this.getTestCaseId(test);
                }

                Long time = System.currentTimeMillis();
                stmt.setString(1, Utils.getUniqueId("r"));
                stmt.setString(2, execId);
                stmt.setInt(3, tcid);
                stmt.setString(4, ExecutionResult.QUEUED.name());
                stmt.setLong(5, time);
                stmt.setLong(6, time + 11);
                stmt.setString(7, "?");
                stmt.setString(8, "?");
                LOG.debug("{}", stmt);
                int i = stmt.executeUpdate();
            }
        }
    }

    @Override
    protected int getTestCaseId(TestCase test) throws SQLException {
        LOG.info("Query for id of test case {} ", test.format());
        try (Connection conn = this.getConnection()) {
            final String sql = "SELECT * FROM " + TABLES.test_case.name() + " WHERE "
                    + Test_Case.SUITE_CLASS + " = ? AND "
                    + Test_Case.TEST_CLASS + " = ? AND "
                    + Test_Case.TEST_METHOD + " = ? AND "
                    + Test_Case.TEST_DATA_INFO + " = ? AND "
                    + Test_Case.TEST_DATA + " = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, test.getSuiteClass());
            stmt.setString(2, test.getTestClass());
            stmt.setString(3, test.getTestMethod());
            stmt.setString(4, test.getTestDataInfo());
            stmt.setString(5, test.getTestData());
            stmt.setMaxRows(1);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(Test_Case.TEST_CASE_ID.name());
            }
        }

        try (Connection conn = this.getConnection()) {
            final String sql = "INSERT INTO " + TABLES.test_case.name() + " ("
                    + Test_Case.SUITE_CLASS.name() + ", "
                    + Test_Case.TEST_CLASS.name() + ", "
                    + Test_Case.TEST_METHOD.name() + ", "
                    + Test_Case.TEST_DATA_INFO.name() + ", "
                    + Test_Case.TEST_DATA.name()
                    + ") VALUES (?,?,?,?,?);";

            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, test.getSuiteClass());
            stmt.setString(2, test.getTestClass());
            stmt.setString(3, test.getTestMethod());
            stmt.setString(4, test.getTestDataInfo());
            stmt.setString(5, test.getTestData());
            int i = stmt.executeUpdate();
        }

        try (Connection conn = this.getConnection()) {
            final String sql = "SELECT * FROM " + TABLES.test_case.name() + " WHERE "
                    + Test_Case.SUITE_CLASS + " = ? AND "
                    + Test_Case.TEST_CLASS + " = ? AND "
                    + Test_Case.TEST_METHOD + " = ? AND "
                    + Test_Case.TEST_DATA_INFO + " = ? AND "
                    + Test_Case.TEST_DATA + " = ? ORDER BY " + Test_Case.TEST_CASE_ID.name() + " DESC;";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, test.getSuiteClass());
            stmt.setString(2, test.getTestClass());
            stmt.setString(3, test.getTestMethod());
            stmt.setString(4, test.getTestDataInfo());
            stmt.setString(5, test.getTestData());
            stmt.setMaxRows(1);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(Test_Case.TEST_CASE_ID.name());
            }
        }
        throw new SQLException();
    }

    @Override
    public void updateSuiteExecutionResult(String execId) throws SQLException {
        LOG.info("Update test suite execution result with execution id {}", execId);
        int total = 0, fail = 0;

        try (Connection conn = this.getConnection();) {
            final String sql1 = "SELECT " + Test_Result.EXECUTION_RESULT.name() + " FROM "
                    + TABLES.test_result.name() + " WHERE " + Test_Result.SUITE_RESULT.name() + " = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(sql1,
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                stmt.setString(1, execId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    total++;
                    String result = rs.getString(Test_Result.EXECUTION_RESULT.name());
                    if (!result.equals(ExecutionResult.PASS.name()) && !result.endsWith("/0")) {
                        fail++;
                    }
                }
            }
        }

        try (Connection conn = this.getConnection();) {
            final String sql = "SELECT * FROM " + TABLES.suite_result.name()
                    + " WHERE " + Suite_Result.SUITE_RESULT_ID.name() + " = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(sql,
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
        }
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return connPool.getConnection();
    }

    @Override
    protected boolean acquireExecutionLock(Connection conn, String lock) throws SQLException {
        return true;
    }

    @Override
    protected boolean releaseExecutionLock(Connection conn, String lock) throws SQLException {
        return true;
    }

    private void initSchema() throws SQLException, IOException {
        try (Connection conn = this.getConnection()) {
            ScriptRunner runner = new ScriptRunner(conn, true, true);
            runner.runScript(
                    new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("thr.sql")));
        }
    }
}
