package com.terzobinf.drone.utils;

import android.util.Log;

public class Logger {

    private static final String TAG = "Drone";
    private static final boolean DEBUG = true;

    public static void print(Object object) {
        if (DEBUG) {
            Log.d(TAG, object == null ? "null" : String.valueOf(object));
        }
    }
}
