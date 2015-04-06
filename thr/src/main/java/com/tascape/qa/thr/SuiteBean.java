package com.tascape.qa.thr;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong
 */
@ManagedBean
@RequestScoped
public class SuiteBean extends ResultBase {

    private static final Logger LOG = LoggerFactory.getLogger(SuiteBean.class);

    @ManagedProperty(value = "#{param.srid}")
    private String srid;

    private Map<String, Object> suite;

    private List<Map<String, Object>> tests;

    public void init() throws SQLException, NamingException, IOException {
        if (this.srid.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().redirect("suites.xhtml");
            return;
        }

        if ("true".equals(this.params.getProperty("setinvisible"))) {
            this.setInvisible();
        }
        if ("true".equals(this.params.getProperty("setvisible"))) {
            this.setVisible();
        }
        {
            try (Connection conn = this.getConnection()) {
                final String sqlSuite = "SELECT * FROM suite_result WHERE SUITE_RESULT_ID = ?;";
                PreparedStatement stmt = conn.prepareStatement(sqlSuite);
                stmt.setString(1, this.srid);
                LOG.trace("{}", stmt);
                ResultSet rs = stmt.executeQuery();
                List<Map<String, Object>> rows = this.dumpResultSetToList(rs);
                if (rows.isEmpty()) {
                    FacesContext.getCurrentInstance().getExternalContext().redirect("SuiteBean-error.html");
                    return;
                }
                this.suite = rows.get(0);
            }
        }
        {
            try (Connection conn = this.getConnection()) {
                String sb = "SELECT * FROM test_result tr INNER JOIN test_case tc "
                        + "WHERE tr.TEST_CASE_ID = tc.TEST_CASE_ID AND SUITE_RESULT = ? "
                        + "ORDER BY SUITE_CLASS, TEST_CLASS, TEST_METHOD, TEST_DATA_INFO;";
                PreparedStatement stmt = conn.prepareStatement(sb);
                stmt.setString(1, this.srid);
                LOG.trace("{}", stmt);
                ResultSet rs = stmt.executeQuery();
                this.tests = this.dumpResultSetToList(rs);
            }
        }

        for (Map<String, Object> test : this.tests) {
            test.put("_suite_class", StringUtils.substringAfterLast(test.get("SUITE_CLASS") + "", "."));
            test.put("_result_id", StringUtils.right(test.get("TEST_RESULT_ID").toString(), 8));
            test.put("_log_url", this.getLogUrl(test.get("TEST_STATION") + "", test.get("LOG_DIR") + ""));

            try {
                long start = Long.parseLong(test.get("START_TIME") + "");
                long stop = Long.parseLong(test.get("STOP_TIME") + "");
                test.put("_exec_time", new Period(stop - start).toString().substring(2));
            }
            catch (NumberFormatException ex) {
                LOG.debug("", ex);
            }
        }
    }

    public void setVisible() throws NamingException, SQLException {
        this.setInvisible(false);
    }

    public void setInvisible() throws NamingException, SQLException {
        this.setInvisible(true);
    }

    private void setInvisible(boolean invisible) throws NamingException, SQLException {
        try (Connection conn = this.getConnection()) {
            String sql = "UPDATE suite_result SET INVISIBLE_ENTRY = ? WHERE SUITE_RESULT_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, invisible);
            stmt.setString(2, this.srid);
            LOG.trace("{}", stmt);
            stmt.executeUpdate();
        }
    }

    public String getSrid() {
        return srid;
    }

    public void setSrid(String srid) {
        this.srid = srid;
    }

    public Map<String, Object> getSuite() {
        return suite;
    }

    public void setSuite(Map<String, Object> suite) {
        this.suite = suite;
    }

    public List<Map<String, Object>> getTests() {
        return tests;
    }

    public void setTests(List<Map<String, Object>> tests) {
        this.tests = tests;
    }
}
