package com.example.eyesyhopefyp.Common;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class volumeHandler {

    public TextToSpeech textToSpeech;
    public static AudioManager audio;
    public Context context;

    public volumeHandler(){
    }

    public volumeHandler(Context context, TextToSpeech tts) {
        this.context = context;
        this.audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.textToSpeech = tts;
    }

    public static void volumeControlFull() {

        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float percent = 1f;
        int seventyVolume = (int) (maxVolume * percent);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
        Log.d("Vol", String.valueOf(currentVolume)+ "Max vol "+String.valueOf(maxVolume));
    }
    public static void volumeControlMedium() {

        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float percent = 0.7f;
        int seventyVolume = (int) (maxVolume * percent);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
        Log.d("Vol", String.valueOf(currentVolume)+ "Max vol "+String.valueOf(maxVolume));
    }
    public static void volumeControlLow() {

        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float percent = 0.4f;
        int seventyVolume = (int) (maxVolume * percent);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
        Log.d("Vol", String.valueOf(currentVolume)+ "Max vol "+String.valueOf(maxVolume));
    }
}
