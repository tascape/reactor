package com.tascape.qa.th.comm;

import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.driver.AndroidUiAutomatorDevice;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class Adb extends EntityCommunication {
    private static final Logger LOG = LoggerFactory.getLogger(AndroidUiAutomatorDevice.class);

    public static final String SYSPROP_ADB_EXECUTABLE = "qa.comm.ADB_EXECUTABLE";

    static {
        LOG.debug("Please specify where adb executable is by setting system property {}={}",
                SYSPROP_ADB_EXECUTABLE, "/path/to/your/sdk/platform-tools/adb");
    }

    private final String adb = SystemConfiguration.getInstance().getProperty(SYSPROP_ADB_EXECUTABLE, "adb");

    private String serial = "";

    public Adb() {
        this("");
    }

    public Adb(String serial) {
        this.serial = serial;
    }

    @Override
    public void connect() throws Exception {

    }

    @Override
    public void disconnect() throws Exception {
    }

    public int adb(final List<String> arguments) throws IOException {
        CommandLine cmdLine = new CommandLine(adb);
        if (!this.serial.isEmpty()) {
            cmdLine.addArgument("-s");
            cmdLine.addArgument(serial);
        }
        for (String arg : arguments) {
            cmdLine.addArgument(arg);
        }
        LOG.debug("{}", cmdLine.toString());
        Executor executor = new DefaultExecutor();
        int exitValue = executor.execute(cmdLine);
        return exitValue;
    }

    public void adb0(final List<String> arguments) throws IOException {
        CommandLine cmdLine = new CommandLine(adb);
        if (!this.serial.isEmpty()) {
            cmdLine.addArgument("-s");
            cmdLine.addArgument(serial);
        }
        for (String arg : arguments) {
            cmdLine.addArgument(arg);
        }
        LOG.debug("{}", cmdLine.toString());
        Executor executor = new DefaultExecutor();
        executor.setStreamHandler(new AdbStreamHandler());
        executor.execute(cmdLine, new DefaultExecuteResultHandler());
    }

    public void setupAdbPortForward(int local, int remote) throws IOException, InterruptedException {
        List<String> cmdLine = new ArrayList<>();
        cmdLine.add("forward");
        cmdLine.add("tcp:" + local);
        cmdLine.add("tcp:" + remote);

        int exitValue = this.adb(cmdLine);
        if (exitValue != 0) {
            throw new IOException("Fail to start adb forward");
        }
        LOG.debug("Device of serial '{}' is at localhost:{}", this.serial, local);
    }

    private class AdbStreamHandler implements ExecuteStreamHandler {
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
}
