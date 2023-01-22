package org.tensorflow.lite.examples.detection;

import android.app.Activity;

public class IntroductionMessage extends Thread {
    Activity activity;
    String S ;

    public IntroductionMessage(Activity activity , String s) {
        this.activity = activity;
        this.S = s;
    }

    @Override
    public void run() {
        Voice.speak(activity, S, true);
    }
}
