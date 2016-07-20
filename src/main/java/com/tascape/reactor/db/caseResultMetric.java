/*
 * Copyright 2015 - 2016 Nebula Bay.
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
package com.tascape.reactor.db;

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
@Table(name = "case_result_metric")
@XmlRootElement
public class caseResultMetric implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TABLE_NAME = "case_result_metric";

    public static final String CASE_RESULT_METRIC_ID = "CASE_RESULT_METRIC_ID";

    public static final String CASE_RESULT_ID = "CASE_RESULT_ID";

    public static final String METRIC_GROUP = "METRIC_GROUP";

    public static final String METRIC_NAME = "METRIC_NAME";

    public static final String METRIC_VALUE = "METRIC_VALUE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "CASE_RESULT_METRIC_ID")
    private Integer caseResultMetricId;

    @Column(name = "METRIC_GROUP")
    private String metricGroup;

    @Column(name = "METRIC_NAME")
    private String metricName;

    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "METRIC_VALUE")
    private Double metricValue;

    @JoinColumn(name = "CASE_RESULT_ID", referencedColumnName = "CASE_RESULT_ID")
    @ManyToOne
    private CaseResult caseResultId;

    public caseResultMetric() {
    }

    public caseResultMetric(Integer caseResultMetricId) {
        this.caseResultMetricId = caseResultMetricId;
    }

    public Integer getCaseResultMetricId() {
        return caseResultMetricId;
    }

    public void setCaseResultMetricId(Integer caseResultMetricId) {
        this.caseResultMetricId = caseResultMetricId;
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

    public CaseResult getCaseResultId() {
        return caseResultId;
    }

    public void setCaseResultId(CaseResult caseResultId) {
        this.caseResultId = caseResultId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (caseResultMetricId != null ? caseResultMetricId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof caseResultMetric)) {
            return false;
        }
        caseResultMetric other = (caseResultMetric) object;
        return !((this.caseResultMetricId == null && other.caseResultMetricId != null)
            || (this.caseResultMetricId != null && !this.caseResultMetricId.equals(other.caseResultMetricId)));
    }

    @Override
    public String toString() {
        return caseResultMetricId + " " + metricName + "=" + metricValue;
    }
}
