package com.spectratech.sp530demo;

import com.spectratech.lib.CustomBaseApplication;
import com.spectratech.lib.Logger;
import com.spectratech.lib.conf.Config;

/**
 * SP530DemoBaseApplication is a child class used to inherit from CustomBaseApplication
 */
public class SP530DemoBaseApplication extends CustomBaseApplication {

    public static final String KEY_SP_LANGUAGEPREFERENCE="KEY_SP_LANGUAGEPREFERENCE";

    @Override
    public void onCreate() {
        Config instConfig= Config.getInstance();

        // set to development environment
        if (BuildConfig.DEBUG) {
            instConfig.setIsENVDEV();
            Logger.setEnableLogging(true);
        }
        else {
            instConfig.setIsENVPRO();
            Logger.setEnableLogging(false);
        }

        super.onCreate();
    }

}
