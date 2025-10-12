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

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class AbstractCaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCaseResource.class);

    public static final SystemConfiguration SYS_CONFIG = SystemConfiguration.getInstance();

    private static final ThreadLocal<Path> CASE_LOG_PATH = new ThreadLocal<Path>() {
        @Override
        protected Path initialValue() {
            String execId = SystemConfiguration.getInstance().getExecId();
            Path caseLogPath = SystemConfiguration.getInstance().getLogPath().resolve(execId);
            caseLogPath.toFile().mkdirs();
            return caseLogPath;
        }
    };

    public static void setCaseLogPath(Path caseLogPath) {
        LOG.trace("Set runtime log directory {}: {}", Thread.currentThread().getName(), caseLogPath);
        CASE_LOG_PATH.set(caseLogPath);
    }

    public static Path getCaseLogPath() {
        return CASE_LOG_PATH.get();
    }

    protected final SystemConfiguration sysConfig = SystemConfiguration.getInstance();

    public Path getLogPath() {
        return AbstractCaseResource.getCaseLogPath();
    }

    public File saveAsTextFile(String prefix, CharSequence data) throws IOException {
        return this.saveIntoFile(prefix, "txt", data);
    }

    public File saveIntoFile(String prefix, JSONObject json) throws IOException {
        return this.saveIntoFile(prefix, "json", json.toString(2));
    }

    public File saveIntoFile(String prefix, JSONArray json) throws IOException {
        return this.saveIntoFile(prefix, "json", json.toString(2));
    }

    public File saveIntoFile(String prefix, String suffix, CharSequence data) throws IOException {
        Path path = this.getLogPath();
        File p = path.toFile();
        if (!p.exists() && !p.mkdirs()) {
            throw new IOException("Cannot create log directory " + p);
        }
        File f = File.createTempFile(prefix + "-", "." + suffix, p);
        FileUtils.write(f, data, Charset.defaultCharset());
        LOG.debug("Save data into file {}", f.getAbsolutePath());
        return f;
    }

    public File saveAsTempTextFile(String filePrefix, CharSequence data) throws IOException {
        Path path = this.getLogPath();
        File f = File.createTempFile(SystemConfiguration.CONSTANT_LOG_KEEP_ALIVE_PREFIX + filePrefix, ".txt",
                path.toFile());
        FileUtils.write(f, data, Charset.defaultCharset());
        LOG.debug("Save data into file {}", f.getAbsolutePath());
        return f;
    }

    /**
     * @return png file
     */
    public File captureScreen() {
        Path path = this.getLogPath();
        File png = path.resolve("screen-" + System.currentTimeMillis() + ".png").toFile();
        png = Utils.getKeepAliveFile(png);
        try {
            Utils.captureScreen(png);
        } catch (AWTException | IOException ex) {
            LOG.warn("Cannot take screenshot", ex);
        }
        return png;
    }

    /**
     * Blocks current thread for specified milliseconds. Shortcut to Thread.sleep. Throws RuntimeException.
     *
     * @param millis milliseconds
     */
    public void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected File createDataFile(String prefix) throws IOException {
        return this.createDataFile(prefix, "txt");
    }

    protected File createDataFile(String prefix, String extension) throws IOException {
        File f = File.createTempFile(prefix + "-", "." + extension, this.getLogPath().toFile());
        return f;
    }

    protected File createKeepAliveLogFile(String prefix, String extension) throws IOException {
        File f = File.createTempFile(prefix + "-", "." + extension, this.getLogPath().toFile());
        return Utils.getKeepAliveFile(f);
    }
}
