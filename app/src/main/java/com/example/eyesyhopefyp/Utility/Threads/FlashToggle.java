package com.example.eyesyhopefyp.Utility.Threads;

import android.app.Activity;

import com.example.eyesyhopefyp.Shopping.CameraConfiguration;

public class FlashToggle extends Thread {
    CameraConfiguration cameraConfigurations;
    Activity activity;
    public FlashToggle(Activity activity , CameraConfiguration cameraConfigurations){
        this.activity = activity;
        this.cameraConfigurations = cameraConfigurations;
    }

    @Override
    public void run() {

        cameraConfigurations.toggleFlash(activity);
    }
}
