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
package com.tascape.reactor.data;

import com.tascape.reactor.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class CaseIterationData extends AbstractCaseData {
    private static final Logger LOG = LoggerFactory.getLogger(CaseIterationData.class);
    
    public static final String USE_ITERATIONS = "useIterations";

    private int iteration = 1;

    private int iterations = 1;

    public CaseIterationData() {
    }

    private CaseIterationData(int iteration, int iterations) {
        this.iteration = iteration;
        this.iterations = iterations;
    }

    public CaseIterationData[] getData(String sysPropIterations) {
        String n = SystemConfiguration.getInstance().getProperty(sysPropIterations);
        return useIterations(n);
    }

    public CaseIterationData[] useIterations(String n) {
        int iters = 1;
        try {
            iters = Integer.parseInt(n);
        } catch (Exception ex) {
            LOG.warn(ex.getMessage());
        }
        CaseIterationData[] data = new CaseIterationData[iters];
        for (int i = 0; i < iters; i++) {
            data[i] = new CaseIterationData(i, iters);
        }
        return data;
    }

    public CaseIterationData[] useSystemProperty(String sysPropIterations) {
        String n = SystemConfiguration.getInstance().getProperty(sysPropIterations);
        return useIterations(n);
    }

    @Override
    public String getValue() {
        int len = (this.iterations + "").length();
        return String.format("%0" + len + "d/%d", this.iteration + 1, this.iterations);
    }

    public int getIteration() {
        return iteration;
    }

    public int getIterations() {
        return iterations;
    }
}
