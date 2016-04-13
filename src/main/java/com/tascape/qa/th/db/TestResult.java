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
package com.tascape.qa.th.db;

import com.tascape.qa.th.ExecutionResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlTransient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class TestResult {
    private static final Logger LOG = LoggerFactory.getLogger(TestResult.class);

    public static final String TABLE_NAME = "test_result";

    public static final String TEST_RESULT_ID = "TEST_RESULT_ID";

    public static final String SUITE_RESULT = "SUITE_RESULT";

    public static final String TEST_CASE_ID = "TEST_CASE_ID";

    public static final String EXECUTION_RESULT = "EXECUTION_RESULT";

    public static final String AUT = "AUT";

    public static final String START_TIME = "START_TIME";

    public static final String STOP_TIME = "STOP_TIME";

    public static final String RETRY = "RETRY";

    public static final String TEST_STATION = "TEST_STATION";

    public static final String LOG_DIR = "LOG_DIR";

    public static final String EXTERNAL_ID = "EXTERNAL_ID";

    public static final String TEST_ENV = "TEST_ENV";

    @Id
    @Basic(optional = false)
    @Column(name = "TEST_RESULT_ID")
    private String testResultId;

    @Column(name = "EXECUTION_RESULT")
    private String executionResult = ExecutionResult.NA.getName();

    private String aut;

    @Column(name = "START_TIME")
    private Long startTime;

    @Column(name = "STOP_TIME")
    private Long stopTime;

    private Integer retry = 2;

    @Column(name = "TEST_STATION")
    private String testStation;

    @Column(name = "LOG_DIR")
    private String logDir;

    @Column(name = "EXTERNAL_ID")
    private String externalId;

    @Column(name = "TEST_ENV")
    private String testEnv;

    @JoinColumn(name = "SUITE_RESULT", referencedColumnName = "SUITE_RESULT_ID")
    @ManyToOne
    private SuiteResult suiteResult;

    @JoinColumn(name = "TEST_CASE_ID", referencedColumnName = "TEST_CASE_ID")
    @ManyToOne
    private TestCase testCaseId;

    @OneToMany(mappedBy = "testResultId")
    private List<com.tascape.qa.th.db.TestResultMetric> testResultMetricList;

    private ExecutionResult result = ExecutionResult.NA;

    private TestCase testCase = null;

    private String suiteResultId = "";

    private Throwable exception = null;

    private String stacktrace = "";

    TestResult() {
    }

    public TestResult(TestCase tc) {
        this.testCase = tc;
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

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getStopTime() {
        return stopTime;
    }

    public void setStopTime(Long stopTime) {
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

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTestEnv() {
        return testEnv;
    }

    public void setTestEnv(String testEnv) {
        this.testEnv = testEnv;
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

    public String getSuiteResultId() {
        return suiteResultId;
    }

    public void setSuiteResultId(String suiteResultId) {
        this.suiteResultId = suiteResultId;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
        if (this.exception != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            this.exception.printStackTrace(pw);
            this.stacktrace = sw.toString();
        }
    }

    public String getStacktrace() {
        return stacktrace;
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
        return "com.tascape.qa.th.db.TestResult[ testResultId=" + testResultId + " ]";
    }

    public ExecutionResult getResult() {
        return result;
    }

    public void setResult(ExecutionResult result) {
        this.result = result;
    }
}
