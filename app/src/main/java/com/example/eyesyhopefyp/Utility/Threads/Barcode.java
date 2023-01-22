package com.example.eyesyhopefyp.Utility.Threads;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;

import com.example.eyesyhopefyp.Utility.UI_Connection;

public class Barcode extends Thread {
    Activity activity;
    Bitmap bitmap;
    Application application;

    public Barcode(Activity activity, Bitmap bitmap, Application application) {
        this.activity = activity;
        this.bitmap = bitmap;
        this.application = application;
    }
    public void reSetData(Activity activity, Bitmap bitmap,Application application){
        this.activity = activity;
        this.bitmap = bitmap;
        this.application = application;
    }
    @Override
    public void run() {
        UI_Connection.get_Barcode(bitmap, activity, application);
    }

}
