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
package com.tascape.reactor.driver;

import com.tascape.reactor.task.AbstractCase;

/**
 *
 * @author linsong wang
 */
public class CaseDriver {

    private final Class<? extends AbstractCase> caseClass;

    private final Class<? extends EntityDriver> driverClass;

    public CaseDriver(Class<? extends AbstractCase> caseClass, Class<? extends EntityDriver> driverClass) {
        this.caseClass = caseClass;
        this.driverClass = driverClass;
    }

    @Override
    public String toString() {
        return caseClass.getName() + "+" + driverClass.getName() + "+" + this.hashCode();
    }

    public Class<? extends AbstractCase> getCaseClass() {
        return caseClass;
    }

    public Class<? extends EntityDriver> getDriverClass() {
        return driverClass;
    }
}
