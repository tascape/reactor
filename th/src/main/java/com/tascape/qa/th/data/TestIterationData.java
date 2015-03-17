package com.tascape.qa.th.data;

import com.tascape.qa.th.SystemConfiguration;

/**
 *
 * @author linsong wang
 */
public class TestIterationData extends AbstractTestData {

    private static final SystemConfiguration config = SystemConfiguration.getInstance();

    private int iteration = 1;

    private int iterations = 1;

    public TestIterationData() {
    }

    private TestIterationData(int iteration, int iterations) {
        this.iteration = iteration;
        this.iterations = iterations;
    }

    public TestIterationData[] getData(String sysPropIterations) {
        String n = config.getProperty(sysPropIterations);
        return useIterations(n);
    }

    public TestIterationData[] useIterations(String n) {
        int iters = 1;
        try {
            iters = Integer.parseInt(n);
        } catch (Exception ex) {
        }
        TestIterationData[] data = new TestIterationData[iters];
        for (int i = 0; i < iters; i++) {
            data[i] = new TestIterationData(i, iters);
        }
        return data;
    }

    @Override
    public String format() {
        return (this.iteration + 1) + "/" + this.iterations;
    }

    public int getIteration() {
        return iteration;
    }

    public int getIterations() {
        return iterations;
    }
}
