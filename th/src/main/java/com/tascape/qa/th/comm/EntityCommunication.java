package com.tascape.qa.th.comm;

import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.driver.EntityDriver;
import com.tascape.qa.th.test.AbstractTest;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author linsong wang
 */
public abstract class EntityCommunication {

    protected static final SystemConfiguration SYSCONFIG = SystemConfiguration.getInstance();

    private EntityDriver driver;

    private AbstractTest test;

    public abstract void connect() throws Exception;

    public abstract void disconnect() throws Exception;

    public EntityDriver getDriver() {
        return driver;
    }

    public void setDriver(EntityDriver driver) {
        this.driver = driver;
    }

    public void setTest(AbstractTest test) {
        this.test = test;
    }

    public AbstractTest getTest() {
        return test;
    }

    public Path getLogPath() {
        if (this.test == null) {
            return Paths.get(System.getProperty("user.home"), "test");
        }
        return this.test.getTestLogPath();
    }
}
