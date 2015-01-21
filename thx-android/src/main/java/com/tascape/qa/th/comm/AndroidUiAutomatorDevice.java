package com.tascape.qa.th.comm;

import com.android.uiautomator.stub.IUiDevice;
import com.android.uiautomator.stub.IUiObject;
import java.io.IOException;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Client;

/**
 *
 * @author linsong wang
 */
public class AndroidUiAutomatorDevice {
    private final IUiDevice uiDeviceStub;

    private final IUiObject uiObjectStub;

    public AndroidUiAutomatorDevice(String serial, int port) throws IOException {
        CallHandler callHandler = new CallHandler();
        Client client = new Client("localhost", port, callHandler);
        this.uiDeviceStub = IUiDevice.class.cast(client.getGlobal(IUiDevice.class));
        this.uiObjectStub = IUiObject.class.cast(client.getGlobal(IUiObject.class));
    }

    public IUiDevice getUiDeviceStub() {
        return uiDeviceStub;
    }

    public IUiObject getUiObjectStub() {
        return uiObjectStub;
    }
}
