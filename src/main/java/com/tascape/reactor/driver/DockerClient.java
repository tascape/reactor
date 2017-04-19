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
package com.tascape.reactor.driver;

import com.google.common.collect.Lists;
import com.tascape.reactor.SystemConfiguration;
import com.tascape.reactor.libx.DefaultExecutor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class DockerClient extends EntityDriver {
    private static final Logger LOG = LoggerFactory.getLogger(DockerClient.class);

    public static final String SYSPROP_DOCKER_EXECUTABLE = "reactor.comm.DOCKER_EXECUTABLE";

    private final static String DOCKER = locateDocker();

    private static String locateDocker() {
        String sysDocker = SystemConfiguration.getInstance().getProperty(SYSPROP_DOCKER_EXECUTABLE);
        if (sysDocker != null) {
            return sysDocker;
        } else {
            String paths = System.getenv().get("PATH");
            if (StringUtils.isBlank(paths)) {
                paths = System.getenv().get("Path");
            }
            if (StringUtils.isBlank(paths)) {
                paths = System.getenv().get("path");
            }
            if (StringUtils.isNotBlank(paths)) {
                paths += System.getProperty("path.separator") + "/usr/local/bin"; // this for macOS
                String[] path = paths.split(System.getProperty("path.separator"));
                for (String p : path) {
                    LOG.debug("path {}", p);
                    File f = Paths.get(p, "docker").toFile();
                    if (f.exists()) {
                        return f.getAbsolutePath();
                    }
                    f = Paths.get(p, "docker.bat").toFile();
                    if (f.exists()) {
                        return f.getAbsolutePath();
                    }
                    f = Paths.get(p, "docker.exe").toFile();
                    if (f.exists()) {
                        return f.getAbsolutePath();
                    }
                }
            }
        }
        throw new RuntimeException("Cannot find docker based on system PATH. You can specify system property "
                + SYSPROP_DOCKER_EXECUTABLE + "=/full/path/to/docker");
    }

    /**
     * Runs docker command synchronously.
     *
     * @param arguments command line arguments
     *
     * @return command stdout
     *
     * @throws IOException when command has error
     */
    public List<String> docker(final List<Object> arguments) throws IOException {
        CommandLine cmdLine = new CommandLine(DOCKER);
        arguments.forEach((arg) -> {
            cmdLine.addArgument(arg + "");
        });
        LOG.debug("[{} {}]", cmdLine.getExecutable(), StringUtils.join(cmdLine.getArguments(), " "));
        List<String> output = new ArrayList<>();
        Executor executor = new DefaultExecutor();
        executor.setStreamHandler(new ESH(output));
        if (executor.execute(cmdLine) != 0) {
            throw new IOException(cmdLine + " failed");
        }
        return output;
    }

    /**
     * Runs docker command asynchronously.
     *
     * @param arguments command line arguments
     * @param out       stdout will be written in to this file
     *
     * @return a handle used to stop the command process
     *
     * @throws IOException when command has error
     */
    public ExecuteWatchdog dockerAsync(final List<Object> arguments, OutputStream out) throws IOException {
        CommandLine cmdLine = new CommandLine(DOCKER);
        arguments.forEach((arg) -> {
            cmdLine.addArgument(arg + "");
        });
        LOG.debug("[{} {}]", cmdLine.getExecutable(), StringUtils.join(cmdLine.getArguments(), " "));
        List<String> output = new ArrayList<>();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
        Executor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        PrintWriter writer = new PrintWriter(out);
        ESH esh = new ESH(writer);
        executor.setStreamHandler(esh);
        executor.execute(cmdLine, new DefaultExecuteResultHandler());
        return watchdog;
    }

    public ExecuteWatchdog tailServiceLogs(String serviceName) throws IOException {
        List<Object> cmd = Lists.newArrayList("service", "logs", "--tail", "1", "-f", serviceName);
        File log = super.saveAsTextFile(serviceName, "");
        OutputStream out = FileUtils.openOutputStream(log);
        return dockerAsync(cmd, out);
    }

    @Override
    public String getName() {
        return DockerClient.class.getSimpleName();
    }

    @Override
    public String getVersion() {
        try {
            return this.docker(Lists.newArrayList("-v")).get(0);
        } catch (IOException ex) {
            LOG.warn(ex.getMessage());
        }
        return "NA";
    }

    @Override
    public void reset() throws Exception {
        LOG.debug("na");
    }

    private class ESH implements ExecuteStreamHandler {
        private BufferedReader bis;

        private final List<String> list;

        private final PrintWriter writer;

        ESH() {
            this.list = null;
            this.writer = null;
        }

        ESH(List<String> list) {
            this.list = list;
            this.writer = null;
        }

        ESH(PrintWriter writer) {
            this.list = null;
            this.writer = writer;
        }

        @Override
        public void setProcessInputStream(OutputStream out) throws IOException {
        }

        @Override
        public void setProcessErrorStream(InputStream in) throws IOException {
        }

        @Override
        public void setProcessOutputStream(InputStream in) throws IOException {
            bis = new BufferedReader(new InputStreamReader(in));
        }

        @Override
        public void start() throws IOException {
            while (true) {
                String line = bis.readLine();
                if (line == null) {
                    break;
                }
                LOG.trace(line);
                if (list != null) {
                    list.add(line);
                }
                if (writer != null) {
                    writer.println(line);
                    writer.flush();
                }
            }
        }

        @Override
        public void stop() throws IOException {
        }
    }

    public static void main(String[] args) throws Exception {
        DockerClient dc = new DockerClient();
        String v = dc.getVersion();
        LOG.info(v);

        ExecuteWatchdog dog = dc.tailServiceLogs("msc_siteservice");
        dc.delay(3000);
        dog.killedProcess();
        System.exit(0);
    }
}
