package com.tascape.qa.thr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
@ManagedBean(name = "testsBean", eager = false)
@RequestScoped
public class TestsBean extends ResultBase {
    private static final Logger LOG = LoggerFactory.getLogger(TestsBean.class);

    private int number = 100;

    @ManagedProperty(value = "#{param.testclass}")
    private String testClass = "";

    @ManagedProperty(value = "#{param.testmethod}")
    private String testMethod = "";

    private Date startTime = new Date(System.currentTimeMillis() - 604800000); // a week

    private Date stopTime = new Date();

    private long suiteId = 0;

    private List<Map<String, Object>> tests;

    public void loadTestResults() throws SQLException, NamingException {

        try (Connection conn = this.getConnection()) {
            StringBuilder sb = new StringBuilder("SELECT * FROM Test_Result WHERE ")
                    .append("START_TIME > ").append(startTime.getTime()).append(" AND ")
                    .append("STOP_TIME < ").append(stopTime.getTime());
            sb.append(" AND TEST_CLASS = '").append(testClass).append("'");
            sb.append(" AND TEST_METHOD = '").append(testMethod).append("'");
            if (suiteId != 0) {
                sb.append(" AND SUITE_ID = ").append(suiteId);
            }
            sb.append(" ORDER BY TEST_CASE_ID DESC LIMIT ").append(number).append(";");
            String sql = sb.toString();
            LOG.trace(sql);
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            this.setTests(this.dumpResultSetToList(rs));
        }
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
        LOG.debug("start time {} = {}", this.startTime.toString(), this.startTime.getTime());
    }

    public Date getStopTime() {
        return stopTime;
    }

    public void setStopTime(Date stopTime) {
        this.stopTime = stopTime;
        LOG.debug("stop time {} = {}", this.stopTime.toString(), this.stopTime.getTime());
    }

    public long getSuiteId() {
        return suiteId;
    }

    public void setSuiteId(long suiteId) {
        this.suiteId = suiteId;
    }

    public List<Map<String, Object>> getTests() {
        return tests;
    }

    public void setTests(List<Map<String, Object>> tests) {
        this.tests = tests;
    }

    public String getTestClass() {
        return testClass;
    }

    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
        if (ResultBase.TEST_METHOD_NA.equals(this.testMethod)) {
            this.testMethod = "";
        }
    }
}
