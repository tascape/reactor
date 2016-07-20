/*
 * Copyright 2015 - 2016 Nebula Bay.
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
package com.tascape.reactor.db;

import com.tascape.reactor.ExecutionResult;
import com.tascape.reactor.TestSuite;
import com.tascape.reactor.Utils;
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
public class PostgresqlHandler extends DbHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PostgresqlHandler.class);

    private static final String DB_DRIVER = "org.postgresql.Driver";

    private static final String DB_HOST = SYS_CONFIG.getDatabaseHost();

    private static final String DB_SCHEMA = SYS_CONFIG.getDatabaseSchema();

    private static final String DB_USER = SYS_CONFIG.getDatabaseUser();

    private static final String DB_PASS = SYS_CONFIG.getDatabasePass();

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
        connPoolConfig.setJdbcUrl("jdbc:postgresql://" + DB_HOST + "/" + DB_SCHEMA);
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
        LOG.debug("Queue test suite for execution with execution id {}", execId);
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
        LOG.debug("Queueing test suite result with execution id {} ", execId);
        final String sql = "SELECT * FROM " + SuiteResult.TABLE_NAME + " WHERE "
            + SuiteResult.SUITE_RESULT_ID + " = ?";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE,
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

                rs.updateString(SuiteResult.SUITE_RESULT_ID, execId);
                rs.updateString(SuiteResult.SUITE_NAME, suite.getName());
                rs.updateString(SuiteResult.PROJECT_NAME, suite.getProjectName());
                rs.updateString(SuiteResult.JOB_NAME, SYS_CONFIG.getJobName());
                rs.updateInt(SuiteResult.JOB_BUILD_NUMBER, SYS_CONFIG.getJobBuildNumber());
                rs.updateString(SuiteResult.JOB_BUILD_URL, SYS_CONFIG.getJobBuildUrl());
                rs.updateLong(SuiteResult.START_TIME, time);
                rs.updateLong(SuiteResult.STOP_TIME, time);
                rs.updateString(SuiteResult.EXECUTION_RESULT, ExecutionResult.QUEUED.getName());
                rs.updateInt(SuiteResult.NUMBER_OF_TESTS, suite.getTests().size());
                rs.updateInt(SuiteResult.NUMBER_OF_FAILURE, suite.getTests().size());
                rs.updateNString(SuiteResult.PRODUCT_UNDER_TEST, SYS_CONFIG.getProdUnderTest());

                rs.insertRow();
                rs.last();
                rs.updateRow();
                return true;
            }
        }
    }

    @Override
    protected int getTestCaseId(TestCase test) throws SQLException {
        LOG.debug("Query for id of test case {} ", test.format());
        final String sql = "SELECT * FROM " + TestCase.TABLE_NAME + " WHERE "
            + TestCase.SUITE_CLASS + " = ? AND "
            + TestCase.TEST_CLASS + " = ? AND "
            + TestCase.TEST_METHOD + " = ? AND "
            + TestCase.TEST_DATA_INFO + " = ? AND "
            + TestCase.TEST_DATA + " = ?";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE,
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
                rs.updateString(TestCase.SUITE_CLASS, test.getSuiteClass());
                rs.updateString(TestCase.TEST_CLASS, test.getTestClass());
                rs.updateString(TestCase.TEST_METHOD, test.getTestMethod());
                rs.updateString(TestCase.TEST_DATA_INFO, test.getTestDataInfo());
                rs.updateString(TestCase.TEST_DATA, test.getTestData());

                rs.insertRow();
                rs.last();
                rs.updateRow();
            }
            return rs.getInt(TestCase.TEST_CASE_ID);
        }
    }

    @Override
    protected void queueTestCaseResults(String execId, List<TestCase> tests) throws SQLException {
        LOG.debug("Queue {} test case result(s) with execution id {} ", tests.size(), execId);
        final String sql = "SELECT * FROM " + TestCase.TABLE_NAME + " WHERE "
            + TestResult.SUITE_RESULT + " = ?";
        Map<String, Integer> idMap = this.getTestCaseIds(tests);

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE,
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

                    rs.updateString(TestResult.TEST_RESULT_ID, Utils.getUniqueId());
                    rs.updateString(TestResult.SUITE_RESULT, execId);
                    rs.updateInt(TestResult.TEST_CASE_ID, tcid);
                    rs.updateString(TestResult.EXECUTION_RESULT, ExecutionResult.QUEUED.getName());
                    rs.updateLong(TestResult.START_TIME, System.currentTimeMillis());
                    rs.updateLong(TestResult.STOP_TIME, System.currentTimeMillis());
                    rs.updateString(TestResult.TEST_STATION, "?");
                    rs.updateString(TestResult.LOG_DIR, "?");

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
    public void updateSuiteExecutionResult(String execId) throws SQLException {
        LOG.debug("Update test suite execution result with execution id {}", execId);
        String lock = "testharness." + execId;

        Connection conn = this.getConnection();
        try {
            if (!this.acquireExecutionLock(conn, lock)) {
                throw new SQLException("Cannot acquire lock of name " + lock);
            }

            int total = 0, fail = 0;
            final String sql1 = "SELECT " + TestResult.EXECUTION_RESULT + " FROM "
                + TestCase.TABLE_NAME + " WHERE " + TestResult.SUITE_RESULT
                + " = ?;";
            try (PreparedStatement stmt = this.getConnection().prepareStatement(sql1,
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY)) {
                stmt.setString(1, execId);
                stmt.setFetchSize(Integer.MIN_VALUE);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    total++;
                    String result = rs.getString(TestResult.EXECUTION_RESULT);
                    if (!result.equals(ExecutionResult.PASS.getName()) && !result.endsWith("/0")) {
                        fail++;
                    }
                }
            }

            final String sql2 = "SELECT * FROM " + SuiteResult.TABLE_NAME
                + " WHERE " + SuiteResult.SUITE_RESULT_ID + " = ?;";
            try (PreparedStatement stmt = this.getConnection().prepareStatement(sql2,
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
        final String sqlLock = String.format("SELECT pg_advisory_lock(%d);", this.hash(lock));
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
        final String sqlRelease = String.format("SELECT pg_advisory_unlock(%d);", this.hash(lock));
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
        PostgresqlHandler db = new PostgresqlHandler();
        TestCase tc = new TestCase();
        tc.setSuiteClass("a");
        LOG.debug("test case id = {}", db.getTestCaseId(tc));
    }
}
