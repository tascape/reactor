/*
 * Copyright (c) 2019 - present Nebula Bay.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run native commands.
 * <p>
 * @author linsong wang
 */
public class Native extends EntityDriver {
    private static final Logger LOG = LoggerFactory.getLogger(Native.class);

    public Process cmdAsync(String[] commands) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true);
        LOG.debug("Running command {}", pb.command().toString().replaceAll(",", ""));
        return pb.start();
    }

    public void waitForOutput(final Process process, final int timeoutSeconds) throws IOException {
        waitForOutputLine(process, null, timeoutSeconds);
    }

    public boolean waitForOutputLine(final Process process, String lineExpected, final int timeoutSeconds)
        throws IOException {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeoutSeconds * 1000);
                    LOG.warn("Timed out in {} seconds", timeoutSeconds);
                } catch (InterruptedException ex) {
                    LOG.warn(ex.getMessage());
                } finally {
                    if (process != null) {
                        process.destroy();
                    }
                }
            }
        };
        if (process == null) {
            return false;
        }
        File file = this.saveAsTextFile("cmd", "");
        t.setName(Thread.currentThread().getName() + "-" + t.hashCode());
        t.setDaemon(true);
        t.start();

        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
            PrintWriter pw = new PrintWriter(file)) {
            String console = "console-" + stdIn.hashCode();
            for (String line = stdIn.readLine(); line != null;) {
                LOG.debug("{}: {}", console, line);
                pw.println(line);

                if (StringUtils.isNotBlank(lineExpected) && line.contains(lineExpected)) {
                    LOG.info("Found expected line '{}'", line);
                    return true;
                }
                try {
                    line = stdIn.readLine();
                } catch (IOException ex) {
                    LOG.warn(ex.getLocalizedMessage());
                }
            }
        } finally {
            t.interrupt();
        }

        return false;
    }

    @Override
    public String getName() {
        return "Native";
    }

    @Override
    public void reset() throws Exception {
        LOG.debug("noop");
    }
}
