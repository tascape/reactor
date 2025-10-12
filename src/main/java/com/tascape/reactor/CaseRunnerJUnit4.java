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

import com.tascape.reactor.data.AbstractCaseData;
import com.tascape.reactor.db.DbHandler;
import com.tascape.reactor.db.SuiteProperty;
import com.tascape.reactor.db.TaskCase;
import com.tascape.reactor.db.CaseResult;
import com.tascape.reactor.suite.AbstractSuite;
import com.tascape.reactor.suite.Environment;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tascape.reactor.data.CaseData;
import com.tascape.reactor.db.SuiteResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.Result;

/**
 *
 * @author linsong wang
 */
public class CaseRunnerJUnit4 extends AbstractCaseRunner implements Callable<CaseResult> {

    private static final Logger LOG = LoggerFactory.getLogger(CaseRunnerJUnit4.class);

    public CaseRunnerJUnit4(DbHandler db, CaseResult tcr) {
        this.db = db;
        this.tcr = tcr;
        this.execId = this.tcr.getSuiteResultId();
    }

    @Override
    public CaseResult call() throws Exception {
        tcr.setCaseEnv(Thread.currentThread().getName());
        Path logFile = newLogFile(tcr.getTaskCase());
        boolean acquired = false;
        try {
            acquired = db.acquireCaseResult(this.tcr);
            if (!acquired) {
                return null;
            }

            AbstractCaseRunner.setCaseResult(this.tcr);
            this.injectCaseEnvironment();

            this.runTaskCase();
        } catch (Throwable ex) {
            LOG.error("Cannot execute case {}", this.tcr.getTaskCase().format(), ex);
            this.tcr.setResult(ExecutionResult.FAIL);
            this.tcr.setException(ex);
            this.db.updateCaseExecutionResult(this.tcr);
            throw ex;
        } finally {
            removeLog4jAppender(logFile);
            if (acquired) {
                this.generateHtml(logFile);
            } else {
                FileUtils.deleteQuietly(logFile.getParent().toFile());
            }
        }
        return this.tcr;
    }

    @Override
    public void runTaskCase() throws Exception {
        AbstractCaseData.setCaseData(null);
        String caseDataInfo = this.tcr.getTaskCase().getCaseDataInfo();
        if (!caseDataInfo.isEmpty()) {
            CaseData caseData = AbstractCaseData.getCaseData(caseDataInfo);
            LOG.debug("Injecting case data: {} = {}", caseDataInfo, caseData.getValue());
            AbstractCaseData.setCaseData(caseData);
        }

        TaskCase tc = this.tcr.getTaskCase();
        LOG.debug("Loading case {}", tc.format());
        CaseRunListener trl = new CaseRunListener(this.db, this.tcr);
        JUnitCore core = new JUnitCore();
        core.addListener(trl);
        Result result = core.run(Request.method(Class.forName(tc.getCaseClass()), tc.getCaseMethod()));
        LOG.debug("junit result: {}", result.wasSuccessful());
    }

    private void injectCaseEnvironment() throws Exception {
        String suiteClass = this.tcr.getTaskCase().getSuiteClass();
        if (suiteClass == null || suiteClass.isEmpty()) {
            return;
        }
        Environment env = AbstractSuite.getEnvionment(suiteClass);
        if (env == null) {
            LOG.info("Initialize suite runtime environment");
            AbstractSuite abstractSuite = AbstractSuite.class.cast(Class.forName(suiteClass).newInstance());
            try {
                abstractSuite.setUp();
            } catch (Throwable t) {
                try {
                    abstractSuite.tearDown();
                } catch (Throwable t0) {
                    LOG.warn("Teardown failed");
                }
                throw t;
            }
            AbstractSuite.addSuite(abstractSuite);
            env = AbstractSuite.getEnvionment(suiteClass);

            SuiteResult suitResult = tcr.getSuiteResult();
            if (StringUtils.isBlank(suitResult.getProductUnderTask())) {
                LOG.debug("Getting product-under-task");
                try {
                    String productUnderTask = abstractSuite.getProductUnderTask();
                    db.updateSuiteProductUnderTask(execId, productUnderTask);
                    suitResult.setProductUnderTask(productUnderTask);
                } catch (Exception ex) {
                    LOG.warn("Cannot get product-under-task", ex);
                }
            }

            SuiteProperty prop = new SuiteProperty();
            prop.setSuiteResultId(tcr.getSuiteResultId());
            prop.setPropertyName(SystemConfiguration.SYSPROP_CASE_ENV + "." + Thread.currentThread().getName());
            prop.setPropertyValue(env.getName());
            this.db.addSuiteExecutionProperty(prop);
        }
    }
}
