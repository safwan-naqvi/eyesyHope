package com.example.eyesyhopefyp.Introduction;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.WindowManager;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.eyesyhopefyp.Common.Voice;
import com.example.eyesyhopefyp.Dashboard.dashboardActivity;
import com.example.eyesyhopefyp.R;
import com.example.eyesyhopefyp.UserCommon.signUpActivity;
import com.example.eyesyhopefyp.UserCommon.signUpBlindActivity;
import com.example.eyesyhopefyp.introductoryActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class splashScreen extends AppCompatActivity {


    TextView title, desc, credit;
    LottieAnimationView lottieAnimationView;
    Timer timer;
    TextToSpeech textToSpeech;
    private static MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        initWidget();

        checkPermissions();

        title.animate().translationY(-1400).setDuration(1000).setStartDelay(4000);
        desc.animate().translationY(-1400).setDuration(1000).setStartDelay(4000);
        credit.animate().translationY(1400).setDuration(1000).setStartDelay(4000);
        lottieAnimationView.animate().translationX(1400).setDuration(1000).setStartDelay(4000);


        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                newActivity();
            }
        }, 5200);


    }

    private void initWidget() {
        this.textToSpeech = new TextToSpeech(splashScreen.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.speak("Eyesy Hope is about to Start!", TextToSpeech.QUEUE_FLUSH, null, null);
                }

            }
        }, "com.google.android.tts");

        title = findViewById(R.id.splash_title);
        desc = findViewById(R.id.splash_desc);
        credit = findViewById(R.id.splash_credit);
        lottieAnimationView = findViewById(R.id.animation_lottie);
    }

    private void newActivity() {
        Intent in = new Intent(splashScreen.this, dashboardActivity.class); //Manipulating it and sending after splash to dashboardintroductoryActivity
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();

    }

    public void checkPermissions() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

}