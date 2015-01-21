package com.android.uiautomator.stub;

import java.io.Serializable;
import java.io.File;
import net.sf.lipermi.exception.LipeRMIException;

/**
 *
 * @author linsong wang
 */
public interface IUiDevice extends Serializable {

    /**
     * Clears the text from the last UI traversal event. See
     * {@link #getLastTraversedText()}.
     */
    void clearLastTraversedText();

    /**
     * Perform a click at arbitrary coordinates specified by the user
     *
     * @param x
     *          coordinate
     * @param y
     *          coordinate
     *
     * @return true if the click succeeded else false
     */
    boolean click(int x, int y);

    /**
     * Helper method used for debugging to dump the current window's layout
     * hierarchy. The file root location is /data/local/tmp
     *
     * @param fileName
     */
    void dumpWindowHierarchy(String fileName);

    /**
     * Disables the sensors and freezes the device rotation at its current
     * rotation state.
     *
     * @throws LipeRMIException
     */
    void freezeRotation() throws LipeRMIException;

    /**
     * Retrieves the last activity to report accessibility events.
     *
     * @return String name of activity
     */
    String getCurrentActivityName();

    /**
     * Retrieves the name of the last package to report accessibility events.
     *
     * @return String name of package
     */
    String getCurrentPackageName();

    /**
     * Gets the height of the display, in pixels. The size is adjusted based on
     * the current orientation of the display.
     *
     * @return height in pixels or zero on failure
     */
    int getDisplayHeight();

    /**
     * Returns the current rotation of the display, as defined in
     * {@link Surface}
     *
     * @return
     */
    int getDisplayRotation();

    /**
     * Returns the display size in dp (device-independent pixel)
     *
     * The returned display size is adjusted per screen rotation
     *
     * @return a Point containing the display size in dp
     */
    public Point getDisplaySizeDp();

    /**
     * Returns the display width in dp (device-independent pixel)
     *
     * The returned display width is adjusted per screen rotation
     *
     * @return width in dp
     *
     * @hide
     */
    int getDisplayWidthDp();

    /**
     * Returns the display height in dp (device-independent pixel)
     *
     * The returned display height is adjusted per screen rotation
     *
     * @return height in dp
     *
     * @hide
     */
    int getDisplayHeightDp();

    /**
     * Gets the width of the display, in pixels. The width and height details
     * are reported based on the current orientation of the display.
     *
     * @return width in pixels or zero on failure
     */
    int getDisplayWidth();

    /**
     * Retrieves the text from the last UI traversal event received.
     *
     * You can use this method to read the contents in a WebView container
     * because the accessibility framework fires events as each text is
     * highlighted. You can write a test to perform directional arrow presses to
     * focus on different elements inside a WebView, and call this method to get
     * the text from each traversed element. If you are testing a view container
     * that can return a reference to a Document Object Model (DOM) object, your
     * test should use the view's DOM instead.
     *
     * @return text of the last traversal event, else return an empty string
     */
    String getLastTraversedText();

    /**
     * Retrieves the product name of the device.
     *
     * This method provides information on what type of device the test is
     * running on. If you are trying to test for different types of UI screen
     * sizes, your test should use {@link UiDevice#getDisplaySizeDp()} instead.
     * This value is the same returned by invoking #adb shell getprop
     * ro.product.name.
     *
     * @return product name of the device
     */
    String getProductName();

    /**
     * Checks if any registered {@link UiWatcher} have triggered.
     *
     * See {@link #registerWatcher(String, UiWatcher)} See
     * {@link #hasWatcherTriggered(String)}
     *
     * @return
     */
    boolean hasAnyWatcherTriggered();

    /**
     * Checks if a specific registered {@link UiWatcher} has triggered. See
     * {@link #registerWatcher(String, UiWatcher)}. If a UiWatcher runs and its
     * {@link UiWatcher#checkForCondition()} call returned <code>true</code>,
     * then the UiWatcher is considered triggered. This is helpful if a watcher
     * is detecting errors from ANR or crash dialogs and the test needs to know
     * if a UiWatcher has been triggered.
     *
     * @param watcherName
     *
     * @return true if triggered else false
     */
    boolean hasWatcherTriggered(String watcherName);

    /**
     * Check if the device is in its natural orientation. This is determined by
     * checking if the orientation is at 0 or 180 degrees.
     *
     * @return true if it is in natural orientation
     */
    boolean isNaturalOrientation();

    /**
     * Checks the power manager if the screen is ON.
     *
     * @return true if the screen is ON else false
     *
     * @throws LipeRMIException
     */
    boolean isScreenOn() throws LipeRMIException;

    /**
     * Simulates a short press on the BACK button.
     *
     * @return true if successful, else return false
     */
    boolean pressBack();

    /**
     * Simulates a short press on the CENTER button.
     *
     * @return true if successful, else return false
     */
    boolean pressDPadCenter();

    /**
     * Simulates a short press on the DOWN button.
     *
     * @return true if successful, else return false
     */
    boolean pressDPadDown();

    /**
     * Simulates a short press on the LEFT button.
     *
     * @return true if successful, else return false
     */
    boolean pressDPadLeft();

    /**
     * Simulates a short press on the RIGHT button.
     *
     * @return true if successful, else return false
     */
    boolean pressDPadRight();

    /**
     * Simulates a short press on the UP button.
     *
     * @return true if successful, else return false
     */
    boolean pressDPadUp();

    /**
     * Simulates a short press on the DELETE key.
     *
     * @return true if successful, else return false
     */
    boolean pressDelete();

    /**
     * Simulates a short press on the ENTER key.
     *
     * @return true if successful, else return false
     */
    boolean pressEnter();

    /**
     * Simulates a short press on the HOME button.
     *
     * @return true if successful, else return false
     */
    boolean pressHome();

    /**
     * Simulates a short press using a key code.
     *
     * See {@link KeyEvent}
     *
     * @param keyCode
     *
     * @return true if successful, else return false
     */
    boolean pressKeyCode(int keyCode);

    /**
     * Simulates a short press using a key code.
     *
     * See {@link KeyEvent}.
     *
     * @param keyCode
     *                  the key code of the event.
     * @param metaState
     *                  an integer in which each bit set to 1 represents a pressed
     *                  meta key
     *
     * @return true if successful, else return false
     */
    boolean pressKeyCode(int keyCode, int metaState);

    /**
     * Simulates a short press on the MENU button.
     *
     * @return true if successful, else return false
     */
    boolean pressMenu();

    /**
     * Simulates a short press on the Recent Apps button.
     *
     * @return true if successful, else return false
     *
     * @throws LipeRMIException
     */
    boolean pressRecentApps() throws LipeRMIException;

    /**
     * Simulates a short press on the SEARCH button.
     *
     * @return true if successful, else return false
     */
    boolean pressSearch();

    /**
     * Registers a {@link UiWatcher} to run automatically when the testing
     * framework is unable to find a match using a {@link UiSelector}. See
     * {@link #runWatchers()}
     *
     * @param name
     *                to register the UiWatcher
     * @param watcher
     *                {@link UiWatcher}
     */
//    void registerWatcher(String name, UiWatcher watcher);
    /**
     * Removes a previously registered {@link UiWatcher}.
     *
     * See {@link #registerWatcher(String, UiWatcher)}
     *
     * @param name
     *             used to register the UiWatcher
     */
    void removeWatcher(String name);

    /**
     * Resets a {@link UiWatcher} that has been triggered. If a UiWatcher runs
     * and its {@link UiWatcher#checkForCondition()} call returned
     * <code>true</code>, then the UiWatcher is considered triggered. See
     * {@link #registerWatcher(String, UiWatcher)}
     */
    void resetWatcherTriggers();

    /**
     * This method forces all registered watchers to run. See
     * {@link #registerWatcher(String, UiWatcher)}
     */
    void runWatchers();

    /**
     * Simulates orienting the device to the left and also freezes rotation by
     * disabling the sensors.
     *
     * If you want to un-freeze the rotation and re-enable the sensors see
     * {@link #unfreezeRotation()}.
     *
     * @throws LipeRMIException
     */
    void setOrientationLeft() throws LipeRMIException;

    /**
     * Simulates orienting the device into its natural orientation and also
     * freezes rotation by disabling the sensors.
     *
     * If you want to un-freeze the rotation and re-enable the sensors see
     * {@link #unfreezeRotation()}.
     *
     * @throws LipeRMIException
     */
    void setOrientationNatural() throws LipeRMIException;

    /**
     * Simulates orienting the device to the right and also freezes rotation by
     * disabling the sensors.
     *
     * If you want to un-freeze the rotation and re-enable the sensors see
     * {@link #unfreezeRotation()}.
     *
     * @throws LipeRMIException
     */
    void setOrientationRight() throws LipeRMIException;

    /**
     * This method simply presses the power button if the screen is ON else it
     * does nothing if the screen is already OFF.
     *
     * @throws LipeRMIException
     */
    void sleep() throws LipeRMIException;

    /**
     * Performs a swipe from one coordinate to another using the number of steps
     * to determine smoothness and speed. Each step execution is throttled to
     * 5ms per step. So for a 100 steps, the swipe will take about 1/2 second to
     * complete.
     *
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @param steps
     *               is the number of move steps sent to the system
     *
     * @return false if the operation fails or the coordinates are invalid
     */
    boolean swipe(int startX, int startY, int endX, int endY, int steps);

    /**
     * Performs a swipe between points in the Point array. Each step execution
     * is throttled to 5ms per step. So for a 100 steps, the swipe will take
     * about 1/2 second to complete
     *
     * @param segments
     *                     is Point array containing at least one Point object
     * @param segmentSteps
     *                     steps to inject between two Points
     *
     * @return true on success
     */
    boolean swipe(Point[] segments, int segmentSteps);

    /**
     * Take a screenshot of current window and store it as PNG
     *
     * Default scale of 1.0f (original size) and 90% quality is used
     *
     * @param storePath
     *                  where the PNG should be written to
     *
     * @return
     */
    boolean takeScreenshot(File storePath);

    /**
     * Take a screenshot of current window and store it as PNG
     *
     * The screenshot is adjusted per screen rotation;
     *
     * @param storePath
     *                  where the PNG should be written to
     * @param scale
     *                  scale the screenshot down if needed; 1.0f for original size
     * @param quality
     *                  quality of the PNG compression; range: 0-100
     *
     * @return
     */
    boolean takeScreenshot(File storePath, float scale, int quality);

    /**
     * Re-enables the sensors and un-freezes the device rotation allowing its
     * contents to rotate with the device physical rotation. During a test
     * execution, it is best to keep the device frozen in a specific orientation
     * until the test case execution has completed.
     *
     * @throws LipeRMIException
     */
    void unfreezeRotation() throws LipeRMIException;

    /**
     * Waits for the current application to idle. Default wait timeout is 10
     * seconds
     */
    void waitForIdle();

    /**
     * Waits for the current application to idle.
     *
     * @param time
     */
    void waitForIdle(long time);

    /**
     * Waits for a window content update event to occur.
     *
     * If a package name for the window is specified, but the current window
     * does not have the same package name, the function returns immediately.
     *
     * @param packageName
     *                    the specified window package name (can be <code>null</code>).
     *                    If <code>null</code>, a window update from any front-end
     *                    window will end the wait
     * @param timeout
     *                    the timeout for the wait
     *
     * @return true if a window update occurred, false if timeout has elapsed or
     *         if the current window does not have the specified package name
     */
    boolean waitForWindowUpdate(final String packageName, long timeout);

    /**
     * This method simulates pressing the power button if the screen is OFF else
     * it does nothing if the screen is already ON.
     *
     * If the screen was OFF and it just got turned ON, this method will insert
     * a 500ms delay to allow the device time to wake up and accept input.
     *
     * @throws LipeRMIException
     */
    void wakeUp() throws LipeRMIException;
}
