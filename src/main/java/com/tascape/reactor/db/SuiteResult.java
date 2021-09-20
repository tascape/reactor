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

import com.tascape.reactor.TaskSuite;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import static com.tascape.reactor.db.DbHandler.SYS_CONFIG;

/**
 *
 * @author linsong wang
 */
@MappedSuperclass
@Table(name = "suite_result")
@XmlRootElement
public class SuiteResult implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TABLE_NAME = "suite_result";

    public static final String SUITE_RESULT_ID = "SUITE_RESULT_ID";

    public static final String SUITE_NAME = "SUITE_NAME";

    public static final String PROJECT_NAME = "PROJECT_NAME";

    public static final String JOB_NAME = "JOB_NAME";

    public static final String JOB_BUILD_NUMBER = "JOB_BUILD_NUMBER";

    public static final String JOB_BUILD_URL = "JOB_BUILD_URL";

    public static final String EXECUTION_RESULT = "EXECUTION_RESULT";

    public static final String START_TIME = "START_TIME";

    public static final String STOP_TIME = "STOP_TIME";

    public static final String NUMBER_OF_CASES = "NUMBER_OF_CASES";

    public static final String NUMBER_OF_FAILURE = "NUMBER_OF_FAILURE";

    public static final String INVISIBLE_ENTRY = "INVISIBLE_ENTRY";

    public static final String PRODUCT_UNDER_TASK = "PRODUCT_UNDER_TASK";

    @Id
    @Basic(optional = false)
    @Column(name = "SUITE_RESULT_ID")
    private String suiteResultId;

    @Column(name = "SUITE_NAME")
    private String suiteName;

    @Column(name = "PROJECT_NAME")
    private String projectName;

    @Column(name = "JOB_NAME")
    private String jobName;

    @Column(name = "JOB_BUILD_NUMBER")
    private Integer jobBuildNumber;

    @Column(name = "JOB_BUILD_URL")
    private String jobBuildUrl;

    @Column(name = "EXECUTION_RESULT")
    private String executionResult;

    @Column(name = "START_TIME")
    private Long startTime;

    @Column(name = "STOP_TIME")
    private Long stopTime;

    @Column(name = "NUMBER_OF_CASES")
    private Integer numberOfCases;

    @Column(name = "NUMBER_OF_FAILURE")
    private Integer numberOfFailure;

    @Column(name = "INVISIBLE_ENTRY")
    private Boolean invisibleEntry;

    @Column(name = "PRODUCT_UNDER_TASK")
    private String productUnderTask;

    @OneToMany(mappedBy = "suiteResult")
    private List<CaseResult> caseResultList;

    @OneToMany(mappedBy = "suiteResult")
    private List<SuiteProperty> suitePropertyList;

    public SuiteResult() {
    }

    public SuiteResult(TaskSuite taskSuite, String execId) throws InterruptedException {
        Long time = System.currentTimeMillis();
        this.suiteResultId = execId;
        this.suiteName = taskSuite.getName();
        this.projectName = taskSuite.getProjectName();
        this.jobName = SYS_CONFIG.getJobName();
        this.jobBuildNumber = SYS_CONFIG.getJobBuildNumber();
        this.jobBuildUrl = SYS_CONFIG.getJobBuildUrl();
        this.executionResult = "";
        this.startTime = time;
        this.stopTime = time + 1;
        this.numberOfCases = taskSuite.getCases().size();
        this.numberOfFailure = taskSuite.getCases().size();
        this.invisibleEntry = false;
        this.productUnderTask = SYS_CONFIG.getProdUnderTask();
        this.invisibleEntry = !SYS_CONFIG.getResultVisibility();
    }

    public SuiteResult(ResultSet rs) throws SQLException {
        this.suiteResultId = rs.getString(SUITE_RESULT_ID);
        this.suiteName = rs.getString(SUITE_NAME);
        this.projectName = rs.getNString(PROJECT_NAME);
        this.jobName = rs.getString(JOB_NAME);
        this.jobBuildNumber = rs.getInt(JOB_BUILD_NUMBER);
        this.jobBuildUrl = rs.getString(JOB_BUILD_URL);
        this.executionResult = rs.getString(EXECUTION_RESULT);
        this.startTime = rs.getLong(START_TIME);
        this.stopTime = rs.getLong(STOP_TIME);
        this.numberOfCases = rs.getInt(NUMBER_OF_CASES);
        this.numberOfFailure = rs.getInt(NUMBER_OF_FAILURE);
        this.invisibleEntry = rs.getBoolean(INVISIBLE_ENTRY);
        this.productUnderTask = rs.getString(PRODUCT_UNDER_TASK);
        this.invisibleEntry = !SYS_CONFIG.getResultVisibility();
    }

    public void update(ResultSet rs) throws SQLException {
        rs.updateString(SUITE_RESULT_ID, this.getSuiteResultId());
        rs.updateString(SUITE_NAME, this.getSuiteName());
        rs.updateString(PROJECT_NAME, this.getProjectName());
        rs.updateString(JOB_NAME, this.getJobName());
        rs.updateInt(JOB_BUILD_NUMBER, this.getJobBuildNumber());
        rs.updateString(JOB_BUILD_URL, this.getJobBuildUrl());
        rs.updateLong(START_TIME, this.getStartTime());
        rs.updateLong(STOP_TIME, this.getStartTime());
        rs.updateString(EXECUTION_RESULT, this.getExecutionResult());
        rs.updateInt(NUMBER_OF_CASES, this.getNumberOfCases());
        rs.updateInt(NUMBER_OF_FAILURE, this.getNumberOfFailure());
        rs.updateBoolean(INVISIBLE_ENTRY, this.getInvisibleEntry());
        rs.updateString(PRODUCT_UNDER_TASK, this.getProductUnderTask());
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

    public Integer getNumberOfCases() {
        return numberOfCases;
    }

    public void setNumberOfCases(Integer numberOfCases) {
        this.numberOfCases = numberOfCases;
    }

    public Integer getNumberOfFailure() {
        return numberOfFailure;
    }

    public void setNumberOfFailure(Integer numberOfFailure) {
        this.numberOfFailure = numberOfFailure;
    }

    public Boolean getInvisibleEntry() {
        return invisibleEntry;
    }

    public void setInvisibleEntry(Boolean invisibleEntry) {
        this.invisibleEntry = invisibleEntry;
    }

    public String getProductUnderTask() {
        return productUnderTask;
    }

    public void setProductUnderTask(String productUnderTask) {
        this.productUnderTask = productUnderTask;
    }

    @XmlTransient
    public List<CaseResult> getCaseResultList() {
        return caseResultList;
    }

    public void setCaseResultList(List<CaseResult> caseResultList) {
        this.caseResultList = caseResultList;
    }

    public List<SuiteProperty> getSuitePropertyList() {
        return suitePropertyList;
    }

    public void setSuitePropertyList(List<SuiteProperty> suitePropertyList) {
        this.suitePropertyList = suitePropertyList;
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
        return this.getClass().getName() + "[ suiteResultId=" + suiteResultId + " ]";
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
