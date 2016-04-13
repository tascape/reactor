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

import java.security.InvalidParameterException;

/**
 *
 * @author linsong wang
 */
public class ExecutionResult {
    public static final ExecutionResult NA = new ExecutionResult("NA");

    public static final ExecutionResult QUEUED = new ExecutionResult("QUEUED");

    public static final ExecutionResult ACQUIRED = new ExecutionResult("ACQUIRED");

    public static final ExecutionResult RUNNING = new ExecutionResult("RUNNING");

    public static final ExecutionResult PASS = new ExecutionResult("PASS");

    public static final ExecutionResult FAIL = new ExecutionResult("FAIL");

    public static final ExecutionResult CANCEL = new ExecutionResult("CANCEL");

    public static synchronized ExecutionResult newMultiple() {
        return new ExecutionResult("MULTIPLE");
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
}
