package com.tascape.qa.th.driver;

import java.util.Properties;

/**
 *
 * @author linsong wang
 */
public abstract class PoolableEntityDriver extends EntityDriver {

    private PoolableEntityDriver driver;

    private boolean bIdle = true;

    public PoolableEntityDriver next() {
        return this.driver;
    }

    public void next(PoolableEntityDriver entityDriver) {
        this.driver = entityDriver;
    }

    public PoolableEntityDriver next(Properties properties) {
        PoolableEntityDriver d = this;
        while (d != null) {
            if (d.matches(properties) && d.idle()) {
                return d;
            } else {
                d = d.next();
            }
        }
        return null;
    }

    public boolean idle() {
        return this.bIdle;
    }

    public void idle(boolean idle) {
        this.bIdle = idle;
    }

    public abstract boolean matches(Properties properties);
}
