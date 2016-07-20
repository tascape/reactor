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

import com.tascape.reactor.test.Priority;
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
public class TestCase {
    private static final Logger LOG = LoggerFactory.getLogger(TestCase.class);

    public static final String TABLE_NAME = "test_case";

    public static final String TEST_CASE_ID = "TEST_CASE_ID";

    public static final String SUITE_CLASS = "SUITE_CLASS";

    public static final String TEST_CLASS = "TEST_CLASS";

    public static final String TEST_METHOD = "TEST_METHOD";

    public static final String TEST_DATA_INFO = "TEST_DATA_INFO";

    public static final String TEST_DATA = "TEST_DATA";

    public static final String TEST_ISSUES = "TEST_ISSUES";

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
    private String testDataInfo = "";

    @Column(name = "TEST_DATA")
    private String testData = "";

    @Column(name = "TEST_ISSUES")
    private String testIssues = "";

    @OneToMany(mappedBy = "testCaseId")
    private List<com.tascape.reactor.db.TestResult> testResultList;

    private int priority = Priority.P3;

    public TestCase(TestCase tc) {
        this.suiteClass = tc.getSuiteClass() + "";
        this.testClass = tc.getTestClass() + "";
        this.testMethod = tc.getTestMethod() + "";
        this.testDataInfo = tc.getTestDataInfo() + "";
        this.testData = tc.getTestData() + "";
        this.testIssues = tc.getTestIssues() + "";
        this.priority = tc.getPriority();
    }

    public TestCase(Map<String, Object> row) {
        this.testCaseId = (int) row.get(TestCase.TEST_CASE_ID);
        this.suiteClass = row.get(TestCase.SUITE_CLASS) + "";
        this.testClass = row.get(TestCase.TEST_CLASS) + "";
        this.testMethod = row.get(TestCase.TEST_METHOD) + "";
        this.testDataInfo = row.get(TestCase.TEST_DATA_INFO) + "";
        this.testData = row.get(TestCase.TEST_DATA) + "";
    }

    public TestCase() {
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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
        return "com.tascape.qa.th.db.TestCase[ testCaseId=" + testCaseId + " ]";
    }

    public String format() {
        return String.format("%s.%s.%s.%s.%s", this.suiteClass, this.testClass, this.testMethod, testDataInfo, testData);
    }

    public String formatForLogPath() {
        return String.format("%s.%s.%s.%s",
            StringUtils.substringAfterLast(this.suiteClass, "."),
            StringUtils.substringAfterLast(this.testClass, "."), this.testMethod,
            this.testDataInfo.isEmpty() ? "" : StringUtils.substringAfterLast(this.testDataInfo, "#"));
    }
}
