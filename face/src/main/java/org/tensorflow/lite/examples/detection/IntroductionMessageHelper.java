package org.tensorflow.lite.examples.detection;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

public class IntroductionMessageHelper {
    Activity activity;
    Context context;

    boolean firstStartEnglish;
    SharedPreferences prefEnglish;
    SharedPreferences.Editor editorE;
    IntroductionMessage enIntroductionMessage;

    public IntroductionMessageHelper(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public boolean introductionMessage(boolean hasCameraPermission) {
        prefEnglish = activity.getSharedPreferences("EnglishIntroFace", MODE_PRIVATE);
        firstStartEnglish = prefEnglish.getBoolean("EnglishIntroFace", true);

        if (firstStartEnglish) {
            return removeIntroductionMessageEnglish(hasCameraPermission);
        } else {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    enIntroductionMessage = new IntroductionMessage(activity, "AppCommands/faceWelcomeScreen.mp3");
                    enIntroductionMessage.start();
                }
            }, 1000);
        }
        return true;
    }

    public boolean removeIntroductionMessageEnglish(boolean hasCameraPermission) {
        if (hasCameraPermission) {
            //todo : put the english message
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    enIntroductionMessage = new IntroductionMessage(activity, "AppCommands/intro_face.mp3");
                    enIntroductionMessage.start();
                }
            }, 1000);

            prefEnglish = activity.getSharedPreferences("EnglishIntroFace", MODE_PRIVATE);
            editorE = prefEnglish.edit();
            editorE.putBoolean("EnglishIntroFace", false);
            editorE.commit();
        }
        return false;
    }
}
