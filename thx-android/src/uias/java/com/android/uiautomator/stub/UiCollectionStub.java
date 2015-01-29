package com.android.uiautomator.stub;

import com.android.uiautomator.core.UiCollection;

/**
 * @author linsong wang
 */
public class UiCollectionStub extends UiObjectStub implements IUiCollection {

    private static final long serialVersionUID = -87233566495L;

    protected UiCollection uiCollection;

    @Override
    public void useUiCollectionSelector(UiSelector selector) {
        com.android.uiautomator.core.UiSelector uiSelector = UiDeviceStub.convert(selector);
        this.uiCollection = new UiCollection(uiSelector);
        super.useUiObjectSelector(selector);
    }

    @Override
    public boolean selectChildByDescription(UiSelector childPattern, String text) {
        com.android.uiautomator.core.UiSelector uiSelector = UiDeviceStub.convert(childPattern);
        try {
            this.uiObject = this.uiCollection.getChildByDescription(uiSelector, text);
            return true;
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean selectChildByInstance(UiSelector childPattern, int instance) {
        com.android.uiautomator.core.UiSelector uiSelector = UiDeviceStub.convert(childPattern);
        try {
            this.uiObject = this.uiCollection.getChildByInstance(uiSelector, instance);
            return true;
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public boolean selectChildByText(UiSelector childPattern, String text) {
        com.android.uiautomator.core.UiSelector uiSelector = UiDeviceStub.convert(childPattern);
        try {
            this.uiObject = this.uiCollection.getChildByText(uiSelector, text);
            return true;
        } catch (com.android.uiautomator.core.UiObjectNotFoundException ex) {
            this.setUiObjectNotFoundException(ex);
        }
        return false;
    }

    @Override
    public int getChildCount(UiSelector childPattern) {
        com.android.uiautomator.core.UiSelector uiSelector = UiDeviceStub.convert(childPattern);
        return this.uiCollection.getChildCount(uiSelector);
    }
}
