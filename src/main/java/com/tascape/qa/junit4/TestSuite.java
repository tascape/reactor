/*
 * Copyright 2015.
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
package com.tascape.qa.junit4;

import com.tascape.qa.th.test.Priority;
import com.tascape.qa.th.AbstractTestSuite;
import com.tascape.qa.th.data.AbstractTestData;
import com.tascape.qa.th.data.TestData;
import com.tascape.qa.th.data.TestDataInfo;
import com.tascape.qa.th.data.TestDataProvider;
import com.tascape.qa.th.db.TestCase;
import com.tascape.qa.th.suite.AbstractSuite;
import com.tascape.qa.th.test.AbstractTest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class TestSuite implements AbstractTestSuite {
    private static final Logger LOG = LoggerFactory.getLogger(TestSuite.class);

    private String name;

    private List<TestCase> tests = new ArrayList<>();

    public TestSuite(String suiteClass, Pattern testClassRegex, Pattern testMethodRegex, int priority)
        throws Exception {
        LOG.info("Find test cases in target test suite {}", suiteClass);
        AbstractSuite suite = AbstractSuite.class.cast(Class.forName(suiteClass).newInstance());
        this.name = suite.getName();
        if (this.name == null || this.name.isEmpty()) {
            this.name = suiteClass;
        }
        suite.setUpTestClasses();
        for (Class<? extends AbstractTest> clazz : suite.getTestClasses()) {
            this.getTestMethods(clazz).stream()
                .map(method -> {
                    TestCase tc = new TestCase();
                    tc.setSuiteClass(suiteClass);
                    tc.setTestClass(clazz.getName());
                    tc.setTestMethod(method.getName());
                    return tc;
                }).forEach(tc -> {
                    this.tests.add(tc);
                });
        }

        this.tests = this.processTestAnnotations();

        this.tests = this.filter(testClassRegex, testMethodRegex);

        this.tests = this.filter(priority);
    }

    @Override
    public List<TestCase> getTests() {
        return tests;
    }

    @Override
    public String getName() {
        return name;
    }

    private List<TestCase> filter(Pattern testClassRegex, Pattern testMethodRegex) {
        LOG.info("Use debug class  name fileter {}", testClassRegex);
        LOG.info("Use debug method name fileter {}", testMethodRegex);
        List<TestCase> tcs = new ArrayList<>();
        this.tests.stream().forEach((tc) -> {
            Matcher mc = testClassRegex.matcher(tc.getTestClass());
            Matcher mm = testMethodRegex.matcher(tc.getTestMethod());
            if (mc.find() && mm.find()) {
                tcs.add(tc);
            }
        });
        return tcs;
    }

    private List<TestCase> filter(int priority) {
        List<TestCase> tcs = new ArrayList<>();
        this.tests.stream().filter((tc) -> !(tc.getPriority() > priority)).forEach((tc) -> {
            tcs.add(tc);
        });
        return tcs;
    }

    private List<TestCase> processTestAnnotations() {
        LOG.debug("Checking method annotation TestDataProvider for each test case");
        List<TestCase> tcs = new ArrayList<>();

        this.tests.stream().forEach((tc) -> {
            try {
                Class<?> testClass = Class.forName(tc.getTestClass());
                Method testMethod = testClass.getDeclaredMethod(tc.getTestMethod());

                Priority p = testMethod.getAnnotation(Priority.class);
                if (p != null) {
                    tc.setPriority(p.level());
                }

                TestDataProvider tdp = testMethod.getAnnotation(TestDataProvider.class);
                if (tdp == null) {
                    LOG.debug("Adding test case {}", tc.format());
                    tcs.add(tc);
                } else {
                    LOG.trace("Calling class {}, method {}, with parameter {}", tdp.klass(), tdp.method(),
                        tdp.parameter());
                    TestData[] data = AbstractTestData.getTestData(tdp.klass(), tdp.method(), tdp.parameter());
                    LOG.debug("{} is a data-driven test case, test data size is {}", tc.format(), data.length);
                    int length = (data.length + "").length();
                    for (int i = 0; i < data.length; i++) {
                        TestCase t = new TestCase(tc);
                        TestDataInfo tdi = new TestDataInfo(tdp.klass(), tdp.method(), tdp.parameter(), i);
                        t.setTestDataInfo(tdi.format(length));
                        t.setTestData(data[i].getValue());
                        t.setPriority(Math.min(t.getPriority(), data[i].getPriority()));

                        LOG.debug("Adding test case {}", t.format());
                        tcs.add(t);
                    }
                }
            } catch (Exception ex) {
                LOG.warn("Cannot process test case {}, skipping. Check @TestDataProvider", tc.format(), ex);
            }
        });
        return tcs;
    }

    private <T extends AbstractTest> List<Method> getTestMethods(Class<T> testClass) {
        List<Method> methods = new ArrayList<>();
        for (Method m : testClass.getDeclaredMethods()) {
            if (m.getAnnotation(Test.class) != null) {
                methods.add(m);
            }
        }
        return methods;
    }
}
