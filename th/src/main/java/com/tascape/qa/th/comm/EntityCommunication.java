package com.tascape.qa.th.comm;

import com.tascape.qa.th.test.AbstractTest;

/**
 *
 * @author linsong wang
 */
public abstract class EntityCommunication {

    private AbstractTest test;

    public abstract void connect() throws Exception;

    public abstract void disconnect() throws Exception;

    public void setTest(AbstractTest test) {
        this.test = test;
    }

    protected AbstractTest getTest() {
        return test;
    }
}
