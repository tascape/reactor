/*
 * Copyright (c) 2015 - present Nebula Bay.
 * All rights reserved.
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

import com.tascape.reactor.ExecutionResult;
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
public class CaseResult {
    private static final Logger LOG = LoggerFactory.getLogger(CaseResult.class);

    public static final String TABLE_NAME = "case_result";

    public static final String CASE_RESULT_ID = "CASE_RESULT_ID";

    public static final String SUITE_RESULT = "SUITE_RESULT";

    public static final String TASK_CASE_ID = "TASK_CASE_ID";

    public static final String EXECUTION_RESULT = "EXECUTION_RESULT";

    public static final String AUT = "AUT";

    public static final String START_TIME = "START_TIME";

    public static final String STOP_TIME = "STOP_TIME";

    public static final String RETRY = "RETRY";

    public static final String CASE_STATION = "CASE_STATION";

    public static final String LOG_DIR = "LOG_DIR";

    public static final String EXTERNAL_ID = "EXTERNAL_ID";

    public static final String CASE_ENV = "CASE_ENV";

    @Id
    @Basic(optional = false)
    @Column(name = "CASE_RESULT_ID")
    private String caseResultId;

    @Column(name = "EXECUTION_RESULT")
    private String executionResult = ExecutionResult.NA.getName();

    private String aut;

    @Column(name = "START_TIME")
    private Long startTime;

    @Column(name = "STOP_TIME")
    private Long stopTime;

    private Integer retry = 2;

    @Column(name = "CASE_STATION")
    private String caseStation;

    @Column(name = "LOG_DIR")
    private String logDir;

    @Column(name = "EXTERNAL_ID")
    private String externalId;

    @Column(name = "CASE_ENV")
    private String caseEnv;

    @JoinColumn(name = "SUITE_RESULT", referencedColumnName = "SUITE_RESULT_ID")
    @ManyToOne
    private SuiteResult suiteResult;

    @JoinColumn(name = "TASK_CASE_ID", referencedColumnName = "TASK_CASE_ID")
    @ManyToOne
    private TaskCase taskCaseId;

    @OneToMany(mappedBy = "caseResultId")
    private List<com.tascape.reactor.db.CaseResultMetric> caseResultMetricList;

    private ExecutionResult result = ExecutionResult.NA;

    private TaskCase taskCase = null;

    private String suiteResultId = "";

    private Throwable exception = null;

    private String stacktrace = "";

    CaseResult() {
    }

    public CaseResult(TaskCase tc) {
        this.taskCase = tc;
    }

    public String getCaseResultId() {
        return caseResultId;
    }

    public void setCaseResultId(String caseResultId) {
        this.caseResultId = caseResultId;
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

    public String getCaseStation() {
        return caseStation;
    }

    public void setCaseStation(String caseStation) {
        this.caseStation = caseStation;
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

    public String getCaseEnv() {
        return caseEnv;
    }

    public void setCaseEnv(String caseEnv) {
        this.caseEnv = caseEnv;
    }

    public SuiteResult getSuiteResult() {
        return suiteResult;
    }

    public void setSuiteResult(SuiteResult suiteResult) {
        this.suiteResult = suiteResult;
    }

    public TaskCase getTaskCaseId() {
        return taskCaseId;
    }

    public void setTaskCaseId(TaskCase taskCaseId) {
        this.taskCaseId = taskCaseId;
    }

    @XmlTransient
    public List<CaseResultMetric> getCaseResultMetricList() {
        return caseResultMetricList;
    }

    public void setCaseResultMetricList(List<CaseResultMetric> caseResultMetricList) {
        this.caseResultMetricList = caseResultMetricList;
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

    public TaskCase getTaskCase() {
        return taskCase;
    }

    public void setTaskCase(TaskCase taskCase) {
        this.taskCase = taskCase;
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
        hash += (caseResultId != null ? caseResultId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CaseResult)) {
            return false;
        }
        CaseResult other = (CaseResult) object;
        return !((this.caseResultId == null && other.caseResultId != null)
            || (this.caseResultId != null && !this.caseResultId.equals(other.caseResultId)));
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + caseResultId + "]";
    }

    public ExecutionResult getResult() {
        return result;
    }

    public void setResult(ExecutionResult result) {
        this.result = result;
    }
}
