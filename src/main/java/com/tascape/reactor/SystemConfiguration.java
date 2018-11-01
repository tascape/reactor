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
package com.tascape.reactor;

import com.tascape.reactor.db.DbHandler;
import com.tascape.reactor.suite.JUnit4Suite;
import com.tascape.reactor.task.Priority;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public final class SystemConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(SystemConfiguration.class);

    public static final Path HOME_PATH = Paths.get(FileUtils.getUserDirectory().getAbsolutePath(), ".reactor");

    public static final String CONSTANT_SYSPROP_PREFIX = "reactor.";

    public static final String CONSTANT_SYSPROP_FILE = CONSTANT_SYSPROP_PREFIX + "properties";

    public static final String CONSTANT_LOG_KEEP_ALIVE_PREFIX = "lka_";

    public static final String CONSTANT_EXEC_ID_PREFIX = "rx_";

    public static final String SYSPROP_CONF_FILES = "reactor.conf.files";

    public static final String SYSPROP_EXECUTION_ID = "reactor.exec.id";

    public static final String SYSPROP_SUITE_PROJECT_NAME = "reactor.suite.project.name";

    public static final String SYSPROP_EXECUTION_THREAD_COUNT = "reactor.exec.thread.count";

    public static final String SYSPROP_CASE_LOAD_LIMIT = "reactor.case.load.limit";

    public static final String SYSPROP_CASE_STATION = "reactor.case.station";

    public static final String SYSPROP_CASE_RETRY = "reactor.case.retry";

    public static final String SYSPROP_LOG_PATH = "reactor.log.path";

    public static final String SYSPROP_CASE_SUITE = "reactor.case.suite";

    public static final String SYSPROP_SHUFFLE_CASES = "reactor.shuffle.cases";

    public static final String SYSPROP_RESULT_VISIBILITY = "reactor.result.visibility";

    public static final String SYSPROP_DEBUG_CLASS_REGEX = "reactor.debug.class.regex";

    public static final String SYSPROP_DEBUG_METHOD_RESGX = "reactor.debug.method.regex";

    public static final String SYSPROP_DEBUG_CLASS_EXCLUDE_REGEX = "reactor.debug.class.exclude.regex";

    public static final String SYSPROP_DEBUG_METHOD_EXCLUDE_RESGX = "reactor.debug.method.exclude.regex";

    public static final String SYSPROP_CASE_PRIORITY = "reactor.case.priority";

    public static final String SYSPROP_PRODUCT_UNDER_TASK = "reactor.product.under.task";

    public static final String SYSPROP_CASE_ENV = "reactor.case.env";

    public static final String SYSPROP_CASE_LOG_LEVEL = "reactor.case.log.level";

    public static final String SYSPROP_CASE_LIST_FILE = "reactor.case.list.file";

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
            LOG.info("Load properties from /{} on classpath", CONSTANT_SYSPROP_FILE);
            InputStream is = SystemConfiguration.class.getResourceAsStream("/" + CONSTANT_SYSPROP_FILE);
            this.properties.load(is);
        } catch (IOException ex) {
            LOG.warn("", ex);
        }

        Path conf = HOME_PATH.resolve(CONSTANT_SYSPROP_FILE);
        Properties p = this.loadSystemPropertiesFromPath(conf);
        this.properties.putAll(p);

        String confFiles = System.getProperty(SYSPROP_CONF_FILES, "").trim();
        if (StringUtils.isNotBlank(confFiles)) {
            LOG.trace("Load properties from {}", confFiles);
            String[] paths = confFiles.split(System.getProperty("path.separator"));
            Stream.of(paths).forEach(path -> {
                this.properties.putAll(this.loadSystemPropertiesFromPath(Paths.get(path)));
            });
        }

        LOG.info("Load system properties starting with {}", CONSTANT_SYSPROP_PREFIX);
        List<String> keys = new ArrayList<>(System.getProperties().stringPropertyNames());
        Properties sysProps = new Properties();
        keys.stream()
                .filter((key)
                        -> (key.startsWith(CONSTANT_SYSPROP_PREFIX)) && StringUtils.isNotBlank(System.getProperty(key)))
                .forEach((key) -> {
                    sysProps.setProperty(key, System.getProperty(key));
                });
        this.properties.putAll(sysProps);

        String execId = this.properties.getProperty(SYSPROP_EXECUTION_ID);
        if (StringUtils.isEmpty(execId)) {
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
        if (StringUtils.isBlank(v)) {
            LOG.debug("System property '{}' is not defined, or blank, default value '{}' will be used", name,
                    defaultValue);
            return defaultValue;
        }
        return v;
    }

    /**
     *
     * @param name name of the system property
     *
     * @return integer, or Integer.MIN_VALUE if no corresponding system property found
     */
    public int getIntProperty(String name) {
        String v = this.properties.getProperty(name);
        if (StringUtils.isBlank(v)) {
            return Integer.MIN_VALUE;
        }
        return Integer.parseInt(v);
    }

    public int getIntProperty(String name, int defaultValue) {
        String v = this.getProperty(name);
        if (StringUtils.isBlank(v)) {
            LOG.debug("System property '{}' is not defined, default value '{}' will be used", name, defaultValue);
            return defaultValue;
        }
        return Integer.parseInt(v);
    }

    public boolean getBooleanProperty(String name, boolean defaultValue) {
        String v = this.getProperty(name);
        if (StringUtils.isBlank(v)) {
            LOG.debug("System property '{}' is not defined, default value '{}' will be used", name, defaultValue);
            return defaultValue;
        }
        return Boolean.parseBoolean(v);
    }

    public boolean isShuffleCases() {
        return this.getBooleanProperty(SYSPROP_SHUFFLE_CASES, false);
    }

    public boolean getResultVisibility() {
        return this.getBooleanProperty(SYSPROP_RESULT_VISIBILITY, true);
    }

    public Path getLogPath() {
        String p = this.getProperty(SYSPROP_LOG_PATH);
        if (StringUtils.isBlank(p)) {
            return HOME_PATH.resolve("logs");
        } else {
            return Paths.get(p);
        }
    }

    public String getSuiteProjectName() {
        return this.getProperty(SYSPROP_SUITE_PROJECT_NAME);
    }

    /**
     * Default is no retry.
     *
     * @return number of retries
     */
    public int getCaseRetry() {
        return this.getIntProperty(SYSPROP_CASE_RETRY, 0);
    }

    public int getExecutionThreadCount() {
        return this.getIntProperty(SYSPROP_EXECUTION_THREAD_COUNT, 0);
    }

    public void setExecutionThreadCount(int count) {
        this.getProperties().setProperty(SYSPROP_EXECUTION_THREAD_COUNT, count + "");
    }

    public int getCaseLoadLimit() {
        return this.getIntProperty(SYSPROP_CASE_LOAD_LIMIT, 100);
    }

    public String getHostName() {
        String hn = this.getProperty(SYSPROP_CASE_STATION);
        if (StringUtils.isEmpty(hn)) {
            try {
                hn = Utils.cmd("hostname").get(0);
            } catch (IOException | InterruptedException ex) {
                LOG.trace("Cannot get host name", ex);
                hn = "unknow host";
            }
            this.properties.setProperty(SYSPROP_CASE_STATION, hn);
        }
        return hn;
    }

    public String getExecId() {
        return this.getProperty(SYSPROP_EXECUTION_ID);
    }

    /**
     * Gets the array of suite class name.
     *
     * @return the full class name
     */
    public String getSuite() {
        String suite = this.getProperty(SYSPROP_CASE_SUITE);
        if (StringUtils.isBlank(suite)) {
            LOG.warn("There is no valid suite class name specified by system property {}", SYSPROP_CASE_SUITE);
            suite = JUnit4Suite.class.getName();
            LOG.warn("Use framework default {}", suite);
        }
        return suite;
    }

    public void setSuite(String suiteClassName) {
        this.properties.setProperty(SYSPROP_CASE_SUITE, suiteClassName);
    }

    public Pattern getCaseClassRegex() {
        String regex = this.getProperty(SYSPROP_DEBUG_CLASS_REGEX, ".+");
        return Pattern.compile(regex);
    }

    public Pattern getCaseMethodRegex() {
        String regex = this.getProperty(SYSPROP_DEBUG_METHOD_RESGX, ".+");
        return Pattern.compile(regex);
    }

    public int getCasePriority() {
        return this.getIntProperty(SYSPROP_CASE_PRIORITY, Priority.P3);
    }

    public String getDatabaseType() {
        return this.getProperty(DbHandler.SYSPROP_DATABASE_TYPE, "h2");
    }

    public String getDatabaseHost() {
        return this.getProperty(DbHandler.SYSPROP_DATABASE_HOST, "127.0.0.1:3306");
    }

    public String getDatabaseSchema() {
        return this.getProperty(DbHandler.SYSPROP_DATABASE_SCHEMA, "reactor");
    }

    public String getDatabaseUser() {
        return this.properties.getProperty(DbHandler.SYSPROP_DATABASE_USER, "reactor");
    }

    public String getDatabasePass() {
        return this.getProperty(DbHandler.SYSPROP_DATABASE_PASS, "p@ssword");
    }

    public int getDatabasePoolSize() {
        return this.getIntProperty(DbHandler.SYSPROP_DATABASE_POOL_SIZE,
                this.getIntProperty(SYSPROP_EXECUTION_THREAD_COUNT) + 10);
    }

    public String getProdUnderTask() {
        return this.getProperty(SYSPROP_PRODUCT_UNDER_TASK, "");
    }

    public Level getCaseLogLevel() {
        String l = this.getProperty(SYSPROP_CASE_LOG_LEVEL, "DEBUG");
        return Level.toLevel(l);
    }

    public List<String> getCaseList() {
        String pf = getProperty(SYSPROP_CASE_LIST_FILE);
        if (pf != null) {
            try {
                return Files.readAllLines(Paths.get(pf)).stream()
                        .filter(c -> StringUtils.isNotBlank(c))
                        .map(c -> c.trim())
                        .distinct()
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                LOG.warn("{}", ex.getLocalizedMessage());
            }
        }
        return new ArrayList<>();
    }

    public String getJobName() {
        String value = this.getProperty(pre(SYSENV_JOB_NAME));
        if (StringUtils.isBlank(value)) {
            value = System.getenv().get(SYSENV_JOB_NAME);
        }
        if (StringUtils.isBlank(value)) {
            value = System.getProperty("user.name") + "@" + StringUtils.substringBefore(this.getHostName(), ".");
        }
        return value.trim();
    }

    public int getJobBuildNumber() {
        String value = this.getProperty(pre(SYSENV_JOB_NUMBER));
        if (StringUtils.isBlank(value)) {
            value = System.getenv().get(SYSENV_JOB_NUMBER);
        }
        try {
            return StringUtils.isBlank(value) ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            LOG.warn("Cannot parse {}={}", SYSENV_JOB_NUMBER, value, ex);
            return -1;
        }
    }

    public String getJobBuildUrl() {
        String value = this.getProperty(pre(SYSENV_JOB_BUILD_URL));
        if (StringUtils.isBlank(value)) {
            value = System.getenv().get(SYSENV_JOB_BUILD_URL);
        }
        return StringUtils.isBlank(value) ? "#" : value;
    }

    public void listAppProperties() {
        LOG.debug("Application properties");
        List<String> keys = new ArrayList<>(this.properties.stringPropertyNames());
        Collections.sort(keys);
        keys.stream()
                .filter(key -> !key.startsWith(pre("db.")))
                .forEach((key) -> {
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

    private Properties loadSystemPropertiesFromPath(Path path) {
        File f = path.toFile();
        LOG.debug("Loading properties from {}", f.getAbsolutePath());
        Properties p = new Properties();
        if (!f.exists()) {
            LOG.warn("Cannot find properties file {}", path);
        } else {
            try (InputStream is = new FileInputStream(f)) {
                p.load(is);
            } catch (IOException ex) {
                LOG.warn("Cannot load properties from {} - {}", path, ex.getMessage());
            }
        }
        return p;
    }

    private String pre(String name) {
        return CONSTANT_SYSPROP_PREFIX + name;
    }
}
