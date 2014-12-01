package com.tascape.qa.th;

import com.tascape.qa.th.data.AbstractTestData;
import com.tascape.qa.th.data.TestData;
import com.tascape.qa.th.data.TestDataInfo;
import com.tascape.qa.th.data.TestDataProvider;
import com.tascape.qa.th.db.TestCase;
import com.tascape.qa.th.suite.AbstractSuite;
import com.tascape.qa.th.test.AbstractTest;
import com.tascape.qa.th.test.Priority;
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
public class TestSuite {
    private static final Logger LOG = LoggerFactory.getLogger(TestSuite.class);

    private List<TestCase> tests = new ArrayList<>();

    public TestSuite(String suiteClass, Pattern testClassRegex, Pattern testMethodRegex, int priority)
        throws Exception {
        LOG.info("Find test cases in target test suite");
        AbstractSuite suite = AbstractSuite.class.cast(Class.forName(suiteClass).newInstance());
        suite.setUpTestClasses();
        for (Class<? extends AbstractTest> clazz : suite.getTestClasses()) {
            for (Method method : this.getTestMethods(clazz)) {
                TestCase tc = new TestCase();
                tc.setSuiteClass(suiteClass);
                tc.setTestClass(clazz.getName());
                tc.setTestMethod(method.getName());
                this.tests.add(tc);
            }
        }

        this.tests = this.processTestAnnotations();

        this.tests = this.filter(testClassRegex, testMethodRegex);

        this.tests = this.filter(priority);
    }

    public List<TestCase> getTests() {
        return tests;
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
                    LOG.trace("Calling class {}, method {}, with parameters {}", tdp.klass(), tdp.method(),
                        tdp.parameter());
                    TestData[] data = AbstractTestData.getTestData(tdp.klass(), tdp.method(), tdp.parameter());
                    LOG.debug("{} is a data-driven test case, test data size is {}", tc, data.length);
                    int length = (data.length + "").length();
                    for (int i = 0; i < data.length; i++) {
                        TestCase t = new TestCase(tc);
                        TestDataInfo tdi = new TestDataInfo(tdp.klass(), tdp.method(), tdp.parameter(), i);
                        t.setTestDataInfo(tdi.format(length));
                        t.setTestData(data[i].format());
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

    private List<Method> getTestMethods(Class<? extends AbstractTest> testClass) {
        List<Method> methods = new ArrayList<>();
        for (Method m : testClass.getDeclaredMethods()) {
            if (m.getAnnotation(Test.class) != null) {
                methods.add(m);
            }
        }
        return methods;
    }
}
