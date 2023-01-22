package com.example.eyesyhopefyp.Utility;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.util.Log;

import com.example.eyesyhopefyp.Shopping.BarCodeRecognizer;

import java.util.HashMap;
import java.util.List;

public class UI_Connection {
    private static BarCodeRecognizer barcodeRecognizer;

    public static void get_Barcode(Bitmap bitmap, final Activity activity, Application application) {
        if (barcodeRecognizer == null)
            barcodeRecognizer = new BarCodeRecognizer();
        barcodeRecognizer.getBarcode(bitmap, activity, application);
    }


}
