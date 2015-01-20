package com.tascape.qa.th.comm;

import com.android.uiautomator.stub.IUiDevice;
import com.android.uiautomator.stub.Point;

public class SampleUiAuotmatorTest {

    public static void main(String[] args) throws Exception {
        AndroidUiAutomatorDevice rc = new AndroidUiAutomatorDevice(8998);
        IUiDevice device = rc.getDevice();
        while (true) {
            device.pressHome();
            device.waitForIdle();
            device.click(500, 500);

            System.out.println(device.getProductName());
            System.out.println(device.getDisplayWidth());
            System.out.println(device.getDisplayHeight());
            Point p = device.getDisplaySizeDp();
            System.out.println(p.x + "/" + p.y);
            device.swipe(100, 0, 100, 500, 2);
            System.out.println(device.getCurrentActivityName());

            device.swipe(new Point[]{new Point(100, 500), new Point(100, 0)}, 2);
            device.swipe(100, 500, 100, 0, 2);
            System.out.println(device.getCurrentActivityName());
            System.out.println(device.isNaturalOrientation());
        }
    }
}
