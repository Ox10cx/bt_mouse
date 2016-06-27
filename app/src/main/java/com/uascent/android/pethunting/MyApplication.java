package com.uascent.android.pethunting;

import android.app.Activity;
import android.app.Application;
import android.graphics.Typeface;

import com.uascent.android.pethunting.tools.Lg;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Created by lenovo001 on 2016/5/24.
 */
public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    public LinkedList<Activity> activityList = new LinkedList<Activity>();
    private static MyApplication instance;
    public boolean isAutoBreak = false;
    public static Typeface typeFace;

    @Override
    public void onCreate() {
        super.onCreate();
        //设置字体样式
        setTypeface();
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

    public void setTypeface() {
//        typeFace = Typeface.createFromAsset(getAssets(), "fonts/aaa.ttf");
//        typeFace = Typeface.createFromAsset(getAssets(), "fonts/test.ttf");
        typeFace = Typeface.createFromAsset(getAssets(), "fonts/arial.ttf");
        Lg.i(TAG, "setTypeface()");
        try {
            Field field = Typeface.class.getDeclaredField("SERIF");
            field.setAccessible(true);
            field.set(null, typeFace);
            Lg.i(TAG, "setTypeface()_suceess");
        } catch (NoSuchFieldException e) {
            Lg.i(TAG, "setTypeface()_suceess_fail1");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Lg.i(TAG, "setTypeface()_suceess_fail2");
        }
    }


}
