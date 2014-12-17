package com.tascape.qa.thr;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
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

    private int numberOfRecords = 100;

    private boolean invisibleIncluded = false;
    
    private String suiteName = null;

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
            this.numberOfRecords = Integer.parseInt(v);
        }
        v = this.getParameter("invisible");
        if (v != null) {
            this.invisibleIncluded = Boolean.parseBoolean(v);
        }
        this.suiteName = this.getParameter("suite");
        LOG.info("{} -> {}", this.startTime, this.stopTime);
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public boolean isInvisibleIncluded() {
        return invisibleIncluded;
    }

    public String getSuiteName() {
        return suiteName;
    }

    private String getParameter(String name) {
        HttpServletRequest req
                = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return req.getParameter(name);
    }
}
