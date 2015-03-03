package com.tascape.qa.th.suite;

import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.TestHarness;
import com.tascape.qa.th.driver.EntityDriver;
import com.tascape.qa.th.driver.PoolableEntityDriver;
import com.tascape.qa.th.test.AbstractTest;
import java.lang.reflect.Field;
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

    protected final SystemConfiguration SYSCONFIG = SystemConfiguration.getInstance();

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

    protected <T extends AbstractTest> void putDirver(Class<T> testClazz, String name, EntityDriver driver) {
        String key = testClazz.getName() + "." + name;
        LOG.debug("Putting runtime driver {}={} into suite test environment", key, driver);
        EntityDriver d = this.suiteEnvironment.get(key);
        if (d == null) {
            this.suiteEnvironment.put(key, driver);
            return;
        }
        if (driver.equals(d)) {
            LOG.warn("Tried to add the same driver again: {}={}", key, driver);
            return;
        }
        if (d instanceof PoolableEntityDriver && driver instanceof PoolableEntityDriver) {
            PoolableEntityDriver.class.cast(d).next(PoolableEntityDriver.class.cast(driver));
            return;
        }
        throw new UnsupportedOperationException("Cannot add non-poolable driver with the same key " + key);
    }

    protected <T extends AbstractTest> void addTestClass(Class<T> clazz) {
        this.testClasses.add(clazz);
    }

    protected String getSuiteProperty(String name, String defValue) {
        String value = this.SYSCONFIG.getProperty(name);
        if (value == null) {
            value = defValue;
        }
        return value;
    }

    public String getName() {
        return this.getClass().getName();
    }

    public abstract void setUpTestClasses();

    protected abstract void setUpEnvironment() throws Exception;

    protected abstract void tearDownEnvironment();

    /**
     * This is used to launch TestHarness from within individual test suite classes.
     *
     * @param args
     *
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        SystemConfiguration sysConfig = SystemConfiguration.getInstance();
        Field fClasses = ClassLoader.class.getDeclaredField("classes");
        ClassLoader cl = AbstractSuite.class.getClassLoader();
        fClasses.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Class<?>> classes = (List<Class<?>>) fClasses.get(cl);
        String suiteClassName = "";
        String stop = AbstractSuite.class.getName() + "$1";
        for (Class<?> c : classes) {
            String className = c.getName();
            if (className.equals(stop)) {
                break;
            }
            suiteClassName = className;
        }
        sysConfig.setTestSuite(suiteClassName);
        TestHarness.main(args);
    }
}
