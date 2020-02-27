package com.zyfdroid.tomatoclock;

import android.app.Application;

import com.zyfdroid.tomatoclock.util.SpUtils;

/**
 * Created by WorldSkills2020 on 2/23/2020.
 */

public class Program extends Application {

    public static final String ACTION_CLOCK="com.zyfdroid.tomatoclock.ACTION_CLOCK";

    @Override
    public void onCreate() {
        super.onCreate();
        SpUtils.init(this);
    }
}
