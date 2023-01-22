package com.example.eyesyhopefyp.UserCommon;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eyesyhopefyp.R;
import com.example.eyesyhopefyp.Receivers.BatteryReceiver;
import com.example.eyesyhopefyp.Receivers.NetworkStatus;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class signUpBlindActivity extends AppCompatActivity {

    TextView battery;
    BatteryReceiver mBattery;
    Vibrator vibrator;
    NetworkStatus networkStatus;
    TextInputLayout nameTextInput, phoneNoTextInput, guardianPhoneTextInput;
    Button btnSignUpBlind;
    String[] perms = {Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECEIVE_SMS};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up_blind);
        initWidget();
        //Services
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //Battery Receiver
        mBattery = new BatteryReceiver(battery, getApplicationContext());
        registerReceiver(mBattery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //Check Internet Connection
        networkStatus = new NetworkStatus(signUpBlindActivity.this);
    }

    private void initWidget() {
        battery = findViewById(R.id.battery_Indication);
        nameTextInput = (TextInputLayout) findViewById(R.id.name_Blind);
        phoneNoTextInput = (TextInputLayout) findViewById(R.id.phoneNo_Blind);
        guardianPhoneTextInput = (TextInputLayout) findViewById(R.id.guardian_Verify_phoneNo);
        btnSignUpBlind = findViewById(R.id.btn_sign_up_blind);
    }

    private boolean validateFullName() {
        String value = nameTextInput.getEditText().getText().toString();
        if (value.isEmpty()) {
            nameTextInput.setError("Field Cannot be Empty");
            return false;
        } else {
            nameTextInput.setError(null);
            nameTextInput.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePhoneNumberGuardian() {
        String val = phoneNoTextInput.getEditText().getText().toString().trim();
        // Creating a Pattern class object
        Pattern p = Pattern.compile("^\\d{10}$");

        // Pattern class contains matcher() method
        // to find matching between given number
        // and regular expression for which
        // object of Matcher class is created
        Matcher m = p.matcher(val);
        if (val.isEmpty()) {
            phoneNoTextInput.setError("Enter valid phone number");
            return false;
        } else if (!m.matches()) {
            phoneNoTextInput.setError("No White spaces are allowed!");
            return false;
        } else {
            phoneNoTextInput.setError(null);
            phoneNoTextInput.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePhoneNumberBlind() {
        String val = guardianPhoneTextInput.getEditText().getText().toString().trim();
        // Creating a Pattern class object
        Pattern p = Pattern.compile("^\\d{10}$");

        // Pattern class contains matcher() method
        // to find matching between given number
        // and regular expression for which
        // object of Matcher class is created
        Matcher m = p.matcher(val);
        if (val.isEmpty()) {
            guardianPhoneTextInput.setError("Enter valid phone number");
            return false;
        } else if (!m.matches()) {
            guardianPhoneTextInput.setError("No White spaces are allowed!");
            return false;
        } else {
            guardianPhoneTextInput.setError(null);
            guardianPhoneTextInput.setErrorEnabled(false);
            return true;
        }
    }

    public void callOTPScreen(View view) {

        if (!validateFullName() | !validatePhoneNumberGuardian() | !validatePhoneNumberBlind()) {
            return;
        } else {
            Intent intent = new Intent(getApplicationContext(), otpVerifyActivity.class);
            intent.putExtra("name", nameTextInput.getEditText().getText().toString());
            intent.putExtra("phoneNo", "+92" + phoneNoTextInput.getEditText().getText().toString());
            intent.putExtra("phoneNoGuardian", "+92" + guardianPhoneTextInput.getEditText().getText().toString());
            intent.putExtra("type", "Blind");
            startActivity(intent);
        }

    }

}