package com.tascape.qa.thr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
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
public class SuitesBean extends ResultBase {

    private static final Logger LOG = LoggerFactory.getLogger(SuitesBean.class);

    private int number = 50;

    private boolean invisibleIncluded = false;

    @ManagedProperty(value = "#{param.jobname}")
    private String jobName = "";

    private Date startTime = new Date(System.currentTimeMillis() - 2419200000L); // 4 weeks

    private Date stopTime = new Date(System.currentTimeMillis() + 86400000); // a day

    private long id = 0;

    private String suiteIds;

    private List<Map<String, Object>> suites;

    public void init() throws NamingException, SQLException {
        if (this.number == 0) {
            this.number = 50;
        }
        try (Connection conn = this.getConnection()) {
            StringBuilder sb = new StringBuilder("SELECT * FROM suite_result WHERE ")
                    .append("START_TIME > ").append(startTime.getTime()).append(" AND ")
                    .append("STOP_TIME < ").append(stopTime.getTime());
            if (id != 0) {
                sb.append(" AND SUITE_RESULT_ID = ").append(id);
            }
            if (jobName != null && !jobName.isEmpty()) {
                sb.append(" AND JOB_NAME = '").append(jobName).append("'");
            }
            if (!this.invisibleIncluded) {
                sb.append(" AND INVISIBLE_ENTRY=0");
            }
            sb.append(" ORDER BY STOP_TIME DESC LIMIT ").append(number).append(";");
            String sql = sb.toString();
            LOG.trace(sql);
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            this.suites = this.dumpResultSetToList(rs);
        }

        for (Map<String, Object> suite : this.suites) {
            suite.put("_result_id", StringUtils.right(suite.get("SUITE_RESULT_ID").toString(), 8));
            long start = Long.parseLong(suite.get("START_TIME") + "");
            long stop = Long.parseLong(suite.get("STOP_TIME") + "");
            suite.put("_exec_time", new Period(stop - start).toString().substring(2));
        }

        List<String> ids = new ArrayList<>(this.suites.size());
        for (Map<String, Object> suite : this.suites) {
            ids.add(suite.get("SUITE_RESULT_ID") + "");
        }
        this.suiteIds = StringUtils.join(ids, ",");
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public List<Map<String, Object>> getSuites() {
        return suites;
    }

    public void setSuites(List<Map<String, Object>> suites) {
        this.suites = suites;
    }

    public String getSuiteIds() {
        return suiteIds;
    }

    public void setSuiteIds(String suiteIds) {
        this.suiteIds = suiteIds;
    }

    public boolean isInvisibleIncluded() {
        return invisibleIncluded;
    }

    public void setInvisibleIncluded(boolean invisibleIncluded) {
        this.invisibleIncluded = invisibleIncluded;
    }
}
