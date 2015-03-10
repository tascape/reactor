package com.tascape.qa.th.data;

/**
 *
 * @author linsong wang
 */
public interface TestData {

    /**
     * The string representation of one piece of test data.
     *
     * @return for data display
     */
    String format();

    String getClassName();

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
