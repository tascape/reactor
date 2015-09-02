/*
 * Copyright 2015.
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
package com.tascape.qa.th.driver;

import com.tascape.qa.th.AbstractTestResource;
import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.Utils;
import com.tascape.qa.th.comm.EntityCommunication;
import com.tascape.qa.th.test.AbstractTest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            return Paths.get(System.getProperty("user.home"), "test");
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

    protected File createDataFile(String prefix) throws IOException {
        return this.createDataFile(prefix, "txt");
    }

    protected File createDataFile(String prefix, String extension) throws IOException {
        File f = File.createTempFile(prefix + "-", "." + extension, this.getLogPath().toFile());
        return f;
    }

    protected File createKeepAliveLogFile(String prefix, String extension) throws IOException {
        File f = File.createTempFile(prefix + "-", "." + extension, this.getLogPath().toFile());
        return Utils.getKeepAliveFile(f);
    }

    /**
     * Name of the entity driver, max length 200 chars.
     *
     * @return name
     */
    public abstract String getName();

    public abstract void reset() throws Exception;
}
