package com.tascape.qa.th;

import com.tascape.qa.th.db.DbHandler;
import com.tascape.qa.th.db.TestCase;
import com.tascape.qa.th.db.TestResult;
import com.tascape.qa.th.suite.AbstractSuite;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    private final SystemConfiguration sysConfig = SystemConfiguration.getInstance();

    private final String execId = sysConfig.getExecId();

    private static final Map<String, TestCase> UNSUPPORTED_TESTS = new HashMap<>();

    public synchronized static void addUnspportedTestCase(TestCase tc) {
        UNSUPPORTED_TESTS.put(tc.format(), tc);
    }

    public SuiteRunner(TestSuite testSuite) throws Exception {
        LOG.info("Run suite {} with execution id {}", testSuite.getName(), execId);
        this.ts = testSuite;

        this.db = DbHandler.getInstance();
        db.queueSuiteExecution(ts, this.execId);
    }

    public int startExecution() throws IOException, InterruptedException, SQLException {
        File dir = sysConfig.getLogPath().resolve(execId).toFile();
        LOG.info("Create suite execution log directory {}", dir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create directory " + dir);
        }

        int threadCount = sysConfig.getExecutionThreadCount();
        LOG.info("Start execution engine with {} thread(s)", threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CompletionService<TestResult> completionService = new ExecutorCompletionService<>(executorService);

        LOG.info("Start to acquire test cases to execute");
        int numberOfFailures = 0;
        try {
            List<TestResult> tcrs = this.filter(this.db.getQueuedTestCaseResults(this.execId, 100));
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
                        if (tcr == null) {
                            continue;
                        }
                        String result = tcr.getExecutionResult().result();
                        LOG.info("Get result of test case {} - {}", tcr.getTestCase().format(), result);
                        if (!ExecutionResult.PASS.name().equals(result) && !result.endsWith("/0")) {
                            numberOfFailures++;
                        }
                    } catch (Throwable ex) {
                        LOG.error("Error executing test thread", ex);
                        numberOfFailures++;
                    }
                }

                tcrs = this.filter(this.db.getQueuedTestCaseResults(this.execId, 100));
            }
        } finally {
            AbstractSuite.getSuites().stream().forEach((suite) -> {
                try {
                    suite.tearDown();
                } catch (Exception ex) {
                    LOG.warn("Error tearing down suite {} -  {}", suite.getClass(), ex.getMessage());
                }
            });
        }
        executorService.shutdown();

        LOG.info("No more test case to run on this host, updating suite execution result");
        this.db.updateSuiteExecutionResult(this.execId);
        return numberOfFailures;
    }

    private List<TestResult> filter(List<TestResult> tcrs) {
        List<TestResult> tcrs0 = new ArrayList<>();
        tcrs.stream().filter((tcr) -> (UNSUPPORTED_TESTS.get(tcr.getTestCase().format()) == null)).forEach((tcr) -> {
            tcrs0.add(tcr);
        });
        return tcrs0;
    }
}
