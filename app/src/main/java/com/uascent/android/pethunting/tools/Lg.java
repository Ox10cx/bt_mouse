package com.uascent.android.pethunting.tools;

import android.util.Log;

public class Lg {
    private static boolean debug = true;
    private final static String TAG = "PetHunting";

    public static void i(String tag, String msg) {
        if (debug) {
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
        if (debug) {
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
        if (debug) {
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
        if (debug) {
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
