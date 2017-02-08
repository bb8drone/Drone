package com.terzobinf.drone.activity.base;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.terzobinf.drone.R;
import com.terzobinf.drone.service.DroneControllerService;

public abstract class DroneActivity extends AppCompatActivity {

    @LayoutRes
    public abstract int getLayoutRes();
    public abstract void onDroneEvent(String droneEvent);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());

        if (!DroneControllerService.isRunning(getApplicationContext())) {
            startService(new Intent(getApplicationContext(), DroneControllerService.class));
        }

        final ProgressDialog progressDialog = ProgressDialog.show(this, "", getString(R.string.loading), true);

        DroneControllerService.getAsync(new DroneControllerService.OnServiceLoadListener() {
            @Override
            public void onServiceLoad(DroneControllerService droneControllerService) {
                progressDialog.dismiss();

                if (!droneControllerService.isActivityListeningToEvents(DroneActivity.this)) {
                    droneControllerService.listenToDroneEvents(DroneActivity.this);
                }
            }
        });

        init();
    }

    public void init() {
    }
}
