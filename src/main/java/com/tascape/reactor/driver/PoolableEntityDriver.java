/*
 * Copyright 2015 - 2016 Nebula Bay.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tascape.reactor.driver;

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
