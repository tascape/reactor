package com.tascape.qa.seleniumide.test.data;

import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.data.AbstractTestData;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class SomeWebAppSeleniumIdeHtmlFiles extends AbstractTestData {
    private static final Logger LOG = LoggerFactory.getLogger(SomeWebAppSeleniumIdeHtmlFiles.class);

    public static final String SYSPROP_IDE_HTML_FILES_ROOT = "qa.test.data.somewebapp.ide.html.files.root";

    public static final String[] FEATURES = {
        "FeatureOne",
        "FeatureTwo"
    };

    private static final Map<String, SomeWebAppSeleniumIdeHtmlFiles[]> FEATURE_TEST_HTMLS = new HashMap<>();

    static {
        SystemConfiguration config = SystemConfiguration.getInstance();
        /*
         * you need to specify system property qa.test.data.somewebapp.ide.html.files.root=/dir/to/your/html/files
         */
        String htmlRoot = config.getProperty(SYSPROP_IDE_HTML_FILES_ROOT);
        LOG.info("SeleniumIDE html files root directory: {}", htmlRoot);
        Path root = Paths.get(htmlRoot);
        for (String feature : FEATURES) {
            Path path = root.resolve(feature);
            List<SomeWebAppSeleniumIdeHtmlFiles> htmls = new ArrayList<>();
            File[] fs = path.toFile().listFiles();
            if (fs == null) {
                throw new RuntimeException("Cannot get sub-directories of " + path);
            }
            List<File> files = Arrays.asList(fs);
            Collections.sort(files);
            files.stream()
                .filter(html -> !html.getName().contains("TestSuite") && html.getName().endsWith(".html"))
                .forEach(html -> htmls.add(new SomeWebAppSeleniumIdeHtmlFiles(html)));
            FEATURE_TEST_HTMLS.put(feature, htmls.toArray(new SomeWebAppSeleniumIdeHtmlFiles[0]));
        }
    }

    public static SomeWebAppSeleniumIdeHtmlFiles[] getIdeHtmlFilesFeatureOne() {
        return FEATURE_TEST_HTMLS.get(FEATURES[0]);
    }

    public static SomeWebAppSeleniumIdeHtmlFiles[] getIdeHtmlFilesFeatureTwo() {
        return FEATURE_TEST_HTMLS.get(FEATURES[1]);
    }

    @Override
    public String format() {
        return this.getTestCaseHtmlFile().getName();
    }

    public File getTestCaseHtmlFile() {
        return testCaseHtmlFile;
    }

    private final File testCaseHtmlFile;

    private SomeWebAppSeleniumIdeHtmlFiles(File testCaseHtmlFile) {
        this.testCaseHtmlFile = testCaseHtmlFile;
    }
}
