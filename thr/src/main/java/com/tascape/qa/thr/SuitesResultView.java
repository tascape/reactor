package com.tascape.qa.thr;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
@Named
@RequestScoped
public class SuitesResultView implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(SuitesResultView.class);

    private static final long serialVersionUID = 1L;

    private long startTime = System.currentTimeMillis() - 5184000000L; // two months

    private long stopTime = System.currentTimeMillis() + 86400000L; // one day

    private int numberOfEntries = 100;

    private boolean invisibleIncluded = false;

    private String suiteName = "";

    private String jobName = "";

    @Inject
    private MySqlBaseBean db;

    private List<Map<String, Object>> results;

    private List<Map<String, Object>> resultsSelected;

    @PostConstruct
    public void init() {
        this.getParameters();

        try {
            this.results = this.db.getSuitesResult(this.startTime, this.stopTime, this.numberOfEntries,
                    this.suiteName, this.jobName, this.invisibleIncluded);
        }
        catch (NamingException | SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    public boolean isInvisibleIncluded() {
        return invisibleIncluded;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }

    public List<Map<String, Object>> getResultsSelected() {
        return resultsSelected;
    }

    public void setResultsSelected(List<Map<String, Object>> resultsSelected) {
        this.resultsSelected = resultsSelected;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public void setNumberOfEntries(int numberOfEntries) {
        this.numberOfEntries = numberOfEntries;
    }

    public void setInvisibleIncluded(boolean invisibleIncluded) {
        this.invisibleIncluded = invisibleIncluded;
    }

    private void getParameters() {
        Map<String, String> map = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String v = map.get("start");
        if (v != null) {
            this.startTime = Long.parseLong(v);
            LOG.debug("start={}", this.startTime);
        }
        v = map.get("stop");
        if (v != null) {
            this.stopTime = Long.parseLong(v);
            LOG.debug("stop={}", this.stopTime);
        }
        v = map.get("number");
        if (v != null) {
            this.numberOfEntries = Integer.parseInt(v);
            LOG.debug("number={}", this.numberOfEntries);
        }
        v = map.get("invisible");
        if (v != null) {
            this.invisibleIncluded = Boolean.parseBoolean(v);
            LOG.debug("invisible={}", this.invisibleIncluded);
        }
        v = map.get("suite");
        if (v != null) {
            this.suiteName = v;
            LOG.warn("suite={}", this.suiteName);
        }
        v = map.get("job");
        if (v != null) {
            this.jobName = v;
            LOG.warn("job={}", this.jobName);
        }
    }
}
