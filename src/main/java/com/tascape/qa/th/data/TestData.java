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
package com.tascape.qa.th.data;

/**
 *
 * @author linsong wang
 */
public interface TestData {

    String getClassName();

    /**
     * The string representation of one piece of test data.
     *
     * @return value
     */
    String getValue();

    /**
     * For data-driven test cases, the test case external id is defined in corresponding test data. Please implement
     * this method to provide external id based on the test case management system, such as TestRail, that is used for
     * result reporting.
     *
     * @return value
     */
    String getExternalId();

    /**
     * Sets value for updating test method name. The final test method name will look like testMethod(value).
     *
     * @param value the value for display
     */
    void setValue(String value);

    int getPriority();

    TestData setPriority(int priority);
}
