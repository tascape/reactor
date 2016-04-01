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
import com.tascape.qa.th.db.SuiteProperty;
import com.tascape.qa.th.db.TestCase;
import com.tascape.qa.th.db.TestResult;
import com.tascape.qa.th.suite.AbstractSuite;
import com.tascape.qa.th.suite.Environment;
import java.io.IOException;
import java.nio.file.Path;
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
        Path logFile = this.newLogFile();
        try {
            if (!db.acquireTestCaseResult(this.tcr)) {
                return null;
            }

            AbstractTestRunner.setTestCaseResult(this.tcr);
            this.injectTestEnvironment();

            this.runTestCase();
        } catch (Throwable ex) {
            LOG.error("Cannot execute test case {}", this.tcr.getTestCase().format(), ex);
            this.tcr.setResult(ExecutionResult.FAIL);
            this.tcr.setException(ex);
            this.db.updateTestExecutionResult(this.tcr);
            throw ex;
        } finally {
            removeLog4jAppender(logFile.toFile().getAbsolutePath());
            this.generateHtml(logFile);
        }
        return this.tcr;
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
        LOG.info("Loading test case {}", tc.format());
        TestRunListener trl = new TestRunListener(this.db, this.tcr);
        JUnitCore core = new JUnitCore();
        core.addListener(trl);
        core.run(Request.method(Class.forName(tc.getTestClass()), tc.getTestMethod()));
    }

    private void injectTestEnvironment() throws Exception {
        String suiteClass = this.tcr.getTestCase().getSuiteClass();
        if (suiteClass == null || suiteClass.isEmpty()) {
            return;
        }
        Environment env = AbstractSuite.getEnvionment(suiteClass);
        if (env == null) {
            AbstractSuite abstractSuite = AbstractSuite.class.cast(Class.forName(suiteClass).newInstance());
            abstractSuite.setUp();
            AbstractSuite.addSuite(abstractSuite);
            env = AbstractSuite.getEnvionment(suiteClass);

            SuiteProperty prop = new SuiteProperty();
            prop.setSuiteResultId(tcr.getSuiteResultId());
            prop.setPropertyName(SystemConfiguration.SYSPROP_TEST_ENV + "." + Thread.currentThread().getName());
            prop.setPropertyValue(env.getName());
            this.db.addSuiteExecutionProperty(prop);
        }
    }

    private Path newLogFile() throws IOException {
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
        Path logFile = testLogPath.resolve("test.log");
        addLog4jFileAppender(logFile.toFile().getAbsolutePath());
        return logFile;
    }
}
