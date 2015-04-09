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
     * Sets value for updating test method name. The final test method name will look like testMethod(value).
     *
     * @param value the value for display
     */
    void setValue(String value);

    int getPriority();

    TestData setPriority(int priority);
}
