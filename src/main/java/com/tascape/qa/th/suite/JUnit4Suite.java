package com.tascape.qa.th.suite;

import com.tascape.qa.th.test.JUnit4Test;

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
}
