package com.tascape.qa.th;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class TestHarness {
    private static final Logger LOG = LoggerFactory.getLogger(TestHarness.class);

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            SystemConfiguration config = SystemConfiguration.getInstance();
            Utils.cleanDirectory(config.getLogPath().toFile().getAbsolutePath(), 240,
                    SystemConfiguration.CONSTANT_LOG_KEEP_ALIVE_PREFIX);

            String suiteClass = config.getTestSuite();
            Pattern testClassRegex = config.getTestClassRegex();
            Pattern testMethodRegex = config.getTestMethodRegex();
            int priority = config.getTestPriority();
            LOG.info("Running test suite class: {}", suiteClass);
            TestSuite ts = new TestSuite(suiteClass, testClassRegex, testMethodRegex, priority);

            if (ts.getTests().isEmpty()) {
                throw new RuntimeException("No test cases found based on system properties");
            }

            SuiteRunner sr = new SuiteRunner(ts);
            exitCode = sr.startExecution();
        } catch (Throwable t) {
            LOG.error("TestHarness finishes with exception", t);
            exitCode = -1;
        } finally {
            LOG.error("TestHarness finishes with exit code {}", exitCode);
            System.exit(exitCode);
        }
    }
}
