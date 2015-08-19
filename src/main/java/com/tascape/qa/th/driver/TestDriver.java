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
    public static final String ONLY_INSTANCE = "ONLY_DRIVER_INSTANCE_IN_TEST";

    private final Class<? extends AbstractTest> testClass;

    private final Class<? extends EntityDriver> driverClass;

    private final String name;

    @Deprecated
    public TestDriver(Class<? extends AbstractTest> testClazz) {
        this(testClazz, ONLY_INSTANCE);
    }

    @Deprecated
    public TestDriver(Class<? extends AbstractTest> testClazz, String name) {
        this(testClazz, null, name);
    }

    public TestDriver(Class<? extends AbstractTest> testClazz, Class<? extends EntityDriver> driverClazz) {
        this(testClazz, driverClazz, ONLY_INSTANCE);
    }

    public TestDriver(Class<? extends AbstractTest> testClazz, Class<? extends EntityDriver> driverClazz, String name) {
        this.testClass = testClazz;
        this.driverClass = driverClazz;
        this.name = name;
    }

    @Override
    public String toString() {
        return testClass + "+" + driverClass + "+" + name;
    }

    public Class<? extends AbstractTest> getTestClass() {
        return testClass;
    }

    public Class<? extends EntityDriver> getDriverClass() {
        return driverClass;
    }

    public String getName() {
        return name;
    }
}
