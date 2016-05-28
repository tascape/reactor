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
package com.tascape.qa.th.db;

import com.tascape.qa.th.ExecutionResult;
import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.TestSuite;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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

    public static final String SYSPROP_DATABASE_TYPE = "qa.th.db.type";

    public static final String SYSPROP_DATABASE_HOST = "qa.th.db.host";

    public static final String SYSPROP_DATABASE_SCHEMA = "qa.th.db.schema";

    public static final String SYSPROP_DATABASE_USER = "qa.th.db.user";

    public static final String SYSPROP_DATABASE_PASS = "qa.th.db.pass";

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

    public abstract boolean queueTestSuite(TestSuite suite, String execId) throws SQLException;

    protected abstract int getTestCaseId(TestCase test) throws SQLException;

    protected Map<String, Integer> getTestCaseIds(List<TestCase> tests) throws SQLException {
        Set<String> suiteClasses = new HashSet<>();
        tests.stream().forEach((tc) -> {
            suiteClasses.add(tc.getSuiteClass());
        });

        Map<String, Integer> idMap = new HashMap<>();
        String sql = "SELECT * FROM " + TestCase.TABLE_NAME + " WHERE ";
        String sql0 = "";
        sql0 = suiteClasses.stream()
            .map((sc) -> " OR " + TestCase.SUITE_CLASS + "='" + sc + "'")
            .reduce(sql0, String::concat);
        sql += sql0.substring(4);
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            LOG.debug("{}", stmt.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TestCase tc = new TestCase();
                tc.setSuiteClass(rs.getString(TestCase.SUITE_CLASS));
                tc.setTestClass(rs.getString(TestCase.TEST_CLASS));
                tc.setTestMethod(rs.getString(TestCase.TEST_METHOD));
                tc.setTestDataInfo(rs.getString(TestCase.TEST_DATA_INFO));
                tc.setTestData(rs.getString(TestCase.TEST_DATA));
                idMap.put(tc.format(), rs.getInt(TestCase.TEST_CASE_ID));
            }
            LOG.debug("Found {} tests exist", idMap.size());
            return idMap;
        }
    }

    protected abstract void queueTestCaseResults(String execId, List<TestCase> tests) throws SQLException;

    public List<TestResult> getQueuedTestCaseResults(String execId, int limit) throws SQLException {
        LOG.debug("Query database for all queued test cases");
        final String sql = "SELECT * FROM " + TestResult.TABLE_NAME + " tr "
            + "INNER JOIN " + TestCase.TABLE_NAME + " tc "
            + "ON tr.TEST_CASE_ID=tc.TEST_CASE_ID AND " + TestResult.EXECUTION_RESULT + " = ? "
            + "WHERE " + TestResult.SUITE_RESULT + " = ? "
            + "ORDER BY SUITE_CLASS, TEST_CLASS, TEST_METHOD, TEST_DATA_INFO "
            + "LIMIT ?;";
        List<TestResult> tcrs = new ArrayList<>();

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, ExecutionResult.QUEUED.getName());
            stmt.setString(2, execId);
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TestResult tcr = new TestResult();
                tcr.setTestResultId(rs.getString(TestResult.TEST_RESULT_ID));
                tcr.setSuiteResultId(execId);
                tcr.setStartTime(rs.getLong(TestResult.START_TIME));
                tcr.setStopTime(rs.getLong(TestResult.STOP_TIME));
                tcr.setRetry(rs.getInt(TestResult.RETRY));

                TestCase tc = new TestCase();
                tc.setSuiteClass(rs.getString(TestCase.SUITE_CLASS));
                tc.setTestClass(rs.getString(TestCase.TEST_CLASS));
                tc.setTestMethod(rs.getString(TestCase.TEST_METHOD));
                tc.setTestDataInfo(rs.getString(TestCase.TEST_DATA_INFO));
                tc.setTestData(rs.getString(TestCase.TEST_DATA));
                tcr.setTestCase(tc);

                tcr.setTestStation(rs.getString(TestResult.TEST_STATION));
                tcr.setTestEnv(rs.getString(TestResult.TEST_ENV));
                tcr.setLogDir(rs.getString(TestResult.LOG_DIR));
                tcr.setExternalId(rs.getString(TestResult.EXTERNAL_ID));
                tcrs.add(tcr);
            }
        }

        int num = tcrs.size();
        LOG.debug("Found {} test case{} in DB with QUEUED state", num, num > 1 ? "s" : "");
        return tcrs;
    }

    public boolean acquireTestCaseResult(TestResult tcr) throws SQLException {
        LOG.debug("Try to acquire test case {}", tcr.getTestCase().format());
        final String sql = "SELECT * FROM " + TestResult.TABLE_NAME + " WHERE "
            + TestResult.TEST_RESULT_ID + " = ? LIMIT 1;";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, tcr.getTestResultId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String host = rs.getString(TestResult.TEST_STATION);
                if (rs.getString(TestResult.EXECUTION_RESULT).equals(ExecutionResult.QUEUED.result())) {
                    LOG.debug("Found test case {} in DB with QUEUED state", tcr.getTestCase().format());
                    if (SYS_CONFIG.getHostName().equals(host)) {
                        LOG.debug("This test case Failed on current host, and was requeue. Skip...");
                        return false;
                    }
                    rs.updateString(TestResult.EXECUTION_RESULT, ExecutionResult.ACQUIRED.result());
                    rs.updateString(TestResult.TEST_STATION, SYS_CONFIG.getHostName());
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
        LOG.debug("Update test result {} ({}) to {}", tcr.getTestResultId(), tcr.getTestCase().format(),
            tcr.getResult().result());
        final String sql = "SELECT tr.* FROM " + TestResult.TABLE_NAME + " tr INNER JOIN " + TestCase.TABLE_NAME
            + " tc WHERE tr.TEST_CASE_ID=tc.TEST_CASE_ID AND "
            + TestResult.TEST_RESULT_ID + " = ?;";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, tcr.getTestResultId());
            ResultSet rs = stmt.executeQuery();
            if (rs.first()) {
// TODO: update test data into test case table
//                rs.updateString(TestCase.TEST_DATA.name(), tcr.getTestCase().getTestData());
                rs.updateString(TestResult.EXECUTION_RESULT, tcr.getResult().result());
                rs.updateString(TestResult.AUT, tcr.getAut());
                rs.updateLong(TestResult.START_TIME, tcr.getStartTime());
                rs.updateLong(TestResult.STOP_TIME, tcr.getStopTime());
                rs.updateInt(TestResult.RETRY, tcr.getRetry());
                rs.updateString(TestResult.TEST_STATION, tcr.getTestStation());
                rs.updateString(TestResult.TEST_ENV, tcr.getTestEnv());
                rs.updateString(TestResult.LOG_DIR, tcr.getLogDir());
                rs.updateString(TestResult.EXTERNAL_ID, tcr.getExternalId());
                rs.updateRow();
            } else {
                LOG.warn("Cannot update test result");
            }
        }
    }

    public abstract void updateSuiteExecutionResult(String execId) throws SQLException;

    public ExecutionResult updateSuiteExecutionResult(String execId, String productUnderTest) throws SQLException {
        LOG.debug("Update test suite execution result with execution id {}", execId);
        String lock = "testharness." + execId;

        ExecutionResult suiteResult = ExecutionResult.newMultiple();
        int total = 0, fail = 0;
        try (Connection conn = this.getConnection()) {
            try {
                if (!this.acquireExecutionLock(conn, lock)) {
                    throw new SQLException("Cannot acquire lock of name " + lock);
                }

                {
                    final String sql1 = "SELECT " + TestResult.EXECUTION_RESULT + " FROM "
                        + TestResult.TABLE_NAME + " WHERE " + TestResult.SUITE_RESULT
                        + " = ?;";
                    try (PreparedStatement stmt = this.getConnection().prepareStatement(sql1,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY)) {
                        stmt.setString(1, execId);
                        // stmt.setFetchSize(Integer.MIN_VALUE); // mysql only
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            String result = rs.getString(TestResult.EXECUTION_RESULT);
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
                                throw new RuntimeException("Cannot parse test execution result " + result);
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
                            rs.updateInt(SuiteResult.NUMBER_OF_TESTS, total);
                            rs.updateInt(SuiteResult.NUMBER_OF_FAILURE, fail);
                            rs.updateString(SuiteResult.EXECUTION_RESULT, fail == 0 ? "PASS" : "FAIL");
                            rs.updateLong(SuiteResult.STOP_TIME, System.currentTimeMillis());
                            if (rs.getNString(SuiteResult.PRODUCT_UNDER_TEST).isEmpty()
                                && !StringUtils.isEmpty(productUnderTest)) {
                                rs.updateString(SuiteResult.PRODUCT_UNDER_TEST, productUnderTest);
                            }
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
        LOG.debug("Adjust test suite execution result of execution id {} with test iterations", execId);
        String lock = "testharness." + execId;

        ExecutionResult suiteResult = ExecutionResult.newMultiple();
        int total = 0, fail = 0;
        try (Connection conn = this.getConnection()) {
            try {
                if (!this.acquireExecutionLock(conn, lock)) {
                    throw new SQLException("Cannot acquire lock of name " + lock);
                }

                {
                    String iterDataInfo = "com.tascape.qa.th.data.TestIterationData";
                    Map<String, Boolean> iterationTests = new HashMap<>();
                    String sql = "SELECT tr.*, tc.* FROM "
                        + TestResult.TABLE_NAME + " tr JOIN " + TestCase.TABLE_NAME + " tc"
                        + " ON tr." + TestResult.TEST_CASE_ID + " = tc." + TestCase.TEST_CASE_ID
                        + " WHERE " + TestResult.SUITE_RESULT + " = ?;";
                    try (PreparedStatement stmt = this.getConnection().prepareStatement(sql)) {
                        stmt.setString(1, execId);
                        LOG.debug("{}", stmt);
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            String result = rs.getString(TestResult.EXECUTION_RESULT);
                            int p = 0, f = 0;
                            String[] pf = result.split("/");
                            if (pf.length == 1) {
                                if (result.equals(ExecutionResult.PASS.getName())) {
                                    p = 1;
                                } else {
                                    f = 1;
                                }
                            } else if (pf.length == 2) {
                                p = Integer.parseInt(pf[0]);
                                f = Integer.parseInt(pf[1]);
                            } else {
                                throw new RuntimeException("Cannot parse test execution result " + result);
                            }

                            if (rs.getString(TestCase.TEST_DATA_INFO).startsWith(iterDataInfo)) {
                                String key = rs.getString(TestCase.SUITE_CLASS) + rs.getString(TestCase.TEST_CLASS)
                                    + rs.getString(TestCase.TEST_METHOD);
                                Boolean r = iterationTests.get(key);
                                if (r == null) {
                                    iterationTests.put(key, f > 0 ? Boolean.FALSE : Boolean.TRUE);
                                } else {
                                    iterationTests.put(key, f > 0 ? Boolean.FALSE : r);
                                }
                            } else {
                                total += p + f;
                                fail += f;
                            }
                        }
                        LOG.debug("{} {}", total, fail);
                    }
                    total += iterationTests.size();
                    fail += iterationTests.values().stream().filter(v -> v.equals(Boolean.FALSE)).count();
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
                            rs.updateInt(SuiteResult.NUMBER_OF_TESTS, total);
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
        LOG.debug("Overwrite test suite execution result with execution id {} with {}", execId, result);
        int total = result.getPass() + result.getFail();
        if (total == 0) {
            return;
        }
        int fail = result.getFail();

        String lock = "testharness." + execId;
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
                        rs.updateInt(SuiteResult.NUMBER_OF_TESTS, total);
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
        try (PreparedStatement stmt = this.getConnection().prepareStatement(sql)) {
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
                    xsw.writeAttribute("tests", rs.getInt(SuiteResult.NUMBER_OF_TESTS) + "");
                    xsw.writeAttribute("failures", rs.getInt(SuiteResult.NUMBER_OF_FAILURE) + "");
                    xsw.writeAttribute("time", (rs.getLong(SuiteResult.STOP_TIME)
                        - rs.getLong(TestResult.START_TIME)) / 1000.0 + "");
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
                    pwh.printf("<h4>tests %d, failures %d</h4>",
                        rs.getInt(SuiteResult.NUMBER_OF_TESTS), rs.getInt(SuiteResult.NUMBER_OF_FAILURE));
                    pwh.println("<table><thead><tr><th>index</th><th>test case</th><th>result</th></thead><tbody>");

                    final String sql1 = "SELECT * FROM " + TestResult.TABLE_NAME + " tr JOIN "
                        + TestCase.TABLE_NAME + " tc ON "
                        + "tr." + TestResult.TEST_CASE_ID + " = tc." + TestCase.TEST_CASE_ID
                        + " WHERE " + TestResult.SUITE_RESULT + " = ?;";
                    try (PreparedStatement stmt1 = this.getConnection().prepareStatement(sql1)) {
                        stmt1.setString(1, execId);
                        ResultSet rs1 = stmt1.executeQuery();
                        int i = 0;
                        while (rs1.next()) {
                            String result = rs1.getString(TestResult.EXECUTION_RESULT);
                            xsw.writeCharacters("  ");
                            xsw.writeStartElement("testcase");
                            xsw.writeAttribute("name", rs1.getString(TestCase.TEST_METHOD) + "("
                                + rs1.getString(TestCase.TEST_DATA) + ")");
                            xsw.writeAttribute("classname", rs1.getString(TestCase.TEST_CLASS));
                            xsw.writeAttribute("result", result);
                            xsw.writeAttribute("time", (rs1.getLong(TestResult.STOP_TIME)
                                - rs1.getLong(TestResult.START_TIME)) / 1000.0 + "");
                            if (ExecutionResult.isFailure(result)) {
                                xsw.writeStartElement("failure");
                                xsw.writeAttribute("type", "failure");
                                xsw.writeEndElement();
                                xsw.writeCharacters("\n");
                            }
                            xsw.writeEndElement();
                            xsw.writeCharacters("\n");

                            String l = rs1.getString(TestResult.LOG_DIR);
                            String r = rs1.getString(TestResult.EXECUTION_RESULT);
                            pwh.printf("<tr><td>%d</td><td>%s</td>"
                                + "<td><a style='color: %s; font-weight: bold' href='%s/log.html' target='_blank'>%s</a></td></tr>",
                                ++i,
                                rs1.getString(TestCase.TEST_METHOD) + "(" + rs1.getString(TestCase.TEST_DATA) + ")",
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
                LOG.error("No test suite result of exec id {}", execId);
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
                    LOG.error("No test suite result of exec id {}", execId);

                }
                Map<String, Object> r = l.get(0);
                r.entrySet().forEach(entry -> {
                    sr.put(entry.getKey(), entry.getValue());
                });
            }
        }
        List<Map<String, Object>> metrics;
        {
            final String sql = "SELECT trm.* FROM " + TestResultMetric.TABLE_NAME + " trm JOIN "
                + TestResult.TABLE_NAME + " tr ON"
                + " trm." + TestResultMetric.TEST_RESULT_ID + " = tr." + TestResult.TEST_RESULT_ID
                + " WHERE " + TestResult.SUITE_RESULT + " = ?;";
            try (Connection conn = this.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, execId);
                metrics = dumpResultSetToList(stmt.executeQuery());
            }
        }
        {
            JSONArray trs = new JSONArray();
            final String sql = "SELECT * FROM " + TestResult.TABLE_NAME + " tr JOIN "
                + TestCase.TABLE_NAME + " tc ON"
                + " tr." + TestResult.TEST_CASE_ID + " = tc." + TestCase.TEST_CASE_ID
                + " WHERE " + TestResult.SUITE_RESULT + " = ?;";
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
                        .filter(r -> j.getString(TestResult.TEST_RESULT_ID).equals(r.get(TestResult.TEST_RESULT_ID)))
                        .forEach(r -> {
                            JSONObject jm = new JSONObject();
                            r.entrySet().forEach(c -> {
                                jm.put(c.getKey(), c.getValue());
                            });
                            trms.put(jm);
                        });
                    j.put("test_result_metrics", trms);
                });
            }
            sr.put("test_results", trs);
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
        FileUtils.write(path.toFile(), new JSONObject().put("suite_result", sr).toString(2));
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

        JSONArray trs = sr.getJSONArray("test_results");
        int len = trs.length();

        try (Connection conn = this.getConnection()) {
            String sql = String.format("SELECT * FROM %s WHERE %s=? AND %s=? AND %s=? AND %s=? AND %s=?;",
                TestCase.TABLE_NAME,
                TestCase.SUITE_CLASS,
                TestCase.TEST_CLASS,
                TestCase.TEST_METHOD,
                TestCase.TEST_DATA_INFO,
                TestCase.TEST_DATA
            );
            PreparedStatement stmt
                = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setMaxRows(1);
            for (int i = 0; i < len; i++) {
                JSONObject tr = trs.getJSONObject(i);
                stmt.setString(1, tr.getString(TestCase.SUITE_CLASS));
                stmt.setString(2, tr.getString(TestCase.TEST_CLASS));
                stmt.setString(3, tr.getString(TestCase.TEST_METHOD));
                stmt.setString(4, tr.getString(TestCase.TEST_DATA_INFO));
                stmt.setString(5, tr.getString(TestCase.TEST_DATA));
                ResultSet rs = stmt.executeQuery();
                if (!rs.first()) {
                    rs.moveToInsertRow();
                    rs.updateString(TestCase.SUITE_CLASS, tr.getString(TestCase.SUITE_CLASS));
                    rs.updateString(TestCase.TEST_CLASS, tr.getString(TestCase.TEST_CLASS));
                    rs.updateString(TestCase.TEST_METHOD, tr.getString(TestCase.TEST_METHOD));
                    rs.updateString(TestCase.TEST_DATA_INFO, tr.getString(TestCase.TEST_DATA_INFO));
                    rs.updateString(TestCase.TEST_DATA, tr.getString(TestCase.TEST_DATA));
                    rs.insertRow();
                    rs.last();
                    rs.updateRow();
                    rs = stmt.executeQuery();
                    rs.first();
                }
                tr.put(TestCase.TEST_CASE_ID, rs.getLong(TestCase.TEST_CASE_ID));
            }
        }
        LOG.debug("tcid updated");

        try (Connection conn = this.getConnection()) {
            String sql = "SELECT * FROM " + TestResult.TABLE_NAME + " WHERE " + TestResult.SUITE_RESULT + " = ?;";
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

    public void saveTestResultMetrics(String trid, List<TestResultMetric> resultMetrics) throws SQLException {
        if (resultMetrics.isEmpty()) {
            return;
        }
        final String sql = "INSERT INTO " + TestResultMetric.TABLE_NAME + " ("
            + TestResultMetric.TEST_RESULT_ID + ", "
            + TestResultMetric.METRIC_GROUP + ", "
            + TestResultMetric.METRIC_NAME + ", "
            + TestResultMetric.METRIC_VALUE + ") VALUES (?,?,?,?)";
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            LOG.trace("save metric data for {}", trid);
            for (TestResultMetric metric : resultMetrics) {
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
