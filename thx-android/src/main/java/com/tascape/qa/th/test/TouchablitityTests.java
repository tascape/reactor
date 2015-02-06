package com.tascape.qa.th.test;

import com.tascape.qa.th.comm.Adb;
import com.tascape.qa.th.driver.AndroidAdbDevice;
import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class TouchablitityTests {
    private static final Logger LOG = LoggerFactory.getLogger(TouchablitityTests.class);

    private AndroidAdbDevice adbDevice;

    public void setup() throws Exception {
        Adb adb = new Adb();
        this.adbDevice = new AndroidAdbDevice();
        this.adbDevice.setAdb(adb);
    }

    public void testOne() throws Exception {
        int seconds = 10;

        File log = this.adbDevice.logTouchEvents(seconds);
        String mp4 = this.adbDevice.recordScreen(seconds, 512000);

        LOG.info("Please interact with touch screen for {} seconds", seconds);
        Thread.sleep(seconds * 1100L);

        LOG.info("Done recording");

        LOG.debug("{}", log);

        List<Long> events = this.adbDevice.getTouchEvents(log);
        List<Long> updates = this.adbDevice.getScreenUpdates(mp4);

        LOG.info("Touch Events: {}", events);
        LOG.info("Screen Updates: {}", updates);

        // todo
        // data presentation and analysis
    }

    public static void main(String[] args) {
        try {
            TouchablitityTests tests = new TouchablitityTests();
            tests.setup();
            tests.testOne();
            System.exit(0);
        } catch (Throwable ex) {
            LOG.error("", ex);
        } finally {
            System.exit(0);
        }
    }
}
