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
package com.tascape.qa.sample;

import com.tascape.qa.junit4.AbstractSuite;

/**
 *
 * @author linsong wang
 */
public class JUnit4Suite extends AbstractSuite {

    @Override
    public void setUpTestClasses() {
        this.addTestClass(JUnit4Test.class);
    }

    @Override
    protected void setUpEnvironment() throws Exception {
    }

    @Override
    protected void tearDownEnvironment() {
    }

    @Override
    public String getProductUnderTest() {
        return "JUnit4 Sample 1.1";
    }
}
