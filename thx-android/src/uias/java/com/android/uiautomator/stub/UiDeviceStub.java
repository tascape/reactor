package com.android.uiautomator.stub;

import java.io.File;

import android.os.RemoteException;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiWatcher;

/**
 *
 * @author linsong wang
 */
public class UiDeviceStub implements IUiDevice {

    private static final long serialVersionUID = 1L;

    private final UiDevice uiDevice = UiDevice.getInstance();

    @Override
    public void clearLastTraversedText() {
        this.uiDevice.clearLastTraversedText();
    }

    @Override
    public boolean click(int x, int y) {
        return this.uiDevice.click(x, y);
    }

    @Override
    public void dumpWindowHierarchy(String fileName) {
        this.uiDevice.dumpWindowHierarchy(fileName);
    }

    @Override
    public void freezeRotation() throws RemoteException {
        this.uiDevice.freezeRotation();
    }

    @Override
    public String getCurrentActivityName() {
        return this.uiDevice.getCurrentActivityName();
    }

    @Override
    public String getCurrentPackageName() {
        return this.uiDevice.getCurrentPackageName();
    }

    @Override
    public int getDisplayHeight() {
        return this.uiDevice.getDisplayHeight();
    }

    @Override
    public int getDisplayRotation() {
        return this.uiDevice.getDisplayRotation();
    }

    @Override
    public Point getDisplaySizeDp() {
        android.graphics.Point p = this.uiDevice.getDisplaySizeDp();
        return new Point(p.x, p.y);
    }

    @Override
    public int getDisplayWidthDp() {
        return this.uiDevice.getDisplaySizeDp().x;
    }

    @Override
    public int getDisplayHeightDp() {
        return this.uiDevice.getDisplaySizeDp().y;
    }

    @Override
    public int getDisplayWidth() {
        return this.uiDevice.getDisplayWidth();
    }

    @Override
    public String getLastTraversedText() {
        return this.uiDevice.getLastTraversedText();
    }

    @Override
    public String getProductName() {
        return this.uiDevice.getProductName();
    }

    @Override
    public boolean hasAnyWatcherTriggered() {
        return this.uiDevice.hasAnyWatcherTriggered();
    }

    @Override
    public boolean hasWatcherTriggered(String watcherName) {
        return this.uiDevice.hasWatcherTriggered(watcherName);
    }

    @Override
    public boolean isNaturalOrientation() {
        return this.uiDevice.isNaturalOrientation();
    }

    @Override
    public boolean isScreenOn() throws RemoteException {
        return this.uiDevice.isScreenOn();
    }

    @Override
    public boolean pressBack() {
        return this.uiDevice.pressBack();
    }

    @Override
    public boolean pressDPadCenter() {
        return this.uiDevice.pressDPadCenter();
    }

    @Override
    public boolean pressDPadDown() {
        return this.uiDevice.pressDPadDown();
    }

    @Override
    public boolean pressDPadLeft() {
        return this.uiDevice.pressDPadLeft();
    }

    @Override
    public boolean pressDPadRight() {
        return this.uiDevice.pressDPadRight();
    }

    @Override
    public boolean pressDPadUp() {
        return this.uiDevice.pressDPadUp();
    }

    @Override
    public boolean pressDelete() {
        return this.uiDevice.pressDelete();
    }

    @Override
    public boolean pressEnter() {
        return this.uiDevice.pressEnter();
    }

    @Override
    public boolean pressHome() {
        return this.uiDevice.pressHome();
    }

    @Override
    public boolean pressKeyCode(int keyCode) {
        return this.uiDevice.pressKeyCode(keyCode);
    }

    @Override
    public boolean pressKeyCode(int keyCode, int metaState) {
        return this.uiDevice.pressKeyCode(keyCode, metaState);
    }

    @Override
    public boolean pressMenu() {
        return this.uiDevice.pressMenu();
    }

    @Override
    public boolean pressRecentApps() throws RemoteException {
        return this.uiDevice.pressRecentApps();
    }

    @Override
    public boolean pressSearch() {
        return this.uiDevice.pressSearch();
    }

    @Override
    public void registerWatcher(String name, UiWatcher watcher) {
        this.uiDevice.registerWatcher(name, watcher);
    }

    @Override
    public void removeWatcher(String name) {
        this.uiDevice.removeWatcher(name);
    }

    @Override
    public void resetWatcherTriggers() {
        this.uiDevice.resetWatcherTriggers();
    }

    @Override
    public void runWatchers() {
        this.uiDevice.runWatchers();

    }

    @Override
    public void setOrientationLeft() throws RemoteException {
        this.uiDevice.setOrientationLeft();
    }

    @Override
    public void setOrientationNatural() throws RemoteException {
        this.uiDevice.setOrientationNatural();
    }

    @Override
    public void setOrientationRight() throws RemoteException {
        this.uiDevice.setOrientationRight();

    }

    @Override
    public void sleep() throws RemoteException {
        this.uiDevice.sleep();
    }

    @Override
    public boolean swipe(int startX, int startY, int endX, int endY, int steps) {
        return this.uiDevice.swipe(startX, startY, endX, endY, steps);
    }

    @Override
    public boolean swipe(Point[] segments, int segmentSteps) {
        android.graphics.Point[] aSegments = new android.graphics.Point[segments.length];
        for (int i = 0; i < segments.length; i++) {
            aSegments[i] = new android.graphics.Point(segments[i].x, segments[i].y);
        }
        System.out.println(aSegments[0].x);
        return this.uiDevice.swipe(aSegments, segmentSteps);
    }

    @Override
    public boolean takeScreenshot(File storePath) {
        return this.uiDevice.takeScreenshot(storePath);
    }

    @Override
    public boolean takeScreenshot(File storePath, float scale, int quality) {
        return this.uiDevice.takeScreenshot(storePath, scale, quality);
    }

    @Override
    public void unfreezeRotation() throws RemoteException {
        this.uiDevice.unfreezeRotation();
    }

    @Override
    public void waitForIdle() {
        this.uiDevice.waitForIdle();
    }

    @Override
    public void waitForIdle(long time) {
        this.uiDevice.waitForIdle(time);
    }

    @Override
    public boolean waitForWindowUpdate(String packageName, long timeout) {
        return this.uiDevice.waitForWindowUpdate(packageName, timeout);
    }

    @Override
    public void wakeUp() throws RemoteException {
        this.uiDevice.wakeUp();
    }
}
