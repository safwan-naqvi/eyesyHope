package com.example.eyesyhopefyp.Utility;

import android.app.Activity;
import android.app.Application;

import com.example.eyesyhopefyp.Shopping.BitmapConfiguration;
import com.example.eyesyhopefyp.Shopping.CameraConfiguration;
import com.example.eyesyhopefyp.Utility.Threads.Barcode;
import com.example.eyesyhopefyp.Utility.Threads.FlashToggle;


public class ThreadHelper {
    Activity activity;
    Application application;
    BitmapConfiguration bitmapConfiguration;
    CameraConfiguration cameraConfigurations;

    Barcode barcodeThread;
    FlashToggle flashToggleThread;

    public ThreadHelper(Activity activity, BitmapConfiguration bitmapConfiguration,
                        CameraConfiguration cameraConfigurations, Application application) {
        this.activity = activity;
        this.bitmapConfiguration = bitmapConfiguration;
        this.cameraConfigurations = cameraConfigurations;
        this.application = application;
    }

    public void flashToggleThread() {
        killAllThreadsAndReleaseVoice();
        if (flashToggleThread != null) {
            flashToggleThread.run();
        } else {
            flashToggleThread = new FlashToggle(activity, cameraConfigurations);
            flashToggleThread.start();
        }
    }

    public void barcodeThread() {
        killAllThreadsAndReleaseVoice();
        if (barcodeThread != null) {
            barcodeThread.reSetData(activity, bitmapConfiguration.getBitmap(cameraConfigurations.getFrame()), application);
            barcodeThread.run();
        } else {
            barcodeThread = new Barcode(activity, bitmapConfiguration.getBitmap(cameraConfigurations.getFrame()), application);
            barcodeThread.start();
        }
    }

    public void killAllThreadsAndReleaseVoice() {

        if (barcodeThread != null && barcodeThread.isAlive()) {
            barcodeThread.interrupt();
        }
        Voice.release();
    }
}
