package com.tascape.qa.th.driver;

import com.tascape.qa.th.comm.Adb;

/**
 *
 * @author wlinsong
 */
public class AndroidAdbDevice extends EntityDriver {

    protected Adb adb;

    public Adb getAdb() {
        return adb;
    }

    public void setAdb(Adb adb) {
        this.adb = adb;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void reset() throws Exception {
    }
}
