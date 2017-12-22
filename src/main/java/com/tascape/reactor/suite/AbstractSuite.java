/*
 * Copyright (c) 2015 - present Nebula Bay.
 * All rights reserved.
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
package com.tascape.reactor.suite;

import com.tascape.reactor.ExecutionResult;
import com.tascape.reactor.SystemConfiguration;
import com.tascape.reactor.Reactor;
import com.tascape.reactor.driver.EntityDriver;
import com.tascape.reactor.driver.CaseDriver;
import com.tascape.reactor.driver.PoolableEntityDriver;
import com.tascape.reactor.task.AbstractCase;
import com.tascape.reactor.task.Priority;
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

    private final List<Class<? extends AbstractCase>> caseClasses = new ArrayList<>();

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

    /**
     * Gets the minimal priority of cases to run in this suite.
     *
     * @return Priority.P3
     */
    public int getPriority() {
        LOG.warn("Please override to return the minimal priority, of which you would like to run cases in this suite. "
            + "The default is Priority.P3");
        return Priority.P3;
    }

    /**
     * This method provides suite a change to fail fast if setUpEnvironment() fails. Please override this method if
     * fail fast is needed - swallow the Throwable and mark some driver and/or communication objects as null.
     *
     * @param t caused by setUpEnvironment
     *
     * @throws java.lang.Exception environment setup issue
     */
    public void runFailFast(Throwable t) throws Exception {
        LOG.warn("there is no fail fast operations in suite, please override, and return true, if you want");
    }

    public void setUp() throws Exception {
        Environment env = AbstractSuite.getEnvionment(this.getClass().getName());
        if (env == null || env.isEmpty()) {
            try {
                this.setUpEnvironment();
            } catch (Throwable t) {
                this.runFailFast(t);
            }
            AbstractSuite.putEnvionment(this.getClass().getName(), this.suiteEnvironment);
            this.suiteEnvironment.setName(Thread.currentThread().getName() + " " + this.getEnvironmentName());
        }
    }

    public void runByClass() throws Exception {
        this.caseClasses.forEach((clazz) -> {
            JUnitCore core = new JUnitCore();
            core.run(Request.classWithoutSuiteMethod(clazz));
        });
    }

    public void tearDown() {
        try {
            this.tearDownEnvironment();
        } catch (Throwable t) {
            LOG.warn("Cannot tearing down properly", t);
        }
    }

    public List<Class<? extends AbstractCase>> getCaseClasses() {
        return caseClasses;
    }

    protected void putCaseDirver(CaseDriver caseDriver, EntityDriver driver) {
        String key = caseDriver.toString();
        LOG.debug("Putting runtime driver {}={} into suite environment", key, driver);
        Class<? extends EntityDriver> clazz = caseDriver.getDriverClass();
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

    protected <T extends AbstractCase> void addCaseClass(Class<T> clazz) {
        if (caseClasses.contains(clazz)) {
            throw new UnsupportedOperationException("Adding same case class multiple times is not supported yet.");
        }
        this.caseClasses.add(clazz);
    }

    protected <T extends AbstractCase> void addCaseClass(String clazz) throws ClassNotFoundException {
        Class c = AbstractSuite.class.getClassLoader().loadClass(clazz);
        if (caseClasses.contains(c)) {
            throw new UnsupportedOperationException("Adding same case class multiple times is not supported yet.");
        }
        this.caseClasses.add(c);
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

    public abstract String getProjectName();

    public abstract void setUpCaseClasses();

    protected abstract void setUpEnvironment() throws Exception;

    protected String getEnvironmentName() {
        return "";
    }

    /**
     * Gets the info of product-under-task. This method is called at the end of your suite, before the suite
     * environment is torn down automatically.
     *
     * @return name and version of product
     */
    public abstract String getProductUnderTask();

    protected abstract void tearDownEnvironment();

    public void setExecutionResult(ExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    public ExecutionResult getExecutionResult() {
        return ExecutionResult.NA;
    }

    /**
     * This is used to launch Reactor from within individual suite classes.
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
        sysConfig.setSuite(suiteClassName);
        Reactor.main(args);
    }
}
