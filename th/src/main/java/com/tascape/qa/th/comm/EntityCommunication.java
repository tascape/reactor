package com.tascape.qa.th.comm;

import com.tascape.qa.th.test.AbstractTest;
import java.io.IOException;

/**
 *
 * @author linsong wang
 */
public abstract class EntityCommunication {

    private AbstractTest test;

    public abstract void connect() throws IOException;

    public abstract void disconnect() throws IOException;

    public void setTest(AbstractTest test) {
        this.test = test;
    }

    protected AbstractTest getTest() {
        return test;
    }
}
