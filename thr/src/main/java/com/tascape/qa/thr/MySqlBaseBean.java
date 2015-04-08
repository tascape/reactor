package com.tascape.qa.thr;

import com.tascape.qa.th.db.DbHandler.Test_Result;
import com.tascape.qa.th.db.SuiteResult;
import com.tascape.qa.th.db.TestCase;
import com.tascape.qa.th.db.TestResult;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
@Named
@ApplicationScoped
public class MySqlBaseBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(MySqlBaseBean.class);

    @Resource(name = "jdbc/thr")
    private DataSource ds;

    public List<Map<String, Object>> getSuitesResult(long startTime, long stopTime, int numberOfEntries,
            String suiteName, String jobName, boolean invisibleIncluded)
            throws NamingException, SQLException {
        String sql = "SELECT * FROM " + SuiteResult.TABLE_NAME
                + " WHERE " + SuiteResult.START_TIME + " > ?"
                + " AND " + SuiteResult.STOP_TIME + " < ?";
        if (notEmpty(suiteName)) {
            sql += " AND " + SuiteResult.SUITE_NAME + " = ?";
        } else if (notEmpty(jobName)) {
            sql += " AND " + SuiteResult.JOB_NAME + " = ?";
        }
        if (!invisibleIncluded) {
            sql += " AND NOT " + SuiteResult.INVISIBLE_ENTRY;
        }
        sql += " ORDER BY " + SuiteResult.START_TIME + " DESC;";
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, startTime);
            stmt.setLong(2, stopTime);
            if (notEmpty(suiteName)) {
                stmt.setString(3, suiteName);
            } else if (notEmpty(jobName)) {
                stmt.setString(3, jobName);
            }
            LOG.trace("{}", stmt);
            stmt.setMaxRows(numberOfEntries);
            ResultSet rs = stmt.executeQuery();
            return this.dumpResultSetToList(rs);
        }
    }

    public Map<String, Object> getSuiteResult(String srid) throws NamingException, SQLException {
        String sql = "SELECT * FROM " + SuiteResult.TABLE_NAME
                + " WHERE " + SuiteResult.SUITE_RESULT_ID + " = ?;";
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, srid);
            LOG.trace("{}", stmt);
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> data = this.dumpResultSetToList(rs);
            if (data.isEmpty()) {
                throw new SQLException("No data for suite result id " + srid);
            }
            return data.get(0);
        }
    }

    public List<Map<String, Object>> getTestsResult(String srid) throws NamingException, SQLException {
        String sql = "SELECT * FROM " + TestResult.TABLE_NAME + " TR "
                + "INNER JOIN " + TestCase.TABLE_NAME + " TC "
                + "ON TR.TEST_CASE_ID = TC.TEST_CASE_ID "
                + "WHERE " + Test_Result.SUITE_RESULT.name() + " = ? "
                + "ORDER BY " + Test_Result.START_TIME.name() + " DESC;";
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, srid);
            LOG.trace("{}", stmt);
            ResultSet rs = stmt.executeQuery();
            return this.dumpResultSetToList(rs);
        }
    }

    public List<Map<String, Object>> getTestsResult(List<String> srids) throws NamingException, SQLException {
        String sql = "SELECT * FROM " + TestResult.TABLE_NAME + " TR "
                + "INNER JOIN " + TestCase.TABLE_NAME + " TC "
                + "ON TR.TEST_CASE_ID = TC.TEST_CASE_ID "
                + "WHERE " + Test_Result.SUITE_RESULT.name() + " IN (" + StringUtils.join(srids, ",") + ") "
                + "ORDER BY " + TestCase.SUITE_CLASS + "," + TestCase.TEST_CLASS + ","
                + TestCase.TEST_METHOD + "," + TestCase.TEST_DATA_INFO + " DESC;";
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            LOG.trace("{}", stmt);
            ResultSet rs = stmt.executeQuery();
            return this.dumpResultSetToList(rs);
        }
    }

    public void setSuiteResultInvisible(String srid, boolean invisible) throws NamingException, SQLException {
        String sql = "UPDATE " + SuiteResult.TABLE_NAME
                + " SET " + SuiteResult.INVISIBLE_ENTRY + " = ?"
                + " WHERE " + SuiteResult.SUITE_RESULT_ID + " = ?;";
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            stmt.setBoolean(1, invisible);
            stmt.setString(2, srid);
            LOG.trace("{}", stmt);
            stmt.executeUpdate();
        }
    }

    public List<Map<String, Object>> getSuiteResultDetailHistory(long startTime, long stopTime, int numberOfEntries,
            String suiteName, boolean invisibleIncluded) throws NamingException, SQLException {
        String sr = "SELECT " + SuiteResult.SUITE_RESULT_ID + " FROM " + SuiteResult.TABLE_NAME
                + " WHERE " + SuiteResult.START_TIME + " > ?"
                + " AND " + SuiteResult.STOP_TIME + " < ?"
                + " AND " + SuiteResult.SUITE_NAME + " = ?";
        if (!invisibleIncluded) {
            sr += " AND NOT " + SuiteResult.INVISIBLE_ENTRY;
        }
        sr += " ORDER BY " + SuiteResult.START_TIME + " DESC;";

        String tr = "SELECT * FROM " + TestResult.TABLE_NAME
                + " WHERE " + Test_Result.EXECUTION_RESULT.name()
                + " IN (" + sr + ")"
                + " ORDER BY " + SuiteResult.START_TIME + " DESC;";
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(tr);
            stmt.setLong(1, startTime);
            stmt.setLong(2, stopTime);
            if (suiteName != null && !suiteName.isEmpty()) {
                stmt.setString(3, suiteName);
            }
            LOG.trace("{}", stmt);
            stmt.setMaxRows(numberOfEntries);
            ResultSet rs = stmt.executeQuery();
            return this.dumpResultSetToList(rs);
        }
    }

    public List<Map<String, Object>> dumpResultSetToList(ResultSet rs) throws SQLException {
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

    public Date convertToDate(long time) {
        return new Date(time);
    }

    public static void setLogUrl(Map<String, Object> row) {
        String dir = row.get("LOG_DIR").toString();
        dir = dir.replaceAll("\\\\", "/");
        int logs = dir.indexOf("/qa/logs/");
        if (logs >= 0) {
            row.put("_url", "http://" + row.get("TEST_STATION") + "/" + dir.substring(logs + 4) + "/log.html");
        } else {
            row.put("_url", ".");
        }
    }

    public static long getMillis(String time) {
        if (time == null || time.trim().isEmpty()) {
            return System.currentTimeMillis();
        } else {
            LocalDateTime ldt = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LOG.trace("ldt {}", ldt);
            ZoneId zone = ZoneId.of("America/Los_Angeles");
            ldt.atZone(zone);
            LOG.trace("ldt {}", ldt);
            return ldt.toInstant(ZoneOffset.ofHours(-8)).toEpochMilli();
        }
    }

    public static boolean notEmpty(String string) {
        if (string == null) {
            return false;
        }
        if (string.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    private Connection getConnection() throws NamingException, SQLException {
        Connection conn = this.ds.getConnection();
        if (conn == null) {
            throw new SQLException("Can't get database connection");
        }
        return conn;
    }
}
