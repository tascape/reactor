/*
 * Copyright 2015 tascape.
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
package com.tascape.qa.th.suite;

import com.tascape.qa.th.ExecutionResult;
import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.TestHarness;
import com.tascape.qa.th.driver.EntityDriver;
import com.tascape.qa.th.driver.TestDriver;
import com.tascape.qa.th.driver.PoolableEntityDriver;
import com.tascape.qa.th.test.AbstractTest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final ThreadLocal<Map<String, Environment>> ENVIRONMENTS
        = new ThreadLocal<Map<String, Environment>>() {
        @Override
        protected Map<String, Environment> initialValue() {
            return new HashMap<>();
        }
    };

    public static void putEnvionment(String suiteClass, Environment env) {
        ENVIRONMENTS.get().put(suiteClass, env);
    }

    public static Environment getEnvionment(String suiteClass) {
        Environment env = ENVIRONMENTS.get().get(suiteClass);
        return env;
    }

    private static final List<AbstractSuite> SUITES = new ArrayList<>();

    private final List<Class<? extends AbstractTest>> testClasses = new ArrayList<>();

    private final Environment suiteEnvironment = new Environment();

    protected final SystemConfiguration SYSCONFIG = SystemConfiguration.getInstance();

    public static void addSuite(AbstractSuite suite) {
        SUITES.add(suite);
    }

    public static List<AbstractSuite> getSuites() {
        return SUITES;
    }

    protected ExecutionResult executionResult;

    public int getNumberOfEnvs() {
        return 0;
    }

    public void setUp() throws Exception {
        Environment env = AbstractSuite.getEnvionment(this.getClass().getName());
        env.setName(Thread.currentThread().getName());
        if (env.isEmpty()) {
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

    protected void putTestDirver(TestDriver testDriver, EntityDriver driver) {
        String key = testDriver.toString();
        LOG.debug("Putting runtime driver {}={} into suite test environment", key, driver);
        Class<? extends EntityDriver> clazz = testDriver.getDriverClass();
        if (clazz != null && !clazz.isInstance(driver)) {
            throw new RuntimeException("wrong driver type, " + key + " vs " + driver);
        }

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
        if (testClasses.contains(clazz)) {
            throw new UnsupportedOperationException("Adding same test class multiple times is not supported yet.");
        }
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

    public String getProjectName() {
        return "";
    }

    public abstract void setUpTestClasses();

    protected abstract void setUpEnvironment() throws Exception;

    /**
     * Gets the info of product-under-test. This method is called at the end of your test suite, before the suite
     * environment is torn down automatically.
     *
     * @return name and version of product
     */
    public abstract String getProductUnderTest();

    protected abstract void tearDownEnvironment();

    public void setExecutionResult(ExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    public ExecutionResult getExecutionResult() {
        return ExecutionResult.NA;
    }

    /**
     * This is used to launch TestHarness from within individual test suite classes.
     *
     * @param args command arguments
     *
     * @throws Exception any issue
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
