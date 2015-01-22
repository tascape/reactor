package com.android.uiautomator.stub;

/**
 *
 * @author linsong wang
 */
public class UiObjectNotFoundException extends Exception {

    public UiObjectNotFoundException(Throwable throwable) {
        super(throwable.toString());
    }
}
