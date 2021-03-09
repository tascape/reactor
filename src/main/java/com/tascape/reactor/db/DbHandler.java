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
import com.tascape.reactor.Reactor;
import com.tascape.reactor.SystemConfiguration;
import com.tascape.reactor.TaskSuite;
import com.tascape.reactor.data.CaseIterationData;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class DbHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DbHandler.class);

    static final SystemConfiguration SYS_CONFIG = SystemConfiguration.getInstance();

    public static final String SYSPROP_DATABASE_TYPE = "reactor.db.type";

    public static final String SYSPROP_DATABASE_HOST = "reactor.db.host";

    public static final String SYSPROP_DATABASE_SCHEMA = "reactor.db.schema";

    public static final String SYSPROP_DATABASE_USER = "reactor.db.user";

    public static final String SYSPROP_DATABASE_PASS = "reactor.db.pass";

    public static final String SYSPROP_DATABASE_POOL_SIZE = "reactor.db.pool.size";

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

    protected String getDbLock(String execId) {
        return Reactor.class.getSimpleName() + "." + execId;
    }

    public SuiteResult getSuiteResult(String id) throws SQLException {
        LOG.debug("Query for suite result with execution id {}", id);
        final String sql = "SELECT * FROM " + SuiteResult.TABLE_NAME + " WHERE "
            + SuiteResult.SUITE_RESULT_ID + " = ?";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setMaxRows(1);
            stmt.setString(1, id);

            ResultSet rs = stmt.executeQuery();
            SuiteResult tsr = new SuiteResult();
            if (rs.first()) {
                tsr.setSuiteResultId(rs.getString(SuiteResult.SUITE_RESULT_ID));
                tsr.setJobName(rs.getString(SuiteResult.JOB_NAME));
                tsr.setJobBuildNumber(rs.getInt(SuiteResult.JOB_BUILD_NUMBER));
                tsr.setJobBuildUrl(rs.getString(SuiteResult.JOB_BUILD_URL));
            } else {
                LOG.warn("no suite result with execution id {}", id);
            }
            return tsr;
        }
    }

    public void queueSuiteExecution(TaskSuite suite, String execId) throws SQLException {
        LOG.debug("Queue case suite for execution with execution id {}", execId);
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

    public void setSuiteExecutionProperties(List<SuiteProperty> properties) throws SQLException {
        if (properties.isEmpty()) {
            return;
        }
        for (SuiteProperty prop : properties) {
            this.addSuiteExecutionProperty(prop);
        }
    }

    public void addSuiteExecutionProperty(SuiteProperty prop) throws SQLException {
        final String sql = "INSERT INTO " + SuiteProperty.TABLE_NAME + " ("
            + SuiteProperty.SUITE_RESULT_ID + ", "
            + SuiteProperty.PROPERTY_NAME + ", "
            + SuiteProperty.PROPERTY_VALUE + ") VALUES (?,?,?)";
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, prop.getSuiteResultId());
            stmt.setString(2, StringUtils.left(prop.getPropertyName(), 255));
            stmt.setString(3, StringUtils.left(prop.getPropertyValue(), 255));
            stmt.executeUpdate();
        }
    }

    public abstract boolean queueTaskSuite(TaskSuite suite, String execId) throws SQLException;

    protected abstract int getCaseId(TaskCase kase) throws SQLException;

    protected Map<String, Integer> getCaseIds(List<TaskCase> cases) throws SQLException {
        Set<String> suiteClasses = new HashSet<>();
        cases.stream().forEach((tc) -> {
            suiteClasses.add(tc.getSuiteClass());
        });

        Map<String, Integer> idMap = new HashMap<>();
        String sql = "SELECT * FROM " + TaskCase.TABLE_NAME + " WHERE ";
        String sql0 = "";
        sql0 = suiteClasses.stream()
            .map((sc) -> " OR " + TaskCase.SUITE_CLASS + "='" + sc + "'")
            .reduce(sql0, String::concat);
        sql += sql0.substring(4);
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            LOG.debug("{}", stmt.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TaskCase tc = new TaskCase();
                tc.setSuiteClass(rs.getString(TaskCase.SUITE_CLASS));
                tc.setCaseClass(rs.getString(TaskCase.CASE_CLASS));
                tc.setCaseMethod(rs.getString(TaskCase.CASE_METHOD));
                tc.setCaseDataInfo(rs.getString(TaskCase.CASE_DATA_INFO));
                tc.setCaseData(rs.getString(TaskCase.CASE_DATA));
                idMap.put(tc.format(), rs.getInt(TaskCase.TASK_CASE_ID));
            }
            LOG.debug("Found {} cases exist", idMap.size());
            return idMap;
        }
    }

    protected abstract void queueCaseResults(String execId, List<TaskCase> cases) throws SQLException;

    public List<CaseResult> getQueuedCaseResults(String execId, int limit) throws SQLException {
        LOG.debug("Query database for all queued cases");
        final String sql = "SELECT * FROM " + CaseResult.TABLE_NAME + " tr "
            + "INNER JOIN " + TaskCase.TABLE_NAME + " tc "
            + "ON tr.TASK_CASE_ID=tc.TASK_CASE_ID AND " + CaseResult.EXECUTION_RESULT + " = ? "
            + "WHERE " + CaseResult.SUITE_RESULT + " = ? "
            + "ORDER BY SUITE_CLASS, CASE_CLASS, CASE_METHOD, CASE_DATA_INFO "
            + "LIMIT ?;";
        List<CaseResult> tcrs = new ArrayList<>();

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, ExecutionResult.QUEUED.getName());
            stmt.setString(2, execId);
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CaseResult tcr = new CaseResult();
                tcr.setCaseResultId(rs.getString(CaseResult.CASE_RESULT_ID));
                tcr.setSuiteResultId(execId);
                tcr.setStartTime(rs.getLong(CaseResult.START_TIME));
                tcr.setStopTime(rs.getLong(CaseResult.STOP_TIME));
                tcr.setRetry(rs.getInt(CaseResult.RETRY));

                TaskCase tc = new TaskCase();
                tc.setSuiteClass(rs.getString(TaskCase.SUITE_CLASS));
                tc.setCaseClass(rs.getString(TaskCase.CASE_CLASS));
                tc.setCaseMethod(rs.getString(TaskCase.CASE_METHOD));
                tc.setCaseDataInfo(rs.getString(TaskCase.CASE_DATA_INFO));
                tc.setCaseData(rs.getString(TaskCase.CASE_DATA));
                tcr.setTaskCase(tc);

                tcr.setCaseStation(rs.getString(CaseResult.CASE_STATION));
                tcr.setCaseEnv(rs.getString(CaseResult.CASE_ENV));
                tcr.setLogDir(rs.getString(CaseResult.LOG_DIR));
                tcr.setExternalId(rs.getString(CaseResult.EXTERNAL_ID));
                tcrs.add(tcr);
            }
        }

        int num = tcrs.size();
        LOG.debug("Found {} case{} in DB with QUEUED state", num, num > 1 ? "s" : "");
        return tcrs;
    }

    public boolean acquireCaseResult(CaseResult tcr) throws SQLException {
        LOG.debug("Try to acquire case {}", tcr.getTaskCase().format());
        final String sql = "SELECT * FROM " + CaseResult.TABLE_NAME + " WHERE "
            + CaseResult.CASE_RESULT_ID + " = ? LIMIT 1;";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, tcr.getCaseResultId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String host = rs.getString(CaseResult.CASE_STATION);
                if (rs.getString(CaseResult.EXECUTION_RESULT).equals(ExecutionResult.QUEUED.result())) {
                    LOG.debug("Found case {} in DB with QUEUED state", tcr.getTaskCase().format());
//                    if (SYS_CONFIG.getHostName().equals(host)) {
//                        LOG.debug("This case Failed on current host, and was requeue. Skip...");
//                        return false;
//                    }
                    rs.updateString(CaseResult.EXECUTION_RESULT, ExecutionResult.ACQUIRED.result());
                    rs.updateString(CaseResult.CASE_STATION, SYS_CONFIG.getHostName());
                    rs.updateRow();
                    return true;
                } else {
                    LOG.debug("Case {} was acquired by {}", tcr.getTaskCase().format(), host);
                }
            }
        }
        return false;
    }

    public void updateSuiteProductUnderTask(String execId, String productUnderTask) throws SQLException {
        if (StringUtils.isBlank(productUnderTask)) {
            return;
        }
        final String sql = "SELECT * FROM " + SuiteResult.TABLE_NAME
            + " WHERE " + SuiteResult.SUITE_RESULT_ID + " = ?;";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(sql,
            ResultSet.TYPE_SCROLL_SENSITIVE,
            ResultSet.CONCUR_UPDATABLE)) {
            stmt.setString(1, execId);
            ResultSet rs = stmt.executeQuery();
            if (rs.first()) {
                if (StringUtils.isBlank(rs.getString(SuiteResult.PRODUCT_UNDER_TASK))) {
                    rs.updateString(SuiteResult.PRODUCT_UNDER_TASK, productUnderTask);
                }
                rs.updateRow();
            }
        }
    }

    public void updateCaseExecutionResult(CaseResult tcr) throws SQLException {
        LOG.trace("Update case result {} ({}) to {}", tcr.getCaseResultId(), tcr.getTaskCase().format(),
            tcr.getResult().result());
        final String sql = "SELECT tr.* FROM " + CaseResult.TABLE_NAME + " tr INNER JOIN " + TaskCase.TABLE_NAME
            + " tc WHERE tr.TASK_CASE_ID=tc.TASK_CASE_ID AND "
            + CaseResult.CASE_RESULT_ID + " = ?;";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, tcr.getCaseResultId());
            ResultSet rs = stmt.executeQuery();
            if (rs.first()) {
// TODO: update case data into task case table
//                rs.updateString(TaskCase.CASE_DATA, tcr.getTaskCase().getCaseData());
                rs.updateString(CaseResult.EXECUTION_RESULT, tcr.getResult().result());
                rs.updateString(CaseResult.AUT, tcr.getAut());
                rs.updateLong(CaseResult.START_TIME, tcr.getStartTime());
                rs.updateLong(CaseResult.STOP_TIME, tcr.getStopTime());
                rs.updateInt(CaseResult.RETRY, tcr.getRetry());
                rs.updateString(CaseResult.CASE_STATION, tcr.getCaseStation());
                rs.updateString(CaseResult.CASE_ENV, tcr.getCaseEnv());
                rs.updateString(CaseResult.LOG_DIR, tcr.getLogDir());
                rs.updateString(CaseResult.EXTERNAL_ID, tcr.getExternalId());
                rs.updateRow();
            } else {
                LOG.warn("Cannot update case result");
            }
        }
    }

    public ExecutionResult updateSuiteExecutionResult(String execId) throws SQLException {
        LOG.debug("Update suite execution result with execution id {}", execId);
        String lock = this.getDbLock(execId);

        ExecutionResult suiteResult = ExecutionResult.newMultiple();
        int total = 0, fail = 0;
        try (Connection conn = this.getConnection()) {
            try {
                if (!this.acquireExecutionLock(conn, lock)) {
                    throw new SQLException("Cannot acquire lock of name " + lock);
                }

                {
                    final String sql1 = "SELECT " + CaseResult.EXECUTION_RESULT + " FROM "
                        + CaseResult.TABLE_NAME + " WHERE " + CaseResult.SUITE_RESULT
                        + " = ?;";
                    try (PreparedStatement stmt = this.getConnection().prepareStatement(sql1,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY)) {
                        stmt.setString(1, execId);
                        // stmt.setFetchSize(Integer.MIN_VALUE); // mysql only
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            String result = rs.getString(CaseResult.EXECUTION_RESULT);
                            String[] pf = result.split("/");
                            if (pf.length == 1) {
                                total++;
                                if (!result.equals(ExecutionResult.PASS.getName())) {
                                    fail++;
                                }
                            } else if (pf.length == 2) {
                                int p = Integer.parseInt(pf[0]);
                                int f = Integer.parseInt(pf[1]);
                                total += p + f;
                                fail += f;
                            } else {
                                throw new RuntimeException("Cannot parse case execution result " + result);
                            }
                        }
                    }
                }
                {
                    final String sql = "SELECT * FROM " + SuiteResult.TABLE_NAME
                        + " WHERE " + SuiteResult.SUITE_RESULT_ID + " = ?;";
                    try (PreparedStatement stmt = this.getConnection().prepareStatement(sql,
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
                            suiteResult.setPass(total - fail);
                            suiteResult.setFail(fail);
                        }
                    }
                }
            } finally {
                this.releaseExecutionLock(conn, lock);
            }
        }
        return suiteResult;
    }

    public ExecutionResult adjustSuiteExecutionResult(String execId) throws SQLException {
        LOG.debug("Adjust suite execution result of execution id {} with iterations and TBI", execId);
        String lock = this.getDbLock(execId);

        ExecutionResult suiteResult = ExecutionResult.newMultiple();
        int total = 0, fail = 0;
        try (Connection conn = this.getConnection()) {
            try {
                if (!this.acquireExecutionLock(conn, lock)) {
                    throw new SQLException("Cannot acquire lock of name " + lock);
                }

                {
                    String iterDataInfo = CaseIterationData.class.getName();
                    Map<String, Boolean> iterationCases = new HashMap<>();
                    String sql = "SELECT tr.*, tc.* FROM "
                        + CaseResult.TABLE_NAME + " tr JOIN " + TaskCase.TABLE_NAME + " tc"
                        + " ON tr." + CaseResult.TASK_CASE_ID + " = tc." + TaskCase.TASK_CASE_ID
                        + " WHERE " + CaseResult.SUITE_RESULT + " = ? ORDER BY tr." + CaseResult.START_TIME
                        + " DESC;";
                    try (PreparedStatement stmt = this.getConnection().prepareStatement(sql)) {
                        stmt.setString(1, execId);
                        LOG.debug("{}", stmt);
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            LOG.trace("{}.{}", rs.getString(TaskCase.CASE_CLASS), rs.getString(TaskCase.CASE_METHOD));
                            String result = rs.getString(CaseResult.EXECUTION_RESULT);
                            if (result.equals(ExecutionResult.TBI.getName())) {
                                LOG.warn("skip TBI case {}.{}",
                                    rs.getString(TaskCase.CASE_CLASS), rs.getString(TaskCase.CASE_METHOD));
                                continue;
                            }
                            int p = 0, f = 0;
                            String[] pf = result.split("/");
                            switch (pf.length) {
                                case 1:
                                    if (ExecutionResult.isPass(result)) {
                                        p = 1;
                                    } else {
                                        f = 1;
                                    }
                                    break;
                                case 2:
                                    p = Integer.parseInt(pf[0]);
                                    f = Integer.parseInt(pf[1]);
                                    break;
                                default:
                                    throw new RuntimeException("Cannot parse case execution result " + result);
                            }

                            if (rs.getString(TaskCase.CASE_DATA_INFO).startsWith(iterDataInfo)) {
                                String key = rs.getString(TaskCase.SUITE_CLASS) + rs.getString(TaskCase.CASE_CLASS)
                                    + rs.getString(TaskCase.CASE_METHOD);
                                LOG.debug("aggregate result of run iterations {}", key);
                                Boolean r = iterationCases.get(key);
                                if (r == null) {
                                    iterationCases.put(key, f > 0 ? Boolean.FALSE : Boolean.TRUE);
                                } else {
                                    iterationCases.put(key, f > 0 ? Boolean.FALSE : r);
                                }
                                continue;
                            }

                            total += p + f;
                            fail += f;
                            LOG.trace("total {}, fail {}", total, fail);
                        }
                        LOG.debug("{} {}", total, fail);
                    }
                    total += iterationCases.size();
                    fail += iterationCases.values().stream().filter(v -> v.equals(Boolean.FALSE)).count();
                    LOG.debug("{} {}", total, fail);
                    suiteResult.setFail(fail);
                    suiteResult.setPass(total - fail);
                }
                {
                    final String sql = "SELECT * FROM " + SuiteResult.TABLE_NAME
                        + " WHERE " + SuiteResult.SUITE_RESULT_ID + " = ?;";
                    try (PreparedStatement stmt = this.getConnection().prepareStatement(sql,
                        ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                        stmt.setString(1, execId);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.first()) {
                            rs.updateInt(SuiteResult.NUMBER_OF_CASES, total);
                            rs.updateInt(SuiteResult.NUMBER_OF_FAILURE, fail);
                            rs.updateString(SuiteResult.EXECUTION_RESULT, fail == 0 ? "PASS" : "FAIL");
                            rs.updateRow();
                        }
                    }
                }
            } finally {
                this.releaseExecutionLock(conn, lock);
            }
        }
        return suiteResult;
    }

    public void overwriteSuiteExecutionResult(String execId, ExecutionResult result) throws SQLException {
        LOG.debug("Overwrite suite execution result with execution id {} with {}", execId, result);
        int total = result.getPass() + result.getFail();
        if (total == 0) {
            return;
        }
        int fail = result.getFail();

        String lock = this.getDbLock(execId);
        try (Connection conn = this.getConnection()) {
            try {
                if (!this.acquireExecutionLock(conn, lock)) {
                    throw new SQLException("Cannot acquire lock of name " + lock);
                }

                final String sql = "SELECT * FROM " + SuiteResult.TABLE_NAME
                    + " WHERE " + SuiteResult.SUITE_RESULT_ID + " = ?;";
                try (PreparedStatement stmt = this.getConnection().prepareStatement(sql,
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                    stmt.setString(1, execId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.first()) {
                        rs.updateInt(SuiteResult.NUMBER_OF_CASES, total);
                        rs.updateInt(SuiteResult.NUMBER_OF_FAILURE, fail);
                        rs.updateString(SuiteResult.EXECUTION_RESULT, fail == 0 ? "PASS" : "FAIL");
                        rs.updateRow();
                    }
                }
            } finally {
                this.releaseExecutionLock(conn, lock);
            }
        }
    }

    public void saveJunitXml(String execId) throws IOException, SQLException, XMLStreamException {
        Path path = SYS_CONFIG.getLogPath().resolve(execId).resolve("result.xml");
        Path html = SYS_CONFIG.getLogPath().resolve(execId).resolve("result.html");
        LOG.debug("Generate JUnit XML result");

        final String sql = "SELECT * FROM " + SuiteResult.TABLE_NAME + " WHERE "
            + SuiteResult.SUITE_RESULT_ID + " = ?;";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            stmt.setString(1, execId);
            ResultSet rs = stmt.executeQuery();
            if (rs.first()) {
                try (
                    OutputStream os = new FileOutputStream(path.toFile());
                    PrintWriter pwh = new PrintWriter(html.toFile())) {
                    XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(os);
                    xsw.writeStartDocument();
                    xsw.writeCharacters("\n");

                    xsw.writeStartElement("testsuite");
                    xsw.writeAttribute("name", rs.getString(SuiteResult.SUITE_NAME));
                    xsw.writeAttribute("projectname", rs.getString(SuiteResult.PROJECT_NAME) + "");
                    xsw.writeAttribute("tests", rs.getInt(SuiteResult.NUMBER_OF_CASES) + "");
                    xsw.writeAttribute("failures", rs.getInt(SuiteResult.NUMBER_OF_FAILURE) + "");
                    xsw.writeAttribute("time", (rs.getLong(SuiteResult.STOP_TIME)
                        - rs.getLong(CaseResult.START_TIME)) / 1000.0 + "");
                    xsw.writeAttribute("srid", rs.getString(SuiteResult.SUITE_RESULT_ID));
                    xsw.writeCharacters("\n");

                    pwh.println("<html><head><style>");
                    pwh.println("body {font-family: Consolas,Monaco,Lucida Console,Liberation Mono,DejaVu Sans Mono,"
                        + "Bitstream Vera Sans Mono,Courier New, monospace;}");
                    pwh.println("tr:hover {background-color: lightgray;}");
                    pwh.println("td {padding-left: 20px; padding-right: 20px;}");
                    pwh.println("</style></head<body>");
                    pwh.printf("<h2>%s</h2>", rs.getString(SuiteResult.SUITE_NAME));
                    pwh.printf("<h3>%s</h3>", rs.getString(SuiteResult.PROJECT_NAME));
                    pwh.printf("<h4>total cases %d, failures %d</h4>",
                        rs.getInt(SuiteResult.NUMBER_OF_CASES), rs.getInt(SuiteResult.NUMBER_OF_FAILURE));
                    pwh.println("<table><thead><tr><th>index</th><th>case</th><th>result</th></thead><tbody>");

                    final String sql1 = "SELECT * FROM " + CaseResult.TABLE_NAME + " tr JOIN "
                        + TaskCase.TABLE_NAME + " tc ON "
                        + "tr." + CaseResult.TASK_CASE_ID + " = tc." + TaskCase.TASK_CASE_ID
                        + " WHERE " + CaseResult.SUITE_RESULT + " = ?;";
                    try (PreparedStatement stmt1 = this.getConnection().prepareStatement(sql1)) {
                        stmt1.setString(1, execId);
                        ResultSet rs1 = stmt1.executeQuery();
                        int i = 0;
                        while (rs1.next()) {
                            String result = rs1.getString(CaseResult.EXECUTION_RESULT);
                            xsw.writeCharacters("  ");
                            xsw.writeStartElement("testcase");
                            xsw.writeAttribute("name", rs1.getString(TaskCase.CASE_METHOD) + "("
                                + rs1.getString(TaskCase.CASE_DATA) + ")");
                            xsw.writeAttribute("classname", rs1.getString(TaskCase.CASE_CLASS));
                            xsw.writeAttribute("result", result);
                            xsw.writeAttribute("time", (rs1.getLong(CaseResult.STOP_TIME)
                                - rs1.getLong(CaseResult.START_TIME)) / 1000.0 + "");
                            if (!ExecutionResult.isPass(result)) {
                                xsw.writeStartElement("failure");
                                xsw.writeAttribute("type", "failure");
                                xsw.writeEndElement();
                                xsw.writeCharacters("\n");
                            }
                            xsw.writeEndElement();
                            xsw.writeCharacters("\n");

                            String l = rs1.getString(CaseResult.LOG_DIR);
                            String r = rs1.getString(CaseResult.EXECUTION_RESULT);
                            pwh.printf("<tr><td>%d</td><td>%s</td>"
                                + "<td><a style='color: %s; font-weight: bold' href='%s/log.html' target='_blank'>%s</a></td></tr>",
                                ++i,
                                rs1.getString(TaskCase.CASE_METHOD) + "(" + rs1.getString(TaskCase.CASE_DATA) + ")",
                                r.equals("PASS") || r.endsWith("/0") ? "green" : "red", l, r);
                        }
                    }

                    pwh.println("</tbody></table></body></html>");

                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                    xsw.writeEndDocument();
                    xsw.close();
                }
            } else {
                LOG.error("No suite result of exec id {}", execId);
            }
        }
    }

    public void exportToJson(String execId) throws IOException, SQLException, XMLStreamException {
        Path path = SYS_CONFIG.getLogPath().resolve(execId).resolve("result.json");
        LOG.debug("Generate JSON result");

        JSONObject sr = new JSONObject();
        {
            final String sql = "SELECT * FROM " + SuiteResult.TABLE_NAME + " WHERE "
                + SuiteResult.SUITE_RESULT_ID + " = ?;";
            try (Connection conn = this.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, execId);
                List<Map<String, Object>> l = dumpResultSetToList(stmt.executeQuery());
                if (l.isEmpty()) {
                    LOG.error("No suite result of exec id {}", execId);

                }
                Map<String, Object> r = l.get(0);
                r.entrySet().forEach(entry -> {
                    sr.put(entry.getKey(), entry.getValue());
                });
            }
        }
        List<Map<String, Object>> metrics;
        {
            final String sql = "SELECT trm.* FROM " + CaseResultMetric.TABLE_NAME + " trm JOIN "
                + CaseResult.TABLE_NAME + " tr ON"
                + " trm." + CaseResultMetric.CASE_RESULT_ID + " = tr." + CaseResult.CASE_RESULT_ID
                + " WHERE " + CaseResult.SUITE_RESULT + " = ?;";
            try (Connection conn = this.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, execId);
                metrics = dumpResultSetToList(stmt.executeQuery());
            }
        }
        {
            JSONArray trs = new JSONArray();
            final String sql = "SELECT * FROM " + CaseResult.TABLE_NAME + " tr JOIN "
                + TaskCase.TABLE_NAME + " tc ON"
                + " tr." + CaseResult.TASK_CASE_ID + " = tc." + TaskCase.TASK_CASE_ID
                + " WHERE " + CaseResult.SUITE_RESULT + " = ?;";
            try (Connection conn = this.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, execId);
                List<Map<String, Object>> l = dumpResultSetToList(stmt.executeQuery());
                l.forEach(row -> {
                    JSONObject j = new JSONObject();
                    row.entrySet().forEach(col -> {
                        j.put(col.getKey(), col.getValue());
                    });
                    trs.put(trs.length(), j);

                    JSONArray trms = new JSONArray();
                    metrics.stream()
                        .filter(r -> j.getString(CaseResult.CASE_RESULT_ID).equals(r.get(CaseResult.CASE_RESULT_ID)))
                        .forEach(r -> {
                            JSONObject jm = new JSONObject();
                            r.entrySet().forEach(c -> {
                                jm.put(c.getKey(), c.getValue());
                            });
                            trms.put(jm);
                        });
                    j.put("case_result_metrics", trms);
                });
            }
            sr.put("case_results", trs);
        }
        {
            JSONArray sps = new JSONArray();
            final String sql = "SELECT * FROM " + SuiteProperty.TABLE_NAME
                + " WHERE " + SuiteProperty.SUITE_RESULT_ID + " = ?;";
            try (Connection conn = this.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, execId);
                List<Map<String, Object>> l = dumpResultSetToList(stmt.executeQuery());
                l.forEach(row -> {
                    JSONObject j = new JSONObject();
                    row.entrySet().forEach(col -> {
                        j.put(col.getKey(), col.getValue());
                    });
                    sps.put(sps.length(), j);
                });
            }
            sr.put("suite_properties", sps);
        }
        FileUtils.write(path.toFile(), new JSONObject().put("suite_result", sr).toString(2), Charset.defaultCharset());
    }

    public void importFromJson(String json) throws SQLException {
        JSONObject j = new JSONObject(json);
        JSONObject sr = j.getJSONObject("suite_result");
        String srid = sr.getString(SuiteResult.SUITE_RESULT_ID);
        LOG.debug("srid {}", srid);
        try (Connection conn = this.getConnection()) {
            String sql = "SELECT * FROM " + SuiteResult.TABLE_NAME + " WHERE " + SuiteResult.SUITE_RESULT_ID + " = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, srid);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            if (rs.first()) {
                LOG.debug("already imported {}", srid);
                return;
            }
            rs.moveToInsertRow();
            for (int col = 1; col <= rsmd.getColumnCount(); col++) {
                String cn = rsmd.getColumnLabel(col);
                rs.updateObject(cn, sr.get(cn));
            }
            rs.insertRow();
            rs.last();
            rs.updateRow();
        }
        LOG.debug("sr imported");

        JSONArray trs = sr.getJSONArray("case_results");
        int len = trs.length();

        try (Connection conn = this.getConnection()) {
            String sql = String.format("SELECT * FROM %s WHERE %s=? AND %s=? AND %s=? AND %s=? AND %s=?;",
                TaskCase.TABLE_NAME,
                TaskCase.SUITE_CLASS,
                TaskCase.CASE_CLASS,
                TaskCase.CASE_METHOD,
                TaskCase.CASE_DATA_INFO,
                TaskCase.CASE_DATA
            );
            PreparedStatement stmt
                = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setMaxRows(1);
            for (int i = 0; i < len; i++) {
                JSONObject tr = trs.getJSONObject(i);
                stmt.setString(1, tr.getString(TaskCase.SUITE_CLASS));
                stmt.setString(2, tr.getString(TaskCase.CASE_CLASS));
                stmt.setString(3, tr.getString(TaskCase.CASE_METHOD));
                stmt.setString(4, tr.getString(TaskCase.CASE_DATA_INFO));
                stmt.setString(5, tr.getString(TaskCase.CASE_DATA));
                ResultSet rs = stmt.executeQuery();
                if (!rs.first()) {
                    rs.moveToInsertRow();
                    rs.updateString(TaskCase.SUITE_CLASS, tr.getString(TaskCase.SUITE_CLASS));
                    rs.updateString(TaskCase.CASE_CLASS, tr.getString(TaskCase.CASE_CLASS));
                    rs.updateString(TaskCase.CASE_METHOD, tr.getString(TaskCase.CASE_METHOD));
                    rs.updateString(TaskCase.CASE_DATA_INFO, tr.getString(TaskCase.CASE_DATA_INFO));
                    rs.updateString(TaskCase.CASE_DATA, tr.getString(TaskCase.CASE_DATA));
                    rs.insertRow();
                    rs.last();
                    rs.updateRow();
                    rs = stmt.executeQuery();
                    rs.first();
                }
                tr.put(TaskCase.TASK_CASE_ID, rs.getLong(TaskCase.TASK_CASE_ID));
            }
        }
        LOG.debug("tcid updated");

        try (Connection conn = this.getConnection()) {
            String sql = "SELECT * FROM " + CaseResult.TABLE_NAME + " WHERE " + CaseResult.SUITE_RESULT + " = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, srid);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 0; i < len; i++) {
                rs.moveToInsertRow();
                JSONObject tr = trs.getJSONObject(i);
                for (int col = 1; col <= rsmd.getColumnCount(); col++) {
                    String cn = rsmd.getColumnLabel(col);
                    rs.updateObject(cn, tr.get(cn));
                }
                rs.insertRow();
                rs.last();
                rs.updateRow();
            }
        }
        LOG.debug("trs imported");
    }

    public void saveCaseResultMetrics(String trid, List<CaseResultMetric> resultMetrics) throws SQLException {
        if (resultMetrics.isEmpty()) {
            return;
        }
        final String sql = "INSERT INTO " + CaseResultMetric.TABLE_NAME + " ("
            + CaseResultMetric.CASE_RESULT_ID + ", "
            + CaseResultMetric.METRIC_GROUP + ", "
            + CaseResultMetric.METRIC_NAME + ", "
            + CaseResultMetric.METRIC_VALUE + ") VALUES (?,?,?,?)";
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            LOG.trace("save metric data for {}", trid);
            for (CaseResultMetric metric : resultMetrics) {
                stmt.setString(1, trid);
                stmt.setString(2, metric.getMetricGroup());
                stmt.setString(3, metric.getMetricName());
                stmt.setDouble(4, metric.getMetricValue());
                stmt.executeUpdate();
            }
        }
    }

    public static List<Map<String, Object>> dumpResultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rsml = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();

        while (rs.next()) {
            Map<String, Object> d = new LinkedHashMap<>();
            for (int col = 1; col <= rsmd.getColumnCount(); col++) {
                d.put(rsmd.getColumnLabel(col), rs.getObject(col));
            }
            rsml.add(d);
        }
        LOG.trace("{} rows loaded", rsml.size());
        return rsml;
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

    public static void main(String[] args) throws Exception {
        SystemConfiguration.getInstance();
        DbHandler db = DbHandler.getInstance();
        db.adjustSuiteExecutionResult("th_f592936e_69b7_46eb_9c76_eaed9bbbeea3");
    }
}
