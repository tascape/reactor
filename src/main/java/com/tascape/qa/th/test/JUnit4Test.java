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
package com.tascape.qa.th.test;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 * Base test class for JUnit4 test cases.
 *
 * @author linsong wang
 */
@Priority(level = 2)
public class JUnit4Test extends AbstractTest {
    private static final Logger LOG = LoggerFactory.getLogger(JUnit4Test.class);

    public JUnit4Test() {
    }

    @Before
    public void setUp() throws Exception {
        LOG.debug("Please override");
    }

    @After
    public void tearDown() throws Exception {
        LOG.debug("Please override");
    }

    @Override
    public String getApplicationUnderTest() {
        LOG.debug("Please override");
        return "NA";
    }

    @Test
    @Priority(level = 0)
    public void testPositive() throws Exception {
        LOG.info("Sample positive test");
        this.doSomethingGood();
    }

    @Test
    @Priority(level = 0)
    public void testNegative() throws Exception {
        LOG.info("Sample negative test");
        expectedException.expect(Exception.class);
        expectedException.expectMessage("something bad");
        this.doSomethingBad();
    }

    @Test
    @Priority(level = 1)
    public void testNegativeAgain() throws Exception {
        LOG.info("Sample negative test again");
        expectedException.expect(Exception.class);
        expectedException.expectMessage("something bad again");
        this.doSomethingBadAgain();
    }

    private void doSomethingGood() throws IOException {
        assertTrue(true);
    }

    private void doSomethingBad() throws Exception {
        throw new Exception("something bad");
    }

    private void doSomethingBadAgain() throws Exception {
        throw new Exception("something bad again");
    }
}
