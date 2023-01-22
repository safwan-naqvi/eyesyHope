package com.example.eyesyhopefyp.Receivers;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

import com.example.eyesyhopefyp.detectObject.CameraActivity;

public class Utility {
    Context context;

    public Utility(Context context) {
        this.context = context;
    }

    public void torchToggle(String command) throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String cameraID = null;
        if (cameraManager != null) {
            cameraID = cameraManager.getCameraIdList()[0];
        }

        if (cameraManager != null) {
            if (command.equals("on")) {
                Log.i("inside", 0 +"   turning on torch");
                cameraManager.setTorchMode(cameraID, true);
                CameraActivity.isFlashLightOn = true;
            } else {
                Log.i("inside", cameraManager +"   turning off torch");
                cameraManager.setTorchMode(cameraID, false);
                CameraActivity.isFlashLightOn = false;
            }
        }

    }
}
