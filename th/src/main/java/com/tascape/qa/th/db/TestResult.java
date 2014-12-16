package com.tascape.qa.th.db;

import com.tascape.qa.th.ExecutionResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class TestResult {
    private static final Logger LOG = LoggerFactory.getLogger(TestResult.class);

    private static final long serialVersionUID = 1L;

    private TestCase testCase = null;

    private String id = "";

    private String suiteResult = "";

    private long startTime = 0;

    private long stopTime = 0;

    private ExecutionResult executionResult = ExecutionResult.NA;

    private String host = "";

    private String logDirectory = "";

    private Throwable exception = null;

    private String stacktrace = "";

    private String aut = "";

    private int retry = 2;

    TestResult() {
    }

    public TestResult(TestCase tc) {
        this.testCase = tc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSuiteResult() {
        return suiteResult;
    }

    public void setSuiteResult(String suiteResult) {
        this.suiteResult = suiteResult;
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getLogDirectory() {
        return logDirectory;
    }

    public void setLogDirectory(String logDirectory) {
        this.logDirectory = logDirectory;
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

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public String getAut() {
        return aut;
    }

    public void setAut(String aut) {
        this.aut = aut;
    }

}
