package com.android.uiautomator.stub;

import com.android.uiautomator.testrunner.UiAutomatorTestCase;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Server;

/**
 * cd <eclipse-project-folder>
 * <android-sdk>/tools/android create uitest-project -n ui_rmi_server -t 1 -p .
 * ant build
 * adb push bin/ui_rmi_server.jar /data/local/tmp/
 * adb shell uiautomator runtest ui_rmi_server.jar -c com.android.uiautomator.stub.UiRmiServer
 *
 * adb forward --remove tcp:local_port
 * adb forward tcp:local_port tcp:8998
 *
 * @author linsong wang
 */
public class UiRmiServer extends UiAutomatorTestCase {
    private static final CallHandler callHandler = new CallHandler();

    static {
        try {
            Server server = new Server();
            server.bind(8998, callHandler);

            callHandler.registerGlobal(IUiDevice.class, new UiDeviceStub());
            callHandler.registerGlobal(IUiObject.class, new UiObjectStub());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void testRmiServer() throws Exception {
        while (true) {
            System.out.println("UiAutomator RMI Server is running");
            Thread.sleep(60000);
        }
    }
}
