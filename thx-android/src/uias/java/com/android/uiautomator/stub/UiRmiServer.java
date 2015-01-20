package com.android.uiautomator.stub;

import com.android.uiautomator.testrunner.UiAutomatorTestCase;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Server;

/**
 *
 * @author linsong wang
 */
public class UiRmiServer extends UiAutomatorTestCase {
    private static final CallHandler callHandler = new CallHandler();

    static {
        try {
            Server server = new Server();
            server.bind(8998, callHandler);
            /**
             * adb forward --remove-all
             * adb forward tcp:8998 tcp:8998
             */

            callHandler.registerGlobal(IUiDevice.class, new UiDeviceStub());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void testRmiServer() throws Exception {
        while (true) {
            System.out.println("UiAutomatorServer is running");
            Thread.sleep(60000);
        }
    }
}
