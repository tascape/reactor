/*
 * Copyright 2015 - 2016 Nebula Bay.
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
package com.tascape.reactor;

import com.tascape.reactor.data.AbstractCaseData;
import com.tascape.reactor.data.CaseDataInfo;
import com.tascape.reactor.db.TaskCase;
import com.tascape.reactor.suite.AbstractSuite;
import com.tascape.reactor.task.AbstractCase;
import com.tascape.reactor.task.Priority;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tascape.reactor.data.CaseData;
import com.tascape.reactor.data.CaseDataProvider;

/**
 *
 * @author linsong wang
 */
public class TaskSuite {
    private static final Logger LOG = LoggerFactory.getLogger(TaskSuite.class);

    private String name;

    private final String projectName;
    
    private final int numberOfEnvs;

    private List<TaskCase> tests = new ArrayList<>();

    public TaskSuite(String suiteClass, Pattern testClassRegex, Pattern testMethodRegex, int priority)
        throws Exception {
        LOG.debug("Find test cases in target test suite {}", suiteClass);
        AbstractSuite suite = AbstractSuite.class.cast(Class.forName(suiteClass).newInstance());
        this.name = suite.getName();
        this.projectName = suite.getProjectName();
        this.numberOfEnvs = suite.getNumberOfEnvs();
        if (this.name == null || this.name.isEmpty()) {
            this.name = suiteClass;
        }
        suite.setUpTestClasses();
        for (Class<? extends AbstractCase> clazz : suite.getCaseClasses()) {
            for (Method method : this.getTestMethods(clazz)) {
                TaskCase tc = new TaskCase();
                tc.setSuiteClass(suiteClass);
                tc.setCaseClass(clazz.getName());
                tc.setCaseMethod(method.getName());
                this.tests.add(tc);
            }
        }

        this.tests = this.processTestAnnotations();

        this.tests = this.filter(testClassRegex, testMethodRegex);

        this.tests = this.filter(priority);

        if (SystemConfiguration.getInstance().isShuffleCases()) {
            LOG.debug("do test cases shuffle");
            Collections.shuffle(tests);
        }
    }

    public List<TaskCase> getCases() {
        return tests;
    }

    public String getName() {
        return name;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getNumberOfEnvs() {
        return numberOfEnvs;
    }

    private List<TaskCase> filter(Pattern testClassRegex, Pattern testMethodRegex) {
        LOG.debug("Use debug class  name fileter {}", testClassRegex);
        LOG.debug("Use debug method name fileter {}", testMethodRegex);
        List<TaskCase> tcs = new ArrayList<>();
        this.tests.stream().forEach((tc) -> {
            Matcher mc = testClassRegex.matcher(tc.getCaseClass());
            Matcher mm = testMethodRegex.matcher(tc.getCaseMethod());
            if (mc.find() && mm.find()) {
                tcs.add(tc);
            }
        });
        return tcs;
    }

    private List<TaskCase> filter(int priority) {
        List<TaskCase> tcs = new ArrayList<>();
        this.tests.stream().filter((tc) -> !(tc.getPriority() > priority)).forEach((tc) -> {
            tcs.add(tc);
        });
        return tcs;
    }

    private List<TaskCase> processTestAnnotations() {
        LOG.debug("Checking method annotation TestDataProvider for each test case");
        List<TaskCase> tcs = new ArrayList<>();

        this.tests.stream().forEach((tc) -> {
            try {
                Class<?> testClass = Class.forName(tc.getCaseClass());
                Method testMethod = testClass.getDeclaredMethod(tc.getCaseMethod());

                Priority p = testMethod.getAnnotation(Priority.class);
                if (p != null) {
                    tc.setPriority(p.level());
                }

                CaseDataProvider tdp = testMethod.getAnnotation(CaseDataProvider.class);
                if (tdp == null) {
                    LOG.debug("Adding test case {}", tc.format());
                    tcs.add(tc);
                } else {
                    LOG.trace("Calling class {}, method {}, with parameter {}", tdp.klass(), tdp.method(),
                        tdp.parameter());
                    CaseData[] data = AbstractCaseData.getCaseData(tdp.klass(), tdp.method(), tdp.parameter());
                    LOG.debug("{} is a data-driven test case, test data size is {}", tc.format(), data.length);
                    int length = (data.length + "").length();
                    for (int i = 0; i < data.length; i++) {
                        TaskCase t = new TaskCase(tc);
                        CaseDataInfo tdi = new CaseDataInfo(tdp.klass(), tdp.method(), tdp.parameter(), i);
                        t.setCaseDataInfo(tdi.format(length));
                        String value = data[i].getValue();
                        if (StringUtils.isEmpty(value)) {
                            value = String.format("%s-%0" + length + "d", data[i].getClassName(), i);
                        }
                        t.setCaseData(value);
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

    private <T extends AbstractCase> List<Method> getTestMethods(Class<T> testClass) {
        List<Method> methods = new ArrayList<>();
        for (Method m : testClass.getDeclaredMethods()) {
            if (m.getAnnotation(Test.class) != null) {
                methods.add(m);
            }
        }
        return methods;
    }
}
