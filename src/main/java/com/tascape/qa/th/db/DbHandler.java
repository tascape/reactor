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
import com.tascape.qa.th.TestSuite;
import java.io.File;
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

    public enum TABLES {
        test_result_metric,
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
        LOG.info("Query database for all queued test cases");
        final String sql = "SELECT * FROM " + TestResult.TABLE_NAME + " tr "
            + "INNER JOIN " + TestCase.TABLE_NAME + " tc "
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
                tcr.setTestResultId(rs.getString(Test_Result.TEST_RESULT_ID.name()));
                tcr.setSuiteResultId(execId);
                tcr.setStartTime(rs.getLong(Test_Result.START_TIME.name()));
                tcr.setStopTime(rs.getLong(Test_Result.STOP_TIME.name()));
                tcr.setRetry(rs.getInt(Test_Result.RETRY.name()));

                TestCase tc = new TestCase();
                tc.setSuiteClass(rs.getString(TestCase.SUITE_CLASS));
                tc.setTestClass(rs.getString(TestCase.TEST_CLASS));
                tc.setTestMethod(rs.getString(TestCase.TEST_METHOD));
                tc.setTestDataInfo(rs.getString(TestCase.TEST_DATA_INFO));
                tc.setTestData(rs.getString(TestCase.TEST_DATA));
                tcr.setTestCase(tc);

                tcr.setTestStation(rs.getString(Test_Result.TEST_STATION.name()));
                tcr.setLogDir(rs.getString(Test_Result.LOG_DIR.name()));
                tcrs.add(tcr);
            }
        }

        int num = tcrs.size();
        LOG.debug("Found {} test case{} in DB with QUEUED state", num, num > 1 ? "s" : "");
        return tcrs;
    }

    public boolean acquireTestCaseResult(TestResult tcr) throws SQLException {
        LOG.info("Acquire test case {}", tcr.getTestCase().format());
        final String sql = "SELECT * FROM " + TestResult.TABLE_NAME + " WHERE "
            + Test_Result.TEST_RESULT_ID.name() + " = ? LIMIT 1;";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, tcr.getTestResultId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String host = rs.getString(Test_Result.TEST_STATION.name());
                if (rs.getString(Test_Result.EXECUTION_RESULT.name()).equals(ExecutionResult.QUEUED.result())) {
                    LOG.debug("Found test case {} in DB with QUEUED state", tcr.getTestCase().format());
                    if (SYS_CONFIG.getHostName().equals(host)) {
                        LOG.debug("This test case Failed on current host, and was requeue. Skip...");
                        return false;
                    }
                    rs.updateString(Test_Result.EXECUTION_RESULT.name(), ExecutionResult.ACQUIRED.result());
                    rs.updateString(Test_Result.TEST_STATION.name(), SYS_CONFIG.getHostName());
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
        LOG.info("Update test result {} ({}) to {}", tcr.getTestResultId(), tcr.getTestCase().format(),
            tcr.getResult().result());
        final String sql = "SELECT tr.* FROM " + TestResult.TABLE_NAME + " tr INNER JOIN " + TestCase.TABLE_NAME
            + " tc WHERE tr.TEST_CASE_ID=tc.TEST_CASE_ID AND "
            + Test_Result.TEST_RESULT_ID.name() + " = ?;";

        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, tcr.getTestResultId());
            ResultSet rs = stmt.executeQuery();
            if (rs.first()) {
// TODO: update test data into test case table
//                rs.updateString(TestCase.TEST_DATA.name(), tcr.getTestCase().getTestData());
                rs.updateString(Test_Result.EXECUTION_RESULT.name(), tcr.getResult().result());
                rs.updateString(Test_Result.AUT.name(), tcr.getAut());
                rs.updateLong(Test_Result.START_TIME.name(), tcr.getStartTime());
                rs.updateLong(Test_Result.STOP_TIME.name(), tcr.getStopTime());
                rs.updateInt(Test_Result.RETRY.name(), tcr.getRetry());
                rs.updateString(Test_Result.TEST_STATION.name(), tcr.getTestStation());
                rs.updateString(Test_Result.LOG_DIR.name(), tcr.getLogDir());
                rs.updateRow();
            } else {
                LOG.warn("Cannot update test result");
            }
        }
    }

    public abstract void updateSuiteExecutionResult(String execId) throws SQLException;

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
                    xsw.writeAttribute("tests", rs.getInt(SuiteResult.NUMBER_OF_TESTS) + "");
                    xsw.writeAttribute("failures", rs.getInt(SuiteResult.NUMBER_OF_FAILURE) + "");
                    xsw.writeAttribute("time", (rs.getLong(SuiteResult.STOP_TIME)
                        - rs.getLong(Test_Result.START_TIME.name())) / 1000.0 + "");
                    xsw.writeAttribute("srid", rs.getString(SuiteResult.SUITE_RESULT_ID));
                    xsw.writeCharacters("\n");

                    pwh.println("<html><body>");
                    pwh.printf("<h2>%s</h2>", rs.getString(SuiteResult.SUITE_NAME));
                    pwh.printf("<h4>tests %d, failures %d</h4>",
                        rs.getInt(SuiteResult.NUMBER_OF_TESTS), rs.getInt(SuiteResult.NUMBER_OF_FAILURE));
                    pwh.println("<ol>");

                    final String sql1 = "SELECT * FROM " + TestResult.TABLE_NAME + " tr JOIN "
                        + TestCase.TABLE_NAME + " tc ON "
                        + "tr." + Test_Result.TEST_CASE_ID + " = tc." + TestCase.TEST_CASE_ID
                        + " WHERE " + Test_Result.SUITE_RESULT.name() + " = ?;";
                    try (PreparedStatement stmt1 = this.getConnection().prepareStatement(sql1)) {
                        stmt1.setString(1, execId);
                        ResultSet rs1 = stmt1.executeQuery();
                        while (rs1.next()) {
                            xsw.writeCharacters("  ");
                            xsw.writeStartElement("testcase");
                            xsw.writeAttribute("name", rs1.getString(TestCase.TEST_METHOD) + "("
                                + rs1.getString(TestCase.TEST_DATA) + ")");
                            xsw.writeAttribute("classname", rs1.getString(TestCase.TEST_CLASS));
                            xsw.writeAttribute("result", rs1.getString(Test_Result.EXECUTION_RESULT.name()));
                            xsw.writeAttribute("time", (rs1.getLong(Test_Result.STOP_TIME.name())
                                - rs1.getLong(Test_Result.START_TIME.name())) / 1000.0 + "");
                            xsw.writeEndElement();
                            xsw.writeCharacters("\n");

                            String l = rs1.getString(Test_Result.LOG_DIR.name());
                            int i = l.lastIndexOf("/");
                            if (i >= 0) {
                                pwh.printf("<li><a href='%s/log.html'>%s</a> - %s</li>", l.substring(i + 1),
                                    rs1.getString(TestCase.TEST_METHOD) + "(" + rs1.getString(TestCase.TEST_DATA) + ")",
                                    rs1.getString(Test_Result.EXECUTION_RESULT.name()));
                            }
                        }
                    }
                    pwh.println("</ol></body></html>");

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
                ResultSet rs = stmt.executeQuery();
                List<Map<String, Object>> l = dumpResultSetToList(rs);
                if (l.isEmpty()) {
                    LOG.error("No test suite result of exec id {}", execId);

                }
                Map<String, Object> r = l.get(0);
                r.entrySet().forEach(entry -> {
                    sr.put(entry.getKey(), entry.getValue());
                });
            }
        }
        JSONArray trs = new JSONArray();
        {
            final String sql = "SELECT * FROM " + TestResult.TABLE_NAME + " tr JOIN "
                + TestCase.TABLE_NAME + " tc ON "
                + "tr." + Test_Result.TEST_CASE_ID + " = tc." + TestCase.TEST_CASE_ID
                + " WHERE " + Test_Result.SUITE_RESULT.name() + " = ?;";
            try (Connection conn = this.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, execId);
                ResultSet rs1 = stmt.executeQuery();
                List<Map<String, Object>> l = dumpResultSetToList(rs1);
                l.forEach(row -> {
                    JSONObject j = new JSONObject();
                    row.entrySet().forEach(col -> {
                        j.put(col.getKey(), col.getValue());
                    });
                    trs.put(trs.length(), j);
                });
            }
        }
        sr.put("test_results", trs);
        FileUtils.write(path.toFile(), new JSONObject().put("suite_result", sr).toString(2));
    }

    public void importFromJson(String json) throws SQLException {
        JSONObject j = new JSONObject(json);
        JSONObject sr = j.getJSONObject("suite_result");
        String execId = sr.getString(SuiteResult.SUITE_RESULT_ID);
        JSONArray trs = sr.getJSONArray("test_results");
        {
            String sql = "SELECT * FROM " + SuiteResult.TABLE_NAME + " WHERE "
                + SuiteResult.SUITE_RESULT_ID + " = ?;";
            try (Connection conn = this.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql,
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                stmt.setString(1, execId);
                ResultSet rs = stmt.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();
                if (!rs.first()) {
                    rs.moveToInsertRow();
                    rs.insertRow();
                }
                for (int col = 1; col <= rsmd.getColumnCount(); col++) {
                    String cn = rsmd.getColumnLabel(col);
                    rs.updateObject(cn, sr.get(cn));
                }
                rs.last();
                rs.updateRow();
            }
        }
    }

    public void saveTestResultMetrics(String trid, List<TestResultMetric> resultMetrics) throws SQLException {
        final String sql = "SELECT * FROM " + TABLES.test_result_metric.name() + " WHERE "
            + Test_Result_Metric.TEST_RESULT_ID.name() + " = ?;";
        try (Connection conn = this.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            stmt.setMaxRows(1);
            stmt.setString(1, trid);
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

    public static List<Map<String, Object>> dumpResultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rsml = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();

        int row = 1;
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
        String json = FileUtils.readFileToString(new File(args[0]));
        db.importFromJson(json);
    }
}
