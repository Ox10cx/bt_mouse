package com.uascent.android.pethunting.tools;

import android.util.Log;

import com.uascent.android.pethunting.BuildConfig;

public class Lg {
//    private static boolean BuildConfig.debug = true;
    private final static String TAG = "PetHunting";

    /**
     * 在项目中打印log
     *
     * @param msg
     */
    public static void i(String msg) {
        if (BuildConfig.debug) {
            Log.i(TAG, msg);
        }
    }

    /**
     * 在单个文件中打印log
     *
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg) {
        if (BuildConfig.debug) {
            if (tag == null) {
                tag = TAG;
            }
            if (msg == null) {
                msg = "";
            }
            Log.i(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.debug) {
            if (tag == null) {
                tag = TAG;
            }
            if (msg == null) {
                msg = "";
            }
            Log.e(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (BuildConfig.debug) {
            if (tag == null) {
                tag = TAG;
            }
            if (msg == null) {
                msg = "";
            }
            Log.v(tag, msg);
        }
    }


    public static void w(String tag, String msg) {
        if (BuildConfig.debug) {
            if (tag == null) {
                tag = TAG;
            }
            if (msg == null) {
                msg = "";
            }
            Log.w(tag, msg);
        }
    }
}
