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
package com.tascape.qa.th.test;

import com.tascape.qa.th.AbstractTestRunner;
import com.tascape.qa.th.ExecutionResult;
import com.tascape.qa.th.data.AbstractTestData;
import com.tascape.qa.th.data.TestData;
import com.tascape.qa.th.db.TestResult;
import com.tascape.qa.th.driver.EntityDriver;
import com.tascape.qa.th.suite.AbstractSuite;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tascape.qa.th.AbstractTestResource;
import com.tascape.qa.th.db.TestResultMetric;
import com.tascape.qa.th.driver.TestDriver;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class AbstractTest extends AbstractTestResource {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class);

    private static final ThreadLocal<AbstractTest> ABSTRACT_TEST = new ThreadLocal<>();

    public static void setTest(AbstractTest test) {
        ABSTRACT_TEST.set(test);
    }

    public static AbstractTest getTest() {
        return ABSTRACT_TEST.get();
    }

    @Rule
    public TestName testName = new TestName();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public Timeout globalTimeout = new Timeout(15, TimeUnit.MINUTES);

    protected String execId = sysConfig.getExecId();

    private final Path testLogPath = AbstractTestRunner.getTestLogPath();

    protected TestData testData = AbstractTestData.getTestData();

    private final TestResult tcr = AbstractTestRunner.getTestCaseResult();

    private ExecutionResult result = ExecutionResult.NA;

    private final ExecutorService backgroundExecutorService;

    private final List<TestResultMetric> resultMetrics = new LinkedList<>();

    private String externalId = "";

    public abstract String getApplicationUnderTest();

    public AbstractTest() {
        this.result.setPass(0);
        this.result.setFail(0);

        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setDaemon(true);
        builder.setNameFormat(Thread.currentThread().getName() + "-%d");
        this.backgroundExecutorService = Executors.newCachedThreadPool(builder.build());

        AbstractTest.setTest(this); // TODO: move this to somewhere else
    }

    @Override
    public Path getLogPath() {
        return testLogPath;
    }

    protected <D extends EntityDriver> D getEntityDriver(TestDriver testDriver) {
        String key = testDriver.toString();
        Class<? extends EntityDriver> clazz = testDriver.getDriverClass();
        if (clazz == null) {
            throw new RuntimeException("EntityDriver type was not specified in TestDriver instance.");
        }
        LOG.debug("Getting runtime driver (name={}, type={}) from suite test environment", key, clazz.getName());

        String suiteClass = this.tcr.getTestCase().getSuiteClass();
        if (suiteClass.isEmpty()) {
            return null;
        }

        Map<String, EntityDriver> env = AbstractSuite.getEnvionment(suiteClass);
        EntityDriver driver = env.get(key);
        if (driver == null) {
            LOG.error("Cannot find driver of name={} and type={}, please check suite test environemnt",
                key, clazz.getName());
            return null;
        }
        driver.setTest(this);
        return (D) driver;
    }

    protected TestData getTestData() {
        if (this.testData != null) {
            LOG.debug("Getting injected test data {}={}", this.testData.getClass().getName(), this.testData.getValue());
        }
        return this.testData;
    }

    protected <T extends TestData> T getTestData(Class<T> clazz) throws Exception {
        TestData td = getTestData();
        if (td == null) {
            LOG.debug("There is no injected test data, create a new instance of ", clazz);
            td = clazz.newInstance();
        }
        return clazz.cast(td);
    }

    /**
     * External id is for exporting test result into other test case management system, such as TestRail.
     *
     * @return external id for test result export
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * This is called in test method to register external id (if any). Do not call this if test result will not be
     * exported into other test case management system, such as TestRail.
     *
     * @param externalId external id for test result export
     */
    protected void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public ExecutionResult getExecutionResult() {
        return result;
    }

    public void submitBackgroundTask(Runnable runnable) {
        this.backgroundExecutorService.submit(runnable);
    }

    public void cleanBackgoundTasks() {
        this.backgroundExecutorService.shutdownNow();
    }

    /**
     * Updates the test metrics presentation for easy understanding.
     *
     * @param value update test data value
     */
    protected void updateTestDataFormat(String value) {
        this.testData.setValue(value);
        this.tcr.getTestCase().setTestData(value);
    }

    protected void setExecutionResult(ExecutionResult executionResult) {
        this.result = executionResult;
    }

    public List<TestResultMetric> getTestResultMetrics() {
        return resultMetrics;
    }

    protected void putResultMetric(String group, String name, double value) {
        TestResultMetric metric = new TestResultMetric();
        metric.setMetricGroup(group);
        metric.setMetricName(name);
        metric.setMetricValue(value);
        LOG.info("Test result metric '{}' - '{}' - {}", group, name, value);
        this.resultMetrics.add(metric);
    }

    protected void captureScreens(final long intervalMillis) {
        this.submitBackgroundTask(() -> {
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(intervalMillis);
                } catch (InterruptedException ex) {
                    LOG.trace(ex.getMessage());
                    return;
                }
                AbstractTest.this.captureScreen();
            }
        });
    }
}
