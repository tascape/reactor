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
package com.tascape.reactor.task;

import com.tascape.reactor.data.AbstractCaseData;

/**
 *
 * @author linsong wang
 */
public class SampleData extends AbstractCaseData {

    String caseParameter = "";

    private static final SampleData[] DATA = new SampleData[]{
        new SampleData() {
            {
                super.value = "scenario-one";
                caseParameter = "paramter one for case to use";
            }
        },
        new SampleData() {
            {
                super.value = "scenario-two";
                caseParameter = "paramter two for case to use";
            }
        },
        new SampleData() {
            {
                super.value = "scenario-three";
                caseParameter = "paramter three for case to use";
            }
        }};

    public SampleData[] getData() {
        return DATA;
    }

    /**
     * Gets the actual data for case to use during execution.
     *
     * @return case data
     */
    public String getCaseParameter() {
        return caseParameter;
    }
}
