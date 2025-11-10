/*
 * Copyright (c) 2015 - present Nebula Bay.
 * All rights reserved.
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
import com.tascape.reactor.TaskSuite;
import com.tascape.reactor.Utils;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public final class SqliteHandler extends DbHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SqliteHandler.class);

    private static final String DB_DRIVER = "org.sqlite.JDBC";

    static {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Cannot load database driver: " + DB_DRIVER, ex);
        }
    }

    private final String dbFile = SYS_CONFIG.getLogPath() + "/db/" + SYS_CONFIG.getExecId() + "/sqlite.db";

    public static final List<String> SQL_DDL = new ArrayList<>() {
        {
            add("""
                CREATE TABLE `suite_result` (
                  `SUITE_RESULT_ID` TEXT NOT NULL,
                  `SUITE_NAME` TEXT DEFAULT NULL,
                  `PROJECT_NAME` TEXT DEFAULT NULL,
                  `JOB_NAME` TEXT DEFAULT NULL,
                  `JOB_BUILD_NUMBER` INTEGER DEFAULT NULL,
                  `JOB_BUILD_URL` TEXT DEFAULT NULL,
                  `EXECUTION_RESULT` TEXT DEFAULT NULL,
                  `START_TIME` INTEGER DEFAULT NULL,
                  `STOP_TIME` INTEGER DEFAULT NULL,
                  `NUMBER_OF_CASES` INTEGER DEFAULT NULL,
                  `NUMBER_OF_FAILURE` INTEGER DEFAULT NULL,
                  `INVISIBLE_ENTRY` INTEGER DEFAULT '0',
                  `PRODUCT_UNDER_TASK` TEXT DEFAULT NULL
                )
                """);
            add("""
                CREATE TABLE `suite_property` (
                  `SUITE_PROPERTY_ID` INTEGER PRIMARY KEY AUTOINCREMENT,
                  `SUITE_RESULT_ID` TEXT NOT NULL,
                  `PROPERTY_NAME` TEXT DEFAULT NULL,
                  `PROPERTY_VALUE` TEXT DEFAULT NULL
                )
                """);
            add("""
                CREATE TABLE `task_case` (
                  `TASK_CASE_ID` INTEGER PRIMARY KEY AUTOINCREMENT,
                  `SUITE_CLASS` TEXT NOT NULL,
                  `CASE_CLASS` TEXT NOT NULL,
                  `CASE_METHOD` TEXT NOT NULL,
                  `CASE_DATA_INFO` TEXT NOT NULL,
                  `CASE_DATA` TEXT DEFAULT '',
                  `CASE_ISSUES` TEXT DEFAULT ''
                )
                """);
            add("""
                CREATE TABLE `case_result` (
                  `CASE_RESULT_ID` TEXT NOT NULL,
                  `SUITE_RESULT` TEXT DEFAULT NULL,
                  `TASK_CASE_ID` INTEGER DEFAULT '0',
                  `EXECUTION_RESULT` TEXT DEFAULT NULL,
                  `AUT` TEXT DEFAULT NULL,
                  `START_TIME` INTEGER DEFAULT NULL,
                  `STOP_TIME` INTEGER DEFAULT NULL,
                  `RETRY` INTEGER DEFAULT NULL,
                  `CASE_STATION` TEXT DEFAULT NULL,
                  `LOG_DIR` TEXT DEFAULT NULL,
                  `EXTERNAL_ID` TEXT DEFAULT NULL,
                  `CASE_ENV` TEXT DEFAULT NULL,
                  CONSTRAINT `fk_suite_result` FOREIGN KEY (`SUITE_RESULT`) REFERENCES `suite_result` (`SUITE_RESULT_ID`) ON DELETE CASCADE ON UPDATE CASCADE,
                  CONSTRAINT `fk_task_case` FOREIGN KEY (`TASK_CASE_ID`) REFERENCES `task_case` (`TASK_CASE_ID`) ON UPDATE CASCADE
                )
                """);
            add("""
                CREATE TABLE `case_result_metric` (
                  `CASE_RESULT_METRIC_ID` INTEGER PRIMARY KEY AUTOINCREMENT,
                  `CASE_RESULT_ID` TEXT DEFAULT NULL,
                  `METRIC_GROUP` TEXT DEFAULT NULL,
                  `METRIC_NAME` TEXT DEFAULT NULL,
                  `METRIC_VALUE` TEXT DEFAULT NULL,
                  CONSTRAINT `fk_case_result` FOREIGN KEY (`CASE_RESULT_ID`) REFERENCES `case_result` (`CASE_RESULT_ID`) ON DELETE CASCADE ON UPDATE CASCADE
                )
                """);
        }
    };

    @Override
    public void init() throws Exception {
        File f = new File(this.dbFile);
        FileUtils.createParentDirectories(f);
        if (f.exists()) {
            FileUtils.delete(f);
        }
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
    public boolean queueTaskSuite(TaskSuite suite, String execId) throws SQLException, InterruptedException {
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
                + CaseResult.LOG_DIR + ", "
                + CaseResult.RETRY
                + ") VALUES (?,?,?,?,?,?,?,?,?,?);";
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
                stmt.setInt(10, 0);
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
    protected Connection getConnection() throws SQLException {
        String jdbc = "jdbc:sqlite:" + this.dbFile;
        LOG.debug(jdbc);
        return DriverManager.getConnection(jdbc);
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
            Statement stmt = conn.createStatement();
            SQL_DDL.forEach(sql -> {
                LOG.debug(sql);
                try {
                    stmt.executeUpdate(sql);
                } catch (SQLException ex) {
                    LOG.error("{}", ex.getMessage());
                }
            });
        }
    }
}
