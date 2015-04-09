package com.tascape.qa.th.data;

import com.tascape.qa.th.test.Priority;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author linsong wang
 */
public abstract class AbstractTestData implements TestData {
    private static final ThreadLocal<TestData> TEST_DATA = new ThreadLocal<>();

    public static void setTestData(TestData data) {
        TEST_DATA.set(data);
    }

    public static TestData getTestData() {
        return TEST_DATA.get();
    }

    private static final Map<String, TestData[]> loadedData = new HashMap<>();

    private static final Map<Class<? extends TestData>, Object> loadedProviders = new HashMap<>();

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
        TestData[] data = AbstractTestData.loadedData.get(key);
        if (data == null) {
            Object provider = AbstractTestData.loadedProviders.get(klass);
            if (provider == null) {
                provider = klass.newInstance();
                AbstractTestData.loadedProviders.put(klass, provider);
            }

            if (parameter == null || parameter.isEmpty()) {
                Method m = klass.getDeclaredMethod(method, (Class<?>[]) null);
                data = (TestData[]) m.invoke(provider, (Object[]) null);
            } else {
                Method m = klass.getDeclaredMethod(method, new Class<?>[]{parameter.getClass()});
                data = (TestData[]) m.invoke(provider, new Object[]{parameter});
            }
            AbstractTestData.loadedData.put(key, data);
        }
        return data;
    }
}
