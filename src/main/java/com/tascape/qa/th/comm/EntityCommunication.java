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
package com.tascape.qa.th.comm;

import com.tascape.qa.th.AbstractTestResource;
import com.tascape.qa.th.driver.EntityDriver;
import com.tascape.qa.th.test.AbstractTest;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class EntityCommunication extends AbstractTestResource {
    private static final Logger LOG = LoggerFactory.getLogger(EntityCommunication.class);

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

    @Override
    public Path getLogPath() {
        if (this.test == null) {
            return super.getLogPath();
        }
        return this.test.getLogPath();
    }
}
