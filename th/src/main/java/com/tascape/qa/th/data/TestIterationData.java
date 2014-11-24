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

    private TestIterationData(int iteration, int iterations) {
        this.iteration = iteration;
        this.iterations = iterations;
    }

    public static TestIterationData[] getData(String sysPropIterations) {
        String iters = config.getProperty(sysPropIterations);
        if (iters == null) {
            iters = "1";
        }
        TestIterationData[] data = new TestIterationData[Integer.parseInt(iters)];
        for (int i = 0; i < data.length; i++) {
            data[i] = new TestIterationData(i, data.length);
        }
        return data;
    }

    @Override
    public String format() {
        return this.iteration + "/" + this.iterations;
    }
}
