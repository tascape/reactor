package com.tascape.qa.th.driver;

import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.Utils;
import com.tascape.qa.th.test.AbstractTest;
import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class EntityDriver {

    private static final Logger LOG = LoggerFactory.getLogger(EntityDriver.class);

    protected Properties properties;

    private AbstractTest test;

    public static final Path ROOT_PATH = SystemConfiguration.getInstance().getRootPath();

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getProperty(String name) {
        return this.properties.getProperty(name);
    }

    public Path getLogDirectory() {
        if (this.test == null) {
            return Paths.get(System.getProperty("user.home"), "test");
        }
        return this.test.getLogDirectory();
    }

    public void setTest(AbstractTest test) {
        this.test = test;
    }

    protected AbstractTest getTest() {
        return test;
    }

    protected File createDataFile(String prefix) throws IOException {
        return this.createDataFile(prefix, "txt");
    }

    protected File createDataFile(String prefix, String extension) throws IOException {
        File f = File.createTempFile(prefix + "-", "." + extension, this.getLogDirectory().toFile());
        return f;
    }

    protected File createKeepAliveLogFile(String prefix, String extension) throws IOException {
        File f = File.createTempFile(prefix + "-", "." + extension, this.getLogDirectory().toFile());
        return Utils.getKeepAliveFile(f);
    }

    public abstract String getName();

    public abstract void reset() throws Exception;

    /**
     * @return png file
     */
    protected File captureScreen() {
        Path path = this.getLogDirectory();
        File png = path.resolve("screen-" + System.currentTimeMillis() + ".png").toFile();
        try {
            Utils.captureScreen(png);
        } catch (AWTException | IOException ex) {
            LOG.warn("Cannot take screenshot", ex);
        }
        return png;
    }
}
