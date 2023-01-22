package com.example.eyesyhopefyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eyesyhopefyp.Contacts.contactActivity;
import com.example.eyesyhopefyp.Dashboard.dashboardActivity;
import com.example.eyesyhopefyp.Receivers.BatteryReceiver;
import com.example.eyesyhopefyp.Receivers.NetworkStatus;
import com.example.eyesyhopefyp.UserCommon.loginActivityGuardian;
import com.example.eyesyhopefyp.UserCommon.loginBlindActivity;
import com.example.eyesyhopefyp.UserCommon.signUpActivity;
import com.example.eyesyhopefyp.UserCommon.signUpBlindActivity;
import com.example.eyesyhopefyp.detectObject.DetectorActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class introductoryActivity extends AppCompatActivity {

    private static final int REQ_CODE_INTRO = 70;
    private static final int REQ_CODE_INTRO_RESULT = 110;
    CardView login_user, login_guardian, register_user, register_guardian;
    Button assistant;
    private static int swipesNumber = 4;
    private int swipeStep = 0;
    private introductoryActivity.SwiperListener swiperListener;
    public int click = 0;
    TextView battery;
    BatteryReceiver mBattery;
    Vibrator vibrator;
    NetworkStatus networkStatus;
    TextToSpeech textToSpeech;
    Intent intent;



    String[] perms = {Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECEIVE_SMS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_introductory);

        initWidget();

        //Services
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //Battery Receiver
        mBattery = new BatteryReceiver(battery, getApplicationContext());
        registerReceiver(mBattery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //Check Internet Connection
        networkStatus = new NetworkStatus(introductoryActivity.this);
        swiperListener = new introductoryActivity.SwiperListener(assistant);
    }


    private class SwiperListener implements View.OnTouchListener {
        GestureDetector gestureDetector;

        //Making Constructor
        public SwiperListener(View view) {
            //Required Variables init
            int threshold = 100;
            int velocity_threshold = 100;

            //Init Simple Gesture Listener

            GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    doubleTapToOpenModule();
                    return false;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    float xDiff = e2.getX() - e1.getX();
                    float yDiff = e2.getY() - e1.getY();

                    try {
                        //Checking conditions
                        if (Math.abs(xDiff) > Math.abs(yDiff)) {
                            //Checking Conditions
                            if (Math.abs(xDiff) > threshold && Math.abs(velocityX) > velocity_threshold) {
                                //When X differ is greater than threshold and X velocity is greater than velocity threshold
                                if (xDiff < 0) {
                                    swipeStep = ((swipeStep - 1) % swipesNumber + swipesNumber) % swipesNumber; // to handle Negative value
                                } else {
                                    swipeStep = (swipeStep + 1) % swipesNumber;
                                }
                                switch (swipeStep) {
                                    case 0:
                                        textToSpeech.speak(getString(R.string.swipe_user_login), TextToSpeech.QUEUE_ADD, null, null);
                                        break;
                                    case 1:
                                        textToSpeech.speak(getString(R.string.swipe_guardian_login), TextToSpeech.QUEUE_ADD, null, null);
                                        break;
                                    case 2:
                                        textToSpeech.speak(getString(R.string.swipe_user_register), TextToSpeech.QUEUE_ADD, null, null);
                                        break;
                                    case 3:
                                        textToSpeech.speak(getString(R.string.swipe_guardian_register), TextToSpeech.QUEUE_ADD, null, null);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        } else {
                            if (Math.abs(yDiff) > threshold && Math.abs(velocityY) > velocity_threshold) {
                                //When X differ is greater than threshold and X velocity is greater than velocity threshold
                                if (yDiff > 0) {
                                    //startActivity(new Intent(introductoryActivity.this, DetectorActivity.class));
                                } else {
                                    // startActivity(new Intent(introductoryActivity.this, contactActivity.class));
                                    Toast.makeText(getApplicationContext(), "Up", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            };
            gestureDetector = new GestureDetector(listener);
            view.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //return Gesture event
            return gestureDetector.onTouchEvent(event);
        }
    }

    public void doubleTapToOpenModule() {
        assistant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Normal Single Click Button will be done
                click++;
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        click = 0;
                    }
                };
                if (click == 1) {
                    handler.postDelayed(runnable, 400);
                } else if (click == 2) {
                    callActivityOnDoubleTap();
                } else if (click == 3) {
                    click = 0;
                }
            }
        });

        assistant.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (networkStatus.isConnected(introductoryActivity.this)) {
                    openAssistant("en-us");
                } else {
                    textToSpeech.speak(getString(R.string.network_connectivity_off), TextToSpeech.QUEUE_ADD, null, null);
                }
                return true;
            }
        });

    }

    private void callActivityOnDoubleTap() {
        switch (swipeStep) {
            case 0:
                textToSpeech.speak("Blind User Login Activity is about to start", TextToSpeech.QUEUE_FLUSH, null, null);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        startActivity(new Intent(introductoryActivity.this, loginBlindActivity.class));
                    }
                }, 2000);
                break;
            case 1:
                textToSpeech.speak("Guardian Login is about to start.", TextToSpeech.QUEUE_FLUSH, null, null);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        startActivity(new Intent(introductoryActivity.this, loginActivityGuardian.class));
                    }
                }, 2000);
                break;
            case 2:
                textToSpeech.speak("Blind User Register Activity is about to start", TextToSpeech.QUEUE_FLUSH, null, null);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        startActivity(new Intent(introductoryActivity.this, signUpBlindActivity.class));
                    }
                }, 2000);
                break;
            case 3:
                textToSpeech.speak("Guardian Register Activity is about to start", TextToSpeech.QUEUE_FLUSH, null, null);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        startActivity(new Intent(introductoryActivity.this, signUpActivity.class));
                    }
                }, 2000);

                break;
            default:
                break;
        }
    }

    private void openAssistant(String language_code) {
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language_code);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.assistant_greet));
        try {
            startActivityForResult(intent, REQ_CODE_INTRO_RESULT);
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initWidget() {
        textToSpeech = new TextToSpeech(introductoryActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {

                }

            }
        }, "com.google.android.tts");

        login_user = findViewById(R.id.login_card_blind_intro);
        login_guardian = findViewById(R.id.login_card_guardian_intro);
        register_user = findViewById(R.id.signup_card_blind_intro);
        register_guardian = findViewById(R.id.signup_card_guardian_intro);

        assistant = findViewById(R.id.btn_Assist_intro);

        battery = findViewById(R.id.battery_Indication);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Don't forget to shut down text to speech
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        unregisterReceiver(mBattery);
    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_INTRO_RESULT:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String input = result.get(0).toLowerCase().trim();
                    Log.i("check", input);
                    if (input.contains("login user") || input.contains("user login")) {
                        textToSpeech.speak("User Login Activity is about to start", TextToSpeech.QUEUE_FLUSH, null, null);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                startActivity(new Intent(introductoryActivity.this, loginBlindActivity.class));
                            }
                        }, 2000);
                    } else if (input.contains("login guardian") || input.contains("guardian login")) {
                        textToSpeech.speak("Guardian Login is about to start.", TextToSpeech.QUEUE_FLUSH, null, null);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                startActivity(new Intent(introductoryActivity.this, loginActivityGuardian.class));
                            }
                        }, 2000);
                    } else if (input.contains("guardian register") || input.contains("guardian sign up")) {
                        textToSpeech.speak("User Register Activity is about to start", TextToSpeech.QUEUE_FLUSH, null, null);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                startActivity(new Intent(introductoryActivity.this, signUpBlindActivity.class));
                            }
                        }, 2000);
                    } else if (input.contains("register user") || input.contains("user register") || input.contains("sign up user") || input.contains("user sign up")) {
                        textToSpeech.speak("Guardian Register Activity is about to start", TextToSpeech.QUEUE_FLUSH, null, null);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                startActivity(new Intent(introductoryActivity.this, signUpActivity.class));
                            }
                        }, 2000);
                    } else {
                        textToSpeech.speak("Invalid Command press any where to decline", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }

        }
    }



}