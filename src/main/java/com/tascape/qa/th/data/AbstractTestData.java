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

import com.tascape.qa.th.test.Priority;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class AbstractTestData implements TestData {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTestData.class);

    private static final ThreadLocal<TestData> TEST_DATA = new ThreadLocal<>();

    public static void setTestData(TestData data) {
        TEST_DATA.set(data);
    }

    public static TestData getTestData() {
        return TEST_DATA.get();
    }

    private static final Map<String, TestData[]> LOADED_DATA = new HashMap<>();

    private static final Map<Class<? extends TestData>, Object> LOADED_PROVIDERS = new HashMap<>();

    private String value = null;

    /*
     * works together with Priority of test method. NONE means no data priority specified.
     */
    private int priority = Priority.NONE;

    @Override
    public String getClassName() {
        return "";
    }

    @Override
    public String getValue() {
        if (value == null) {
            LOG.warn("Value of test data is not specified.");
            return this.toString();
        }
        return this.value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public TestData setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public static TestData getTestData(String testDataInfo) throws Exception {
        TestDataInfo info = new TestDataInfo(testDataInfo);
        TestData[] data = getTestData(info.getKlass(), info.getMethod(), info.getParameter());
        int index = info.getIndex();
        if (data.length <= index) {
            throw new Exception("Cannot find test data using " + testDataInfo);
        }
        return data[index];
    }

    public static synchronized TestData[] getTestData(Class<? extends TestData> klass, String method, String parameter)
        throws Exception {
        String key = klass + "." + method + "." + parameter;
        TestData[] data = AbstractTestData.LOADED_DATA.get(key);
        if (data == null) {
            Object provider = AbstractTestData.LOADED_PROVIDERS.get(klass);
            if (provider == null) {
                provider = klass.newInstance();
                AbstractTestData.LOADED_PROVIDERS.put(klass, provider);
            }

            if (parameter == null || parameter.isEmpty()) {
                Method m = klass.getDeclaredMethod(method, (Class<?>[]) null);
                data = (TestData[]) m.invoke(provider, (Object[]) null);
            } else {
                Method m = klass.getDeclaredMethod(method, new Class<?>[]{parameter.getClass()});
                data = (TestData[]) m.invoke(provider, new Object[]{parameter});
            }
            AbstractTestData.LOADED_DATA.put(key, data);
        }
        return data;
    }

    /**
     * The default external id is empty string. Please override this method to provide external id for your test data.
     *
     * @return empty string
     */
    @Override
    public String getExternalId() {
        return "";
    }
}
