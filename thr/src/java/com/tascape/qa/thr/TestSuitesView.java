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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
@Named
@RequestScoped
public class TestSuitesView implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(TestSuitesView.class);

    private static final long serialVersionUID = 1L;

    private long startTime = System.currentTimeMillis() - 5184000000L; // two months

    private long stopTime = System.currentTimeMillis() + 86400000L; // one day

    private int numberOfEntries = 100;

    private boolean invisibleIncluded = false;

    private String suiteName = null;

    @Inject
    private MySqlBaseBean mysql;

    private List<Map<String, Object>> results;

    private List<Map<String, Object>> resultsSelected;

    @PostConstruct
    public void init() {
        String v = this.getParameter("start");
        if (v != null) {
            this.startTime = Long.parseLong(v);
        }
        v = this.getParameter("stop");
        if (v != null) {
            this.stopTime = Long.parseLong(v);
        }
        v = this.getParameter("number");
        if (v != null) {
            this.numberOfEntries = Integer.parseInt(v);
        }
        v = this.getParameter("invisible");
        if (v != null) {
            this.invisibleIncluded = Boolean.parseBoolean(v);
        }
        this.suiteName = this.getParameter("suite");
        LOG.info("{} -> {}", this.startTime, this.stopTime);

        try {
            this.results = this.mysql.getTestSuiteResults(this.startTime, this.stopTime, this.numberOfEntries,
                    this.suiteName, this.invisibleIncluded);
            this.results.stream().forEach(row -> {
                row.put("_EXEC_ID", StringUtils.right(row.get("SUITE_RESULT_ID") + "", 10));
            });
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

    public List<Map<String, Object>> getResultsSelected() {
        return resultsSelected;
    }

    public void setResultsSelected(List<Map<String, Object>> resultsSelected) {
        this.resultsSelected = resultsSelected;
    }

    private String getParameter(String name) {
        Map<String, String> map = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        return map.get(name);
    }
}
