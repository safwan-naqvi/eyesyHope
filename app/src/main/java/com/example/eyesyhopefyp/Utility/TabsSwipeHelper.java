package com.example.eyesyhopefyp.Utility;

import android.app.Activity;
import android.util.Log;


public class TabsSwipeHelper {
    ThreadHelper threadHelper;

    public TabsSwipeHelper(ThreadHelper threadHelper) {
        this.threadHelper = threadHelper;
    }

    public void tabs(Activity activity) {

        threadHelper.barcodeThread();

    }
}

