package com.tascape.qa.th.driver;

import com.android.uiautomator.stub.IUiCollection;
import com.android.uiautomator.stub.IUiDevice;
import com.android.uiautomator.stub.IUiObject;
import com.android.uiautomator.stub.IUiScrollable;
import com.tascape.qa.th.SystemConfiguration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Client;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class AndroidUiAutomatorDevice extends EntityDriver {
    private static final Logger LOG = LoggerFactory.getLogger(AndroidUiAutomatorDevice.class);

    public static final String SYSPROP_ADB_EXECUTABLE = "qa.comm.ADB_EXECUTABLE";

    public static final String SYSPROP_UIAUTOMATOR_RMI_SERVER = "qa.comm.UIAUTOMATOR_RMI_SERVER";

    public static final String UIAUTOMATOR_RMI_SERVER = "uiautomator_rmi_server.jar";

    static {
        LOG.debug("Please specify where adb executable is by setting system property {}={}",
                SYSPROP_ADB_EXECUTABLE, "/path/to/your/sdk/platform-tools/adb");
        LOG.debug("Please specify where uiautomator RMI server jar is by setting system property {}={}",
                SYSPROP_UIAUTOMATOR_RMI_SERVER, "/path/to/your/" + UIAUTOMATOR_RMI_SERVER);
    }

    private String serial = "";

    private String ip = "localhost";

    private int port = IUiDevice.UIAUTOMATOR_RMI_PORT;

    private final Client client;

    private final IUiDevice uiDeviceStub;

    private final IUiObject uiObjectStub;

    private final IUiCollection uiCollectionStub;

    private final IUiScrollable uiScrollableStub;

    private final String adb = SystemConfiguration.getInstance().getProperty(SYSPROP_ADB_EXECUTABLE, "adb");

    private final String uiRmiServer = SystemConfiguration.getInstance().getProperty(SYSPROP_UIAUTOMATOR_RMI_SERVER,
            UIAUTOMATOR_RMI_SERVER);

    private final Map<String, AndroidUiAutomatorDevice> devices = new HashMap<>();

    public AndroidUiAutomatorDevice(int port) throws IOException, InterruptedException {
        this("", "", port);
    }

    public AndroidUiAutomatorDevice(String serial, String ip, int port) throws IOException, InterruptedException {
        this.serial = (serial == null) ? "" : serial;
        this.ip = (ip == null || ip.isEmpty()) ? "localhost" : ip;
        this.port = port;

        this.setupUiAutomatorRmiServer();

        if (this.ip.equals("127.0.0.1") || this.ip.equals("localhost")) {
            this.setupAdbPortForward();
        }

        CallHandler callHandler = new CallHandler();
        client = new Client(this.ip, this.port, callHandler);
        this.uiDeviceStub = IUiDevice.class.cast(client.getGlobal(IUiDevice.class));
        this.uiObjectStub = IUiObject.class.cast(client.getGlobal(IUiObject.class));
        this.uiCollectionStub = IUiCollection.class.cast(client.getGlobal(IUiCollection.class));
        this.uiScrollableStub = IUiScrollable.class.cast(client.getGlobal(IUiScrollable.class));
        LOG.debug("Device of serial '{}' is at {}:{}", this.serial, this.ip, this.port);
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
        CommandLine cmdLine = new CommandLine(adb);
        if (!this.serial.isEmpty()) {
            cmdLine.addArgument("-s");
            cmdLine.addArgument(this.serial);
        }
        cmdLine.addArgument("push");
        cmdLine.addArgument(uiRmiServer);
        cmdLine.addArgument("/data/local/tmp/");
        LOG.debug("{}", cmdLine.toString());
        Executor executor = new DefaultExecutor();
        int exitValue = executor.execute(cmdLine);
        if (exitValue != 0) {
            throw new IOException("Fail to push ui_rmi_server.jar onto device");
        }

        cmdLine = new CommandLine(adb);
        if (!this.serial.isEmpty()) {
            cmdLine.addArgument("-s");
            cmdLine.addArgument(this.serial);
        }
        cmdLine.addArgument("shell");
        cmdLine.addArgument("uiautomator");
        cmdLine.addArgument("runtest");
        cmdLine.addArgument(UIAUTOMATOR_RMI_SERVER);
        cmdLine.addArgument("-c");
        cmdLine.addArgument("com.android.uiautomator.stub.UiAutomatorRmiServer");
        executor.setStreamHandler(new StreamHandler());
        executor.execute(cmdLine, new ResultHandler());

        Thread.sleep(5000);
    }

    private void setupAdbPortForward() throws IOException, InterruptedException {
        CommandLine cmdLine = new CommandLine(adb);
        if (!this.serial.isEmpty()) {
            cmdLine.addArgument("-s");
            cmdLine.addArgument(this.serial);
        }
        cmdLine.addArgument("forward");
        cmdLine.addArgument("tcp:" + this.port);
        cmdLine.addArgument("tcp:" + IUiDevice.UIAUTOMATOR_RMI_PORT);
        LOG.debug("{}", cmdLine.toString());

        Executor executor = new DefaultExecutor();
        int exitValue = executor.execute(cmdLine);
        if (exitValue != 0) {
            throw new IOException("Fail to start adb forward");
        }
    }

    private class StreamHandler implements ExecuteStreamHandler {
        @Override
        public void setProcessInputStream(OutputStream out) throws IOException {
            LOG.debug("setProcessInputStream");
        }

        @Override
        public void setProcessErrorStream(InputStream in) throws IOException {
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while (line != null) {
                line = bis.readLine();
                LOG.debug("ERROR: {}", line);
            }
        }

        @Override
        public void setProcessOutputStream(InputStream in) throws IOException {
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while (line != null) {
                line = bis.readLine();
                LOG.debug("{}", line);
            }
        }

        @Override
        public void start() throws IOException {
            LOG.debug("start");
        }

        @Override
        public void stop() throws IOException {
            LOG.debug("stop");
        }
    }

    private class ResultHandler implements ExecuteResultHandler {
        @Override
        public void onProcessComplete(int i) {
            LOG.debug("{}", i);
        }

        @Override
        public void onProcessFailed(ExecuteException ee) {
            LOG.debug("{}", ee);
        }
    }
}
