/*
 * Copyright (c) 2015 - present Nebula Bay.
 * All rights reserved.
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

import com.tascape.reactor.AbstractCaseResource;
import com.tascape.reactor.comm.EntityCommunication;
import com.tascape.reactor.task.AbstractCase;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class EntityDriver extends AbstractCaseResource {
    private static final Logger LOG = LoggerFactory.getLogger(EntityDriver.class);

    private AbstractCase kase;

    protected String version;

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public Path getLogPath() {
        if (this.kase == null) {
            return super.getLogPath();
        }
        return this.kase.getLogPath();
    }

    public void setCase(AbstractCase kase) {
        this.kase = kase;

        Class c = this.getClass();
        while (!c.equals(EntityDriver.class)) {
            LOG.trace("{} {}", this, c);
            Stream.of(c.getDeclaredFields())
                .filter(f -> EntityCommunication.class.isAssignableFrom(f.getType()))
                .forEach(f -> {
                    f.setAccessible(true);
                    try {
                        EntityCommunication ec = (EntityCommunication) f.get(this);
                        if (ec != null) {
                            ec.setDriver(this);
                            ec.setCase(kase);
                        } else {
                            LOG.trace("null for {}", f);
                        }
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        LOG.warn("", ex);
                    }
                });
            c = c.getSuperclass();
        }
    }

    protected AbstractCase getCase() {
        return kase;
    }

    /**
     * Name of the entity driver, max length 255 chars.
     *
     * @return name
     */
    public abstract String getName();

    public abstract void reset() throws Exception;
}
