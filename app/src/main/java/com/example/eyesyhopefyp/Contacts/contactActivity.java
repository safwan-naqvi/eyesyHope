package com.example.eyesyhopefyp.Contacts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrawalsuneet.dotsloader.loaders.ZeeLoader;
import com.example.eyesyhopefyp.Common.model.Contact;
import com.example.eyesyhopefyp.Common.volumeHandler;
import com.example.eyesyhopefyp.Dashboard.dashboardActivity;
import com.example.eyesyhopefyp.R;
import com.example.eyesyhopefyp.Receivers.BatteryReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class contactActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    private TextView battery;
    private EditText searchBox;
    private TextToSpeech textToSpeech;
    private Button btnAssist;
    private Intent intent;
    private RecyclerView recyclerView;
    ZeeLoader parentLayout;

    public List<Contact> contactPopulate = new ArrayList<>();
    public contactAdapter adapter;
    myDBHelper db;

    public static String APP_LANG = "";
    SharedPreferences pref;
    BatteryReceiver mBattery;
    Vibrator vibrator;
    Timer timer;
    dashboardActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        parentLayout = findViewById(R.id.loader);
        //Database working
        db = new myDBHelper(contactActivity.this);
        //Adding Contact to a db
        contactPopulate = db.getAllContacts();
        InitWidget();
        //
        adapter = new contactAdapter(contactActivity.this, contactPopulate);
        searchBox = findViewById(R.id.searchBox);

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
        Log.i("check","Contact is loading");

        //Services
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //Battery Receiver
        mBattery = new BatteryReceiver(battery, getApplicationContext());
        registerReceiver(mBattery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //Checking For saved language in shared preference
        pref = getSharedPreferences("Settings", MODE_PRIVATE);
        //Handlers
        volumeHandler volumeHandler = new volumeHandler(getApplicationContext(), textToSpeech);
        mainActivity = new dashboardActivity();
        clickOnAssistButton();
        if (contactPopulate.isEmpty()) {
            Log.i("check","Contact is empty");
            new loadTask().execute();
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(contactActivity.this));
            adapter = new contactAdapter(contactActivity.this, contactPopulate);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {

    }

    public class loadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            parentLayout.setVisibility(View.VISIBLE);
            ZeeLoader zeeLoader = new ZeeLoader(
                    contactActivity.this,
                    12,
                    2,
                    ContextCompat.getColor(contactActivity.this, R.color.blue_light),
                    ContextCompat.getColor(contactActivity.this, R.color.blue));

            zeeLoader.setAnimDuration(300);

            parentLayout.addView(zeeLoader);
        }

        @Override
        protected Void doInBackground(Void... params) {
            checkPermission();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            parentLayout.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new LinearLayoutManager(contactActivity.this));
            adapter = new contactAdapter(contactActivity.this, contactPopulate);

            recyclerView.setAdapter(adapter);
            Intent intent = getIntent();
            finishAfterTransition();
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(contactActivity.this).toBundle());
        }

        private void checkPermission() {
            if ((ContextCompat.checkSelfPermission(contactActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(contactActivity.this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)) {
                textToSpeech.speak("Permissions Not Granted", TextToSpeech.QUEUE_ADD, null, null);
            } else {
                getContactList();
            }
        }

        @SuppressLint("Range")
        private void getContactList() {
            if (db.getAllContacts().isEmpty()) {
                //Init Uri
                Uri uri = ContactsContract.Contacts.CONTENT_URI;
                //Sort By Ascending
                String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
                //Init Cursor
                Cursor cursor = getContentResolver().query(uri, null, null, null, sort);
                //Check Condition
                if (cursor.getCount() > 0) {
                    Contact contact = new Contact();
                    //When count is greater than 0 then
                    while (cursor.moveToNext()) {
                        //Cursor moves to next item
                        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        //Init Phone Uri
                        Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                        //Init Selection
                        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?";
                        //Init Phone Cursor
                        Cursor phoneCursor = getContentResolver().query(
                                uriPhone, null, selection, new String[]{id}, null
                        );
                        if (phoneCursor.moveToNext()) {
                            //When Phone Cursor move to next
                            String number = phoneCursor.getString(phoneCursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                            ));
                            //Init Contact Model
                            Contact model = new Contact();
                            //set Name
                            model.setName(contactName);
                            //set Contact Number
                            model.setPhoneNumber(number);
                            //Add model in array
                            //arrayListContact.add(model);
                            //Putting Data in DB
                            db.addContact(model);
                            //Close Phone Cursor
                            phoneCursor.close();
                        }
                    }
                    cursor.close();
                }
                //restartThis();
            }
            //Set layout manager
        }
    }


    private void clickOnAssistButton() {
        btnAssist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!isConnected(contactActivity.this)) {
                            textToSpeech.speak(getString(R.string.network_connectivity_off), TextToSpeech.QUEUE_ADD, null, null);
                        } else {
                            switch (pref.getString("lang", "en")) {
                                case "ur":
                                    APP_LANG = "ur-pk";
                                    openAssistant(APP_LANG);
                                    break;
                                default:
                                    APP_LANG = "en-us";
                                    openAssistant(APP_LANG);
                                    break;
                            }
                        }
                    }
                }, 1000);
            }
        });

        btnAssist.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                GestureOverlayView objGestureOverlay = (GestureOverlayView) findViewById(R.id.widgetGesture);
                if (recyclerView.getVisibility() == View.VISIBLE) {
                    objGestureOverlay.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                    textToSpeech.speak("Gesture Mode On", TextToSpeech.QUEUE_FLUSH, null, null);
                    objGestureOverlay.addOnGesturePerformedListener(contactActivity.this);
                } else {
                    objGestureOverlay.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    textToSpeech.speak("Gesture Mode Off", TextToSpeech.QUEUE_FLUSH, null, null);
                }
                return true;
            }
        });
    }

    public boolean isConnected(contactActivity mainActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiConn != null && wifiConn.isConnected() || (mobileConn != null && mobileConn.isConnected()))) {
            return true;
        } else {
            return false;
        }


    }

    private void openAssistant(String language_code) {
        textToSpeech.speak("How may I help you!", TextToSpeech.QUEUE_FLUSH, null, null);
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language_code);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.assistant_greet));
        try {
            startActivityForResult(intent, 1);
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void openAssistantWithReqCode(String language_code, int reqCode) {
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language_code);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.assistant_greet));
        try {
            startActivityForResult(intent, reqCode);
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String cmd = result.get(0);
                    cmd = cmd.toLowerCase().trim();

                    if (containsWords(cmd, new String[]{"search"})) {
                        textToSpeech.speak("Speak out Contact Name or Number to search!", TextToSpeech.QUEUE_ADD, null, null);
                        try {
                            Thread.sleep(3000);
                            openAssistantWithReqCode(APP_LANG, 10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (containsWords(cmd, new String[]{"call"})) {
                        textToSpeech.speak("Speak out Contact Name or Number to call!", TextToSpeech.QUEUE_ADD, null, null);
                        try {
                            Thread.sleep(3000);
                            openAssistantWithReqCode(APP_LANG, 20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
                break;
            case 10:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String cmd = result.get(0);
                    cmd = cmd.toLowerCase().trim();
                    Log.i("number", cmd);
                    filter(cmd);
                }
                break;
            case 20:
                if (resultCode == RESULT_OK && null != data) {
                    textToSpeech.speak("Speak out Contact Name or Number to call!", TextToSpeech.QUEUE_FLUSH, null, null);
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String cmd = result.get(0);
                    cmd = cmd.toLowerCase().trim();


                }
        }

    }

    private String purifyText(String name) {
        String Name = name.toLowerCase().trim();
        Name = Name.replace(" ", "");
        Log.i("test", Name);
        return Name;
    }

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

    private void InitWidget() {
        recyclerView = findViewById(R.id.contactList);
        btnAssist = findViewById(R.id.assistBtn);
        battery = findViewById(R.id.tv_battery_indicator);


        //Initializing Text to Speech
        textToSpeech = new TextToSpeech(contactActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result;
                    switch (pref.getString("lang", "en")) {
                        case "hi":
                            result = textToSpeech.setLanguage(new Locale("hi"));
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "Language is not supported");
                                textToSpeech.setLanguage(new Locale("en"));
                            } else {
                                textToSpeech.setLanguage(new Locale("hi", "in"));
                            }
                            break;
                        case "ur":
                            result = textToSpeech.setLanguage(new Locale("ur"));
                            if (result == TextToSpeech.LANG_MISSING_DATA
                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "Language is not supported");
                                textToSpeech.setLanguage(new Locale("en"));
                                // else you ask the system to install it
                            } else {
                                textToSpeech.setLanguage(new Locale("ur", "pk"));
                            }
                            break;
                        default:
                            result = textToSpeech.setLanguage(new Locale("en"));
                            if (result == TextToSpeech.LANG_MISSING_DATA
                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "Language is not supported");
                            }
                            break;
                    }
                }

            }
        });


    }

    private void filter(String text) {

        List<Contact> filtered = new ArrayList<>();
        for (Contact item : contactPopulate) {
            if (item.getName().toLowerCase().contains(text.toLowerCase()) || item.getPhoneNumber().toLowerCase().trim().contains(text.toLowerCase())) {
                filtered.add(item);

            } else {

            }
        }

        adapter.filterList(filtered);

    }
}