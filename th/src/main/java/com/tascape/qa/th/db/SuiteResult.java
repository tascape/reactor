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

import com.tascape.qa.th.db1.TestResult;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Id;
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
@Table(name = "suite_result")
@XmlRootElement
public class SuiteResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "SUITE_RESULT_ID")
    private String suiteResultId;

    @Column(name = "SUITE_NAME")
    private String suiteName;

    @Column(name = "JOB_NAME")
    private String jobName;

    @Column(name = "JOB_BUILD_NUMBER")
    private Integer jobBuildNumber;

    @Column(name = "JOB_BUILD_URL")
    private String jobBuildUrl;

    @Column(name = "EXECUTION_RESULT")
    private String executionResult;

    @Column(name = "START_TIME")
    private BigInteger startTime;

    @Column(name = "STOP_TIME")
    private BigInteger stopTime;

    @Column(name = "NUMBER_OF_TESTS")
    private Integer numberOfTests;

    @Column(name = "NUMBER_OF_FAILURE")
    private Integer numberOfFailure;

    @Column(name = "INVISIBLE_ENTRY")
    private Short invisibleEntry;

    @Column(name = "PRODUCT_UNDER_TEST")
    private String productUnderTest;

    @OneToMany(mappedBy = "suiteResult")
    private List<TestResult> testResultList;

    public SuiteResult() {
    }

    public SuiteResult(String suiteResultId) {
        this.suiteResultId = suiteResultId;
    }

    public String getSuiteResultId() {
        return suiteResultId;
    }

    public void setSuiteResultId(String suiteResultId) {
        this.suiteResultId = suiteResultId;
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

    public Integer getJobBuildNumber() {
        return jobBuildNumber;
    }

    public void setJobBuildNumber(Integer jobBuildNumber) {
        this.jobBuildNumber = jobBuildNumber;
    }

    public String getJobBuildUrl() {
        return jobBuildUrl;
    }

    public void setJobBuildUrl(String jobBuildUrl) {
        this.jobBuildUrl = jobBuildUrl;
    }

    public String getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(String executionResult) {
        this.executionResult = executionResult;
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

    public Integer getNumberOfTests() {
        return numberOfTests;
    }

    public void setNumberOfTests(Integer numberOfTests) {
        this.numberOfTests = numberOfTests;
    }

    public Integer getNumberOfFailure() {
        return numberOfFailure;
    }

    public void setNumberOfFailure(Integer numberOfFailure) {
        this.numberOfFailure = numberOfFailure;
    }

    public Short getInvisibleEntry() {
        return invisibleEntry;
    }

    public void setInvisibleEntry(Short invisibleEntry) {
        this.invisibleEntry = invisibleEntry;
    }

    public String getProductUnderTest() {
        return productUnderTest;
    }

    public void setProductUnderTest(String productUnderTest) {
        this.productUnderTest = productUnderTest;
    }

    @XmlTransient
    public List<TestResult> getTestResultList() {
        return testResultList;
    }

    public void setTestResultList(List<TestResult> testResultList) {
        this.testResultList = testResultList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (suiteResultId != null ? suiteResultId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SuiteResult)) {
            return false;
        }
        SuiteResult other = (SuiteResult) object;
        return !((this.suiteResultId == null && other.suiteResultId != null)
            || (this.suiteResultId != null && !this.suiteResultId.equals(other.suiteResultId)));
    }

    @Override
    public String toString() {
        return "com.tascape.qa.th.db1.SuiteResult[ suiteResultId=" + suiteResultId + " ]";
    }
}
