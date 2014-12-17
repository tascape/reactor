package com.tascape.qa.thr;

import com.tascape.qa.th.SystemConfiguration;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong
 */
public class ResultBase implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(ResultBase.class);

    public static final String TEST_METHOD_NA = "no-method";

    @Resource(name = "jdbc/thr")
    private DataSource ds;

    protected Properties params;

    ResultBase() {
        this.loadParameters();
    }

    public Date convertToDate(long time) {
        return new Date(time);
    }

    public Date convertToDate(String time) {
        if (time == null || time.isEmpty()) {
            return new Date(0);
        }
        return this.convertToDate(Long.parseLong(time));
    }

    protected List<Map<String, Object>> dumpResultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rsml = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();

        int row = 1;
        while (rs.next()) {
            Map<String, Object> d = new LinkedHashMap<>();
            for (int col = 1; col <= rsmd.getColumnCount(); col++) {
                d.put(rsmd.getColumnLabel(col), rs.getObject(col));
            }

            d.put("_row_index", row++);

            String result = d.get("EXECUTION_RESULT") + "";
            boolean r = result.equals("PASS") || result.endsWith("/0");
            d.put("_row_result", r);

            Object method = d.get("TEST_METHOD");
            if (method != null && (method + "").isEmpty()) {
                d.put("TEST_METHOD", TEST_METHOD_NA);
            }

            rsml.add(d);
        }
        LOG.trace("{} rows loaded", rsml.size());
        return rsml;
    }

    protected Map<String, Map<String, Object>> dumpResultSetToMap(ResultSet rs, String keyCol) throws SQLException {
        Map<String, Map<String, Object>> rskm = new LinkedHashMap<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> d = new LinkedHashMap<>();
            for (int col = 1; col <= rsmd.getColumnCount(); col++) {
                d.put(rsmd.getColumnLabel(col), rs.getObject(col));
            }

            String k = d.get(keyCol) + "";
            if (rskm.get(k) == null) {
                rskm.put(k, d);
            }
        }
        LOG.trace("{} rows loaded", rskm.size());
        return rskm;
    }

    protected String getLogUrl(String host, String dir) {
        int th_ = dir.indexOf(SystemConfiguration.CONSTANT_EXEC_ID_PREFIX);
        if (th_ > 0) {
            String url = dir.substring(th_).replaceAll("\\\\", "/");
            return String.format("http://%s/logs/%s", host, url);
        }
        return "#";
    }

    protected Connection getConnection() throws NamingException, SQLException {
        Context ctx = new InitialContext();
        ctx = (Context) ctx.lookup("java:comp/env");
        ds = (DataSource) ctx.lookup("jdbc/thr");
        LOG.trace("datasource {}", ds);

        if (ds == null) {
            throw new SQLException("Can't get data source");
        }

        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("Can't get database connection");
        }
        LOG.trace("conn {}", conn.getMetaData().getCatalogs());
        return conn;
    }

    private void loadParameters() {
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                .getRequest();
        this.params = new Properties();

        LOG.trace("{}", req.getRequestURL());
        req.getParameterMap().keySet().stream().map((p) -> {
            LOG.trace("{}={}", p, req.getParameter(p));
            return p;
        }).forEach((p) -> {
            this.params.setProperty(p, req.getParameter(p));
        });
    }
}
