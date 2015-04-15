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
package com.tascape.qa.th;

import com.tascape.qa.th.db.DbHandler;
import com.tascape.qa.th.test.Priority;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public final class SystemConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(SystemConfiguration.class);

    public static final String CONSTANT_LOG_KEEP_ALIVE_PREFIX = "thlka_";

    public static final String CONSTANT_EXEC_ID_PREFIX = "th_";

    public static final String SYSPROP_CONF_FILE = "qa.th.conf.file";

    public static final String SYSPROP_EXECUTION_ID = "qa.th.exec.id";

    public static final String SYSPROP_EXECUTION_THREAD_COUNT = "qa.th.exec.thread.count";

    public static final String SYSPROP_TEST_STATION = "qa.th.test.station";

    public static final String SYSPROP_TEST_RETRY = "qa.th.test.retry";

    public static final String SYSPROP_LOG_PATH = "qa.th.log.path";

    public static final String SYSPROP_TEST_SUITE = "qa.th.test.suite";

    public static final String SYSPROP_DEBUG_CLASS_REGEX = "qa.th.debug.class.regex";

    public static final String SYSPROP_DEBUG_METHOD_RESGX = "qa.th.debug.method.regex";

    public static final String SYSPROP_TEST_PRIORITY = "qa.th.test.priority";

    public static final String SYSPROP_PRODUCT_UNDER_TEST = "qa.th.product.under.test";

    public static final String SYSENV_JOB_NAME = "JOB_NAME";

    public static final String SYSENV_JOB_NUMBER = "BUILD_NUMBER";

    public static final String SYSENV_JOB_BUILD_URL = "BUILD_URL";

    private static final SystemConfiguration CONFIG = new SystemConfiguration();

    private final Properties properties = new Properties();

    public static SystemConfiguration getInstance() {
        return CONFIG;
    }

    private SystemConfiguration() {
        this.listSysProperties();

        try {
            InputStream is = SystemConfiguration.class.getResourceAsStream("/th.properties");
            this.properties.load(is);
        } catch (Exception ex) {
            LOG.warn("", ex);
        }

        Path conf = Paths.get(System.getProperty("user.home"), ".th", "th.properties");
        String confFile = System.getProperty(SYSPROP_CONF_FILE);
        if (confFile != null) {
            conf = Paths.get(confFile);
        }
        LOG.info("Loading system configuration from {}", conf);
        if (conf.toFile().exists()) {
            try (InputStream is = new FileInputStream(conf.toFile())) {
                Properties p = new Properties();
                p.load(is);
                this.properties.putAll(p);
            } catch (IOException ex) {
                throw new RuntimeException("Cannot load system configuration from " + conf, ex);
            }
        }
        List<String> keys = new ArrayList<>(System.getProperties().stringPropertyNames());
        keys.stream()
            .filter((key) -> (key.startsWith("qa.th.")))
            .forEach((key) -> {
                this.properties.setProperty(key, System.getProperty(key));
            });

        String execId = this.properties.getProperty(SYSPROP_EXECUTION_ID);
        if (execId == null || execId.trim().isEmpty()) {
            execId = Utils.getUniqueId(CONSTANT_EXEC_ID_PREFIX);
            LOG.warn("There is no execution id specified, using local new UUID: {}", execId);
            this.properties.setProperty(SYSPROP_EXECUTION_ID, execId);
        }
    }

    public String getProperty(String name) {
        return this.properties.getProperty(name);
    }

    public String getProperty(String name, String defaultValue) {
        String v = this.properties.getProperty(name);
        if (v == null) {
            LOG.debug("System property '{}' is not defined, default value '{}' will be used", name, defaultValue);
            return defaultValue;
        }
        return v;
    }

    public int getIntProperty(String name, int defaultValue) {
        String v = this.getProperty(name);
        if (v == null) {
            LOG.debug("System property '{}' is not defined, default value '{}' will be used", name, defaultValue);
            return defaultValue;
        }
        return Integer.parseInt(v);
    }

    public boolean getBooleanProperty(String name, boolean defaultValue) {
        String v = this.getProperty(name);
        if (v == null) {
            LOG.debug("System property '{}' is not defined, default value '{}' will be used", name, defaultValue);
            return defaultValue;
        }
        return Boolean.parseBoolean(v);
    }

    public Path getLogPath() {
        return Paths.get(this.getProperty(SYSPROP_LOG_PATH, "/qa/logs"));
    }

    public int getTestRetry() {
        return Integer.parseInt(this.getProperty(SYSPROP_TEST_RETRY, "0"));
    }

    public int getExecutionThreadCount() {
        return Integer.parseInt(this.getProperty(SYSPROP_EXECUTION_THREAD_COUNT, "1"));
    }

    public String getHostName() {
        String hn = this.getProperty(SYSPROP_TEST_STATION);
        if (hn == null || hn.isEmpty()) {
            try {
                hn = Utils.cmd("hostname").get(0);
            } catch (IOException | InterruptedException ex) {
                LOG.warn("Cannot get host name", ex);
                hn = "unknow host";
            }
            this.properties.setProperty(SYSPROP_TEST_STATION, hn);
        }
        return hn;
    }

    public String getExecId() {
        return this.getProperty(SYSPROP_EXECUTION_ID);
    }

    /**
     * Gets the array of test suite class name.
     *
     * @return the full class name
     */
    public String getTestSuite() {
        String suite = this.getProperty(SYSPROP_TEST_SUITE);
        if (suite == null || suite.isEmpty()) {
            throw new RuntimeException("There is no test suite class name specified (system property "
                + SYSPROP_TEST_SUITE + ")");
        }
        return suite;
    }

    public void setTestSuite(String suiteClassName) {
        this.properties.setProperty(SYSPROP_TEST_SUITE, suiteClassName);
    }

    public Pattern getTestClassRegex() {
        String regex = this.getProperty(SYSPROP_DEBUG_CLASS_REGEX, ".+");
        return Pattern.compile(regex);
    }

    public Pattern getTestMethodRegex() {
        String regex = this.getProperty(SYSPROP_DEBUG_METHOD_RESGX, ".+");
        return Pattern.compile(regex);
    }

    public int getTestPriority() {
        String v = this.getProperty(SYSPROP_TEST_PRIORITY);
        if (v == null || v.isEmpty()) {
            return Priority.P3;
        }
        return Integer.parseInt(v);
    }

    public String getDatabaseType() {
        return this.getProperty(DbHandler.SYSPROP_DATABASE_TYPE, "th");
    }

    public String getDatabaseHost() {
        return this.getProperty(DbHandler.SYSPROP_DATABASE_HOST, "127.0.0.1:3306");
    }

    public String getDatabaseSchema() {
        return this.getProperty(DbHandler.SYSPROP_DATABASE_SCHEMA, "testharness");
    }

    public String getDatabaseUser() {
        return this.properties.getProperty(DbHandler.SYSPROP_DATABASE_USER, "th");
    }

    public String getDatabasePass() {
        return this.getProperty(DbHandler.SYSPROP_DATABASE_PASS, "p@ssword");
    }

    public String getProdUnderTest() {
        return this.getProperty(SYSPROP_PRODUCT_UNDER_TEST, SYSPROP_PRODUCT_UNDER_TEST + " was not set");
    }

    public String getJobName() {
        String value = this.getProperty("qa.th." + SYSENV_JOB_NAME);
        if (value == null) {
            value = System.getenv().get(SYSENV_JOB_NAME);
        }
        if (value == null) {
            value = System.getProperty("user.name") + "@" + StringUtils.substringBefore(this.getHostName(), ".");
        } else {
            value = value.split("/")[0];
        }
        return value.trim();
    }

    public int getJobBuildNumber() {
        String value = this.getProperty("qa.th." + SYSENV_JOB_NUMBER);
        if (value == null) {
            value = System.getenv().get(SYSENV_JOB_NUMBER);
        }
        try {
            return value == null ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            LOG.warn("Cannot parse {}={}", SYSENV_JOB_NUMBER, value, ex);
            return -1;
        }
    }

    public String getJobBuildUrl() {
        String value = this.getProperty("qa.th." + SYSENV_JOB_BUILD_URL);
        if (value == null) {
            value = System.getenv().get(SYSENV_JOB_BUILD_URL);
        }
        return value == null ? "#" : value;
    }

    public void listAppProperties() {
        LOG.debug("Application properties");
        List<String> keys = new ArrayList<>(this.properties.stringPropertyNames());
        Collections.sort(keys);
        keys.forEach((key) -> {
            LOG.debug(String.format("%50s : %s", key, this.properties.getProperty(key)));
        });
    }

    public Properties getProperties() {
        return properties;
    }

    private void listSysProperties() {
        List<String> keys = new ArrayList<>(System.getProperties().stringPropertyNames());
        Collections.sort(keys);
        LOG.debug("Java system properties");
        for (String key : keys) {
            LOG.debug(String.format("%50s : %s", key, System.getProperties().getProperty(key)));
        }

        keys = new ArrayList<>(System.getenv().keySet());
        Collections.sort(keys);
        LOG.debug("Java environment properties");
        for (String key : keys) {
            LOG.debug(String.format("%50s : %s", key, System.getenv().get(key)));
        }
    }
}
