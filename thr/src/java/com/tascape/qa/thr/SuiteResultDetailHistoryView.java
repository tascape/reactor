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
public class SuiteResultDetailHistoryView implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(SuiteResultDetailHistoryView.class);

    private static final long serialVersionUID = 1L;

    private long startTime = System.currentTimeMillis() - 5184000000L; // two months

    private long stopTime = System.currentTimeMillis() + 86400000L; // one day

    private int numberOfEntries = 100;

    private boolean invisibleIncluded = false;

    private String suiteName = "";

    @Inject
    private MySqlBaseBean db;

    private List<Map<String, Object>> results;

    @PostConstruct
    public void init() {
        this.getParameters();

        try {
            this.results = this.db.getSuitesResult(this.startTime, this.stopTime, this.numberOfEntries,
                this.suiteName, this.invisibleIncluded);
        } catch (NamingException | SQLException ex) {
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

    public List<Map<String, Object>> getResults() {
        return results;
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

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    private void getParameters() {
        Map<String, String> map = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        LOG.trace("request parameters {}", map);
        String v = map.get("start");
        if (v != null) {
            this.startTime = Long.parseLong(v);
        }
        v = map.get("stop");
        if (v != null) {
            this.stopTime = Long.parseLong(v);
        }
        v = map.get("number");
        if (v != null) {
            this.numberOfEntries = Integer.parseInt(v);
        }
        v = map.get("invisible");
        if (v != null) {
            this.invisibleIncluded = Boolean.parseBoolean(v);
        }
        v = map.get("suite");
        if (v != null) {
            this.suiteName = v;
        }
    }
}
