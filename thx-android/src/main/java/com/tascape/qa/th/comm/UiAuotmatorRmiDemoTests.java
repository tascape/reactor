package com.tascape.qa.th.comm;

import com.android.uiautomator.stub.IUiCollection;
import com.android.uiautomator.stub.IUiDevice;
import com.android.uiautomator.stub.IUiObject;
import com.android.uiautomator.stub.IUiScrollable;
import com.android.uiautomator.stub.Point;
import com.android.uiautomator.stub.Rect;
import com.android.uiautomator.stub.UiSelector;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class UiAuotmatorRmiDemoTests {
    private static final Logger LOG = LoggerFactory.getLogger(UiAuotmatorRmiDemoTests.class);

    private static AndroidUiAutomatorDevice uiad;

    static {
        try {
            uiad = new AndroidUiAutomatorDevice(IUiDevice.RMI_PORT);
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private final IUiDevice uiDeviceStub = uiad.getUiDeviceStub();

    private final IUiObject uiObjectStub = uiad.getUiObjectStub();

    private final IUiCollection uiCollection = uiad.getUiCollectionStub();

    private final IUiScrollable uiScrollable = uiad.getUiScrollableStub();

    public void testDemo1() throws Exception {
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
    }

    public void testDemo2() throws Exception {
        for (String app : new String[]{"Apps", "Shop", "Books", "Music", "Games"}) {
            uiDeviceStub.pressHome();
            LOG.debug(app);
            uiObjectStub.useUiObjectSelector(new UiSelector().text(app));
            Rect rect = uiObjectStub.getBounds();
            LOG.debug("{}", rect);
            uiObjectStub.swipeLeft(10);
            uiObjectStub.swipeRight(10);
            uiObjectStub.click();
            uiDeviceStub.waitForIdle();
            Thread.sleep(5000);
        }

        uiDeviceStub.pressHome();
        uiDeviceStub.waitForIdle();
        LOG.debug("Book");
        uiObjectStub.useUiObjectSelector(new UiSelector().text("Book"));
        uiObjectStub.click();
        LOG.debug("hasUiObjectNotFoundException = {}", uiObjectStub.hasUiObjectNotFoundException());
        LOG.debug("Exception!", uiObjectStub.getUiObjectNotFoundException());
        LOG.debug("hasUiObjectNotFoundException = {}", uiObjectStub.hasUiObjectNotFoundException());
    }

    public void testDemo3() throws Exception {
    }

    public static void main(String[] args) throws Exception {
        UiAuotmatorRmiDemoTests t = new UiAuotmatorRmiDemoTests();
        t.testDemo1();
        t.testDemo2();
        t.testDemo3();
    }
}
