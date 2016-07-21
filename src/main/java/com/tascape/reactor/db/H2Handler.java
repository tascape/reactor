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
import com.tascape.reactor.SystemConfiguration;
import com.tascape.reactor.TaskSuite;
import com.tascape.reactor.Utils;
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
                conn.prepareStatement("SELECT * FROM case_result WHERE 0;").executeQuery();
            } catch (SQLException ex) {
                LOG.warn("{}", ex.getMessage());
                this.initSchema();
            }
        }
    }

    @Override
    public boolean queueTaskSuite(TaskSuite suite, String execId) throws SQLException {
        LOG.debug("Queueing suite result with execution id {} ", execId);
        final String sql = "INSERT INTO " + SuiteResult.TABLE_NAME + " ("
            + SuiteResult.SUITE_RESULT_ID + ", "
            + SuiteResult.SUITE_NAME + ", "
            + SuiteResult.PROJECT_NAME + ", "
            + SuiteResult.JOB_NAME + ", "
            + SuiteResult.JOB_BUILD_NUMBER + ", "
            + SuiteResult.JOB_BUILD_URL + ", "
            + SuiteResult.START_TIME + ", "
            + SuiteResult.STOP_TIME + ", "
            + SuiteResult.EXECUTION_RESULT + ", "
            + SuiteResult.NUMBER_OF_CASES + ", "
            + SuiteResult.NUMBER_OF_FAILURE + ", "
            + SuiteResult.PRODUCT_UNDER_TASK
            + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            Long time = System.currentTimeMillis();
            stmt.setString(1, execId);
            stmt.setString(2, suite.getName());
            stmt.setString(3, suite.getProjectName());
            stmt.setString(4, SYS_CONFIG.getJobName());
            stmt.setInt(5, SYS_CONFIG.getJobBuildNumber());
            stmt.setString(6, SYS_CONFIG.getJobBuildUrl());
            stmt.setLong(7, time);
            stmt.setLong(8, time + 11);
            stmt.setString(9, ExecutionResult.QUEUED.getName());
            stmt.setInt(10, suite.getCases().size());
            stmt.setInt(11, suite.getCases().size());
            stmt.setString(12, SYS_CONFIG.getProdUnderTask());
            LOG.debug("{}", stmt);
            int i = stmt.executeUpdate();
            return i == 1;
        }
    }

    @Override
    protected void queueCaseResults(String execId, List<TaskCase> cases) throws SQLException {
        LOG.debug("Queue {} case result(s) with execution id {} ", cases.size(), execId);
        final String sql = "INSERT INTO " + CaseResult.TABLE_NAME + " ("
            + CaseResult.CASE_RESULT_ID + ", "
            + CaseResult.SUITE_RESULT + ", "
            + CaseResult.TASK_CASE_ID + ", "
            + CaseResult.EXECUTION_RESULT + ", "
            + CaseResult.START_TIME + ", "
            + CaseResult.STOP_TIME + ", "
            + CaseResult.CASE_STATION + ", "
            + CaseResult.CASE_ENV + ", "
            + CaseResult.LOG_DIR
            + ") VALUES (?,?,?,?,?,?,?,?,?);";
        Map<String, Integer> idMap = this.getCaseIds(cases);

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            int index = 0;
            for (TaskCase kase : cases) {
                Integer tcid = idMap.get(kase.format());
                if (tcid == null) {
                    tcid = this.getCaseId(kase);
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
                stmt.setString(9, "");
                LOG.debug("{}", stmt);
                int i = stmt.executeUpdate();
            }
        }
    }

    @Override
    protected int getCaseId(TaskCase kase) throws SQLException {
        LOG.debug("Query for id of case {} ", kase.format());
        try (Connection conn = this.getConnection()) {
            final String sql = "SELECT * FROM " + TaskCase.TABLE_NAME + " WHERE "
                + TaskCase.SUITE_CLASS + " = ? AND "
                + TaskCase.CASE_CLASS + " = ? AND "
                + TaskCase.CASE_METHOD + " = ? AND "
                + TaskCase.CASE_DATA_INFO + " = ? AND "
                + TaskCase.CASE_DATA + " = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, kase.getSuiteClass());
            stmt.setString(2, kase.getCaseClass());
            stmt.setString(3, kase.getCaseMethod());
            stmt.setString(4, kase.getCaseDataInfo());
            stmt.setString(5, kase.getCaseData());
            stmt.setMaxRows(1);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(TaskCase.TASK_CASE_ID);
            }
        }

        try (Connection conn = this.getConnection()) {
            final String sql = "INSERT INTO " + TaskCase.TABLE_NAME + " ("
                + TaskCase.SUITE_CLASS + ", "
                + TaskCase.CASE_CLASS + ", "
                + TaskCase.CASE_METHOD + ", "
                + TaskCase.CASE_DATA_INFO + ", "
                + TaskCase.CASE_DATA
                + ") VALUES (?,?,?,?,?);";

            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, kase.getSuiteClass());
            stmt.setString(2, kase.getCaseClass());
            stmt.setString(3, kase.getCaseMethod());
            stmt.setString(4, kase.getCaseDataInfo());
            stmt.setString(5, kase.getCaseData());
            int i = stmt.executeUpdate();
        }

        try (Connection conn = this.getConnection()) {
            final String sql = "SELECT * FROM " + TaskCase.TABLE_NAME + " WHERE "
                + TaskCase.SUITE_CLASS + " = ? AND "
                + TaskCase.CASE_CLASS + " = ? AND "
                + TaskCase.CASE_METHOD + " = ? AND "
                + TaskCase.CASE_DATA_INFO + " = ? AND "
                + TaskCase.CASE_DATA + " = ? ORDER BY " + TaskCase.TASK_CASE_ID
                + " DESC;";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, kase.getSuiteClass());
            stmt.setString(2, kase.getCaseClass());
            stmt.setString(3, kase.getCaseMethod());
            stmt.setString(4, kase.getCaseDataInfo());
            stmt.setString(5, kase.getCaseData());
            stmt.setMaxRows(1);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(TaskCase.TASK_CASE_ID);
            }
        }
        throw new SQLException();
    }

    @Override
    public void updateSuiteExecutionResult(String execId) throws SQLException {
        LOG.debug("Update suite execution result with execution id {}", execId);
        int total = 0, fail = 0;

        try (Connection conn = this.getConnection();) {
            final String sql1 = "SELECT " + CaseResult.EXECUTION_RESULT
                + " FROM " + CaseResult.TABLE_NAME
                + " WHERE " + CaseResult.SUITE_RESULT + " = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                stmt.setString(1, execId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    total++;
                    String result = rs.getString(CaseResult.EXECUTION_RESULT);
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
                    rs.updateInt(SuiteResult.NUMBER_OF_CASES, total);
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
                this.getClass().getClassLoader().getResourceAsStream("db/report-h2.sql"))) {
                runner.runScript(isr);
            }
        }
    }
}
