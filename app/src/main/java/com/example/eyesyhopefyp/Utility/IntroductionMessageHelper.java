package com.example.eyesyhopefyp.Utility;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.example.eyesyhopefyp.Shopping.IntroductionMessage;

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
        prefEnglish = activity.getSharedPreferences("EnglishIntro", MODE_PRIVATE);
        firstStartEnglish = prefEnglish.getBoolean("EnglishIntro", true);

        if (firstStartEnglish) {
            return removeIntroductionMessageEnglish(hasCameraPermission);
        } else {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    enIntroductionMessage = new IntroductionMessage(activity, "AppCommands/welcomeSmartShopping.mp3");
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
                    enIntroductionMessage = new IntroductionMessage(activity, "AppCommands/englishWelcomeMessage.mp3");
                    enIntroductionMessage.start();
                }
            }, 1000);

            prefEnglish = activity.getSharedPreferences("EnglishIntro", MODE_PRIVATE);
            editorE = prefEnglish.edit();
            editorE.putBoolean("EnglishIntro", false);
            editorE.apply();
        }
        return false;
    }

    public boolean introductionMessageForDashboard() {
        prefEnglish = activity.getSharedPreferences("EnglishDashboardIntro", MODE_PRIVATE);
        firstStartEnglish = prefEnglish.getBoolean("EnglishDashboardIntro", true);

        if (firstStartEnglish) {
            return removeIntroductionMessageForDashboard();
        } else {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    enIntroductionMessage = new IntroductionMessage(activity, "AppCommands/welcomeDashboard.mp3");
                    enIntroductionMessage.start();
                }
            }, 1000);
        }
        return true;
    }

    public boolean removeIntroductionMessageForDashboard() {
        //todo : put the english message
        new Handler().postDelayed(new Runnable() {
            public void run() {
                enIntroductionMessage = new IntroductionMessage(activity, "AppCommands/dashboard_intro.mp3");
                enIntroductionMessage.start();
            }
        }, 1000);

        prefEnglish = activity.getSharedPreferences("EnglishDashboardIntro", MODE_PRIVATE);
        editorE = prefEnglish.edit();
        editorE.putBoolean("EnglishDashboardIntro", false);
        editorE.apply();
        return false;
    }

    public boolean introductionMessageForOD() {
        prefEnglish = activity.getSharedPreferences("EnglishODIntro", MODE_PRIVATE);
        firstStartEnglish = prefEnglish.getBoolean("EnglishODIntro", true);

        if (firstStartEnglish) {
            return removeIntroductionMessageForOD();
        } else {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    enIntroductionMessage = new IntroductionMessage(activity, "AppCommands/welcomeObjectDetection.mp3");
                    enIntroductionMessage.start();
                }
            }, 1000);
        }
        return true;
    }

    public boolean removeIntroductionMessageForOD() {
        //todo : put the english message
        new Handler().postDelayed(new Runnable() {
            public void run() {
                enIntroductionMessage = new IntroductionMessage(activity, "AppCommands/introObjectDetection.mp3");
                enIntroductionMessage.start();
            }
        }, 1000);

        prefEnglish = activity.getSharedPreferences("EnglishODIntro", MODE_PRIVATE);
        editorE = prefEnglish.edit();
        editorE.putBoolean("EnglishODIntro", false);
        editorE.apply();
        return false;
    }



}
