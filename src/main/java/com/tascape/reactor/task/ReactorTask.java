/*
 * Copyright (c) 2021 - present Nebula Bay.
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
package com.tascape.reactor.task;

import com.tascape.reactor.Reactor;
import com.tascape.reactor.db.DbHandler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Framework-related tasks.
 *
 * @author linsong wang
 */
@Priority(level = 0)
public class ReactorTask extends AbstractCase {

    private static final Logger LOG = LoggerFactory.getLogger(ReactorTask.class);

    private final DbHandler reactorDb = DbHandler.getInstance();

    @Before
    public void setUp() {
        LOG.debug("Run something before a task");
    }

    @After
    public void tearDown() {
        LOG.debug("Run something after a task");
    }

    @Override
    public String getApplicationUnderTask() {
        return Reactor.class.getName();
    }

    /**
     * The task checks result database to determine if one suite execution finishes, and create result files (xml, html, and json formats).This is useful when
     * you run concurrency testing of one suite over multiple execution hosts, where each result.html may not include all test results of all test cases due to
     * timing. The target task execution id is specified with system property <code>reactor.task.EXEC_ID</code>. The wait timeout is specified with system
     * property <code>reactor.task.EXEC_TIMEOUT_MINUTES</code>.
     *
     * @throws java.sql.SQLException db error
     * @throws java.lang.InterruptedException thread error
     * @throws java.io.IOException io error
     * @throws javax.xml.stream.XMLStreamException xml error
     */
    @Test
    public void waitForExecutionResult() throws SQLException, InterruptedException, IOException, XMLStreamException {
        String TASK_EXEC_ID = "reactor.task.EXEC_ID";
        String taskExecId = sysConfig.getProperty(TASK_EXEC_ID);
        Assert.assertNotNull("please specify system property: " + TASK_EXEC_ID, taskExecId);
        String TASK_TIMEOUT_MINUTE = "reactor.task.EXEC_TIMEOUT_MINUTES";
        int taskExecTimeoutMinute = sysConfig.getIntProperty(TASK_TIMEOUT_MINUTE, 0);
        Assert.assertTrue("please specify system property: " + TASK_TIMEOUT_MINUTE, taskExecTimeoutMinute > 1);

        boolean finished = reactorDb.waitForExecution(taskExecId, taskExecTimeoutMinute);
        Assert.assertTrue(finished);
        Path path = reactorDb.saveJunitXml(taskExecId);
        reactorDb.exportToJson(taskExecId);
        Path currentPath = Paths.get("");
        LOG.debug("copy result.* into current work directory");
        for (String ext : Arrays.asList("xml", "html", "json")) {
            String file = "result." + ext;
            File destFile = currentPath.resolve(file).toFile();
            FileUtils.copyFile(path.resolve(file).toFile(), destFile);
            LOG.info(destFile.getAbsolutePath());
        }
    }
}
