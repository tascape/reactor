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
package com.tascape.qa.th;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class AbstractTestResource {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTestResource.class);

    private static final ThreadLocal<Path> TEST_LOG_PATH = new ThreadLocal<Path>() {
        @Override
        protected Path initialValue() {
            String execId = SystemConfiguration.getInstance().getExecId();
            Path testLogPath = SystemConfiguration.getInstance().getLogPath().resolve(execId);
            testLogPath.toFile().mkdirs();
            return testLogPath;
        }
    };

    public static void setTestLogPath(Path testLogPath) {
        LOG.trace("Set runtime log directory {}: {}", Thread.currentThread().getName(), testLogPath);
        TEST_LOG_PATH.set(testLogPath);
    }

    public static Path getTestLogPath() {
        return TEST_LOG_PATH.get();
    }

    protected final SystemConfiguration sysConfig = SystemConfiguration.getInstance();

    public Path getLogPath() {
        return AbstractTestResource.getTestLogPath();
    }

    public File saveAsTextFile(String prefix, CharSequence data) throws IOException {
        return this.saveIntoFile(prefix, "txt", data);
    }

    public File saveIntoFile(String prefix, String suffix, CharSequence data) throws IOException {
        Path path = this.getLogPath();
        File p = path.toFile();
        if (!p.exists() && !p.mkdirs()) {
            throw new IOException("Cannot create test log directory " + p);
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
