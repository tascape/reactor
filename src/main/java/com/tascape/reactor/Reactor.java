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

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class Reactor {
    private static final Logger LOG = LoggerFactory.getLogger(Reactor.class);

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            SystemConfiguration config = SystemConfiguration.getInstance();
            config.listAppProperties();

            Utils.cleanDirectory(config.getLogPath().toFile().getAbsolutePath(), 240,
                SystemConfiguration.CONSTANT_LOG_KEEP_ALIVE_PREFIX);

            String suiteClass = config.getSuite();
            Pattern caseClassRegex = config.getCaseClassRegex();
            Pattern caseMethodRegex = config.getCaseMethodRegex();
            int priority = config.getCasePriority();
            LOG.debug("Running suite class: {}", suiteClass);
            TaskSuite ts = new TaskSuite(suiteClass, caseClassRegex, caseMethodRegex, priority);

            if (ts.getCases().isEmpty()) {
                throw new RuntimeException("No cases found based on system properties");
            }

            SuiteRunner sr = new SuiteRunner(ts);
            exitCode = sr.runCases();
        } catch (Throwable t) {
            LOG.error("Reactor finishes with exception", t);
            exitCode = -1;
        } finally {
            if (exitCode != 0) {
                LOG.error("Reactor finishes with exit code {}", exitCode);
            }
            System.exit(exitCode);
        }
    }
}
