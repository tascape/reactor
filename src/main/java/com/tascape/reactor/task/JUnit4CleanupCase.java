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
package com.tascape.reactor.task;

import com.tascape.reactor.Reactor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample case class for JUnit4 cases about cleanup.
 *
 * @author linsong wang
 */
@Priority(level = 2)
public class JUnit4CleanupCase extends AbstractCase {
    private static final Logger LOG = LoggerFactory.getLogger(JUnit4CleanupCase.class);

    public JUnit4CleanupCase() {
    }

    @Before
    public void setUp() throws Exception {
        LOG.debug("Run something before case");
        LOG.debug("Please override");
    }

    @After
    public void tearDown() throws Exception {
        LOG.debug("Run something after case");
        throw new Exception("after-task failure");
    }

    @Override
    public String getApplicationUnderTask() {
        LOG.debug("Please override");
        return Reactor.class.getName();
    }

    @Test
    public void runTask() throws Exception {
        LOG.info("Sample failure case cause by @After");
        LOG.info("@Test is OK");
    }
}
