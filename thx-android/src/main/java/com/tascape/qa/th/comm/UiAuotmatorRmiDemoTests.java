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
            uiad = new AndroidUiAutomatorDevice(IUiDevice.UIAUTOMATOR_RMI_PORT);
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private final IUiDevice uiDeviceStub = uiad.getUiDeviceStub();

    private final IUiObject uiObjectStub = uiad.getUiObjectStub();

    private final IUiCollection uiCollectionStub = uiad.getUiCollectionStub();

    private final IUiScrollable uiScrollableStub = uiad.getUiScrollableStub();

    public void testUiDevice() throws Exception {
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

    public void testUiObject() throws Exception {
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
        }
    }

    public void testUiObject2() throws Exception {
        uiDeviceStub.pressHome();
        uiDeviceStub.waitForIdle();
        LOG.debug("Book");
        uiObjectStub.useUiObjectSelector(new UiSelector().text("Book"));
        uiObjectStub.click();
        LOG.debug("hasUiObjectNotFoundException = {}", uiObjectStub.hasUiObjectNotFoundException());
        LOG.debug("Exception!", uiObjectStub.getUiObjectNotFoundException());
        LOG.debug("hasUiObjectNotFoundException = {}", uiObjectStub.hasUiObjectNotFoundException());
    }

    public void testUiCollection() throws Exception {
        uiDeviceStub.pressHome();
        uiDeviceStub.waitForIdle();

        this.uiCollectionStub.useUiCollectionSelector(new UiSelector().resourceId(
                "com.amazon.kindle.otter:id/library_selector_layout"));
        int n = this.uiCollectionStub.getChildCount(new UiSelector().className("android.widget.Button"));
        LOG.debug("buttons {}", n);
        for (int i = 0; i < n; i++) {
            uiDeviceStub.pressHome();
            this.uiCollectionStub.selectChildByInstance(new UiSelector().className("android.widget.Button"), i);
            LOG.debug("text {}, rect {}", this.uiCollectionStub.getText(), this.uiCollectionStub.getBounds());
            this.uiCollectionStub.click();
            uiDeviceStub.waitForIdle();
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            while (true) {
                UiAuotmatorRmiDemoTests t = new UiAuotmatorRmiDemoTests();
                t.testUiDevice();
                t.testUiObject();
                t.testUiObject2();
                t.testUiCollection();
            }
        } finally {
            uiad.disconnect();
            System.exit(0);
        }
    }
}
