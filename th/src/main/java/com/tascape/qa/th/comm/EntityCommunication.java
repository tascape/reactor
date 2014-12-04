package com.tascape.qa.th.comm;

import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.driver.EntityDriver;
import com.tascape.qa.th.test.AbstractTest;

/**
 *
 * @author linsong wang
 */
public abstract class EntityCommunication {

    protected static final SystemConfiguration SYSCONFIG = SystemConfiguration.getInstance();

    private EntityDriver driver;

    private AbstractTest test;

    protected final SystemConfiguration SYS_CONFIG = SystemConfiguration.getInstance();

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

    protected AbstractTest getTest() {
        return test;
    }
}
