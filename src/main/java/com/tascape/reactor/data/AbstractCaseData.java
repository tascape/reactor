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
package com.tascape.reactor.data;

import com.tascape.reactor.task.Priority;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class AbstractCaseData implements CaseData {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCaseData.class);

    private static final ThreadLocal<CaseData> CASE_DATA = new ThreadLocal<>();

    public static void setCaseData(CaseData data) {
        CASE_DATA.set(data);
    }

    public static CaseData getCaseData() {
        return CASE_DATA.get();
    }

    private static final Map<String, CaseData[]> LOADED_DATA = new HashMap<>();

    private static final Map<Class<? extends CaseData>, Object> LOADED_PROVIDERS = new HashMap<>();

    private String value = null;

    /*
     * works together with Priority of case method. NONE means no data priority specified.
     */
    private int priority = Priority.NONE;

    private boolean toBeImplemented = false;

    @Override
    public String getClassName() {
        return "";
    }

    @Override
    public String getValue() {
        if (value == null) {
            LOG.warn("Value of case data is not specified.");
            return this.toString();
        }
        return this.value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public CaseData setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public static CaseData getCaseData(String caseDataInfo) throws Exception {
        CaseDataInfo info = new CaseDataInfo(caseDataInfo);
        CaseData[] data = getCaseData(info.getKlass(), info.getMethod(), info.getParameter());
        int index = info.getIndex();
        if (data.length <= index) {
            throw new Exception("Cannot find case data using " + caseDataInfo);
        }
        return data[index];
    }

    @Override
    public boolean isToBeImplemented() {
        return this.toBeImplemented;
    }

    /**
     * Marks the case corresponding to this data as to-be-implemented.
     */
    public void markAsToBeImplemented() {
        this.toBeImplemented = true;
    }

    public static synchronized CaseData[] getCaseData(Class<? extends CaseData> klass, String method, String parameter)
        throws Exception {
        String key = klass + "." + method + "." + parameter;
        CaseData[] data = AbstractCaseData.LOADED_DATA.get(key);
        if (data == null) {
            Object provider = AbstractCaseData.LOADED_PROVIDERS.get(klass);
            if (provider == null) {
                provider = klass.newInstance();
                AbstractCaseData.LOADED_PROVIDERS.put(klass, provider);
            }

            if (parameter == null || parameter.isEmpty()) {
                Method m = klass.getDeclaredMethod(method, (Class<?>[]) null);
                data = (CaseData[]) m.invoke(provider, (Object[]) null);
            } else {
                Method m = klass.getDeclaredMethod(method, new Class<?>[]{parameter.getClass()});
                data = (CaseData[]) m.invoke(provider, new Object[]{parameter});
            }
            AbstractCaseData.LOADED_DATA.put(key, data);
        }
        return data;
    }

    /**
     * The default external id is empty string. Please override this method to provide external id for your case data.
     *
     * @return empty string
     */
    @Override
    public String getExternalId() {
        return "";
    }
}
