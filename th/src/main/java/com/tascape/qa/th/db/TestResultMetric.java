package com.tascape.qa.th.db;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class TestResultMetric implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(TestResult.class);

    private static final long serialVersionUID = 1L;

    private String metricGroup;

    private String metricName;

    private Double metricValue;

    public TestResultMetric() {
    }

    public String getMetricGroup() {
        return metricGroup;
    }

    public void setMetricGroup(String metricGroup) {
        this.metricGroup = metricGroup;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Double getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Double metricValue) {
        this.metricValue = metricValue;
    }

    public String tosString() {
        return metricGroup + ":" + metricName + ":" + metricValue;
    }
}
