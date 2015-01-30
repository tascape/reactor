package com.tascape.qa.th.comm;

import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.driver.EntityDriver;
import com.tascape.qa.th.test.AbstractTest;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author linsong wang
 * @param <T>
 * @param <D>
 */
public abstract class EntityCommunication<T extends AbstractTest, D extends EntityDriver> {

    protected static final SystemConfiguration SYSCONFIG = SystemConfiguration.getInstance();

    private D driver;

    private T test;

    protected static final SystemConfiguration SYS_CONFIG = SystemConfiguration.getInstance();

    public abstract void connect() throws Exception;

    public abstract void disconnect() throws Exception;

    public D getDriver() {
        return driver;
    }

    public void setDriver(D driver) {
        this.driver = driver;
    }

    public void setTest(T test) {
        this.test = test;
    }

    public T getTest() {
        return test;
    }

    public Path getLogPath() {
        if (this.test == null) {
            return Paths.get(System.getProperty("user.home"), "test");
        }
        return this.test.getTestLogPath();
    }
}
