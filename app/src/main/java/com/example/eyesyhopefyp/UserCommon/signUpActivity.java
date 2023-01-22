package com.example.eyesyhopefyp.UserCommon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eyesyhopefyp.R;
import com.example.eyesyhopefyp.Receivers.BatteryReceiver;
import com.example.eyesyhopefyp.Receivers.NetworkStatus;
import com.example.eyesyhopefyp.introductoryActivity;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class signUpActivity extends AppCompatActivity {

    TextView battery;
    BatteryReceiver mBattery;
    Vibrator vibrator;
    NetworkStatus networkStatus;
    TextInputLayout nameTextInput, phoneNoTextInput, passwordTextInput;
    Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);

        initWidget();

        //Services
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //Battery Receiver
        mBattery = new BatteryReceiver(battery, getApplicationContext());
        registerReceiver(mBattery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //Check Internet Connection
        networkStatus = new NetworkStatus(signUpActivity.this);

    }


    private void initWidget() {
        battery = findViewById(R.id.battery_Indication);
        nameTextInput = (TextInputLayout) findViewById(R.id.name_Guardian);
        phoneNoTextInput = (TextInputLayout) findViewById(R.id.phoneNo_Guardian);
        passwordTextInput = (TextInputLayout) findViewById(R.id.password_Guardian);
        btnSignUp = findViewById(R.id.btn_sign_up_guardian);
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

    private boolean validatePhoneNumber() {
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

    private boolean validatePassword() {
        String val = passwordTextInput.getEditText().getText().toString().trim();
        final Pattern PASSWORD_PATTERN = Pattern.compile("^" +

                "(?=.*[0-9])" +

                "(?=\\S+$)" +

                ".{4,}" +

                "$");

        if (val.isEmpty()) {
            passwordTextInput.setError("Field can not be empty");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(val).matches()) {
            passwordTextInput.setError("Should contain at least 4 characters!");
            return false;
        } else {
            passwordTextInput.setError(null);
            passwordTextInput.setErrorEnabled(false);
            return true;
        }
    }

    public void callOTPScreen(View view) {
        if (!validateFullName() | !validatePhoneNumber() | !validatePassword()) {
            return;
        } else {
            Intent intent = new Intent(getApplicationContext(), otpVerifyActivity.class);
            intent.putExtra("name", nameTextInput.getEditText().getText().toString());
            intent.putExtra("phoneNo", "+92" + phoneNoTextInput.getEditText().getText().toString());
            intent.putExtra("password", passwordTextInput.getEditText().getText().toString());
            intent.putExtra("type", "Guardian");
            startActivity(intent);
        }

    }

}