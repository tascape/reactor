/*
 * Copyright 2015.
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
package com.tascape.qa.th.libx;

/**
 *
 * @author linsong wang
 */
public class DefaultExecutor extends org.apache.commons.exec.DefaultExecutor {

    /**
     * Factory method to create a thread waiting for the result of an
     * asynchronous execution.
     *
     * @param runnable the runnable passed to the thread
     * @param name     the name of the thread
     *
     * @return the thread
     */
    protected Thread createThread(final Runnable runnable, final String name) {
        return new Thread(runnable, Thread.currentThread().getName() + "-exec");
    }
}
