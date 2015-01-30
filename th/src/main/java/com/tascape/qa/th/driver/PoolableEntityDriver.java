/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tascape.qa.th.driver;

import com.tascape.qa.th.test.AbstractTest;
import java.util.Properties;

/**
 *
 * @author wlinsong
 * @param <T>
 * @param <D>
 */
public abstract class PoolableEntityDriver<T extends AbstractTest, D extends PoolableEntityDriver>
        extends EntityDriver<T> {

    private D driver;

    private boolean bIdle = true;

    public D next() {
        return this.driver;
    }

    public void next(D entityDriver) {
        this.driver = entityDriver;
    }

    public D next(Properties properties) {
        PoolableEntityDriver d = this;
        while (d != null) {
            if (d.matches(properties) && d.idle()) {
                return (D) d;
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
