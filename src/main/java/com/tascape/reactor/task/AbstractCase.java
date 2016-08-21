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
package com.tascape.reactor.task;

import com.tascape.reactor.AbstractCaseRunner;
import com.tascape.reactor.ExecutionResult;
import com.tascape.reactor.data.AbstractCaseData;
import com.tascape.reactor.db.CaseResult;
import com.tascape.reactor.driver.EntityDriver;
import com.tascape.reactor.suite.AbstractSuite;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tascape.reactor.AbstractCaseResource;
import com.tascape.reactor.db.CaseResultMetric;
import com.tascape.reactor.driver.CaseDriver;
import com.tascape.reactor.suite.Environment;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tascape.reactor.data.CaseData;

/**
 *
 * @author linsong wang
 */
public abstract class AbstractCase extends AbstractCaseResource {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCase.class);

    private static final ThreadLocal<AbstractCase> ABSTRACT_CASE = new ThreadLocal<>();

    public static void setCase(AbstractCase kase) {
        ABSTRACT_CASE.set(kase);
    }

    public static AbstractCase getCase() {
        return ABSTRACT_CASE.get();
    }

    @Rule
    public TestName testName = new TestName();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public Timeout globalTimeout = new Timeout(15, TimeUnit.MINUTES);

    protected String execId = sysConfig.getExecId();

    private final Path caseLogPath = AbstractCaseResource.getCaseLogPath();

    protected CaseData caseData = AbstractCaseData.getCaseData();

    private final CaseResult tcr = AbstractCaseRunner.getCaseResult();

    private ExecutionResult result = ExecutionResult.NA;

    private final ExecutorService backgroundExecutorService;

    private final List<CaseResultMetric> resultMetrics = new LinkedList<>();

    private final Environment env;

    private final String suiteClass;

    private String externalId = "";

    public abstract String getApplicationUnderTask();

    public AbstractCase() {
        this.result.setPass(0);
        this.result.setFail(0);

        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setDaemon(true);
        builder.setNameFormat(Thread.currentThread().getName() + "-%d");
        this.backgroundExecutorService = Executors.newCachedThreadPool(builder.build());

        suiteClass = this.tcr.getTaskCase().getSuiteClass();
        env = AbstractSuite.getEnvionment(suiteClass);

        AbstractCase.setCase(this); // TODO: move this to somewhere else
    }

    @Override
    public Path getLogPath() {
        return caseLogPath;
    }

    protected <D extends EntityDriver> D getEntityDriver(CaseDriver caseDriver) {
        String key = caseDriver.toString();
        Class<? extends EntityDriver> clazz = caseDriver.getDriverClass();
        if (clazz == null) {
            throw new RuntimeException("EntityDriver type was not specified in CaseDriver instance.");
        }
        LOG.debug("Getting runtime driver (name={}, type={}) from suite environment", key, clazz.getName());

        if (suiteClass.isEmpty()) {
            return null;
        }

        EntityDriver driver = env.get(key);
        if (driver == null) {
            LOG.error("Cannot find driver of name={} and type={}, please check suite environemnt",
                key, clazz.getName());
            return null;
        }
        driver.setCase(this);
        return (D) driver;
    }

    protected CaseData getCaseData() {
        if (this.caseData != null) {
            LOG.debug("Getting injected case data {}={}", this.caseData.getClass().getName(), this.caseData.getValue());
        }
        return this.caseData;
    }

    protected <T extends CaseData> T getCaseData(Class<T> clazz) throws Exception {
        CaseData td = AbstractCase.this.getCaseData();
        if (td == null) {
            LOG.debug("There is no injected case data, create a new instance of ", clazz);
            td = clazz.newInstance();
        }
        return clazz.cast(td);
    }

    /**
     * External id is for exporting case result into other case management system, such as TestRail.
     *
     * @return external id for case result export
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * This is called in case method to register external id (if any). Do not call this if case result will not be
     * exported into other case management system, such as TestRail.
     *
     * @param externalId external id for case result export
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
     * Updates the case metrics presentation for easy understanding.
     *
     * @param value update case data value
     */
    protected void updateCaseDataFormat(String value) {
        this.caseData.setValue(value);
        this.tcr.getTaskCase().setCaseData(value);
    }

    protected void setExecutionResult(ExecutionResult executionResult) {
        this.result = executionResult;
        if (executionResult.isFailure()) {
            throw new AssertionError("execution result: " + executionResult.result());
        }
    }

    public List<CaseResultMetric> getResultMetrics() {
        return resultMetrics;
    }

    protected void putResultMetric(String group, String name, double value) {
        CaseResultMetric metric = new CaseResultMetric();
        metric.setMetricGroup(group);
        metric.setMetricName(name);
        metric.setMetricValue(value);
        LOG.info("Case result metric '{}' - '{}' - {}", group, name, value);
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
                AbstractCase.this.captureScreen();
            }
        });
    }
}
