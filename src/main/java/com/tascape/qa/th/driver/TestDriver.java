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

import com.tascape.qa.th.test.AbstractTest;

/**
 *
 * @author linsong wang
 */
public class TestDriver {

    private final Class<? extends AbstractTest> testClass;

    private final Class<? extends EntityDriver> driverClass;

    public TestDriver(Class<? extends AbstractTest> testClazz, Class<? extends EntityDriver> driverClazz) {
        this.testClass = testClazz;
        this.driverClass = driverClazz;
    }

    @Override
    public String toString() {
        return testClass.getName() + "+" + driverClass.getName() + "+" + this.hashCode();
    }

    public Class<? extends AbstractTest> getTestClass() {
        return testClass;
    }

    public Class<? extends EntityDriver> getDriverClass() {
        return driverClass;
    }
}
