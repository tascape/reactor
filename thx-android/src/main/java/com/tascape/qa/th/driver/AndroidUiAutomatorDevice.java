package com.tascape.qa.th.driver;

import com.android.uiautomator.stub.IUiCollection;
import com.android.uiautomator.stub.IUiDevice;
import com.android.uiautomator.stub.IUiObject;
import com.android.uiautomator.stub.IUiScrollable;
import com.tascape.qa.th.SystemConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class AndroidUiAutomatorDevice extends AndroidAdbDevice {
    private static final Logger LOG = LoggerFactory.getLogger(AndroidUiAutomatorDevice.class);

    public static final String SYSPROP_UIAUTOMATOR_RMI_SERVER = "qa.comm.UIAUTOMATOR_RMI_SERVER";

    public static final String UIAUTOMATOR_RMI_SERVER = "uiautomator_rmi_server.jar";

    static {
        LOG.debug("Please specify where uiautomator RMI server jar is by setting system property {}={}",
            SYSPROP_UIAUTOMATOR_RMI_SERVER, "/path/to/your/" + UIAUTOMATOR_RMI_SERVER);
    }

    private final String ip = "localhost";

    private int port = IUiDevice.UIAUTOMATOR_RMI_PORT;

    private Client client;

    private IUiDevice uiDeviceStub;

    private IUiObject uiObjectStub;

    private IUiCollection uiCollectionStub;

    private IUiScrollable uiScrollableStub;

    private final String uiRmiServer = SystemConfiguration.getInstance().getProperty(SYSPROP_UIAUTOMATOR_RMI_SERVER,
        UIAUTOMATOR_RMI_SERVER);

    public AndroidUiAutomatorDevice(int port) throws IOException, InterruptedException {
        this.port = port;
    }

    public void init() throws IOException, InterruptedException {
        this.setupUiAutomatorRmiServer();

        this.adb.setupAdbPortForward(port, IUiDevice.UIAUTOMATOR_RMI_PORT);

        CallHandler callHandler = new CallHandler();
        client = new Client(this.ip, this.port, callHandler);
        this.uiDeviceStub = IUiDevice.class.cast(client.getGlobal(IUiDevice.class));
        this.uiObjectStub = IUiObject.class.cast(client.getGlobal(IUiObject.class));
        this.uiCollectionStub = IUiCollection.class.cast(client.getGlobal(IUiCollection.class));
        this.uiScrollableStub = IUiScrollable.class.cast(client.getGlobal(IUiScrollable.class));
        LOG.debug("Device product name '{}'", this.uiDeviceStub.getProductName());
    }

    @Override
    public String getName() {
        return AndroidUiAutomatorDevice.class.getSimpleName();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    public IUiDevice getUiDeviceStub() {
        return uiDeviceStub;
    }

    public IUiObject getUiObjectStub() {
        return uiObjectStub;
    }

    public IUiCollection getUiCollectionStub() {
        return uiCollectionStub;
    }

    public IUiScrollable getUiScrollableStub() {
        return uiScrollableStub;
    }

    private void setupUiAutomatorRmiServer() throws IOException, InterruptedException {
        List<String> cmdLine = new ArrayList<>();
        cmdLine.add("push");
        cmdLine.add(uiRmiServer);
        cmdLine.add("/data/local/tmp/");
        int exitValue = adb.adb(cmdLine);
        if (exitValue != 0) {
            throw new IOException("Fail to push ui_rmi_server.jar onto device");
        }

        cmdLine = new ArrayList();
        cmdLine.add("shell");
        cmdLine.add("uiautomator");
        cmdLine.add("runtest");
        cmdLine.add(UIAUTOMATOR_RMI_SERVER);
        cmdLine.add("-c");
        cmdLine.add("com.android.uiautomator.stub.UiAutomatorRmiServer");
        this.adb.adbAsync(cmdLine);

        Thread.sleep(5000);
    }
}
