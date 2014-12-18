package com.tascape.qa.thr;

import com.tascape.qa.th.db.DbHandler.Suite_Result;
import com.tascape.qa.th.db.DbHandler.TABLES;
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
import java.util.Properties;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
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

    protected Properties params;

    public List<Map<String, Object>> getTestSuiteResults(long startTime, long stopTime, int numberOfEntries,
            String suiteName, boolean invisibleIncluded) throws NamingException, SQLException {
        String sql = "SELECT * FROM " + TABLES.suite_result.name() + " "
                + "WHERE " + Suite_Result.START_TIME.name() + " > ? "
                + "AND " + Suite_Result.STOP_TIME.name() + " < ? ";
        if (suiteName != null && !suiteName.isEmpty()) {
            sql += "AND " + Suite_Result.SUITE_NAME.name() + " = ? ";
        }
        sql += ";";
        try (Connection conn = this.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, startTime);
            stmt.setLong(2, stopTime);
            if (suiteName != null && !suiteName.isEmpty()) {
                stmt.setString(3, suiteName);
            }
            stmt.setMaxRows(numberOfEntries);
            ResultSet rs = stmt.executeQuery();
            return this.dumpResultSetToList(rs);
        }
    }

    private Connection getConnection() throws NamingException, SQLException {
        Context ctx = new InitialContext();
        ctx = (Context) ctx.lookup("java:comp/env");
        ds = (DataSource) ctx.lookup("jdbc/thr");
        if (ds == null) {
            throw new SQLException("Can't get data source");
        }

        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("Can't get database connection");
        }
        return conn;
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
}
