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

import com.tascape.qa.th.db.DbHandler;
import com.tascape.qa.th.db.TestResult;
import com.tascape.qa.th.db.TestResultMetric;
import com.tascape.qa.th.suite.AbstractSuite;
import com.tascape.qa.th.suite.Environment;
import com.tascape.qa.th.test.AbstractTest;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class TestRunListener extends RunListener {
    private static final Logger LOG = LoggerFactory.getLogger(TestRunListener.class);

    private DbHandler db = null;

    private TestResult tcr = null;

    private Throwable throwable;

    public TestRunListener(DbHandler db, TestResult tcr) {
        this.db = db;
        this.tcr = tcr;
    }

    /**
     * Called before any tests have been run.
     *
     * @param description describes the tests to be run
     *
     * @throws Exception for various issues
     */
    @Override
    public void testRunStarted(Description description) throws Exception {
        LOG.debug("Test class started {}", description.getClassName());
        if (this.db == null || this.tcr == null) {
            return;
        }

        Class<?> testClass = Class.forName(description.getClassName());
        try {
            Field f = testClass.getField("AUT");
            if (f != null) {
                this.tcr.setAut(f.get(null) + "");
            }
        } catch (NoSuchFieldException ex) {
            LOG.trace("{}", ex.getMessage());
        }

        long millis = System.currentTimeMillis();
        this.tcr.setStartTime(millis);
        this.tcr.setStopTime(millis + 11);
        this.tcr.setResult(ExecutionResult.RUNNING);
        this.tcr.setTestStation(SystemConfiguration.getInstance().getHostName());

        try {
            this.db.updateTestExecutionResult(this.tcr);
        } catch (SQLException ex) {
            LOG.error("Cannot update test result", ex);
            throw ex;
        }
    }

    /**
     * Called when an atomic test flags that it assumes a condition that is false
     *
     * @param failure describes the test that failed and the {@link AssumptionViolatedException} that was thrown
     */
    @Override
    public void testAssumptionFailure(Failure failure) {
        LOG.error("{}", failure.getTestHeader(), failure.getException());
        this.throwable = failure.getException();
    }

    /**
     * Called when an atomic test is about to be started.
     *
     * @param description the description of the test that is about to be run (generally a class and method name)
     *
     * @throws Exception for various issues
     */
    @Override
    public void testStarted(Description description) throws Exception {
        LOG.debug("Test method started {}.{}", description.getClassName(), description.getMethodName());

        AbstractTest test = AbstractTest.getTest();
        if (test != null) {
            String aut = test.getApplicationUnderTest();
            if (aut == null || aut.isEmpty()) {
                return;
            }
            this.tcr.setAut(aut);
            Environment env = AbstractSuite.getEnvionment(tcr.getTestCase().getSuiteClass());
            this.tcr.setTestEnv(env.getName());

            this.db.updateTestExecutionResult(this.tcr);
        }
        LOG.info("Application under test: {}", this.tcr.getAut());
    }

    /**
     * Called when an atomic test fails.
     *
     * @param failure describes the test that failed and the exception that was thrown
     *
     * @throws Exception for various issues
     */
    @Override
    public void testFailure(Failure failure) throws Exception {
        this.throwable = failure.getException();
        LOG.error("{} {}", failure.getDescription().getDisplayName(), this.throwable.getMessage());

        if (this.db == null || this.tcr == null) {
            return;
        }
        if (throwable == null) {
            return;
        }

        AbstractTest test = AbstractTest.getTest();
        this.tcr.setException(throwable);
    }

    /**
     * Called when an atomic test has finished, whether the test succeeds or fails.
     *
     * @param description the description of the test that just ran
     *
     * @throws Exception for various issues
     */
    @Override
    public void testFinished(Description description) throws Exception {
        LOG.debug("Test method finished {}.{}", description.getClassName(), description.getMethodName());
        if (this.db == null || this.tcr == null) {
            return;
        }

        AbstractTest test = AbstractTest.getTest();
        if (test == null) {
            return;
        }

        this.tcr.setExternalId(test.getExternalId());
        test.cleanBackgoundTasks();

        List<TestResultMetric> resultMetrics = test.getTestResultMetrics();
        if (!resultMetrics.isEmpty()) {
            db.saveTestResultMetrics(tcr.getTestResultId(), resultMetrics);
        }
    }

    /**
     * Called when all tests have finished
     *
     * @param result the summary of the test run, including all the tests that failed
     *
     * @throws Exception for various issues
     */
    @Override
    public void testRunFinished(Result result) throws Exception {
        LOG.debug("Test class finished");
        boolean pass = result.wasSuccessful();
        LOG.debug("PASS: {}, time: {} sec", pass, result.getRunTime() / 1000.0);
        if (this.throwable == null) {
            result.getFailures().stream().forEach((f) -> {
                LOG.error("Failure {}", f.getDescription(), f.getException());
            });
        }

        if (this.db == null || this.tcr == null) {
            return;
        }

        this.tcr.setStopTime(System.currentTimeMillis());
        this.tcr.setResult(pass ? ExecutionResult.PASS : ExecutionResult.FAIL);

        AbstractTest test = AbstractTest.getTest();
        if (test != null) {
            ExecutionResult er = test.getExecutionResult();
            if (!ExecutionResult.NA.equals(er)) {
                LOG.debug("Overwriting JUnit4 execution engine result with the one from test case - {}", er.result());
                this.tcr.setResult(er);
            }
        } else {
            LOG.warn("Null test case? Test may have failed in @BeforeClass methods.");
        }
        AbstractTest.setTest(null);

        this.checkExceptionToRequeue(throwable);
        this.db.updateTestExecutionResult(this.tcr);
    }

    /**
     * Called when a test will not be run, generally because a test method is annotated with
     * {@link org.junit.Ignore}.
     *
     * @param description describes the test that will not be run
     *
     * @throws Exception for various issues
     */
    @Override
    public void testIgnored(Description description) throws Exception {
        LOG.debug("{}", description);
    }

    public void throwException() {
        if (this.throwable != null) {
            throw new RuntimeException(this.throwable);
        }
    }

    private void checkExceptionToRequeue(Throwable throwable) {
        /**
         * todo: need to use a domain specific exception type
         */
        if (!(throwable instanceof AssumptionViolatedException)) {
            return;
        }
        LOG.debug("Requeue test case {}", this.tcr.getTestCase().format());
        this.tcr.setRetry(this.tcr.getRetry() + 1);
        this.tcr.setResult(ExecutionResult.QUEUED);
    }
}
