package com.terzobinf.drone.activity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.terzobinf.drone.R;
import com.terzobinf.drone.activity.base.DroneActivity;
import com.terzobinf.drone.service.DroneControllerService;
import com.terzobinf.drone.utils.Logger;

public class MainActivity extends DroneActivity {

    private TextView mDroneStatus;
    private Button mFlyLandButton;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    public void onDroneEvent(String droneEvent) {
        if (droneEvent.equals(AttributeEvent.STATE_CONNECTED)) {
            mDroneStatus.setText("Connected");
        } else if (droneEvent.equals(AttributeEvent.STATE_DISCONNECTED)) {
            mDroneStatus.setText("Disconnected");
        }
    }

    @Override
    public void init() {
        mDroneStatus = (TextView) findViewById(R.id.drone_status);

        mFlyLandButton = (Button) findViewById(R.id.fly_land_button);
        mFlyLandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DroneControllerService.getAsync(new DroneControllerService.OnServiceLoadListener() {
                    @Override
                    public void onServiceLoad(DroneControllerService droneControllerService) {
                        if (!droneControllerService.isDroneConnected()) {
                            Logger.print("Drone not connected, connecting now...");

                            droneControllerService.setConnection(true);

                            return;
                        }

                        if (!droneControllerService.isDroneFlying()) {
                            droneControllerService.fly();

                            mFlyLandButton.setText("Land!");
                        } else {
                            droneControllerService.land();

                            mFlyLandButton.setText("Fly!");
                        }
                    }
                });
            }
        });
    }
}
