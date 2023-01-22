package com.example.eyesyhopefyp.Dashboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eyesyhopefyp.Contacts.contactActivity;
import com.example.eyesyhopefyp.R;
import com.example.eyesyhopefyp.Receivers.BatteryReceiver;
import com.example.eyesyhopefyp.Receivers.NetworkStatus;
import com.example.eyesyhopefyp.Shopping.smartShoppingActivity;
import com.example.eyesyhopefyp.Utility.IntroductionMessageHelper;
import com.example.eyesyhopefyp.Utility.Voice;
import com.example.eyesyhopefyp.Utility.Weather;
import com.example.eyesyhopefyp.detectObject.DetectorActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class dashboardActivity extends AppCompatActivity implements LocationListener {
    private static final int REQ_CODE_DASHBOARD_RESULT = 100;
    CardView textRecognition, objectDetect, smartShop, smartReminder, smartdialer;
    Button assistant;
    private int swipeStep = 0;
    private final static int swipesNumber = 5;
    private int swipeStepDetails = 0;
    private final static int swipesDetailNumber = 4;
    private SwiperListener swiperListener;
    public int click = 0;
    TextView battery;
    BatteryReceiver mBattery;
    Vibrator vibrator;
    NetworkStatus networkStatus;
    Intent intent;

    //region Common Functions Parameters
    String city, country, postal, province;
    FusedLocationProviderClient fusedLocationProviderClient;
    Translator englishUrduTranslator;
    double longt, latt;
    LocationManager locationManager;

    //endregion

    IntroductionMessageHelper introductionMessageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_dashboard);
        Paper.init(this);
        Voice.init(dashboardActivity.this);
        initWidget();
        //firebase language setup
        languageSetup();
        //Services
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //Battery Receiver
        mBattery = new BatteryReceiver(battery, getApplicationContext());
        registerReceiver(mBattery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //Check Internet Connection
        networkStatus = new NetworkStatus(dashboardActivity.this);
        //Intro
        introductionMessageHelper = new IntroductionMessageHelper(this, this);
        introductionMessageHelper.introductionMessageForDashboard();

        swiperListener = new SwiperListener(assistant);
        location_service();
    }

    private void languageSetup() {
        // Create an English-Urdu translator:
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.URDU)
                        .build();
        englishUrduTranslator = Translation.getClient(options);


        dictionary_model();


    }

    private void dictionary_model() {
        //Installation of Firebase Model Urdu
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        englishUrduTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                            }
                        });
    }

    private void location_service() {

        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, (LocationListener) dashboardActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callBatteryLevelChecker() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batterPCT = level / (float) scale;
        float p = batterPCT * 100;
        Voice.speak(dashboardActivity.this, "Your Battery percentage is, " + String.valueOf(Math.round(p)) + ", Please charge if your battery is below 30%", false);

    }

    private void temperature_service() {

        String content;
        Weather weather = new Weather();
        try {
            content = weather.execute("https://api.openweathermap.org/data/2.5/weather?lat=" + latt + "&lon=" + longt + "&appid=386bb5f8fa20832e7ce0bf6c7cbc65b5&units=metric").get();
            Log.i("addr", latt + " " + longt);

            JSONObject jsonObject = new JSONObject(content);
            String weatherData = jsonObject.getString("weather");
            String mainTemperature = jsonObject.getString("main");
            //Toast.makeText(this,weatherData,Toast.LENGTH_LONG).show();

            JSONArray array = new JSONArray(weatherData);

            String main = "";
            String description = "";
            String temperature = "";
            String city_Name = "";
            for (int i = 0; i < array.length(); i++) {
                JSONObject weatherpart = array.getJSONObject(i);
                main = weatherpart.getString("main");
                description = weatherpart.getString("description");
            }
            JSONObject mainpart = new JSONObject(mainTemperature);
            temperature = mainpart.getString("temp");
            //Toast.makeText(MainActivity.this,""+temperature,Toast.LENGTH_LONG).show();
            double temp_int = Double.parseDouble(temperature);
            temp_int = Math.round(temp_int);
            int t_value = (int) temp_int;
            String resultText = " Your Current Temperature in" + city + " is : " + t_value + " degree Celsius";
            Voice.speak(dashboardActivity.this, resultText, false);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Exception" + e.getMessage(), Toast.LENGTH_LONG).show();
        }


    }

    private void dateTimeChecker() {
        String currentTime = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());
        String currentDate = new SimpleDateFormat("EEE,d MMM , yyyy", Locale.getDefault()).format(new Date());
        Voice.speak(dashboardActivity.this, "Current Time is " + currentTime + " and Today's Date is " + currentDate, false);
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
                if (networkStatus.isConnected(dashboardActivity.this)) {
                    openAssistant("en-us");
                } else {
                    Voice.speak(dashboardActivity.this, getString(R.string.network_connectivity_off), false);
                }
                return true;
            }
        });
    }

    private void callActivityOnDoubleTap() {
        switch (swipeStep) {
            case 0:
                // Voice.speak(dashboardActivity.this, "OCR IS ABOUT TO START", false);
                Voice.speak(dashboardActivity.this, "Face Recognition IS ABOUT TO START", false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(dashboardActivity.this, com.example.eyesyhopefyp.FaceRecognition.FaceRecognition.class));
                    }
                }, 2000);
                break;
            case 1:
                Voice.speak(dashboardActivity.this, "OBJECT DETECTION IS ABOUT TO START", false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(dashboardActivity.this, DetectorActivity.class));
                    }
                }, 2000);
                break;
            case 2:
                Voice.speak(dashboardActivity.this, "Smart Shopping IS ABOUT TO START", false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(dashboardActivity.this, smartShoppingActivity.class));
                    }
                }, 2000);
                break;
            case 3:
                Voice.speak(dashboardActivity.this, "Smart Reminder IS ABOUT TO START", false);
                break;
            case 4:
                Voice.speak(dashboardActivity.this, "Smart Dialer IS ABOUT TO START", false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(dashboardActivity.this, contactActivity.class));
                    }
                }, 2000);
                break;
            default:

                break;
        }
    }

    private void initWidget() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(dashboardActivity.this);
        textRecognition = findViewById(R.id.text_recognition_mod);
        objectDetect = findViewById(R.id.objectDetect_mod);
        smartShop = findViewById(R.id.shopping_mod);
        smartReminder = findViewById(R.id.reminder_mod);
        smartdialer = findViewById(R.id.smart_dialer_mod);
        assistant = findViewById(R.id.btn_Assist);
        battery = findViewById(R.id.battery_Indication);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Voice.release();
        unregisterReceiver(mBattery);
    }

    private void openAssistant(String language_code) {
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language_code);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.assistant_greet));
        try {
            startActivityForResult(intent, REQ_CODE_DASHBOARD_RESULT);
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_DASHBOARD_RESULT:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String input = result.get(0).toLowerCase().trim();
                    Log.i("check", input);
                    if (containsWords(input, new String[]{"object detection"})) {
                        Voice.speak(dashboardActivity.this, "OBJECT DETECTION IS ABOUT TO START", false);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                startActivity(new Intent(dashboardActivity.this, DetectorActivity.class));
                            }
                        }, 1000);

                    } else if (containsWords(input, new String[]{"text recognition"})) {
                        Voice.speak(dashboardActivity.this, "OCR IS ABOUT TO START", false);
                    } else if (input.contains("shopping") || input.contains("smart shopping")) {
                        Voice.speak(dashboardActivity.this, "Smart Shopping IS ABOUT TO START", false);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                startActivity(new Intent(dashboardActivity.this, smartShoppingActivity.class));
                            }
                        }, 1200);

                    } else if (input.contains("dialer") || input.contains("dialler")) {
                        Voice.speak(dashboardActivity.this, "Smart Dialer IS ABOUT TO START", false);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                startActivity(new Intent(dashboardActivity.this, contactActivity.class));
                            }
                        }, 1000);
                    } else if (input.contains("reminder") || input.contains("smart reminder")) {
                        Voice.speak(dashboardActivity.this, "Smart Reminder IS ABOUT TO START", false);
                    } else if (input.contains("clear all") || input.contains("remove all")) {
                        Voice.speak(dashboardActivity.this, "Removing all cache data", false);
                        Paper.book().destroy();
                    } else {
                        Voice.speak(dashboardActivity.this, "Invalid Command press any where to decline", false);
                    }
                }

        }
    }

    //Function To Check Either Words exist in Statement
    public static boolean containsWords(String inputString, String[] items) {
        boolean found = true;
        for (String item : items) {
            if (!inputString.contains(item)) {
                found = false;
                break;
            }
        }
        Log.i("app", String.valueOf(found));
        return found;
    }
    //End Of Function


    @Override
    public void onLocationChanged(@NonNull Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        longt = longitude;
        latt = latitude;
        try {
            Geocoder geocoder = new Geocoder(dashboardActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);
            city = addresses.get(0).getLocality();
            country = addresses.get(0).getCountryName();
            postal = addresses.get(0).getPostalCode();
            province = addresses.get(0).getAdminArea();

            Log.i("addr", address);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                                        Voice.speak(dashboardActivity.this, getString(R.string.swipe_text_recognition), false);
                                        break;
                                    case 1:
                                        Voice.speak(dashboardActivity.this, getString(R.string.swipe_object_detection), false);
                                        break;
                                    case 2:
                                        Voice.speak(dashboardActivity.this, getString(R.string.swipe_smart_shopping), false);
                                        break;
                                    case 3:
                                        Voice.speak(dashboardActivity.this, getString(R.string.swipe_reminder), false);
                                        break;
                                    case 4:
                                        Voice.speak(dashboardActivity.this, getString(R.string.swipe_smart_dialer), false);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        } else {
                            if (Math.abs(yDiff) > threshold && Math.abs(velocityY) > velocity_threshold) {
                                //When X differ is greater than threshold and X velocity is greater than velocity threshold
                                if (yDiff > 0) {
                                    Voice.playAssetSound(dashboardActivity.this, "AppCommands/dashboard_aboutme.mp3");
                                } else {
                                    swipeStepDetails = ((swipeStepDetails - 1) % swipesDetailNumber + swipesDetailNumber) % swipesDetailNumber; // to handle Negative value
                                    switch (swipeStepDetails) {
                                        case 0:
                                            Voice.speak(dashboardActivity.this, "Your City is " + city + ", Province is " + province + ", Your Country is " + country + " and your Postal Code is " + postal, false);
                                            break;
                                        case 1:
                                            temperature_service();
                                            break;
                                        case 2:
                                            callBatteryLevelChecker();
                                            break;
                                        case 3:
                                            location_service(); //Calling it before so variables would be init till then
                                            dateTimeChecker();
                                            break;
                                        default:
                                            break;
                                    }
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
        public boolean onTouch(View view, MotionEvent motionEvent) {
            //return Gesture event
            return gestureDetector.onTouchEvent(motionEvent);
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        Voice.release();
    }
}