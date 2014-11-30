package com.tascape.qa.th.data;

import com.tascape.qa.th.test.Priority;
import java.lang.reflect.Method;
import java.util.Random;

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

    private final Random random = new Random();

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
        return value == null ? this.format() : value;
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

    public static TestData[] getTestData(Class<? extends TestData> klass, String method, String parameter)
        throws Exception {
        Method m;
        if (parameter == null || parameter.isEmpty()) {
            m = klass.getDeclaredMethod(method, (Class<?>[]) null);
            return (TestData[]) m.invoke(null, (Object[]) null);
        } else {
            m = klass.getDeclaredMethod(method, new Class<?>[]{parameter.getClass()});
            return (TestData[]) m.invoke(null, new Object[]{parameter});
        }
    }

    public int getRandomInt() {
        return random.nextInt(1000000);
    }
}
