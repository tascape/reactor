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
    public void queueSuiteExecution(TaskSuite suite, String execId) throws SQLException {
        LOG.debug("Queue suite for execution with execution id {}", execId);
        String lock = this.getDbLock(execId);
        try (Connection conn = this.getConnection()) {
            try {
                if (!this.acquireExecutionLock(conn, lock)) {
                    throw new SQLException("Cannot acquire lock of name " + lock);
                }

                if (this.queueTaskSuite(suite, execId)) {
                    this.queueCaseResults(execId, suite.getCases());
                }
            } finally {
                this.releaseExecutionLock(conn, lock);
            }
        }
    }

    @Override
    public boolean queueTaskSuite(TaskSuite suite, String execId) throws SQLException {
        LOG.debug("Queueing suite result with execution id {} ", execId);
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
                rs.updateInt(SuiteResult.NUMBER_OF_CASES, suite.getCases().size());
                rs.updateInt(SuiteResult.NUMBER_OF_FAILURE, suite.getCases().size());
                rs.updateNString(SuiteResult.PRODUCT_UNDER_TASK, SYS_CONFIG.getProdUnderTask());

                rs.insertRow();
                rs.last();
                rs.updateRow();
                return true;
            }
        }
    }

    @Override
    protected int getCaseId(TaskCase kase) throws SQLException {
        LOG.debug("Query for id of case {} ", kase.format());
        final String sql = "SELECT * FROM " + TaskCase.TABLE_NAME + " WHERE "
            + TaskCase.SUITE_CLASS + " = ? AND "
            + TaskCase.CASE_CLASS + " = ? AND "
            + TaskCase.CASE_METHOD + " = ? AND "
            + TaskCase.CASE_DATA_INFO + " = ? AND "
            + TaskCase.CASE_DATA + " = ?";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, kase.getSuiteClass());
            stmt.setString(2, kase.getCaseClass());
            stmt.setString(3, kase.getCaseMethod());
            stmt.setString(4, kase.getCaseDataInfo());
            stmt.setString(5, kase.getCaseData());
            stmt.setMaxRows(1);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.moveToInsertRow();
                rs.updateString(TaskCase.SUITE_CLASS, kase.getSuiteClass());
                rs.updateString(TaskCase.CASE_CLASS, kase.getCaseClass());
                rs.updateString(TaskCase.CASE_METHOD, kase.getCaseMethod());
                rs.updateString(TaskCase.CASE_DATA_INFO, kase.getCaseDataInfo());
                rs.updateString(TaskCase.CASE_DATA, kase.getCaseData());

                rs.insertRow();
                rs.last();
                rs.updateRow();
            }
            return rs.getInt(TaskCase.TASK_CASE_ID);
        }
    }

    @Override
    protected void queueCaseResults(String execId, List<TaskCase> cases) throws SQLException {
        LOG.debug("Queue {} case result(s) with execution id {} ", cases.size(), execId);
        final String sql = "SELECT * FROM " + TaskCase.TABLE_NAME + " WHERE "
            + CaseResult.SUITE_RESULT + " = ?";
        Map<String, Integer> idMap = this.getCaseIds(cases);

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
                for (TaskCase kase : cases) {
                    index++;
                    rs.moveToInsertRow();

                    Integer tcid = idMap.get(kase.format());
                    if (tcid == null) {
                        tcid = this.getCaseId(kase);
                    }

                    rs.updateString(CaseResult.CASE_RESULT_ID, Utils.getUniqueId());
                    rs.updateString(CaseResult.SUITE_RESULT, execId);
                    rs.updateInt(CaseResult.TASK_CASE_ID, tcid);
                    rs.updateString(CaseResult.EXECUTION_RESULT, ExecutionResult.QUEUED.getName());
                    rs.updateLong(CaseResult.START_TIME, System.currentTimeMillis());
                    rs.updateLong(CaseResult.STOP_TIME, System.currentTimeMillis());
                    rs.updateString(CaseResult.CASE_STATION, "?");
                    rs.updateString(CaseResult.LOG_DIR, "?");

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
        TaskCase tc = new TaskCase();
        tc.setSuiteClass("a");
        LOG.debug("case id = {}", db.getCaseId(tc));
    }
}
