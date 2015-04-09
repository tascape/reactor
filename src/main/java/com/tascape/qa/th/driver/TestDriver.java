package com.tascape.qa.th.driver;

import com.tascape.qa.th.test.AbstractTest;

/**
 *
 * @author linsong wang
 */
public class TestDriver {

    private final Class<? extends AbstractTest> testClass;

    private final String name;

    public TestDriver(Class<? extends AbstractTest> testClazz, String name) {
        this.testClass = testClazz;
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getTestClass().getName() + "." + name;
    }

    public Class<? extends AbstractTest> getTestClass() {
        return this.testClass;
    }

    public String getName() {
        return name;
    }
}
