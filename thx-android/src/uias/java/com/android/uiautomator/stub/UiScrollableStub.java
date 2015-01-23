package com.android.uiautomator.stub;

import com.android.uiautomator.core.UiScrollable;

/**
 *
 * @author linsong wang
 */
public class UiScrollableStub extends UiCollectionStub implements IUiScrollable {

    private static final long serialVersionUID = -8087456666495L;

    private UiScrollable uiScrollable;

    @Override
    public void useUiScrollableSelector(UiSelector selector) {
        com.android.uiautomator.core.UiSelector uiSelector = UiDeviceStub.convert(selector);
        this.uiScrollable = new UiScrollable(uiSelector);
    }

    @Override
    public boolean ensureFullyVisible(IUiObject childObject) {
        return false;
    }

    @Override
    public void setAsHorizontalList() {
        this.uiScrollable.setAsHorizontalList();
    }

    @Override
    public void setAsVerticalList() {
        this.uiScrollable.setAsVerticalList();
    }

    @Override
    public boolean selectChildByDescription(UiSelector childPattern, String text) {
        return false;
    }

    @Override
    public boolean selectChildByDescription(UiSelector childPattern, String text, boolean allowScrollSearch) {
        return false;
    }

    @Override
    public boolean selectChildByInstance(UiSelector childPattern, int instance) {
        return false;
    }

    @Override
    public boolean selectChildByText(UiSelector childPattern, String text) {
        return false;
    }

    @Override
    public boolean selectChildByText(UiSelector childPattern, String text, boolean allowScrollSearch) {
        return false;
    }

    @Override
    public boolean scrollDescriptionIntoView(String text) {
        return false;
    }

    @Override
    public boolean scrollIntoView(IUiObject obj) {
        return false;
    }

    @Override
    public boolean scrollIntoView(UiSelector selector) {
        return false;
    }

    @Override
    public boolean scrollTextIntoView(String text) {
        return false;
    }

    @Override
    public void setMaxSearchSwipes(int swipes) {
    }

    @Override
    public int getMaxSearchSwipes() {
        return this.uiScrollable.getMaxSearchSwipes();
    }

    @Override
    public boolean flingForward() {
        try {
            return this.uiScrollable.flingForward();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean scrollForward() {
        try {
            return this.uiScrollable.scrollForward();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean scrollForward(int steps) {
        try {
            return this.uiScrollable.scrollForward(steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean flingBackward() {
        try {
            return this.uiScrollable.flingBackward();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean scrollBackward() {
        try {
            return this.uiScrollable.scrollBackward();
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean scrollBackward(int steps) {
        try {
            return this.uiScrollable.scrollBackward(steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean scrollToBeginning(int maxSwipes, int steps) {
        try {
            return this.uiScrollable.scrollToBeginning(maxSwipes, steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean scrollToBeginning(int maxSwipes) {
        try {
            return this.uiScrollable.scrollToBeginning(maxSwipes);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean flingToBeginning(int maxSwipes) {
        try {
            return this.uiScrollable.flingToBeginning(maxSwipes);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean scrollToEnd(int maxSwipes, int steps) {
        try {
            return this.uiScrollable.scrollToEnd(maxSwipes, steps);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean scrollToEnd(int maxSwipes) {
        try {
            return this.uiScrollable.scrollToEnd(maxSwipes);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean flingToEnd(int maxSwipes) {
        try {
            return this.uiScrollable.flingToEnd(maxSwipes);
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public double getSwipeDeadZonePercentage() {
        return this.uiScrollable.getSwipeDeadZonePercentage();
    }

    @Override
    public void setSwipeDeadZonePercentage(double swipeDeadZonePercentage) {
        this.uiScrollable.setSwipeDeadZonePercentage(swipeDeadZonePercentage);
    }
}
