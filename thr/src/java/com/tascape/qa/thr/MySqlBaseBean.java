package com.tascape.qa.thr;

import com.tascape.qa.th.db.DbHandler.Suite_Result;
import com.tascape.qa.th.db.DbHandler.TABLES;
import com.tascape.qa.th.db.DbHandler.Test_Result;
import com.tascape.qa.th.db.DbHandler.Test_Case;
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
        String suiteName, boolean invisibleIncluded) throws NamingException, SQLException {
        String sql = "SELECT * FROM " + TABLES.suite_result.name()
            + " WHERE " + Suite_Result.START_TIME.name() + " > ?"
            + " AND " + Suite_Result.STOP_TIME.name() + " < ?";
        if (suiteName != null && !suiteName.isEmpty()) {
            sql += " AND " + Suite_Result.SUITE_NAME.name() + " = ?";
        }
        if (!invisibleIncluded) {
            sql += " AND NOT " + Suite_Result.INVISIBLE_ENTRY.name();
        }
        sql += " ORDER BY " + Suite_Result.START_TIME.name() + " DESC;";
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
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

    public Map<String, Object> getSuiteResult(String srid) throws NamingException, SQLException {
        String sql = "SELECT * FROM " + TABLES.suite_result.name()
            + " WHERE " + Suite_Result.SUITE_RESULT_ID.name() + " = ?;";
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
        String sql = "SELECT * FROM " + TABLES.test_result.name() + " TR "
            + "INNER JOIN " + TABLES.test_case + " TC "
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
        String sql = "SELECT * FROM " + TABLES.test_result.name() + " TR "
            + "INNER JOIN " + TABLES.test_case + " TC "
            + "ON TR.TEST_CASE_ID = TC.TEST_CASE_ID "
            + "WHERE " + Test_Result.SUITE_RESULT.name() + " IN (" + StringUtils.join(srids, ",") + ") "
            + "ORDER BY " + Test_Case.SUITE_CLASS.name() + "," + Test_Case.TEST_CLASS.name() + ","
            + Test_Case.TEST_METHOD.name() + "," + Test_Case.TEST_DATA_INFO.name() + " DESC;";
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            LOG.trace("{}", stmt);
            ResultSet rs = stmt.executeQuery();
            return this.dumpResultSetToList(rs);
        }
    }

    public void setSuiteResultInvisible(String srid, boolean invisible) throws NamingException, SQLException {
        String sql = "UPDATE " + TABLES.suite_result.name()
            + " SET " + Suite_Result.INVISIBLE_ENTRY.name() + " = ?"
            + " WHERE " + Suite_Result.SUITE_RESULT_ID.name() + " = ?;";
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
        String sr = "SELECT " + Suite_Result.SUITE_RESULT_ID + " FROM " + TABLES.suite_result.name()
            + " WHERE " + Suite_Result.START_TIME.name() + " > ?"
            + " AND " + Suite_Result.STOP_TIME.name() + " < ?"
            + " AND " + Suite_Result.SUITE_NAME.name() + " = ?";
        if (!invisibleIncluded) {
            sr += " AND NOT " + Suite_Result.INVISIBLE_ENTRY.name();
        }
        sr += " ORDER BY " + Suite_Result.START_TIME.name() + " DESC;";

        String tr = "SELECT * FROM " + TABLES.test_result.name()
            + " WHERE " + Test_Result.EXECUTION_RESULT.name()
            + " IN (" + sr + ")"
            + " ORDER BY " + Suite_Result.START_TIME.name() + " DESC;";
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

    private Connection getConnection() throws NamingException, SQLException {
        Connection conn = this.ds.getConnection();
        if (conn == null) {
            throw new SQLException("Can't get database connection");
        }
        return conn;
    }

}
