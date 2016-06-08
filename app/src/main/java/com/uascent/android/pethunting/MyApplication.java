package com.uascent.android.pethunting;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;

/**
 * Created by lenovo001 on 2016/5/24.
 */
public class MyApplication extends Application {
    public LinkedList<Activity> activityList = new LinkedList<Activity>();
    private static MyApplication instance;
    public boolean isAutoBreak = false;

    @Override
    public void onCreate() {
        super.onCreate();
        //异常重启
//        AppCrashHandler ch = AppCrashHandler.getInstance();
//        ch.init(getApplicationContext());
    }

    public static MyApplication getInstance() {
        if (null == instance) {
            instance = new MyApplication();
        }
        return instance;
    }

    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void removeActivity(Activity activity) {
        activityList.remove(activity);
    }


}
