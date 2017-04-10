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

import com.tascape.reactor.task.Priority;
import java.util.List;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class TaskCase {
    private static final Logger LOG = LoggerFactory.getLogger(TaskCase.class);

    public static final String TABLE_NAME = "task_case";

    public static final String TASK_CASE_ID = "TASK_CASE_ID";

    public static final String SUITE_CLASS = "SUITE_CLASS";

    public static final String CASE_CLASS = "CASE_CLASS";

    public static final String CASE_METHOD = "CASE_METHOD";

    public static final String CASE_DATA_INFO = "CASE_DATA_INFO";

    public static final String CASE_DATA = "CASE_DATA";

    public static final String CASE_ISSUES = "CASE_ISSUES";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "TASK_CASE_ID")
    private Integer taskCaseId;

    @Basic(optional = false)
    @Column(name = "SUITE_CLASS")
    private String suiteClass;

    @Basic(optional = false)
    @Column(name = "CASE_CLASS")
    private String caseClass;

    @Basic(optional = false)
    @Column(name = "CASE_METHOD")
    private String caseMethod;

    @Column(name = "CASE_DATA_INFO")
    private String caseDataInfo = "";

    @Column(name = "CASE_DATA")
    private String caseData = "";

    @Column(name = "CASE_ISSUES")
    private String caseIssues = "";

    @OneToMany(mappedBy = "taskCaseId")
    private List<com.tascape.reactor.db.CaseResult> caseResultList;

    private int priority = Priority.P0;

    public TaskCase(TaskCase tc) {
        this.suiteClass = tc.getSuiteClass() + "";
        this.caseClass = tc.getCaseClass() + "";
        this.caseMethod = tc.getCaseMethod() + "";
        this.caseDataInfo = tc.getCaseDataInfo() + "";
        this.caseData = tc.getCaseData() + "";
        this.caseIssues = tc.getCaseIssues() + "";
        this.priority = tc.getPriority();
    }

    public TaskCase(Map<String, Object> row) {
        this.taskCaseId = (int) row.get(TaskCase.TASK_CASE_ID);
        this.suiteClass = row.get(TaskCase.SUITE_CLASS) + "";
        this.caseClass = row.get(TaskCase.CASE_CLASS) + "";
        this.caseMethod = row.get(TaskCase.CASE_METHOD) + "";
        this.caseDataInfo = row.get(TaskCase.CASE_DATA_INFO) + "";
        this.caseData = row.get(TaskCase.CASE_DATA) + "";
    }

    public TaskCase() {
    }

    public Integer getTaskCaseId() {
        return taskCaseId;
    }

    public void setTaskCaseId(Integer taskCaseId) {
        this.taskCaseId = taskCaseId;
    }

    public String getSuiteClass() {
        return suiteClass;
    }

    public void setSuiteClass(String suiteClass) {
        this.suiteClass = suiteClass;
    }

    public String getCaseClass() {
        return caseClass;
    }

    public void setCaseClass(String caseClass) {
        this.caseClass = caseClass;
    }

    public String getCaseMethod() {
        return caseMethod;
    }

    public void setCaseMethod(String caseMethod) {
        this.caseMethod = caseMethod;
    }

    public String getCaseDataInfo() {
        return caseDataInfo;
    }

    public void setCaseDataInfo(String caseDataInfo) {
        this.caseDataInfo = caseDataInfo;
    }

    public String getCaseData() {
        return caseData;
    }

    public void setCaseData(String caseData) {
        this.caseData = caseData;
    }

    public String getCaseIssues() {
        return caseIssues;
    }

    public void setCaseIssues(String caseIssues) {
        this.caseIssues = caseIssues;
    }

    @XmlTransient
    public List<CaseResult> getCaseResultList() {
        return caseResultList;
    }

    public void setCaseResultList(List<CaseResult> caseResultList) {
        this.caseResultList = caseResultList;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (taskCaseId != null ? taskCaseId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TaskCase)) {
            return false;
        }
        TaskCase other = (TaskCase) object;
        return !((this.taskCaseId == null && other.taskCaseId != null)
            || (this.taskCaseId != null && !this.taskCaseId.equals(other.taskCaseId)));
    }

    @Override
    public String toString() {
        return taskCaseId + " " + format();
    }

    public String format() {
        return String.format("%s.%s.%s.%s.%s", this.suiteClass, this.caseClass, this.caseMethod, caseDataInfo, caseData);
    }

    public String formatForLogPath() {
        return String.format("%s.%s.%s.%s",
            StringUtils.substringAfterLast(this.suiteClass, "."),
            StringUtils.substringAfterLast(this.caseClass, "."), this.caseMethod,
            this.caseDataInfo.isEmpty() ? "" : StringUtils.substringAfterLast(this.caseDataInfo, "#"));
    }
}
