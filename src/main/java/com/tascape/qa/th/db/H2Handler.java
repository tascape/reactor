/*
 * Copyright 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tascape.qa.th.db;

import com.tascape.qa.th.ExecutionResult;
import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.AbstractTestSuite;
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

    private final String dbPath = SYS_CONFIG.getLogPath() + "/db/" + SystemConfiguration.CONSTANT_LOG_KEEP_ALIVE_PREFIX
        + System.currentTimeMillis() + "/";

    private JdbcConnectionPool connPool;

    @Override
    public void init() throws Exception {
        File dir = new File(this.dbPath);
        if (dir.exists()) {
            FileUtils.cleanDirectory(dir);
        }
        this.connPool = JdbcConnectionPool.create("jdbc:h2:" + this.dbPath + SYS_CONFIG.getExecId(), "sa", "sa");
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
    public boolean queueTestSuite(AbstractTestSuite suite, String execId) throws SQLException {
        LOG.info("Queueing test suite result with execution id {} ", execId);
        final String sql = "INSERT INTO " + SuiteResult.TABLE_NAME + " ("
            + SuiteResult.SUITE_RESULT_ID + ", "
            + SuiteResult.SUITE_NAME + ", "
            + SuiteResult.JOB_NAME + ", "
            + SuiteResult.JOB_BUILD_NUMBER + ", "
            + SuiteResult.JOB_BUILD_URL + ", "
            + SuiteResult.START_TIME + ", "
            + SuiteResult.STOP_TIME + ", "
            + SuiteResult.EXECUTION_RESULT + ", "
            + SuiteResult.NUMBER_OF_TESTS + ", "
            + SuiteResult.NUMBER_OF_FAILURE + ", "
            + SuiteResult.PRODUCT_UNDER_TEST
            + ") VALUES (?,?,?,?,?,?,?,?,?,?,?);";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            Long time = System.currentTimeMillis();
            stmt.setString(1, execId);
            stmt.setString(2, suite.getName());
            stmt.setString(3, SYS_CONFIG.getJobName());
            stmt.setInt(4, SYS_CONFIG.getJobBuildNumber());
            stmt.setString(5, SYS_CONFIG.getJobBuildUrl());
            stmt.setLong(6, time);
            stmt.setLong(7, time + 11);
            stmt.setString(8, ExecutionResult.QUEUED.getName());
            stmt.setInt(9, suite.getTests().size());
            stmt.setInt(10, suite.getTests().size());
            stmt.setString(11, SYS_CONFIG.getProdUnderTest());
            LOG.debug("{}", stmt);
            int i = stmt.executeUpdate();
            return i == 1;
        }
    }

    @Override
    protected void queueTestCaseResults(String execId, List<TestCase> tests) throws SQLException {
        LOG.info("Queue {} test case result(s) with execution id {} ", tests.size(), execId);
        final String sql = "INSERT INTO " + TestResult.TABLE_NAME + " ("
            + TestResult.TEST_RESULT_ID + ", "
            + TestResult.SUITE_RESULT + ", "
            + TestResult.TEST_CASE_ID + ", "
            + TestResult.EXECUTION_RESULT + ", "
            + TestResult.START_TIME + ", "
            + TestResult.STOP_TIME + ", "
            + TestResult.TEST_STATION + ", "
            + TestResult.LOG_DIR
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
                stmt.setString(1, Utils.getUniqueId());
                stmt.setString(2, execId);
                stmt.setInt(3, tcid);
                stmt.setString(4, ExecutionResult.QUEUED.getName());
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
            final String sql = "SELECT * FROM " + TestCase.TABLE_NAME + " WHERE "
                + TestCase.SUITE_CLASS + " = ? AND "
                + TestCase.TEST_CLASS + " = ? AND "
                + TestCase.TEST_METHOD + " = ? AND "
                + TestCase.TEST_DATA_INFO + " = ? AND "
                + TestCase.TEST_DATA + " = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, test.getSuiteClass());
            stmt.setString(2, test.getTestClass());
            stmt.setString(3, test.getTestMethod());
            stmt.setString(4, test.getTestDataInfo());
            stmt.setString(5, test.getTestData());
            stmt.setMaxRows(1);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(TestCase.TEST_CASE_ID);
            }
        }

        try (Connection conn = this.getConnection()) {
            final String sql = "INSERT INTO " + TestCase.TABLE_NAME + " ("
                + TestCase.SUITE_CLASS + ", "
                + TestCase.TEST_CLASS + ", "
                + TestCase.TEST_METHOD + ", "
                + TestCase.TEST_DATA_INFO + ", "
                + TestCase.TEST_DATA
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
            final String sql = "SELECT * FROM " + TestCase.TABLE_NAME + " WHERE "
                + TestCase.SUITE_CLASS + " = ? AND "
                + TestCase.TEST_CLASS + " = ? AND "
                + TestCase.TEST_METHOD + " = ? AND "
                + TestCase.TEST_DATA_INFO + " = ? AND "
                + TestCase.TEST_DATA + " = ? ORDER BY " + TestCase.TEST_CASE_ID
                + " DESC;";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, test.getSuiteClass());
            stmt.setString(2, test.getTestClass());
            stmt.setString(3, test.getTestMethod());
            stmt.setString(4, test.getTestDataInfo());
            stmt.setString(5, test.getTestData());
            stmt.setMaxRows(1);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(TestCase.TEST_CASE_ID);
            }
        }
        throw new SQLException();
    }

    @Override
    public void updateSuiteExecutionResult(String execId) throws SQLException {
        LOG.info("Update test suite execution result with execution id {}", execId);
        int total = 0, fail = 0;

        try (Connection conn = this.getConnection();) {
            final String sql1 = "SELECT " + TestResult.EXECUTION_RESULT
                + " FROM " + TestResult.TABLE_NAME
                + " WHERE " + TestResult.SUITE_RESULT + " = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                stmt.setString(1, execId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    total++;
                    String result = rs.getString(TestResult.EXECUTION_RESULT);
                    if (!result.equals(ExecutionResult.PASS.getName()) && !result.endsWith("/0")) {
                        fail++;
                    }
                }
            }
        }

        try (Connection conn = this.getConnection();) {
            final String sql = "SELECT * FROM " + SuiteResult.TABLE_NAME
                + " WHERE " + SuiteResult.SUITE_RESULT_ID + " = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE)) {
                stmt.setString(1, execId);
                ResultSet rs = stmt.executeQuery();
                if (rs.first()) {
                    rs.updateInt(SuiteResult.NUMBER_OF_TESTS, total);
                    rs.updateInt(SuiteResult.NUMBER_OF_FAILURE, fail);
                    rs.updateString(SuiteResult.EXECUTION_RESULT, fail == 0 ? "PASS" : "FAIL");
                    rs.updateLong(SuiteResult.STOP_TIME, System.currentTimeMillis());
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
            try (InputStreamReader isr = new InputStreamReader(
                this.getClass().getClassLoader().getResourceAsStream("db/thr-h2.sql"))) {
                runner.runScript(isr);
            }
        }
    }
}
