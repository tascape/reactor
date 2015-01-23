package com.android.uiautomator.stub;

import com.android.uiautomator.testrunner.UiAutomatorTestCase;
import java.io.IOException;
import net.sf.lipermi.exception.LipeRMIException;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Server;

/**
 * cd <eclipse-project-folder>
 * <android-sdk>/tools/android create uitest-project -n ui_rmi_server -t 1 -p .
 * ant build
 * adb push bin/ui_rmi_server.jar /data/local/tmp/
 * adb shell uiautomator runtest ui_rmi_server.jar -c com.android.uiautomator.stub.UiAutomatorRmiServer
 *
 * adb forward --remove tcp:local_port
 * adb forward tcp:local_port tcp:8998
 *
 * @author linsong wang
 */
public class UiAutomatorRmiServer extends UiAutomatorTestCase {
    private static final CallHandler callHandler = new CallHandler();

    static {
        Server server = new Server();
        try {
            server.bind(IUiDevice.RMI_PORT, callHandler);

            callHandler.registerGlobal(IUiDevice.class, new UiDeviceStub());
            callHandler.registerGlobal(IUiObject.class, new UiObjectStub());
            callHandler.registerGlobal(IUiCollection.class, new UiCollectionStub());
            callHandler.registerGlobal(IUiScrollable.class, new UiScrollableStub());
        } catch (IOException | LipeRMIException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void testRmiServer() throws Exception {
        while (true) {
            System.out.println("UiAutomator RMI Server is running");
            Thread.sleep(60000);
        }
    }
}
