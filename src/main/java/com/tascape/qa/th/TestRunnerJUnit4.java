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
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Callable;
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
        this.execId = this.tcr.getSuiteResultId();
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
            this.tcr.setResult(ExecutionResult.FAIL);
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
        Map<String, ? extends EntityDriver> env = AbstractSuite.getEnvionment(suiteClass);
        if (env == null) {
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
            LOG.info("Injecting test data: {} = {}", testDataInfo, testData.getValue());
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
        String path = testLogPath.toFile().getAbsolutePath();
        this.tcr.setLogDir(path.substring(path.indexOf(this.execId)));

        LOG.info("Creating log file");
        final Path logFile = testLogPath.resolve("test.log");
        Utils.addLog4jFileAppender(logFile.toFile().getAbsolutePath());

        LOG.info("{}", Instant.now());
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
}
