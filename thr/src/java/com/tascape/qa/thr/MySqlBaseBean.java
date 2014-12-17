package com.tascape.qa.thr;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
 * @author linsong wang
 */
public class MySqlBaseBean implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(MySqlBaseBean.class);

    @Resource(name = "jdbc/thr")
    private DataSource ds;

    protected Properties params;

    MySqlBaseBean() {
        this.loadParameters();
    }

    Connection getConnection() throws NamingException, SQLException {
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

    private void loadParameters() {
        HttpServletRequest req
                = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        this.params = new Properties();

        LOG.trace("{}", req.getRequestURL());
        for (String p : req.getParameterMap().keySet()) {
            LOG.trace("{}={}", p, req.getParameter(p));
            this.params.setProperty(p, req.getParameter(p));
        }
    }
}
