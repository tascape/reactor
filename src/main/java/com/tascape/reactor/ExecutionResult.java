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

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class ExecutionResult {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionResult.class);

    public static final ExecutionResult NA = new ExecutionResult("NA");

    public static final ExecutionResult QUEUED = new ExecutionResult("QUEUED");

    public static final ExecutionResult ACQUIRED = new ExecutionResult("ACQUIRED");

    public static final ExecutionResult RUNNING = new ExecutionResult("RUNNING");

    public static final ExecutionResult PASS = new ExecutionResult("PASS");

    public static final ExecutionResult FAIL = new ExecutionResult("FAIL");

    public static final ExecutionResult CANCEL = new ExecutionResult("CANCEL");

    public static final List<String> NON_FINISH_STATES = Arrays.asList(
        NA.getName(),
        QUEUED.getName(),
        ACQUIRED.getName(),
        RUNNING.getName()
    );

    /**
     * to be implemented
     */
    public static final ExecutionResult TBI = new ExecutionResult("TBI");

    public static synchronized ExecutionResult newMultiple() {
        return new ExecutionResult("MULTIPLE");
    }

    public static boolean isPass(String result) {
        return result.equals(ExecutionResult.PASS.name) || result.endsWith("/0");
    }

    private String name = "";

    private int pass = 0;

    private int fail = 0;

    private ExecutionResult(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setPass(int pass) {
        if (pass < 0) {
            throw new InvalidParameterException("Negative integer is not supported.");
        }
        this.pass = pass;
    }

    public void setFail(int fail) {
        if (fail < 0) {
            throw new InvalidParameterException("Negative integer is not supported.");
        }
        this.fail = fail;
    }

    /**
     * For multiple, the format is PASS/FAIL
     *
     * @return the execution result
     */
    public String result() {
        if (this.pass != 0 || this.fail != 0) {
            return this.pass + "/" + this.fail;
        }
        return this.name;
    }

    public boolean equals(ExecutionResult er) {
        return this.result().equals(er.result());
    }

    public int getPass() {
        return pass;
    }

    public int getFail() {
        return fail;
    }

    public boolean isFailure() {
        return !name.equals("PASS") && fail != 0;
    }

    public static void main(String[] args) {
        LOG.debug("tbi {}", ExecutionResult.TBI.isFailure());
        LOG.debug("{}", ExecutionResult.isPass(ExecutionResult.TBI.name));
    }
}
