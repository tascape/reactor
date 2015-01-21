package com.tascape.qa.th.comm;

import com.android.uiautomator.stub.IUiDevice;
import com.android.uiautomator.stub.IUiObject;
import com.android.uiautomator.stub.Point;
import com.android.uiautomator.stub.UiSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class SampleUiAuotmatorTest {
    private static final Logger LOG = LoggerFactory.getLogger(SampleUiAuotmatorTest.class);

    public static void main(String[] args) throws Exception {
        AndroidUiAutomatorDevice uiad = new AndroidUiAutomatorDevice(8998);
        IUiDevice uiDeviceStub = uiad.getUiDeviceStub();
        IUiObject uiObjectStub = uiad.getUiObjectStub();

        while (true) {
            uiDeviceStub.pressHome();
            uiDeviceStub.waitForIdle();
            uiDeviceStub.click(500, 500);

            LOG.debug(uiDeviceStub.getDisplayWidth() + "/" + uiDeviceStub.getDisplayHeight());
            Point p = uiDeviceStub.getDisplaySizeDp();
            LOG.debug(p.x + "/" + p.y);
            uiDeviceStub.swipe(100, 0, 100, 500, 2);
            LOG.debug(uiDeviceStub.getCurrentActivityName());

            uiDeviceStub.swipe(new Point[]{new Point(100, 500), new Point(100, 0)}, 2);
            uiDeviceStub.swipe(100, 500, 100, 0, 2);
            LOG.debug(uiDeviceStub.getCurrentActivityName());

            uiDeviceStub.pressHome();
            uiDeviceStub.waitForIdle();
            UiSelector selector = new UiSelector().text("Apps");
            uiObjectStub.useSelector(selector);
            uiObjectStub.click();
        }
    }
}
