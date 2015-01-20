package com.tascape.qa.th.comm;

import com.android.uiautomator.stub.IUiDevice;
import java.io.IOException;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Client;

/**
 *
 * @author linsong wang
 */
public class AndroidUiAutomatorDevice {
    private final IUiDevice device;

    public AndroidUiAutomatorDevice(int port) throws IOException {
        CallHandler callHandler = new CallHandler();
        Client client = new Client("localhost", port, callHandler);
        this.device = IUiDevice.class.cast(client.getGlobal(IUiDevice.class));
    }

    public IUiDevice getDevice() {
        return device;
    }
}
