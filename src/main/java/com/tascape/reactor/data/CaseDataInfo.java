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
package com.tascape.reactor.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class CaseDataInfo {
    private static final Logger LOG = LoggerFactory.getLogger(CaseDataInfo.class);

    private static final Pattern pattern = Pattern.compile("([\\w\\.]+)\\.(\\w+?)\\((.*)\\)#(\\d+)");

    private static Map<String, Integer> dataSize = new HashMap<>();

    private Class<? extends CaseData> klass;

    private String method;

    private String parameter;

    private int index;

    public CaseDataInfo(Class<? extends CaseData> klass, String method, String parameter, int index) {
        this.klass = klass;
        this.method = method;
        this.parameter = parameter;
        this.index = index;

        String key = klass + method + parameter;
        Integer size = dataSize.get(key);
        if (size == null || index > size) {
            dataSize.put(key, index);
        }
    }

    /**
     *
     * @param infoString format class_name.method_name(parameter)#data_index
     *                   example: "com.example.qa.ui.UserCaseData.getData(sample.json)#39"
     *
     * @throws ClassNotFoundException issue with case data provide class name
     * @throws IOException            IO issue
     */
    public CaseDataInfo(String infoString) throws ClassNotFoundException, IOException {
        Matcher m = pattern.matcher(infoString);
        if (!m.find()) {
            throw new IOException("Cannot parse case data info string " + infoString);
        }

        this.klass = Class.forName(m.group(1)).asSubclass(CaseData.class);
        this.method = m.group(2);
        this.parameter = m.group(3);
        this.index = Integer.parseInt(m.group(4));
    }

    Class<? extends CaseData> getKlass() {
        return klass;
    }

    String getMethod() {
        return method;
    }

    String getParameter() {
        return parameter;
    }

    int getIndex() {
        return index;
    }

    public String getIndexString() {
        int size = (dataSize.get(klass + method) + "").length();
        if (size == 0) {
            size = 1;
        }
        return String.format("%0" + size + "d", this.index);
    }

    public String format(int size) {
        final String fmt = "%s.%s(%s)#%0" + size + "d";
        return String.format(fmt, this.klass.getName(), this.method, this.parameter, index);
    }
}
