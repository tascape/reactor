/*
 * Copyright (c) 2015 - present Nebula Bay.
 * All rights reserved.
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
package com.tascape.reactor;

import com.tascape.reactor.db.DbHandler;
import com.tascape.reactor.db.CaseResult;
import com.tascape.reactor.db.CaseResultMetric;
import com.tascape.reactor.task.AbstractCase;
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
public class CaseRunListener extends RunListener {
    private static final Logger LOG = LoggerFactory.getLogger(CaseRunListener.class);

    private DbHandler db = null;

    private CaseResult tcr = null;

    private Throwable throwable;

    public CaseRunListener(DbHandler db, CaseResult tcr) {
        this.db = db;
        this.tcr = tcr;
    }

    /**
     * Called before any cases have been run.
     *
     * @param description describes the cases to be run
     *
     * @throws Exception for various issues
     */
    @Override
    public void testRunStarted(Description description) throws Exception {
        LOG.trace("Case class started {}", description.getClassName());
        if (this.db == null || this.tcr == null) {
            return;
        }

        Class<?> caseClass = Class.forName(description.getClassName());
        try {
            Field f = caseClass.getField("AUT");
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
        this.tcr.setCaseStation(SystemConfiguration.getInstance().getHostName());

        try {
            this.db.updateCaseExecutionResult(this.tcr);
        } catch (SQLException ex) {
            LOG.error("Cannot update case result", ex);
            throw ex;
        }
    }

    /**
     * Called when an atomic case flags that it assumes a condition that is false
     *
     * @param failure describes the case that failed and the {@link AssumptionViolatedException} that was thrown
     */
    @Override
    public void testAssumptionFailure(Failure failure) {
        LOG.error("{}", failure.getTestHeader(), failure.getException());
        this.throwable = failure.getException();
    }

    /**
     * Called when an atomic case is about to be started.
     *
     * @param description the description of the case that is about to be run (generally a class and method name)
     *
     * @throws Exception for various issues
     */
    @Override
    public void testStarted(Description description) throws Exception {
        LOG.info("Case method started: {}", description.getMethodName());

        AbstractCase kase = AbstractCase.getCase();
        if (kase != null) {
            String aut = kase.getApplicationUnderTask();
            if (aut == null || aut.isEmpty()) {
                return;
            }
            this.tcr.setAut(aut);

            this.db.updateCaseExecutionResult(this.tcr);
        }
        LOG.info("Application under task: {}", this.tcr.getAut());
    }

    /**
     * Called when an atomic case fails.
     *
     * @param failure describes the case that failed and the exception that was thrown
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

        AbstractCase kase = AbstractCase.getCase();
        this.tcr.setException(throwable);
    }

    /**
     * Called when an atomic case has finished, whether the case succeeds or fails.
     *
     * @param description the description of the case that just ran
     *
     * @throws Exception for various issues
     */
    @Override
    public void testFinished(Description description) throws Exception {
        LOG.info("Case method finished: {}", description.getMethodName());
        if (this.db == null || this.tcr == null) {
            return;
        }

        AbstractCase kase = AbstractCase.getCase();
        if (kase == null) {
            return;
        }

        this.tcr.setExternalId(kase.getExternalId());
        kase.cleanBackgoundTasks();

        List<CaseResultMetric> resultMetrics = kase.getResultMetrics();
        if (!resultMetrics.isEmpty()) {
            db.saveCaseResultMetrics(tcr.getCaseResultId(), resultMetrics);
        }
    }

    /**
     * Called when all cases have finished
     *
     * @param result the summary of the case run, including all the cases that failed
     *
     * @throws Exception for various issues
     */
    @Override
    public void testRunFinished(Result result) throws Exception {
        LOG.trace("Case class finished");
        boolean pass = result.wasSuccessful();
        float time = result.getRunTime() / 1000.0f;
        if (pass) {
            LOG.info("PASS: {}, time: {} sec", pass, time);
        } else {
            LOG.error("PASS: {}, time: {} sec", pass, time);
        }
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

        AbstractCase kase = AbstractCase.getCase();
        if (kase != null) {
            ExecutionResult er = kase.getExecutionResult();
            if (!ExecutionResult.NA.equals(er)) {
                LOG.debug("Overwriting JUnit4 execution engine result with the one from case - {}", er.result());
                this.tcr.setResult(er);
            }
        } else {
            LOG.warn("Null case? Case may have failed in environment setup.");
        }
        AbstractCase.setCase(null);

        this.checkExceptionToRequeue(throwable);
        this.db.updateCaseExecutionResult(this.tcr);
    }

    /**
     * Called when a case will not be run, generally because a case method is annotated with {@link org.junit.Ignore}.
     *
     * @param description describes the case that will not be run
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
        LOG.info("Requeue case {}", this.tcr.getTaskCase().format());
        this.tcr.setRetry(this.tcr.getRetry() + 1);
        this.tcr.setResult(ExecutionResult.QUEUED);
    }
}
