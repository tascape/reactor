package com.tascape.qa.th;

import com.tascape.qa.th.db.DbHandler;
import com.tascape.qa.th.db.TestResult;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class AbstractTestRunner {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTestRunner.class);

    private static final ThreadLocal<Path> TEST_LOG_PATH = new ThreadLocal<Path>() {
        @Override
        protected Path initialValue() {
            String execId = SystemConfiguration.getInstance().getExecId();
            Path testLogPath = SystemConfiguration.getInstance().getLogPath().resolve(execId);
            testLogPath.toFile().mkdirs();
            return testLogPath;
        }
    };

    public static void setTestLogPath(Path testLogPath) {
        LOG.trace("Setting runtime log directory {}", testLogPath);
        TEST_LOG_PATH.set(testLogPath);
    }

    public static Path getTestLogPath() {
        return TEST_LOG_PATH.get();
    }

    private static final ThreadLocal<TestResult> TEST_CASE_RESULT = new ThreadLocal<>();

    public static void setTestCaseResult(TestResult testCaseResult) {
        TEST_CASE_RESULT.set(testCaseResult);
    }

    public static TestResult getTestCaseResult() {
        return TEST_CASE_RESULT.get();
    }

    protected SystemConfiguration sysConfig = SystemConfiguration.getInstance();

    protected DbHandler db = null;

    protected TestResult tcr = null;

    protected String execId = "";

    public abstract void runTestCase() throws Exception;

    protected void generateHtml(Path logFile) {
        Path html = logFile.getParent().resolve("log.html");
        LOG.trace("creating file {}", html);
        try (PrintWriter pw = new PrintWriter(html.toFile())) {
            pw.println("<html><body><pre>");
            pw.println("<a href=\"../\">Parent Directory</a>,<a href=\"./\">Current Directory</a>");
            pw.println();
            pw.println(logFile);
            pw.println();
            List<String> lines = FileUtils.readLines(logFile.toFile());
            List<File> files = new ArrayList<>(Arrays.asList(logFile.getParent().toFile().listFiles()));

            for (String line : lines) {
                String newline = line.replaceAll(">", "&gt;");
                newline = newline.replaceAll("<", "&lt;");
                if (newline.contains(" WARN  ")) {
                    newline = "<b>" + newline + "</b> ";
                } else if (newline.contains(" ERROR ") || newline.contains(" Failure in test")) {
                    newline = "<font color='red'><b>" + newline + "</b></font> ";
                }
                pw.println(newline);
                for (File file : files) {
                    if (newline.contains(file.getAbsolutePath())) {
                        if (file.getName().endsWith(".png")) {
                            pw.printf("<a href=\"%s\" target=\"_blank\"><img src=\"%s\" width=\"360px\"/></a>",
                                    file.getName(), file.getName());
                        }
                        pw.printf("open attachment <a href=\"%s\" target=\"_blank\">%s</a>", file.getName(),
                                file.getName());
                        pw.println();
                        files.remove(file);
                        break;
                    }
                }
            }

            pw.println("</pre></body></html>");
            logFile.toFile().delete();
        } catch (IOException ex) {
            LOG.warn(ex.getMessage());
        }
    }
}
