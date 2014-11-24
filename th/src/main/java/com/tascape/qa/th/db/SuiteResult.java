package com.tascape.qa.th.db;

import com.tascape.qa.th.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class SuiteResult {
    private static final Logger LOG = LoggerFactory.getLogger(SuiteResult.class);

    private String id = "";

    private String jobName = "";

    private int jobBuildNumber = 0;

    private String jobBuildUrl = "#";

    private long startTime = 0;

    private long stopTime = 0;

    private ExecutionResult executionResult = ExecutionResult.NA;

    private int priority = 0;

    private int numberOfTests = 0;

    private int numberOfFailure = 0;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getJobBuildUrl() {
        return jobBuildUrl;
    }

    public void setJobBuildUrl(String jobBuildUrl) {
        this.jobBuildUrl = jobBuildUrl;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public ExecutionResult getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(ExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getNumberOfTests() {
        return numberOfTests;
    }

    public void setNumberOfTests(int numberOfTests) {
        this.numberOfTests = numberOfTests;
    }

    public int getNumberOfFailure() {
        return numberOfFailure;
    }

    public void setNumberOfFailure(int numberOfFailure) {
        this.numberOfFailure = numberOfFailure;
    }

    public int getJobBuildNumber() {
        return jobBuildNumber;
    }

    public void setJobBuildNumber(int jobBuildNumber) {
        this.jobBuildNumber = jobBuildNumber;
    }
}
