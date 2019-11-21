/*
 * Copyright 2019 linsong.
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
package com.tascape.reactor.tools;

import com.tascape.reactor.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author linsong
 */
public class NativeTests {

    public NativeTests() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testWaitForOutputLine() throws Exception {
        Process p = Utils.cmdAsync(new String[]{"ping", "google.com"});
        boolean result = Utils.waitForOutputLine(p, "icmp_seq=10", 30);
        assertTrue(result);
    }

    @Test
    public void testWaitForOutput() throws Exception {
        Process p = Utils.cmdAsync(new String[]{"ping", "google.com"});
        Utils.waitForOutput(p, 11);
    }
}
