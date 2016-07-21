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

    private List<TaskCase> cases = new ArrayList<>();

    public TaskSuite(String suiteClass, Pattern caseClassRegex, Pattern caseMethodRegex, int priority)
        throws Exception {
        LOG.debug("Find cases in target suite {}", suiteClass);
        AbstractSuite suite = AbstractSuite.class.cast(Class.forName(suiteClass).newInstance());
        this.name = suite.getName();
        this.projectName = suite.getProjectName();
        this.numberOfEnvs = suite.getNumberOfEnvs();
        if (this.name == null || this.name.isEmpty()) {
            this.name = suiteClass;
        }
        suite.setUpCaseClasses();
        for (Class<? extends AbstractCase> clazz : suite.getCaseClasses()) {
            for (Method method : this.getCaseMethods(clazz)) {
                TaskCase tc = new TaskCase();
                tc.setSuiteClass(suiteClass);
                tc.setCaseClass(clazz.getName());
                tc.setCaseMethod(method.getName());
                this.cases.add(tc);
            }
        }

        this.cases = this.processAnnotations();

        this.cases = this.filter(caseClassRegex, caseMethodRegex);

        this.cases = this.filter(priority);

        if (SystemConfiguration.getInstance().isShuffleCases()) {
            LOG.debug("do case shuffle");
            Collections.shuffle(cases);
        }
    }

    public List<TaskCase> getCases() {
        return cases;
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

    private List<TaskCase> filter(Pattern caseClassRegex, Pattern caseMethodRegex) {
        LOG.debug("Use debug class  name fileter {}", caseClassRegex);
        LOG.debug("Use debug method name fileter {}", caseMethodRegex);
        List<TaskCase> tcs = new ArrayList<>();
        this.cases.stream().forEach((tc) -> {
            Matcher mc = caseClassRegex.matcher(tc.getCaseClass());
            Matcher mm = caseMethodRegex.matcher(tc.getCaseMethod());
            if (mc.find() && mm.find()) {
                tcs.add(tc);
            }
        });
        return tcs;
    }

    private List<TaskCase> filter(int priority) {
        List<TaskCase> tcs = new ArrayList<>();
        this.cases.stream().filter((tc) -> !(tc.getPriority() > priority)).forEach((tc) -> {
            tcs.add(tc);
        });
        return tcs;
    }

    private List<TaskCase> processAnnotations() {
        LOG.debug("Checking method annotation CaseDataProvider for each case");
        List<TaskCase> tcs = new ArrayList<>();

        this.cases.stream().forEach((tc) -> {
            try {
                Class<?> caseClass = Class.forName(tc.getCaseClass());
                Method caseMethod = caseClass.getDeclaredMethod(tc.getCaseMethod());

                Priority p = caseMethod.getAnnotation(Priority.class);
                if (p != null) {
                    tc.setPriority(p.level());
                }

                CaseDataProvider tdp = caseMethod.getAnnotation(CaseDataProvider.class);
                if (tdp == null) {
                    LOG.debug("Adding case {}", tc.format());
                    tcs.add(tc);
                } else {
                    LOG.trace("Calling class {}, method {}, with parameter {}", tdp.klass(), tdp.method(),
                        tdp.parameter());
                    CaseData[] data = AbstractCaseData.getCaseData(tdp.klass(), tdp.method(), tdp.parameter());
                    LOG.debug("{} is a data-driven case, data size is {}", tc.format(), data.length);
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

                        LOG.debug("Adding case {}", t.format());
                        tcs.add(t);
                    }
                }
            } catch (Exception ex) {
                LOG.warn("Cannot process case {}, skipping. Check @" +CaseDataProvider.class.getName(), tc.format(), ex);
            }
        });
        return tcs;
    }

    private <T extends AbstractCase> List<Method> getCaseMethods(Class<T> caseClass) {
        List<Method> methods = new ArrayList<>();
        for (Method m : caseClass.getDeclaredMethods()) {
            if (m.getAnnotation(Test.class) != null) {
                methods.add(m);
            }
        }
        return methods;
    }
}
