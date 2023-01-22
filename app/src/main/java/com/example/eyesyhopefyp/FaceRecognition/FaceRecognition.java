package com.example.eyesyhopefyp.FaceRecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.eyesyhopefyp.R;

import org.tensorflow.lite.examples.detection.DetectorActivity2;

public class FaceRecognition extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);
        startActivity(new Intent(FaceRecognition.this, DetectorActivity2.class));
    }
}