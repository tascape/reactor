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
package com.tascape.qa.th.db1;

import com.tascape.qa.th.db.SuiteResult;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author linsong wang
 */
@MappedSuperclass
@Table(name = "test_result")
@XmlRootElement
public class TestResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "TEST_RESULT_ID")
    private String testResultId;

    @Column(name = "EXECUTION_RESULT")
    private String executionResult;

    private String aut;

    @Column(name = "START_TIME")
    private BigInteger startTime;

    @Column(name = "STOP_TIME")
    private BigInteger stopTime;

    private Integer retry;

    @Column(name = "TEST_STATION")
    private String testStation;

    @Column(name = "LOG_DIR")
    private String logDir;

    @JoinColumn(name = "SUITE_RESULT", referencedColumnName = "SUITE_RESULT_ID")
    @ManyToOne
    private SuiteResult suiteResult;

    @JoinColumn(name = "TEST_CASE_ID", referencedColumnName = "TEST_CASE_ID")
    @ManyToOne
    private TestCase testCaseId;

    @OneToMany(mappedBy = "testResultId")
    private List<TestResultMetric> testResultMetricList;

    public TestResult() {
    }

    public TestResult(String testResultId) {
        this.testResultId = testResultId;
    }

    public String getTestResultId() {
        return testResultId;
    }

    public void setTestResultId(String testResultId) {
        this.testResultId = testResultId;
    }

    public String getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(String executionResult) {
        this.executionResult = executionResult;
    }

    public String getAut() {
        return aut;
    }

    public void setAut(String aut) {
        this.aut = aut;
    }

    public BigInteger getStartTime() {
        return startTime;
    }

    public void setStartTime(BigInteger startTime) {
        this.startTime = startTime;
    }

    public BigInteger getStopTime() {
        return stopTime;
    }

    public void setStopTime(BigInteger stopTime) {
        this.stopTime = stopTime;
    }

    public Integer getRetry() {
        return retry;
    }

    public void setRetry(Integer retry) {
        this.retry = retry;
    }

    public String getTestStation() {
        return testStation;
    }

    public void setTestStation(String testStation) {
        this.testStation = testStation;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public SuiteResult getSuiteResult() {
        return suiteResult;
    }

    public void setSuiteResult(SuiteResult suiteResult) {
        this.suiteResult = suiteResult;
    }

    public TestCase getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(TestCase testCaseId) {
        this.testCaseId = testCaseId;
    }

    @XmlTransient
    public List<TestResultMetric> getTestResultMetricList() {
        return testResultMetricList;
    }

    public void setTestResultMetricList(List<TestResultMetric> testResultMetricList) {
        this.testResultMetricList = testResultMetricList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (testResultId != null ? testResultId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TestResult)) {
            return false;
        }
        TestResult other = (TestResult) object;
        return !((this.testResultId == null && other.testResultId != null)
            || (this.testResultId != null && !this.testResultId.equals(other.testResultId)));
    }

    @Override
    public String toString() {
        return "com.tascape.qa.th.db1.TestResult[ testResultId=" + testResultId + " ]";
    }
}
