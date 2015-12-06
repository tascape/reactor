/*
 * Copyright 2015 tascape.
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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tascape.qa.th.db.DbHandler;
import com.tascape.qa.th.db.SuiteProperty;
import com.tascape.qa.th.db.TestCase;
import com.tascape.qa.th.db.TestResult;
import com.tascape.qa.th.exception.SuiteEnvironmentException;
import com.tascape.qa.th.suite.AbstractSuite;
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

    private TestSuite ts = null;

    private DbHandler db = null;

    private final SystemConfiguration SYS_CONFIG = SystemConfiguration.getInstance();

    private final String execId = SYS_CONFIG.getExecId();

    private static final Map<String, TestCase> UNSUPPORTED_TESTS = new HashMap<>();

    public synchronized static void addUnspportedTestCase(TestCase tc) {
        UNSUPPORTED_TESTS.put(tc.format(), tc);
    }

    public SuiteRunner(TestSuite testSuite) throws Exception {
        LOG.info("Run suite with execution id {}", execId);
        this.ts = testSuite;

        this.db = DbHandler.getInstance();
        db.queueSuiteExecution(ts, this.execId);
    }

    @SuppressWarnings("UseSpecificCatch")
    public int runTests() throws IOException, InterruptedException, SQLException, XMLStreamException {
        File dir = SYS_CONFIG.getLogPath().resolve(execId).toFile();
        LOG.info("Create suite execution log directory {}", dir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create directory " + dir);
        }
        this.saveExectionProperties(dir);
        ExecutorService executorService = this.getExecutorService();
        CompletionService<TestResult> completionService = new ExecutorCompletionService<>(executorService);

        LOG.info("Start to acquire test cases to execute");
        int loadLimit = SYS_CONFIG.getTestLoadLimit();
        LOG.info("Load queued test cases {} per round", loadLimit);

        int numberOfFailures = 0;
        try {
            List<TestResult> tcrs = this.filter(this.db.getQueuedTestCaseResults(this.execId, loadLimit));
            while (!tcrs.isEmpty()) {
                List<Future<TestResult>> futures = new ArrayList<>();

                for (TestResult tcr : tcrs) {
                    LOG.info("Submit test case {}", tcr.getTestCase().format());
                    futures.add(completionService.submit(new TestRunnerJUnit4(db, tcr)));
                }
                LOG.debug("Total {} test cases submitted", futures.size());

                for (Future<TestResult> f : futures) {
                    try {
                        Future<TestResult> future = completionService.take();
                        TestResult tcr = future.get();
                        String result = tcr.getResult().result();
                        LOG.info("Get result of test case {} - {}", tcr.getTestCase().format(), result);
                        if (!ExecutionResult.PASS.getName().equals(result) && !result.endsWith("/0")) {
                            numberOfFailures++;
                        }
                    } catch (Throwable ex) {
                        LOG.error("Error executing test thread", ex);
                        numberOfFailures++;
                    }
                }

                tcrs = this.filter(this.db.getQueuedTestCaseResults(this.execId, 100));
            }
            String productUnderTest = "NA";
            try {
                LOG.debug("Getting suite-under-test");
                if (AbstractSuite.getSuites().isEmpty()) {
                    throw new SuiteEnvironmentException("Cannot setup suite environment");
                }
                productUnderTest = AbstractSuite.getSuites().get(0).getProductUnderTest();
            } catch (Exception ex) {
                LOG.warn("Cannot get product-under-test", ex);
            } finally {
                LOG.info("No more test case to run on this host, updating suite execution result");
                this.db.updateSuiteExecutionResult(this.execId, productUnderTest);
                this.db.adjustSuiteExecutionResult(execId);
            }
        } finally {
            AbstractSuite.getSuites().stream().forEach((suite) -> {
                try {
                    suite.tearDown();
                } catch (Exception ex) {
                    LOG.warn("Error tearing down suite {}", suite.getClass(), ex);
                }
            });
        }
        executorService.shutdown();

        this.db.exportToJson(this.execId);
        this.db.saveJunitXml(this.execId);
        return numberOfFailures;
    }

    private void saveExectionProperties(File dir) throws IOException, SQLException {
        File props = new File(dir, "execution.properties");
        SYS_CONFIG.getProperties().store(FileUtils.openOutputStream(props), "Testharness");

        List<SuiteProperty> sps = new ArrayList<>();
        SYS_CONFIG.getProperties().entrySet().stream()
            .filter(key -> !key.toString().startsWith("qa.th.db."))
            .forEach(entry -> {
                SuiteProperty sp = new SuiteProperty();
                sp.setSuiteResultId(this.execId);
                sp.setPropertyName(entry.getKey().toString());
                sp.setPropertyValue(entry.getValue().toString());
                sps.add(sp);
            });
        this.db.setSuiteExecutionProperties(sps);
    }

    private ExecutorService getExecutorService() {
        int tc = SYS_CONFIG.getExecutionThreadCount();
        LOG.debug("nuber of thread(s) {}", tc);
        if (tc < 0) {
            throw new RuntimeException("Invalid execution thread number");
        }
        int env = ts.getNumberOfEnvs();
        LOG.debug("nuber of environment(s) {}", env);
        if (env < 0) {
            throw new RuntimeException("Invalid execution environment number");
        }
        int threadCount = (tc == 0) ? (env == 0 ? 1 : env) : (env == 0 ? tc : Math.min(tc, env));
        LOG.info("Start execution engine with {} thread(s)", threadCount);
        int len = (threadCount + "").length();
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("th%0" + len + "d").build();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount, namedThreadFactory);
        return executorService;
    }

    private List<TestResult> filter(List<TestResult> tcrs) {
        List<TestResult> tcrs0 = new ArrayList<>();
        tcrs.stream().filter((tcr) -> (UNSUPPORTED_TESTS.get(tcr.getTestCase().format()) == null)).forEach((tcr) -> {
            tcrs0.add(tcr);
        });

        if (SystemConfiguration.getInstance().isShuffleTests()) {
            LOG.debug("do test cases shuffle");
            Collections.shuffle(tcrs0);
        }
        return tcrs0;
    }
}
