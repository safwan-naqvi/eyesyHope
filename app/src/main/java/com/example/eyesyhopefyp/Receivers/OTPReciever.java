package com.example.eyesyhopefyp.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.chaos.view.PinView;

public class OTPReciever extends BroadcastReceiver {

    private static PinView pinView_otp;

    public void setPin_OTP(PinView pinView){
        OTPReciever.pinView_otp = pinView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for(SmsMessage smsMessage:smsMessages){
            String message_body = smsMessage.getMessageBody();
            String getOTP = message_body.split(":")[1];
            pinView_otp.setText(getOTP);
        }
    }
}
