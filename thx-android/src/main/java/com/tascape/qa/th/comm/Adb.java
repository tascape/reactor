package com.tascape.qa.th.comm;

import com.tascape.qa.th.SystemConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public final class Adb extends EntityCommunication {
    private static final Logger LOG = LoggerFactory.getLogger(Adb.class);

    public static final String SYSPROP_ADB_EXECUTABLE = "qa.comm.ADB_EXECUTABLE";

    static {
        LOG.debug("Please specify where adb executable is by setting system property {}={}",
                SYSPROP_ADB_EXECUTABLE, "/path/to/your/sdk/platform-tools/adb");
    }

    private final static String ADB = SystemConfiguration.getInstance().getProperty(SYSPROP_ADB_EXECUTABLE, "adb");

    private String serial = "";

    public static void reset() throws IOException {
        CommandLine cmdLine = new CommandLine(ADB);
        cmdLine.addArgument("kill-server");
        LOG.debug("{}", cmdLine.toString());
        Executor executor = new DefaultExecutor();
        if (executor.execute(cmdLine) != 0) {
            throw new IOException(cmdLine + " failed");
        }
        cmdLine = new CommandLine(ADB);
        cmdLine.addArgument("devices");
        LOG.debug("{}", cmdLine.toString());
        executor = new DefaultExecutor();
        if (executor.execute(cmdLine) != 0) {
            throw new IOException(cmdLine + " failed");
        }
    }

    public Adb() throws IOException {
        this("");
    }

    public Adb(String serial) throws IOException {
        if (serial == null || serial.isEmpty()) {
            List<String> output = this.adb(Arrays.asList(new Object[]{"devices"}));
            for (String line : output) {
                if (line.endsWith("device")) {
                    this.serial = line.split("\\t")[0];
                    break;
                }
            }
        } else {
            this.serial = serial;
        }
        LOG.debug("serial number {}", this.serial);
    }

    @Override
    public void connect() throws Exception {
    }

    @Override
    public void disconnect() throws Exception {
    }

    public List<String> adb(final List<Object> arguments) throws IOException {
        CommandLine cmdLine = new CommandLine(ADB);
        if (!this.serial.isEmpty()) {
            cmdLine.addArgument("-s");
            cmdLine.addArgument(serial);
        }
        for (Object arg : arguments) {
            cmdLine.addArgument(arg + "");
        }
        LOG.debug("{}", cmdLine.toString());
        List<String> output = new ArrayList<>();
        Executor executor = new DefaultExecutor();
        executor.setStreamHandler(new AdbStreamHandler(output));
        if (executor.execute(cmdLine) != 0) {
            throw new IOException(cmdLine + " failed");
        }
        return output;
    }

    public List<String> shell(final List<Object> arguments) throws IOException {
        List<Object> args = new ArrayList<>(arguments);
        args.add(0, "shell");
        return adb(args);
    }

    public ExecuteWatchdog shellAsync(final List<Object> arguments, long timeoutMillis) throws IOException {
        return shellAsync(arguments, timeoutMillis, null);
    }

    public ExecuteWatchdog shellAsync(final List<Object> arguments, long timeoutMillis, File output) throws IOException {
        List<Object> args = new ArrayList<>(arguments);
        args.add(0, "shell");
        return adbAsync(args, timeoutMillis, output);
    }

    public ExecuteWatchdog adbAsync(final List<Object> arguments, long timeoutMillis, File output) throws IOException {
        CommandLine cmdLine = new CommandLine(ADB);
        if (!this.serial.isEmpty()) {
            cmdLine.addArgument("-s");
            cmdLine.addArgument(serial);
        }
        for (Object arg : arguments) {
            cmdLine.addArgument(arg + "");
        }
        LOG.debug("{}", cmdLine.toString());
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeoutMillis);
        Executor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(new AdbStreamToFileHandler(output));
        executor.execute(cmdLine, new DefaultExecuteResultHandler());

        return watchdog;
    }

    public void pull(String device, File local) throws IOException {
        if (local.exists() && !local.delete()) {
            throw new IOException("Cannot delete existing local file");
        }
        this.adb(Arrays.asList(new Object[]{"pull", device, local.getAbsolutePath()}));
        if (!local.exists()) {
            throw new IOException("Cannot pull file from device to local");
        }
    }

    public void setupAdbPortForward(int local, int remote) throws IOException, InterruptedException {
        List<Object> cmdLine = new ArrayList<>();
        cmdLine.add("forward");
        cmdLine.add("tcp:" + local);
        cmdLine.add("tcp:" + remote);

        this.adb(cmdLine);
        LOG.debug("Device of serial '{}' is at localhost:{}", this.serial, local);
    }

    private class AdbStreamHandler implements ExecuteStreamHandler {
        private final List<String> output;

        AdbStreamHandler(List<String> output) {
            this.output = output;
        }

        @Override
        public void setProcessInputStream(OutputStream out) throws IOException {
            LOG.trace("setProcessInputStream");
        }

        @Override
        public void setProcessErrorStream(InputStream in) throws IOException {
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));
            do {
                String line = bis.readLine();
                if (line == null) {
                    break;
                }
                LOG.error(line);
            } while (true);
        }

        @Override
        public void setProcessOutputStream(InputStream in) throws IOException {
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while (line != null) {
                LOG.trace(line);
                output.add(line);
                line = bis.readLine();
            }
        }

        @Override
        public void start() throws IOException {
            LOG.trace("start");
        }

        @Override
        public void stop() throws IOException {
            LOG.trace("stop");
        }
    }

    private class AdbStreamToFileHandler implements ExecuteStreamHandler {
        File output;

        AdbStreamToFileHandler(File output) {
            this.output = output;
        }

        @Override
        public void setProcessInputStream(OutputStream out) throws IOException {
            LOG.trace("setProcessInputStream");
        }

        @Override
        public void setProcessErrorStream(InputStream in) throws IOException {
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));
            do {
                String line = bis.readLine();
                if (line == null) {
                    break;
                }
                LOG.error(line);
            } while (true);
        }

        @Override
        public void setProcessOutputStream(InputStream in) throws IOException {
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));
            if (this.output == null) {
                String line = "";
                while (line != null) {
                    LOG.trace(line);
                    line = bis.readLine();
                }

            } else {
                PrintWriter pw = new PrintWriter(this.output);
                LOG.debug("Log stdout to {}", this.output);
                String line = "";
                try {
                    while (line != null) {
                        pw.println(line);
                        pw.flush();
                        line = bis.readLine();
                    }
                } finally {
                    pw.flush();
                }
            }
        }

        @Override
        public void start() throws IOException {
            LOG.trace("start");
        }

        @Override
        public void stop() throws IOException {
            LOG.trace("stop");
        }
    }

    public static void main(String[] args) throws Exception {
        Adb adb = new Adb();
    }
}
