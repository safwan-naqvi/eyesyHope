package com.example.eyesyhopefyp.UserCommon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eyesyhopefyp.Common.Prevalent.Prevalent;
import com.example.eyesyhopefyp.Common.model.BlindHelper;
import com.example.eyesyhopefyp.Dashboard.dashboardActivity;
import com.example.eyesyhopefyp.R;
import com.example.eyesyhopefyp.Receivers.BatteryReceiver;
import com.example.eyesyhopefyp.Receivers.NetworkStatus;
import com.example.eyesyhopefyp.introductoryActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class loginBlindActivity extends AppCompatActivity {

    TextInputLayout login_phoneNo_User;
    CheckBox remember_me_user;
    TextView battery;
    BatteryReceiver mBattery;
    Vibrator vibrator;
    NetworkStatus networkStatus;
    Button btnLoginUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login_blind);
        initWidget();

        //Services
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //Battery Receiver
        mBattery = new BatteryReceiver(battery, getApplicationContext());
        registerReceiver(mBattery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //Check Internet Connection
        networkStatus = new NetworkStatus(loginBlindActivity.this);

        btnLoginUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Login", Toast.LENGTH_SHORT).show();
                checkUserLogin();
            }
        });
    }

    private void checkUserLogin() {
        if (!validatePhoneNumber()) {
            return;
        }else {
            Toast.makeText(getApplicationContext(), "Login2", Toast.LENGTH_SHORT).show();
            String number = "+92"+login_phoneNo_User.getEditText().getText().toString().trim();
            Query checkUser = FirebaseDatabase.getInstance().getReference("Blind").orderByChild("phoneNo").equalTo(number);
            Log.i("check",checkUser.toString());
            checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                       // BlindHelper user = snapshot.child(number).getValue(BlindHelper.class);
                        //String _fullName = snapshot.child(number).child("userName").getValue(String.class);
                      //  Prevalent.currentOnlineUser = user;
                        startActivity(new Intent(getApplicationContext(), dashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "No User Available", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(getApplicationContext(), dashboardActivity.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initWidget() {
        battery= findViewById(R.id.battery_Indication);
        login_phoneNo_User = (TextInputLayout)findViewById(R.id.login_phoneNo_User);
        btnLoginUser = findViewById(R.id.btn_login_user);
        remember_me_user = findViewById(R.id.check_remember_user);
    }
    private boolean validatePhoneNumber() {
        String val = login_phoneNo_User.getEditText().getText().toString().trim();
        // Creating a Pattern class object
        Pattern p = Pattern.compile("^\\d{10}$");

        // Pattern class contains matcher() method
        // to find matching between given number
        // and regular expression for which
        // object of Matcher class is created
        Matcher m = p.matcher(val);
        if (val.isEmpty()) {
            login_phoneNo_User.setError("Enter valid phone number");
            return false;
        } else if (!m.matches()) {
            login_phoneNo_User.setError("No White spaces are allowed!");
            return false;
        } else {
            login_phoneNo_User.setError(null);
            login_phoneNo_User.setErrorEnabled(false);
            return true;
        }
    }

}