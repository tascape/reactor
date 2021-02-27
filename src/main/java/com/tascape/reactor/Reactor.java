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

import com.tascape.reactor.exception.EntityCommunicationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class Reactor {
    private static final Logger LOG = LoggerFactory.getLogger(Reactor.class);

    public static void main(String[] args) {
        updatePath();

        int exitCode = 0;
        try {
            SystemConfiguration config = SystemConfiguration.getInstance();
            config.listAppProperties();

            Utils.cleanDirectory(config.getLogPath().toFile().getAbsolutePath(), 240,
                    SystemConfiguration.CONSTANT_LOG_KEEP_ALIVE_PREFIX);

            String suiteClass = config.getSuite();
            Pattern caseClassRegex = config.getCaseClassRegex();
            Pattern caseMethodRegex = config.getCaseMethodRegex();
            List<String> caseList = config.getDebugCaseList();
            LOG.debug("Running suite class: {}", suiteClass);
            TaskSuite ts = new TaskSuite(suiteClass, caseClassRegex, caseMethodRegex, caseList);

            if (ts.getCases().isEmpty()) {
                throw new RuntimeException("No case found based on system properties");
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

    private static void updatePath() {
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX) {
            Map<String, String> env = System.getenv();
            String path = env.get("PATH");
            Map<String, String> envNew = new HashMap<>(env);
            envNew.put("PATH", path + ":/usr/local/bin");
            try {
                Utils.setEnv(envNew);
            } catch (Exception ex) {
                throw new EntityCommunicationException(ex);
            }
        }
    }
}
