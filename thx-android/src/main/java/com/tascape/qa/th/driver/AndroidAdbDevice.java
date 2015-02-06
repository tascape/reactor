package com.tascape.qa.th.driver;

import com.tascape.qa.th.comm.Adb;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class AndroidAdbDevice extends EntityDriver {
    private static final Logger LOG = LoggerFactory.getLogger(AndroidAdbDevice.class);

    protected Adb adb;

    public void setAdb(Adb adb) {
        this.adb = adb;
    }

    public File logTouchEvents(int seconds) throws IOException {
        File log = File.createTempFile("TouchEvents", ".log");
        this.adb.shellAsync(Arrays.asList(new Object[]{"getevent", "-lt", "/dev/input/event2"}), seconds * 1000L, log);
        return log;
    }

    public String recordScreen(int seconds, int bitRate) throws IOException {
        String mp4 = "/sdcard/ScreenRecording.mp4";
        this.adb.shellAsync(Arrays.asList(new Object[]{"screenrecord", "--time-limit", seconds, mp4, "--bit-rate",
            bitRate}), seconds * 1000L);
        return mp4;
    }

    /**
     *
     * @param eventLogFile
     *
     * @return
     *
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<Long> getTouchEvents(File eventLogFile) throws FileNotFoundException, IOException {
        List<Long> events = new ArrayList<>();
        BufferedReader bis = new BufferedReader(new FileReader(eventLogFile));
        String line = "";

        Pattern patternEvent = Pattern.compile("\\[(.+?)\\].+");
        while (line != null) {
            Matcher matcherEvent = patternEvent.matcher(line);
            if (matcherEvent.matches()) {
                LOG.trace("start time {}", line);
                String ts = matcherEvent.group(1);
                Double d = Double.parseDouble(ts);
                events.add((long) (d * 1000000));
                break;
            }
            line = bis.readLine();
        }

        Pattern patternButtonUp = Pattern.compile("\\[(.+?)\\] EV_KEY.+?BTN_TOUCH.+?UP.+");
        while (line != null) {
            Matcher matcherUp = patternButtonUp.matcher(line);
            if (matcherUp.matches()) {
                String ts = matcherUp.group(1);
                Double d = Double.parseDouble(ts);
                events.add((long) (d * 1000000));
            }
            line = bis.readLine();
        }

        if (!events.isEmpty()) {
            long start = events.get(0);
            for (int i = 0; i < events.size(); i++) {
                long time = events.get(i);
                events.set(i, time - start);
            }
            events.remove(0);
        }
        return events;
    }

    /**
     *
     * @param screenRecordFile
     *
     * @return
     *
     * @throws java.io.IOException
     * @throws org.bytedeco.javacv.FrameGrabber.Exception
     * @throws java.lang.InterruptedException
     */
    public List<Long> getScreenUpdates(String screenRecordFile) throws IOException, FrameGrabber.Exception,
            InterruptedException {
        File mp4 = this.getScreenRecord(screenRecordFile);
        LOG.debug("{}", mp4);
        List<Long> updates = new ArrayList<>();

        Path p = Paths.get(System.getProperty("user.home"), "touch" + System.currentTimeMillis());
        File dir = p.toFile();
        dir.mkdirs();
        LOG.trace("Save frames to {}", dir);

        FFmpegFrameGrabber g = new FFmpegFrameGrabber(mp4.getAbsolutePath());
        g.start();

        BufferedImage previousImage = null;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            Frame f = g.grabFrame();
            if (f == null) {
                break;
            }
            BufferedImage currentImage = f.image.getBufferedImage();
            long ts = g.getTimestamp();
            if (previousImage != null) {
                if (!this.bufferedImagesEqual(previousImage, currentImage)) {
                    updates.add(ts);
                }
            }

            File name = new File(dir, String.format("frame_%08d.png", i));
            ImageIO.write(currentImage, "png", name);
            previousImage = ImageIO.read(name);
        }

        LOG.debug("total frames {}", g.getFrameNumber());
        LOG.debug("total length {}", g.getLengthInTime());

        g.stop();
        return updates;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void reset() throws Exception {
    }

    private File getScreenRecord(String path) throws IOException {
        File mp4 = File.createTempFile("ScreenRecording", ".mp4");
        this.adb.pull(path, mp4);
        return mp4;
    }

    private boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            return false;
        }

        boolean equal = true;
        for (int x = 100; x < img1.getWidth() - 100; x++) {
            for (int y = 100; y < img1.getHeight() - 100; y++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    equal = false;
                }
            }
        }
        return equal;
    }
}
