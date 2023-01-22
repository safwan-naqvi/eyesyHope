package org.tensorflow.lite.examples.detection;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Vibrate {
    Vibrator vibrator;
    Context context;


    public Vibrate(Context context) {
        this.context = context;
    }



    @SuppressLint("MissingPermission")
    public void Vibration(){
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(1000);
            }
        } else {
            Toast.makeText(context, "Not supported Vibration", Toast.LENGTH_SHORT).show();
        }
    }

}
