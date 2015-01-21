package com.tascape.qa.th.suite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class AndroidUiAutomatorSuite extends AbstractSuite {
    private static final Logger LOG = LoggerFactory.getLogger(AndroidUiAutomatorSuite.class);

    @Override
    protected void setUpEnvironment() throws Exception {
    }

    @Override
    protected void tearDownEnvironment() {
    }
}
