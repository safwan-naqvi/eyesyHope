package com.example.eyesyhopefyp.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eyesyhopefyp.R;

public class BatteryReceiver extends BroadcastReceiver {

    TextView textView;
    TextToSpeech tts;
    Context context;
    Vibrator vibrator;
    boolean saidOnce;


    public BatteryReceiver() {
    }

    public BatteryReceiver(TextView textView, Context context) {
        this.textView = textView;
        this.context = context;
        tts = new TextToSpeech(this.context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                }

            }
        }, "com.google.android.tts");

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        saidOnce = false;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        if (isCharging) {//Means it is on charge
            if (usbCharge) {
                textView.setText(String.valueOf(level) + "% "+ "UC");
            } else if (acCharge) {
                textView.setText(String.valueOf(level) + "% "+ "AC");
            }
        }
        if (level != 0) {
            textView.setText(String.valueOf(level) + "%");
            if(!saidOnce){
                if (level <= 20 && !isCharging) {
                    tts.speak(context.getString(R.string.batteryLow), TextToSpeech.QUEUE_FLUSH, null, null);
                    hapticFeedback();

                } else if (level >= 99 && isCharging) {
                    tts.speak(context.getString(R.string.batteryFull), TextToSpeech.QUEUE_FLUSH, null, null);
                    hapticFeedbackLong();
                    saidOnce = true;
                }}
        }


    }

    public void hapticFeedback() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(1000);
            }
        } else {
            Toast.makeText(context, "Not supported Vibration", Toast.LENGTH_SHORT).show();
            tts.speak(context.getString(R.string.vibrateNotSupported), TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void hapticFeedbackLong() {
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] mVibratePattern = new long[]{0, 400, 800, 600, 800, 800, 800, 1000};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int[] mAmplitudes = new int[]{0, 255, 0, 255, 0, 255, 0, 255};
                // -1 : Play exactly once

                if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect effect = VibrationEffect.createWaveform(mVibratePattern, mAmplitudes, -1);
                    vibrator.vibrate(effect);
                }
            } else {

                // 3 : Repeat this pattern from 3rd element of an array
                vibrator.vibrate(mVibratePattern, 1);
            }
        } else {
            Toast.makeText(context, "Not supported Vibration", Toast.LENGTH_SHORT).show();
            tts.speak(context.getString(R.string.vibrateNotSupported), TextToSpeech.QUEUE_FLUSH, null, null);

        }
    }
}