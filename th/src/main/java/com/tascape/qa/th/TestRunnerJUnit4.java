package com.tascape.qa.th;

import com.tascape.qa.th.data.AbstractTestData;
import com.tascape.qa.th.data.TestData;
import com.tascape.qa.th.db.DbHandler;
import com.tascape.qa.th.db.TestCase;
import com.tascape.qa.th.db.TestResult;
import com.tascape.qa.th.driver.EntityDriver;
import com.tascape.qa.th.suite.AbstractSuite;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class TestRunnerJUnit4 extends AbstractTestRunner implements Callable<TestResult> {
    private static final Logger LOG = LoggerFactory.getLogger(TestRunnerJUnit4.class);

    public TestRunnerJUnit4(DbHandler db, TestResult tcr) {
        this.db = db;
        this.tcr = tcr;
        this.execId = this.tcr.getSuiteResult();
    }

    @Override
    public TestResult call() throws Exception {
        try {
            if (this.db != null) {
                if (!db.acquireTestCaseResult(this.tcr)) {
                    return null;
                }
            }

            AbstractTestRunner.setTestCaseResult(this.tcr);
            this.injectTestEnvironment();

            this.runTestCase();
        } catch (Exception ex) {
            LOG.error("Cannot execute test case {}", this.tcr.getTestCase().format(), ex);
            this.tcr.setExecutionResult(ExecutionResult.FAIL);
            this.tcr.setException(ex);
            this.db.updateTestExecutionResult(this.tcr);
            return null;
        }
        return this.tcr;
    }

    private void injectTestEnvironment() throws Exception {
        String suiteClass = this.tcr.getTestCase().getSuiteClass();
        if (suiteClass == null || suiteClass.isEmpty()) {
            return;
        }
        Map<String, EntityDriver> drivers = AbstractSuite.getEnvionment(suiteClass);
        if (drivers == null) {
            AbstractSuite abstractSuite = AbstractSuite.class.cast(Class.forName(suiteClass).newInstance());
            abstractSuite.setUp();
            AbstractSuite.addSuite(abstractSuite);
        }
    }

    @Override
    public void runTestCase() throws Exception {
        AbstractTestData.setTestData(null);
        String testDataInfo = this.tcr.getTestCase().getTestDataInfo();
        if (!testDataInfo.isEmpty()) {
            TestData testData = AbstractTestData.getTestData(testDataInfo);
            LOG.info("Injecting test data: {} = {}", testDataInfo, testData.format());
            AbstractTestData.setTestData(testData);
        }

        TestCase tc = this.tcr.getTestCase();
        Path testLogPath = sysConfig.getLogPath().resolve(this.execId)
            .resolve(tc.formatForLogPath() + "." + System.currentTimeMillis() + "."
                + Thread.currentThread().getName());

        LOG.info("Creating test case execution log directory {}", testLogPath);
        if (!testLogPath.toFile().mkdirs()) {
            throw new IOException("Cannot create log directory " + testLogPath);
        }
        AbstractTestRunner.setTestLogPath(testLogPath);
        this.tcr.setLogDirectory(testLogPath.toFile().getAbsolutePath());

        LOG.info("Creating log file");
        final Path logFile = testLogPath.resolve("test.log");
        Utils.addLog4jFileAppender(logFile.toFile().getAbsolutePath());

        LOG.info("Loading test case {}", tc.format());
        TestRunListener trl = new TestRunListener(this.db, this.tcr);
        try {
            JUnitCore core = new JUnitCore();
            core.addListener(trl);
            core.run(Request.method(Class.forName(tc.getTestClass()), tc.getTestMethod()));
        } finally {
            Utils.removeLog4jAppender(logFile.toFile().getAbsolutePath());
        }

        this.generateHtml(logFile);
    }

    public static void main(String[] args) throws Exception {
        SystemConfiguration sysConfig = SystemConfiguration.getInstance();
        try {
            TestCase tc = new TestCase();
            tc.setTestClass("com.adara.qa.th.test.JUnit4Test");
            tc.setTestMethod("testPositive");

            TestResult tcr = new TestResult(tc);
            tcr.setSuiteResult(sysConfig.getExecId());
            tcr.setHost(sysConfig.getHostName());

            ExecutorService es = Executors.newFixedThreadPool(1);
            CompletionService<TestResult> executor = new ExecutorCompletionService<>(es);

            executor.submit(new TestRunnerJUnit4(null, tcr));
            TestResult r = executor.take().get();

            LOG.info("execId: {}", r.getSuiteResult());
            LOG.info("test case: {}", tc.format());
            LOG.info("result: {}", r.getExecutionResult().result());
            LOG.info("host: {}", r.getHost());
            LOG.info("start: {}", new Date(r.getStartTime()));
            LOG.info("stop: {}", new Date(r.getStopTime()));
            LOG.info("log: {}", r.getLogDirectory());
        } finally {
            System.exit(0);
        }
    }
}
