package com.tascape.qa.th.suite;

import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.driver.EntityDriver;
import com.tascape.qa.th.test.AbstractTest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class AbstractSuite {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSuite.class);

    private static final ThreadLocal<Map<String, Map<String, EntityDriver>>> ENVIRONMENTS
            = new ThreadLocal<Map<String, Map<String, EntityDriver>>>() {
                @Override
                protected Map<String, Map<String, EntityDriver>> initialValue() {
                    return new HashMap<>();
                }
            };

    public static void putEnvionment(String suiteClass, Map<String, EntityDriver> drivers) {
        ENVIRONMENTS.get().put(suiteClass, drivers);
    }

    public static Map<String, EntityDriver> getEnvionment(String suiteClass) {
        Map<String, EntityDriver> drivers = ENVIRONMENTS.get().get(suiteClass);
        return drivers;
    }

    private static final Set<AbstractSuite> SUITES = new HashSet<>();

    private final List<Class<? extends AbstractTest>> testClasses = new ArrayList<>();

    protected Map<String, EntityDriver> suiteEnvironment = new HashMap<>();

    private final SystemConfiguration config = SystemConfiguration.getInstance();

    public static void addSuite(AbstractSuite suite) {
        SUITES.add(suite);
    }

    public static Set<AbstractSuite> getSuites() {
        return SUITES;
    }

    public void setUp() throws Exception {
        Map<String, EntityDriver> env = AbstractSuite.getEnvionment(this.getClass().getName());
        if (env == null || env.isEmpty()) {
            this.setUpEnvironment();
            AbstractSuite.putEnvionment(this.getClass().getName(), this.suiteEnvironment);
        }
    }

    public void runByClass() throws Exception {
        for (Class<? extends AbstractTest> clazz : this.testClasses) {
            JUnitCore core = new JUnitCore();
            core.run(Request.classWithoutSuiteMethod(clazz));
        }
    }

    public void tearDown() throws Exception {
        this.tearDownEnvironment();
    }

    public List<Class<? extends AbstractTest>> getTestClasses() {
        return testClasses;
    }

    protected <T extends EntityDriver> void putDirver(Class<? extends AbstractTest> testClazz, String name, T driver) {
        String key = testClazz.getName() + "." + name;
        LOG.debug("Putting runtime driver {}={} into suite test environment", key, driver);
        this.suiteEnvironment.put(key, driver);
    }

    protected void addTestClass(Class<? extends AbstractTest> clazz) {
        this.testClasses.add(clazz);
    }

    protected String getSuiteProperty(String name, String defValue) {
        String value = this.config.getProperty(name);
        if (value == null) {
            value = defValue;
        }
        return value;
    }

    public abstract String getName();

    public abstract void setUpTestClasses();

    protected abstract void setUpEnvironment() throws Exception;

    protected abstract void tearDownEnvironment();
}
