/*
 * Copyright (c) 2021 - present Nebula Bay.
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
package com.tascape.reactor.suite;

import com.tascape.reactor.Reactor;
import com.tascape.reactor.task.ReactorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The framework-related suite.
 *
 * @author linsong wang
 */
public class ReactorSuite extends AbstractSuite {
    private static final Logger LOG = LoggerFactory.getLogger(ReactorSuite.class);

    @Override
    public void setUpCaseClasses() {
        super.addCaseClass(ReactorTask.class);
    }

    @Override
    protected void setUpEnvironment() throws Exception {
    }

    @Override
    protected String getEnvironmentName() {
        return "reactor";
    }

    @Override
    public String getProjectName() {
        return "Reactor";
    }

    @Override
    public String getProductUnderTask() {
        return Reactor.class.getName();
    }

    @Override
    protected void tearDownEnvironment() {
    }
}