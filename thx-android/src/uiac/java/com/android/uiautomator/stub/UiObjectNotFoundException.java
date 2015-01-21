package com.android.uiautomator.stub;

/**
 *
 * @author linsong wang
 */
public class UiObjectNotFoundException extends Exception {
    public UiObjectNotFoundException(String msg) {
        super(msg);
    }

    public UiObjectNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UiObjectNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
