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
package com.tascape.reactor.suite;

import com.tascape.reactor.task.JUnit4Case;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class JUnit4Suite extends AbstractSuite {
    private static final Logger LOG = LoggerFactory.getLogger(JUnit4Suite.class);

    @Override
    public void setUpCaseClasses() {
        this.addCaseClass(JUnit4Case.class);
    }

    @Override
    protected void setUpEnvironment() throws Exception {
        LOG.info("some suite setup here");
    }

    @Override
    protected String getEnvironmentName() {
        return "junit4";
    }

    @Override
    protected void tearDownEnvironment() {
    }

    @Override
    public String getProjectName() {
        return "JUnit4";
    }

    @Override
    public String getProductUnderTask() {
        return "JUnit4 Sample 1.1";
    }
}
