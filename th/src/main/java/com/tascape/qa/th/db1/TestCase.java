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

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
@Table(name = "test_case")
@XmlRootElement
public class TestCase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "TEST_CASE_ID")
    private Integer testCaseId;

    @Basic(optional = false)
    @Column(name = "SUITE_CLASS")
    private String suiteClass;

    @Basic(optional = false)
    @Column(name = "TEST_CLASS")
    private String testClass;

    @Basic(optional = false)
    @Column(name = "TEST_METHOD")
    private String testMethod;

    @Column(name = "TEST_DATA_INFO")
    private String testDataInfo;

    @Column(name = "TEST_DATA")
    private String testData;

    @Column(name = "TEST_ISSUES")
    private String testIssues;

    @OneToMany(mappedBy = "testCaseId")
    private List<TestResult> testResultList;

    public TestCase() {
    }

    public TestCase(Integer testCaseId) {
        this.testCaseId = testCaseId;
    }

    public TestCase(Integer testCaseId, String suiteClass, String testClass, String testMethod) {
        this.testCaseId = testCaseId;
        this.suiteClass = suiteClass;
        this.testClass = testClass;
        this.testMethod = testMethod;
    }

    public Integer getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(Integer testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getSuiteClass() {
        return suiteClass;
    }

    public void setSuiteClass(String suiteClass) {
        this.suiteClass = suiteClass;
    }

    public String getTestClass() {
        return testClass;
    }

    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
    }

    public String getTestDataInfo() {
        return testDataInfo;
    }

    public void setTestDataInfo(String testDataInfo) {
        this.testDataInfo = testDataInfo;
    }

    public String getTestData() {
        return testData;
    }

    public void setTestData(String testData) {
        this.testData = testData;
    }

    public String getTestIssues() {
        return testIssues;
    }

    public void setTestIssues(String testIssues) {
        this.testIssues = testIssues;
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
        hash += (testCaseId != null ? testCaseId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TestCase)) {
            return false;
        }
        TestCase other = (TestCase) object;
        return !((this.testCaseId == null && other.testCaseId != null)
            || (this.testCaseId != null && !this.testCaseId.equals(other.testCaseId)));
    }

    @Override
    public String toString() {
        return "com.tascape.qa.th.db1.TestCase[ testCaseId=" + testCaseId + " ]";
    }
}
