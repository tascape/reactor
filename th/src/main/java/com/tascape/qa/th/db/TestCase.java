package com.tascape.qa.th.db;

import com.tascape.qa.th.db.DbHandler.Test_Case;
import com.tascape.qa.th.test.Priority;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class TestCase {
    private static final Logger LOG = LoggerFactory.getLogger(TestCase.class);

    private int id = 0;

    private String suiteClass = "";

    private String testClass = "";

    private String testMethod = "";

    private String testDataInfo = "";

    private String testData = "";

    private String testIssues = "";

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
        this.id = (int) row.get(Test_Case.TEST_CASE_ID.name());
        this.suiteClass = row.get(Test_Case.SUITE_CLASS.name()).toString();
        this.testClass = row.get(Test_Case.TEST_CLASS.name()).toString();
        this.testMethod = row.get(Test_Case.TEST_METHOD.name()).toString();
        this.testDataInfo = row.get(Test_Case.TEST_DATA_INFO.name()).toString();
        this.testData = row.get(Test_Case.TEST_DATA.name()).toString();
    }

    public TestCase() {
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

    public String format() {
        return String.format("%s.%s.%s.%s.%s", this.suiteClass, this.testClass, this.testMethod, testDataInfo, testData);
    }

    public String formatForLogPath() {
        return String.format("%s.%s.%s.%s",
                StringUtils.substringAfterLast(this.suiteClass, "."),
                StringUtils.substringAfterLast(this.testClass, "."), this.testMethod,
                this.testDataInfo.isEmpty() ? "" : StringUtils.substringAfterLast(this.testDataInfo, "#"));
    }

    public String getTestIssues() {
        return testIssues;
    }

    public void setTestIssues(String testIssues) {
        this.testIssues = testIssues;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean equals(TestCase testCase) {
        if (this.id == 0 || testCase.getId() == 0) {
            return this.suiteClass.equals(testCase.getSuiteClass())
                    && this.testClass.equals(testCase.getTestClass())
                    && this.testMethod.equals(testCase.getTestMethod())
                    && this.testDataInfo.equals(testCase.getTestDataInfo());
        }
        return this.id == testCase.getId();
    }
}
