package com.tascape.qa.th.driver;

import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.Utils;
import com.tascape.qa.th.comm.EntityCommunication;
import com.tascape.qa.th.test.AbstractTest;
import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 * @param <T>
 */
public abstract class EntityDriver <T extends AbstractTest> {
    private static final Logger LOG = LoggerFactory.getLogger(EntityDriver.class);

    protected static final SystemConfiguration SYS_CONFIG = SystemConfiguration.getInstance();

    private EntityCommunication entityCommunication;

    private T test;

    public Path getLogPath() {
        if (this.test == null) {
            return Paths.get(System.getProperty("user.home"), "test");
        }
        return this.test.getTestLogPath();
    }

    public EntityCommunication getEntityCommunication() {
        return entityCommunication;
    }

    public void setEntityCommunication(EntityCommunication entityCommunication) {
        this.entityCommunication = entityCommunication;
    }

    public void setTest(T test) {
        this.test = test;
        if (this.entityCommunication != null) {
            this.entityCommunication.setDriver(this);
            this.entityCommunication.setTest(test);
        }
    }

    protected T getTest() {
        return test;
    }

    protected File createDataFile(String prefix) throws IOException {
        return this.createDataFile(prefix, "txt");
    }

    protected File createDataFile(String prefix, String extension) throws IOException {
        File f = File.createTempFile(prefix + "-", "." + extension, this.getLogPath().toFile());
        return f;
    }

    protected File createKeepAliveLogFile(String prefix, String extension) throws IOException {
        File f = File.createTempFile(prefix + "-", "." + extension, this.getLogPath().toFile());
        return Utils.getKeepAliveFile(f);
    }

    public abstract String getName();

    public abstract void reset() throws Exception;

    /**
     * @return png file
     */
    protected File captureScreen() {
        Path path = this.getLogPath();
        File png = path.resolve("screen-" + System.currentTimeMillis() + ".png").toFile();
        try {
            Utils.captureScreen(png);
        } catch (AWTException | IOException ex) {
            LOG.warn("Cannot take screenshot", ex);
        }
        return png;
    }
}
