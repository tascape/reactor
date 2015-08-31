/*
 * Copyright 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tascape.qa.th.db;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author linsong wang
 */
@MappedSuperclass
@Table(name = "test_result_metric")
@XmlRootElement
public class TestResultMetric implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TABLE_NAME = "test_result_metric";

    public static final String TEST_RESULT_METRIC_ID = "TEST_RESULT_METRIC_ID";

    public static final String TEST_RESULT_ID = "TEST_RESULT_ID";

    public static final String METRIC_GROUP = "METRIC_GROUP";

    public static final String METRIC_NAME = "METRIC_NAME";

    public static final String METRIC_VALUE = "METRIC_VALUE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "TEST_RESULT_METRIC_ID")
    private Integer testResultMetricId;

    @Column(name = "METRIC_GROUP")
    private String metricGroup;

    @Column(name = "METRIC_NAME")
    private String metricName;

    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "METRIC_VALUE")
    private Double metricValue;

    @JoinColumn(name = "TEST_RESULT_ID", referencedColumnName = "TEST_RESULT_ID")
    @ManyToOne
    private TestResult testResultId;

    public TestResultMetric() {
    }

    public TestResultMetric(Integer testResultMetricId) {
        this.testResultMetricId = testResultMetricId;
    }

    public Integer getTestResultMetricId() {
        return testResultMetricId;
    }

    public void setTestResultMetricId(Integer testResultMetricId) {
        this.testResultMetricId = testResultMetricId;
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

    public TestResult getTestResultId() {
        return testResultId;
    }

    public void setTestResultId(TestResult testResultId) {
        this.testResultId = testResultId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (testResultMetricId != null ? testResultMetricId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TestResultMetric)) {
            return false;
        }
        TestResultMetric other = (TestResultMetric) object;
        return !((this.testResultMetricId == null && other.testResultMetricId != null)
            || (this.testResultMetricId != null && !this.testResultMetricId.equals(other.testResultMetricId)));
    }

    @Override
    public String toString() {
        return "com.tascape.qa.th.db.TestResultMetric[ testResultMetricId=" + testResultMetricId + " ]";
    }
}
