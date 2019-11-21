/*
 * Copyright (c) 2015 - present Nebula Bay.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tascape.reactor;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of utility methods.
 * <p>
 * @author linsong wang
 */
public class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static final String PASS = Utils.class + "_PASS";

    public static final String FAIL = Utils.class + "_FAIL";

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd' 'HH:mm:ss.SSS";

    private static final String TIME_FORMAT = "HH:mm:ss.SSS";

    private static final String DATE_TIME_STRING = "yyyy_MM_dd_HH_mm_ss_SSS";

    /**
     * system specific file path separator, "/" for Linux, and "\" for Windows, etc
     */
    public static final String FS = System.getProperty("file.separator");

    public static void openFile(File file) {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(file);
        } catch (IOException ex) {
            LOG.warn("Cannot open file {}", file, ex);
        }
    }

    private Utils() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Please use Utils.method");
    }

    public static String formatDateTime(long milliSinceEpoch) {
        return new SimpleDateFormat(DATE_TIME_FORMAT).format(new Date(milliSinceEpoch));
    }

    public static String formatTime(long milliSinceEpoch) {
        return new SimpleDateFormat(TIME_FORMAT).format(new Date(milliSinceEpoch));
    }

    public static String formatTime(long milliSinceEpoch, String format) {
        DateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date(milliSinceEpoch));
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat(DATE_TIME_STRING).format(System.currentTimeMillis());
    }

    public static String getCurrentTime(String format) {
        DateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date());
    }

    /**
     * Executes command, and waits for the expected phrase in console printout.
     *
     * @param command command line
     *
     * @return console output as a list of strings
     *
     * @throws IOException          for command error
     * @throws InterruptedException when interrupted
     */
    public static List<String> cmd(String command) throws IOException, InterruptedException {
        return cmd(command.split(" "));
    }

    /**
     * Executes command, and waits for the expected phrase in console printout.
     *
     * @param command command line
     *
     * @return console output as a list of strings
     *
     * @throws IOException          for command error
     * @throws InterruptedException when interrupted
     */
    public static List<String> cmd(String[] command) throws IOException, InterruptedException {
        return cmd(command, null, null, 300000L, null);
    }

    /**
     * Executes command, and waits for the expected phrase in console printout.
     *
     * @param command    command line
     * @param workindDir working directory
     * @param timeout    in millisecond
     *
     * @return console output as a list of strings
     *
     * @throws IOException          for command error
     * @throws InterruptedException when interrupted
     */
    public static List<String> cmdWithWorkingDir(String[] command, String workindDir, final long timeout)
        throws IOException, InterruptedException {
        return cmd(command, null, null, timeout, workindDir);
    }

    /**
     * Executes command, and waits for the expected phrase in console printout.
     * <p>
     * @param command  command line
     * @param expected some expected output
     *
     * @return console output as a list of strings
     *
     * @throws IOException          for command error
     * @throws InterruptedException when interrupted
     */
    public static List<String> cmd(String[] command, String expected) throws IOException, InterruptedException {
        return cmd(command, expected, null, 300000L, null);
    }

    /**
     * Executes command, and waits for the expected pass/fail phrase in console printout within timeout,
     *
     * @param command    command line
     * @param pass       skip checking if null
     * @param fail       skip checking if null
     * @param timeout    set 0 for not to check the output message, otherwise, waiting for timeout
     * @param workingDir working directory
     *
     * @return console output as a list of strings
     *
     * @throws IOException          for command error
     * @throws InterruptedException when interrupted
     */
    public static List<String> cmd(String[] command, final String pass, final String fail, final long timeout,
        final String workingDir) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (workingDir != null) {
            pb.directory(new File(workingDir));
        }
        pb.redirectErrorStream(true);
        LOG.debug("Running command: " + pb.command().toString().replace(",", ""));
        final Process p = pb.start();
        Thread thread;
        final List<String> output = new ArrayList<>();

        if (timeout == 0) {
            LOG.debug("This is a start-and-exit command");
            output.add(PASS);
            return output;
        } else {
            thread = new Thread() {
                @Override
                public void run() {
                    try {
                        LOG.debug("Command timeouts in {} ms", timeout);
                        Thread.sleep(timeout);
                        try {
                            p.exitValue();
                        } catch (IllegalThreadStateException ex) {
                            LOG.debug("killing subprocess {} - {}", p, ex.getMessage());
                            p.destroy();
                        }
                    } catch (InterruptedException ex) {
                        LOG.trace(ex.getMessage());
                    }
                }
            };
            thread.setName(Thread.currentThread().getName() + "-" + p.hashCode());
            thread.setDaemon(true);
            thread.start();
        }

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String c = p + " - ";
        for (String line = stdIn.readLine(); line != null;) {
            LOG.trace("{}{}", c, line);
            output.add(line);
            try {
                line = stdIn.readLine();
            } catch (IOException ex) {
                LOG.warn(ex.getMessage());
                break;
            }
        }
        LOG.debug("Command exit code {}", p.waitFor());
        thread.interrupt();
        try {
            stdIn.close();
        } catch (IOException ex) {
            LOG.warn("", ex);
        }

        for (String s : output) {
            if (pass != null && (s.contains(pass) || s.matches(pass))) {
                output.add(PASS);
                break;
            } else if (fail != null && s.contains(fail)) {
                output.add(FAIL);
                break;
            }
        }
        return output;
    }

    public static Process cmdAsync(String[] commands) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true);
        LOG.debug("Running command {}", pb.command().toString().replaceAll(",", ""));
        return pb.start();
    }

    public static Process cmd(String[] commands, final File file) throws IOException {
        return cmd(commands, file, null, new String[0]);
    }

    public static Process cmd(String[] commands, final File file, final File workingDir) throws IOException {
        return cmd(commands, file, workingDir, new String[0]);
    }

    public static Process cmd(String[] commands, final File file, final File workingDir, final String... ignoreRegex)
        throws IOException {
        FileUtils.touch(file);
        LOG.debug("Saving console output to {}", file.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true);
        pb.directory(workingDir);
        LOG.debug("Running command {}:  {}", workingDir == null ? "" : workingDir.getAbsolutePath(),
            pb.command().toString().replaceAll(",", ""));
        final Process p = pb.start();

        Thread t = new Thread(Thread.currentThread().getName() + "-" + p.hashCode()) {
            @Override
            public void run() {
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String console = "console-" + stdIn.hashCode();
                try (PrintWriter pw = new PrintWriter(file)) {
                    for (String line = stdIn.readLine(); line != null;) {
                        LOG.trace("{}: {}", console, line);
                        if (null == ignoreRegex || ignoreRegex.length == 0) {
                            pw.println(line);
                        } else {
                            boolean ignore = false;
                            for (String regex : ignoreRegex) {
                                if (!regex.isEmpty() && (line.contains(regex) || line.matches(regex))) {
                                    ignore = true;
                                    break;
                                }
                            }
                            if (!ignore) {
                                pw.println(line);
                            }
                        }
                        pw.flush();
                        line = stdIn.readLine();
                    }
                } catch (IOException ex) {
                    LOG.warn(ex.getMessage());
                }
                LOG.trace("command is done");
            }
        };
        t.setDaemon(true);
        t.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (p != null) {
                    p.destroy();
                }
            }
        });
        return p;
    }

    public static void waitForProcess(final Process process, final int timeoutSeconds) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeoutSeconds * 1000);
                    LOG.warn("Timeed out in {} seconds", timeoutSeconds);
                } catch (InterruptedException ex) {
                    LOG.warn(ex.getMessage());
                } finally {
                    if (process != null) {
                        process.destroy();
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
        if (process != null) {
            int exitValue;
            try {
                exitValue = process.waitFor();
                LOG.debug("process {} exits with {}", process, exitValue);
            } catch (InterruptedException ex) {
                LOG.warn(ex.getMessage());
            }
        }
    }

    public static void waitForOutput(final Process process, final int timeoutSeconds) throws IOException {
        waitForOutputLine(process, null, timeoutSeconds);
    }

    public static boolean waitForOutputLine(final Process process, String lineExpected, final int timeoutSeconds)
        throws IOException {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeoutSeconds * 1000);
                    LOG.warn("Timed out in {} seconds", timeoutSeconds);
                } catch (InterruptedException ex) {
                    LOG.warn(ex.getMessage());
                } finally {
                    if (process != null) {
                        process.destroy();
                    }
                }
            }
        };
        if (process == null) {
            return false;
        }
        t.setName(Thread.currentThread().getName() + "-" + t.hashCode());
        t.setDaemon(true);
        t.start();

        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            for (String line = stdIn.readLine(); line != null;) {
                LOG.debug(line);
                if (StringUtils.isNotBlank(lineExpected) && line.contains(lineExpected)) {
                    LOG.info("Found expected line '{}'", line);
                    return true;
                }
                line = stdIn.readLine();
            }
        } finally {
            t.interrupt();
        }

        return false;
    }

    public static void deleteFileAfterMinutes(final File file, final int minutes) {
        file.deleteOnExit();
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(minutes * 60000);
                    FileUtils.deleteQuietly(file);
                } catch (InterruptedException ex) {
                    LOG.trace(ex.getMessage());
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    public static String getUniqueId() {
        return getUniqueId("");
    }

    public static String getUniqueId(String prefix) {
        return new StringBuilder(prefix).append(UUID.randomUUID()).toString().replaceAll("-", "_");
    }

    public static void sleep(long millis, String message) throws InterruptedException {
        sleep(millis, 5000, message);
    }

    public static void sleep(long millis, int interval, String message) throws InterruptedException {
        long end = System.currentTimeMillis() + millis;
        while (true) {
            LOG.debug("{}", message);
            long t = end - System.currentTimeMillis();
            if (t > interval) {
                Thread.sleep(interval);
            } else if (t > 0) {
                Thread.sleep(t);
                break;
            } else {
                break;
            }
        }
    }

    public static String getKeepAliveFileName(String fileName) {
        return SystemConfiguration.CONSTANT_LOG_KEEP_ALIVE_PREFIX + fileName;
    }

    public static File getKeepAliveFile(File file) {
        String name = Utils.getKeepAliveFileName(file.getName());
        return Paths.get(file.getParent(), name).toFile();
    }

    /**
     *
     * @param path          directory to clean
     * @param keepAliveHour any file/directory having last modified time longer than keepAliveHour will be deleted
     * @param namePrefix    file name prefix
     */
    public static void cleanDirectory(final String path, final float keepAliveHour, final String namePrefix) {
        final long intervalMillis = 3600000;
        final File dir = new File(path);
        if (!dir.exists()) {
            return;
        }
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    long lastModifiedMillis = (long) (System.currentTimeMillis() - keepAliveHour * 3600000);
                    Collection<File> files = FileUtils.listFiles(dir, null, true);
                    for (File file : files) {
                        if (file.lastModified() < lastModifiedMillis && file.getName().startsWith(namePrefix)) {
                            LOG.debug("Delete {}", file);
                            if (!FileUtils.deleteQuietly(file)) {
                                LOG.debug("Cannot delete {}", file);
                            }
                        }
                    }
                    try {
                        LOG.debug("Waiting for next cleanup...");
                        Thread.sleep(intervalMillis);
                    } catch (InterruptedException ex) {
                        LOG.warn(ex.getMessage());
                        return;
                    }
                }
            }
        };
        thread.setName(Thread.currentThread().getName() + "-cleaning-" + thread.hashCode());
        thread.setDaemon(true);
        LOG.debug("Starting directory cleaning thread (scanning hourly), all files/directories in {} and older than {} "
            + "hour(s) and starts with {} will be deleted", dir, keepAliveHour, namePrefix);
        thread.start();
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os.contains("Windows");
    }

    public static String buildClassPath(String[] paths) {
        String classPathDelimiter = Utils.isWindows() ? ";" : ":";
        return StringUtils.join(paths, classPathDelimiter).replaceAll("\\\\", "/");
    }

    /**
     *
     * @param png picture file name
     *
     * @throws AWTException UI related exception
     * @throws IOException  file IO issue
     */
    public static void captureScreen(File png) throws AWTException, IOException {
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        ImageIO.write(image, "png", png);
        LOG.debug("Save screenshot to {}", png.getAbsolutePath());
    }

    public static long getTime(String time, String format) throws ParseException {
        DateFormat formatter = new SimpleDateFormat(format);
        Date date = formatter.parse(time);
        return date.getTime();
    }

    // dirty hack to update system env
    public static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField(
                "theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        {
            Process p = cmdAsync(new String[]{"./ping.sh"});
            waitForOutput(p, 11);
        }
    }
}
