package com.terzobinf.drone.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.property.State;

import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.terzobinf.drone.activity.base.DroneActivity;
import com.terzobinf.drone.utils.Logger;
import com.terzobinf.drone.utils.NetworkUtils;

import java.util.ArrayList;

public class DroneControllerService extends Service implements TowerListener, DroneListener {

    private static final int CONNECTION_PORT = 14550;

    private static final ArrayList<DroneActivity> activityArrayList = new ArrayList<>();
    private static final ArrayList<OnServiceLoadListener> onServiceLoadArrayList = new ArrayList<>();

    private static DroneControllerService sDroneControllerService;

    private ControlTower mControlTower;
    private Drone mDrone;

    public DroneControllerService() {
        sDroneControllerService = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.print("onCreate() service called");

        mControlTower = new ControlTower(getApplicationContext());
        mDrone = new Drone(getApplicationContext());

        mControlTower.connect(this);

        for (OnServiceLoadListener listener : onServiceLoadArrayList) {
            listener.onServiceLoad(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DroneControllerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void getAsync(OnServiceLoadListener onServiceLoadListener) {
        if (sDroneControllerService != null) {
            onServiceLoadListener.onServiceLoad(sDroneControllerService);

            return;
        }

        onServiceLoadArrayList.add(onServiceLoadListener);
    }

    public void broadcastDroneEvent(String droneEvent) {
        for (DroneActivity droneActivity : activityArrayList) {
            if (!droneActivity.isDestroyed()) {
                droneActivity.onDroneEvent(droneEvent);
            }
        }
    }

    public void listenToDroneEvents(DroneActivity droneActivity) {
        activityArrayList.add(droneActivity);
    }

    public void setConnection(boolean on) {
        Logger.print("Toggling connection...");

        if (!on && mDrone.isConnected()) {
            mDrone.disconnect();
        } else {
            mDrone.connect(getConnectionParams());
        }
    }

    public void fly() {
        State vehicleState = mDrone.getAttribute(AttributeType.STATE);
        if (!vehicleState.isArmed()) {
            Logger.print("Not armed");

            VehicleApi.getApi(mDrone).arm(true);
        }

        Logger.print("COPTER_STABILIZE");

        VehicleApi.getApi(mDrone).setVehicleMode(VehicleMode.COPTER_STABILIZE);
    }

    public void land() {
        VehicleApi.getApi(mDrone).setVehicleMode(VehicleMode.COPTER_LAND);
    }

    public boolean isActivityListeningToEvents(DroneActivity droneActivity) {
        return activityArrayList.contains(droneActivity);
    }

    public boolean isDroneConnected() {
        return mDrone != null && mDrone.isConnected();
    }

    public boolean isDroneFlying() {
        return ((State) mDrone.getAttribute(AttributeType.STATE)).isFlying();
    }

    private ConnectionParameter getConnectionParams() {
        return ConnectionParameter.newTcpConnection(
                NetworkUtils.getIp(getApplicationContext()), CONNECTION_PORT, null);
    }

    @Override
    public void onDestroy() {
        if (mDrone.isConnected()) {
            mDrone.disconnect();
        }

        mControlTower.unregisterDrone(mDrone);
        mControlTower.disconnect();
    }

    @Override
    public void onTowerConnected() {
        Logger.print("onTowerConnected");

        mDrone.registerDroneListener(this);
        mDrone.connect(getConnectionParams());

        broadcastDroneEvent(AttributeEvent.STATE_CONNECTED);
    }

    @Override
    public void onTowerDisconnected() {
        broadcastDroneEvent(AttributeEvent.STATE_DISCONNECTED);
    }

    @Override
    public void onDroneEvent(String event, Bundle bundle) {
        broadcastDroneEvent(event);
    }

    @Override
    public void onDroneServiceInterrupted(String s) {
    }

    public interface OnServiceLoadListener {
        void onServiceLoad(DroneControllerService droneControllerService);
    }
}
