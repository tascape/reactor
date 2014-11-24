package com.tascape.qa.th.data;

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
public class TestDataInfo {
    private static final Logger LOG = LoggerFactory.getLogger(TestDataInfo.class);

    private static final Pattern pattern = Pattern.compile("([\\w\\.]+)\\.(\\w+?)\\((.*)\\)#(\\d+)");

    private static Map<String, Integer> dataSize = new HashMap<>();

    private Class<? extends TestData> klass;

    private String method;

    private String parameter;

    private int index;

    public TestDataInfo(Class<? extends TestData> klass, String method, String parameter, int index) {
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
     *                   example: "com.example.qa.ui.USERTestData.getData(sample.json)#39"
     *
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public TestDataInfo(String infoString) throws ClassNotFoundException, IOException {
        Matcher m = pattern.matcher(infoString);
        if (!m.find()) {
            throw new IOException("Cannot parse test data info string " + infoString);
        }

        this.klass = (Class<? extends TestData>) Class.forName(m.group(1));
        this.method = m.group(2);
        this.parameter = m.group(3);
        this.index = Integer.parseInt(m.group(4));
    }

    Class<? extends TestData> getKlass() {
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
