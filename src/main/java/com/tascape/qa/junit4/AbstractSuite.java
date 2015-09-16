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
package com.tascape.qa.junit4;

import com.tascape.qa.th.test.AbstractTest;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;

/**
 *
 * @author linsong wang
 */
public abstract class AbstractSuite extends com.tascape.qa.th.suite.AbstractSuite {

    @Override
    public void runByClass() throws Exception {
        for (Class<? extends AbstractTest> clazz : this.testClasses) {
            JUnitCore core = new JUnitCore();
            core.run(Request.classWithoutSuiteMethod(clazz));
        }
    }
    
}
