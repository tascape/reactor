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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tascape.reactor.db.DbHandler;
import com.tascape.reactor.db.SuiteProperty;
import com.tascape.reactor.db.TaskCase;
import com.tascape.reactor.db.CaseResult;
import com.tascape.reactor.db.SuiteResult;
import com.tascape.reactor.suite.AbstractSuite;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class SuiteRunner {
    private static final Logger LOG = LoggerFactory.getLogger(SuiteRunner.class);

    private TaskSuite ts = null;

    private SuiteResult suitResult = null;

    private DbHandler db = null;

    private final SystemConfiguration SYS_CONFIG = SystemConfiguration.getInstance();

    private final String execId = SYS_CONFIG.getExecId();

    private static final Map<String, TaskCase> UNSUPPORTED_CASES = new HashMap<>();

    public synchronized static void addUnspportedCase(TaskCase tc) {
        UNSUPPORTED_CASES.put(tc.format(), tc);
    }

    public SuiteRunner(TaskSuite taskSuite) throws Exception {
        LOG.debug("Run suite with execution id {}", execId);
        this.ts = taskSuite;

        this.db = DbHandler.getInstance();
        db.queueSuiteExecution(ts, this.execId);

        this.suitResult = new SuiteResult();
        suitResult.setSuiteResultId(execId);
    }

    @SuppressWarnings("UseSpecificCatch")
    public int runCases() throws IOException, InterruptedException, SQLException, XMLStreamException {
        File dir = SYS_CONFIG.getLogPath().resolve(execId).toFile();
        LOG.debug("Create suite execution log directory {}", dir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create directory " + dir);
        }
        int threadCount = this.getNumberOfThreads();
        ExecutorService executorService = this.getExecutorService(threadCount);
        CompletionService<CaseResult> completionService = new ExecutorCompletionService<>(executorService);

        this.saveExectionProperties(dir);
        LOG.debug("Start to acquire cases to execute");
        int loadLimit = SYS_CONFIG.getCaseLoadLimit();
        LOG.debug("Load queued cases {} per round", loadLimit);

        int numberOfFailures = 0;
        try {
            while (true) {
                List<CaseResult> tcrs = this.filter(this.db.getQueuedCaseResults(this.execId, loadLimit));
                if (tcrs.isEmpty()) {
                    break;
                }

                List<Future<CaseResult>> futures = new ArrayList<>();
                tcrs.forEach(tcr -> {
                    tcr.setSuiteResult(suitResult);

                    LOG.debug("Submit case {}", tcr.getTaskCase().format());
                    futures.add(completionService.submit(new CaseRunnerJUnit4(db, tcr)));
                });
                LOG.debug("Total {} cases submitted", futures.size());

                for (Future<CaseResult> f: futures) {
                    try {
                        Future<CaseResult> future = completionService.take();
                        CaseResult tcr = future.get();
                        if (tcr == null) {
                            continue;
                        }
                        String result = tcr.getResult().result();
                        LOG.debug("Get result of case {} - {}", tcr.getTaskCase().format(), result);
                        if (result.equals(ExecutionResult.QUEUED.getName())) {
                            LOG.warn("do not record this re-queued case");
                            continue;
                        }
                        if (result.equals(ExecutionResult.TBI.getName())) {
                            LOG.warn("do not record this to-be-implemented case");
                            continue;
                        }
                        if (!ExecutionResult.PASS.getName().equals(result) && !result.endsWith("/0")) {
                            numberOfFailures++;
                        }
                    } catch (Throwable ex) {
                        LOG.error("Error executing case thread", ex);
                        numberOfFailures++;
                    }
                }
            }

            LOG.debug("No more case to run on this host, updating suite execution result");
            this.db.updateSuiteExecutionResult(this.execId);
            this.db.adjustSuiteExecutionResult(execId);
            return numberOfFailures;
        } finally {
            executorService.shutdown();
            ExecutorService es = Executors.newFixedThreadPool(threadCount);
            AbstractSuite.getSuites().stream().forEach((AbstractSuite suite) -> {
                es.submit(() -> {
                    try {
                        suite.tearDown();
                    } catch (Exception ex) {
                        LOG.warn("Error tearing down suite {}", suite.getClass(), ex);
                    }
                });
            });
            es.shutdown();
            es.awaitTermination(1, TimeUnit.HOURS);

            this.db.exportToJson(this.execId);
            this.db.saveJunitXml(this.execId);
        }
    }

    private void saveExectionProperties(File dir) throws IOException, SQLException {
        File props = new File(dir, "execution.properties");
        SYS_CONFIG.getProperties().store(FileUtils.openOutputStream(props), Reactor.class.getName());

        List<SuiteProperty> sps = new ArrayList<>();
        SYS_CONFIG.getProperties().entrySet().stream()
                .filter(key -> !key.toString().startsWith("reactor.db."))
                .forEach(entry -> {
                    SuiteProperty sp = new SuiteProperty();
                    sp.setSuiteResultId(this.execId);
                    sp.setPropertyName(entry.getKey().toString());
                    sp.setPropertyValue(entry.getValue().toString());
                    sps.add(sp);
                });
        this.db.setSuiteExecutionProperties(sps);
    }

    private int getNumberOfThreads() {
        int tc = SYS_CONFIG.getExecutionThreadCount();
        LOG.debug("thread count {}", tc);
        if (tc < 0) {
            throw new RuntimeException("Invalid execution thread number");
        }
        int env = ts.getNumberOfEnvs();
        LOG.debug("number of environment(s) {}", env);
        if (env < 0) {
            throw new RuntimeException("Invalid execution environment number");
        }
        int threadCount = (tc == 0) ? (env == 0 ? 1 : env) : (env == 0 ? tc : Math.min(tc, env));
        return threadCount;
    }

    private ExecutorService getExecutorService(int threadCount) {
        LOG.debug("Start execution engine with {} thread(s)", threadCount);
        SYS_CONFIG.setExecutionThreadCount(threadCount);
        int len = ((threadCount - 1) + "").length();
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("th%0" + len + "d").build();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount, namedThreadFactory);
        return executorService;
    }

    private List<CaseResult> filter(List<CaseResult> tcrs) {
        List<CaseResult> tcrs0 = tcrs.stream()
                .filter((tcr) -> !UNSUPPORTED_CASES.containsKey(tcr.getTaskCase().format()))
                .collect(Collectors.toList());
        if (SystemConfiguration.getInstance().isShuffleCases()) {
            LOG.debug("do case shuffle");
            Collections.shuffle(tcrs0);
        }
        return tcrs0;
    }
}
