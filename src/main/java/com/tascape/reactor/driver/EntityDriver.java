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

import com.tascape.reactor.AbstractTestResource;
import com.tascape.reactor.comm.EntityCommunication;
import com.tascape.reactor.test.AbstractTest;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class EntityDriver extends AbstractTestResource {
    private static final Logger LOG = LoggerFactory.getLogger(EntityDriver.class);

    private EntityCommunication entityCommunication;

    private AbstractTest test;

    @Override
    public Path getLogPath() {
        if (this.test == null) {
            return super.getLogPath();
        }
        return this.test.getLogPath();
    }

    public EntityCommunication getEntityCommunication() {
        return entityCommunication;
    }

    public void setEntityCommunication(EntityCommunication entityCommunication) {
        this.entityCommunication = entityCommunication;
    }

    public void setTest(AbstractTest test) {
        this.test = test;
        if (this.entityCommunication != null) {
            this.entityCommunication.setDriver(this);
            this.entityCommunication.setTest(test);
        }
    }

    protected AbstractTest getTest() {
        return test;
    }

    /**
     * Name of the entity driver, max length 255 chars.
     *
     * @return name
     */
    public abstract String getName();

    public abstract String getVersion();

    public abstract void reset() throws Exception;
}
