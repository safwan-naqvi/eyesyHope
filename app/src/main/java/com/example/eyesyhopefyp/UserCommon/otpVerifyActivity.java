package com.example.eyesyhopefyp.UserCommon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.example.eyesyhopefyp.Common.Prevalent.Prevalent;
import com.example.eyesyhopefyp.Common.model.BlindHelper;
import com.example.eyesyhopefyp.Common.model.GuardianHelper;
import com.example.eyesyhopefyp.Dashboard.dashboardActivity;
import com.example.eyesyhopefyp.R;
import com.example.eyesyhopefyp.Receivers.OTPReciever;
import com.example.eyesyhopefyp.introductoryActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class otpVerifyActivity extends AppCompatActivity {

    PinView pinFromUser;
    String codeBySystem;
    String _parentDB;
    String _name, _phoneNo, _password, _phoneNoGuardian, _type,_remember;
    TextView codeSentTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_otp_verify);
        initWidget();
        Paper.init(this);
        _name = getIntent().getStringExtra("name");
        _phoneNo = getIntent().getStringExtra("phoneNo");
        _parentDB = getIntent().getStringExtra("type");
        _type = getIntent().getStringExtra("type");

        if (_parentDB.equals("Guardian")) {
            _password = getIntent().getStringExtra("password");
        } else if (_parentDB.equals("Blind")) {
            _phoneNoGuardian = getIntent().getStringExtra("phoneNoGuardian");
        }

        codeSentTo.setText("VERIFICATION CODE SENT TO " + _phoneNo);
        Log.i("check", "Name: " + _name + " parent :" + _parentDB + " phone " + _phoneNo + "guardian" + _phoneNoGuardian);
        sendVerificationCodeToUser(_phoneNo);
        new OTPReciever().setPin_OTP(pinFromUser);
    }

    private void initWidget() {
        pinFromUser = findViewById(R.id.pin_view);
        codeSentTo = findViewById(R.id.verify_otp_msg);
    }

    private void sendVerificationCodeToUser(String phoneNo) {
        Log.i("number", phoneNo);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNo, //Number on which code will be sent
                60, //Code expire time
                TimeUnit.SECONDS, //Unit of Time out
                otpVerifyActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        //If device is different
                        codeBySystem = s;

                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        //System entered code
                        String code = phoneAuthCredential.getSmsCode();
                        if (code != null) {
                            pinFromUser.setText(code);
                            verifyCode(code);
                        }


                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(otpVerifyActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(otpVerifyActivity.this, signUpActivity.class));
                    }
                }
        );

    }

    private void verifyCode(String code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeBySystem, code);
        Toast.makeText(getApplicationContext(), credential.toString(), Toast.LENGTH_SHORT).show();
        signInWithPhoneAuthCredential(credential);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Verification completed successfully here Either
                            // store the data or do whatever desire
                            Log.i("check", "Data is stored");

                            if (_type.equals("UserLogin")){
                                //getStoredUserCredentials();
                            }else if(_type.equals("Guardian") || _type.equals("Blind")){
                                storeNewUsersData();
                            }

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(otpVerifyActivity.this, "Verification Not Completed! Try again.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(otpVerifyActivity.this, introductoryActivity.class));
                            }
                        }
                    }
                });
    }

    /*private void getStoredUserCredentials() {
        Query checkUser = FirebaseDatabase.getInstance().getReference("Blind").orderByChild("phoneNo").equalTo(_phoneNo);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    BlindHelper user = snapshot.child(_phoneNo).getValue(BlindHelper.class);

                    String _fullName = snapshot.child(_phoneNo).child("fullName").getValue(String.class);
                    String _guardianNo = snapshot.child(_phoneNo).child("phoneNoGuardian").getValue(String.class);

                    //Creating a Login Session
                    _remember = getIntent().getStringExtra("remember");
                    if(_remember.equals("true")){

                        Paper.book().write(Prevalent.UserPhoneKey,_phoneNo);
                        Paper.book().write(Prevalent.UserGuardianKey,_guardianNo);
                        Paper.book().write(Prevalent.UserName,_fullName);
                    }
                    Prevalent.currentOnlineUser = user;
                    startActivity(new Intent(otpVerifyActivity.this, dashboardActivity.class));
                    finish();

                }else {
                    Toast.makeText(getApplicationContext(), "User Does not exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
*/
    private void storeNewUsersData() {
        FirebaseDatabase rootRef = FirebaseDatabase.getInstance(); //Database location
        DatabaseReference reference = rootRef.getReference(_parentDB); //Table of User
        if (_parentDB.equals("Blind")) {
            //As we need guardian to be exist in DB First to create any blind account
            guardianExists();
        } else if (_parentDB.equals("Guardian")) {
            GuardianHelper addNewUser = new GuardianHelper(_name, _phoneNo, _password);
            reference.child(_phoneNo).setValue(addNewUser);
            startActivity(new Intent(getApplicationContext(), introductoryActivity.class));
        }

        finish();
    }

    private void guardianExists() {
        Query checkUser = FirebaseDatabase.getInstance().getReference("Guardian").orderByChild("phoneNo").equalTo(_phoneNoGuardian);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    FirebaseDatabase rootRef = FirebaseDatabase.getInstance(); //Database location
                    DatabaseReference reference = rootRef.getReference(_parentDB); //Table of User
                    BlindHelper addNewUser = new BlindHelper(_name, _phoneNo, _phoneNoGuardian);
                    reference.child(_phoneNo).setValue(addNewUser);
                } else {
                    Toast.makeText(getApplicationContext(), "No Guardian Available", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(otpVerifyActivity.this, signUpBlindActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void callNextScreenFromOTP(View view) {
        String code = pinFromUser.getText().toString();
        Toast.makeText(getApplicationContext(), code, Toast.LENGTH_SHORT).show();
        if (!code.isEmpty()) {
            verifyCode(code);
        }
    }
}