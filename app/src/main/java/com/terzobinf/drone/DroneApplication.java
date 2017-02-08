package com.terzobinf.drone;

import android.app.Application;

import com.o3dr.android.client.utils.LogToFileTree;

import timber.log.Timber;

public class DroneApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();

        LogToFileTree logToFileTree = new LogToFileTree();

        Timber.plant(logToFileTree);

        logToFileTree.createFileStartLogging(getApplicationContext());
    }
}
