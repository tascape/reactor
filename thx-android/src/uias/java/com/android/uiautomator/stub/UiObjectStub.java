package com.android.uiautomator.stub;

import android.view.MotionEvent;
import com.android.uiautomator.core.UiObject;
import java.util.ArrayList;
import java.util.List;

/*
 * @author linsong wang
 */
public class UiObjectStub implements IUiObject {

    private static final long serialVersionUID = -87298736495L;

    protected UiObject uiObject;

    protected UiObjectNotFoundException uiObjectNotFoundException;

    @Override
    public void clearUiObjectNotFoundException() {
        this.uiObjectNotFoundException = null;
    }

    @Override
    public UiObjectNotFoundException getUiObjectNotFoundException() {
        try {
            return uiObjectNotFoundException;
        } finally {
            this.clearUiObjectNotFoundException();
        }
    }

    @Override
    public boolean hasUiObjectNotFoundException() {
        return this.uiObjectNotFoundException != null;
    }

    @Override
    public void useUiObjectSelector(UiSelector selector) {
        com.android.uiautomator.core.UiSelector uiSelector = UiDeviceStub.convert(selector);
        this.uiObject = new UiObject(uiSelector);
    }

    @Override
    public boolean selectChild(UiSelector selector) {
        com.android.uiautomator.core.UiSelector uiSelector = UiDeviceStub.convert(selector);
        try {
            this.uiObject = this.uiObject.getChild(uiSelector);
            return true;
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean selectFromParent(UiSelector selector) {
        com.android.uiautomator.core.UiSelector uiSelector = UiDeviceStub.convert(selector);
        try {
            this.uiObject = this.uiObject.getFromParent(uiSelector);
            return true;
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public int getChildCount() {
        try {
            return this.uiObject.getChildCount();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return -1;
    }

    @Override
    public boolean dragTo(IUiObject destObj, int steps) {
        // TODO
        System.err.print("Method 'boolean dragTo(IUiObject destObj, int steps)' is not supported yet.");
        return false;
    }

    @Override
    public boolean dragTo(int destX, int destY, int steps) {
        try {
            return this.uiObject.dragTo(destX, destY, steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean swipeUp(int steps) {
        try {
            return this.uiObject.swipeUp(steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean swipeDown(int steps) {
        try {
            return this.uiObject.swipeDown(steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean swipeLeft(int steps) {
        try {
            return this.uiObject.swipeLeft(steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean swipeRight(int steps) {
        try {
            return this.uiObject.swipeRight(steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean click() {
        try {
            return this.uiObject.click();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean clickAndWaitForNewWindow() {
        try {
            return this.uiObject.clickAndWaitForNewWindow();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean clickAndWaitForNewWindow(long timeout) {
        try {
            return this.uiObject.clickAndWaitForNewWindow(timeout);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean clickTopLeft() {
        try {
            return this.uiObject.clickTopLeft();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean longClickBottomRight() {
        try {
            return this.uiObject.longClickBottomRight();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean clickBottomRight() {
        try {
            return this.uiObject.clickBottomRight();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean longClick() {
        try {
            return this.uiObject.longClick();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean longClickTopLeft() {
        try {
            return this.uiObject.longClickTopLeft();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public String getText() {
        try {
            return this.uiObject.getText();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return null;
    }

    @Override
    public String getClassName() {
        try {
            return this.uiObject.getClassName();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return null;
    }

    @Override
    public String getContentDescription() {
        try {
            return this.uiObject.getContentDescription();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return null;
    }

    @Override
    public boolean setText(String text) {
        try {
            return this.uiObject.setText(text);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public void clearTextField() {
        try {
            this.uiObject.clearTextField();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
    }

    @Override
    public boolean isChecked() {
        try {
            return this.uiObject.isChecked();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean isSelected() {
        try {
            return this.uiObject.isSelected();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean isCheckable() {
        try {
            return this.uiObject.isCheckable();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean isEnabled() {
        try {
            return this.uiObject.isEnabled();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean isClickable() {
        try {
            return this.uiObject.isClickable();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean isFocused() {
        try {
            return this.uiObject.isFocused();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean isFocusable() {
        try {
            return this.uiObject.isFocusable();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean isScrollable() {
        try {
            return this.uiObject.isScrollable();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean isLongClickable() {
        try {
            return this.uiObject.isLongClickable();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public String getPackageName() {
        try {
            return this.uiObject.getPackageName();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return null;
    }

    @Override
    public Rect getVisibleBounds() {
        try {
            android.graphics.Rect rect = this.uiObject.getVisibleBounds();
            return new Rect(rect.left, rect.top, rect.right, rect.bottom);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return null;
    }

    @Override
    public Rect getBounds() {
        try {
            android.graphics.Rect rect = this.uiObject.getBounds();
            return new Rect(rect.left, rect.top, rect.right, rect.bottom);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return null;
    }

    @Override
    public boolean waitForExists(long timeout) {
        return this.uiObject.waitForExists(timeout);
    }

    @Override
    public boolean waitUntilGone(long timeout) {
        return this.uiObject.waitUntilGone(timeout);
    }

    @Override
    public boolean exists() {
        return this.uiObject.exists();
    }

    @Override
    public boolean pinchOut(int percent, int steps) {
        try {
            return this.uiObject.pinchOut(percent, steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean pinchIn(int percent, int steps) {
        try {
            return this.uiObject.pinchIn(percent, steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean performTwoPointerGesture(Point startPoint1, Point startPoint2, Point endPoint1,
            Point endPoint2, int steps) {
        android.graphics.Point sp1 = new android.graphics.Point(startPoint1.x, startPoint1.y);
        android.graphics.Point sp2 = new android.graphics.Point(startPoint2.x, startPoint2.y);

        android.graphics.Point ep1 = new android.graphics.Point(endPoint1.x, endPoint1.y);
        android.graphics.Point ep2 = new android.graphics.Point(endPoint2.x, endPoint2.y);

        return this.uiObject.performTwoPointerGesture(sp1, sp2, ep1, ep2, steps);
    }

    @Override
    public boolean performMultiPointerGesture(PointerCoords[]... touches) {
        List<MotionEvent.PointerCoords[]> ts = new ArrayList<>();
        for (int i = 0; i < touches.length; i++) {
            PointerCoords[] pcss = touches[i];
            MotionEvent.PointerCoords[] mepcss = new MotionEvent.PointerCoords[pcss.length];
            for (PointerCoords pcs : pcss) {
                mepcss[i] = UiObjectStub.convert(pcs);
            }
            ts.add(mepcss);
        }
        throw new UnsupportedOperationException();
//        return this.uiObject.performMultiPointerGesture();
    }

    protected void setUiObjectNotFoundException(com.android.uiautomator.core.UiObjectNotFoundException ex) {
        ex.printStackTrace();
        this.uiObjectNotFoundException = new UiObjectNotFoundException(ex);
    }

    private static MotionEvent.PointerCoords convert(PointerCoords pcs) {
        MotionEvent.PointerCoords mepcs = new MotionEvent.PointerCoords();
        mepcs.x = pcs.x;
        mepcs.y = pcs.y;
        mepcs.pressure = pcs.pressure;
        mepcs.size = pcs.size;
        mepcs.orientation = pcs.orientation;
        mepcs.toolMajor = pcs.toolMajor;
        mepcs.toolMinor = pcs.toolMinor;
        mepcs.touchMajor = pcs.touchMajor;
        mepcs.touchMinor = pcs.touchMinor;
        return mepcs;
    }
}
