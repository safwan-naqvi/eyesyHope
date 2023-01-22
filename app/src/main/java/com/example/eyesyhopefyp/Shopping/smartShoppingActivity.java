package com.example.eyesyhopefyp.Shopping;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.eyesyhopefyp.Dashboard.dashboardActivity;
import com.example.eyesyhopefyp.R;
import com.example.eyesyhopefyp.Utility.IntroductionMessageHelper;
import com.example.eyesyhopefyp.Utility.TabsSwipeHelper;
import com.example.eyesyhopefyp.Utility.ThreadHelper;
import com.example.eyesyhopefyp.Utility.Voice;

import io.fotoapparat.view.CameraView;
import io.fotoapparat.view.FocusView;

public class smartShoppingActivity extends AppCompatActivity {
    private final int CameraCode = 1;
    BitmapConfiguration bitmapConfiguration;
    private FocusView focusView;
    private boolean hasCameraPermission = false;
    private CameraView cameraView;
    private CameraConfiguration cameraConfigurations;
    //threads
    ThreadHelper threadHelper;
    TabsSwipeHelper tabsSwipeHelper;
    IntroductionMessageHelper introductionMessageHelper;
    public int click = 0;
    Button btnShop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_smart_shopping);
        Voice.init(this);
        initWidget();
        cameraConfigurations = new CameraConfiguration(cameraView, this, focusView);
        cameraConfigurations.startCamera();
        bitmapConfiguration = new BitmapConfiguration();
        threadHelper = new ThreadHelper(this, bitmapConfiguration, cameraConfigurations, getApplication());
        tabsSwipeHelper = new TabsSwipeHelper(threadHelper);
        introductionMessageHelper = new IntroductionMessageHelper(this, this);
        //UI_Connection.fillMap();

        introductionMessageHelper.introductionMessage(true);

        btnShop.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(smartShoppingActivity.this, dashboardActivity.class));
                finish();
                return true;
            }
        });
    }



    private void initWidget() {
        btnShop = findViewById(R.id.btn_Assist_ss);
        cameraView = findViewById(R.id.cameraView);
        focusView = findViewById(R.id.focusView);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hasCameraPermission) {
            cameraConfigurations.KillCamera();
        }
        Voice.release();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasCameraPermission) {
            cameraConfigurations.startCamera();
        } else {
            cameraConfigurations.requestCameraPermission(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (hasCameraPermission) {
            cameraConfigurations.KillCamera();
        }
        Voice.release();
    }

    public void mainScreen() {
        final Activity activity = this;
        //Normal Single Click Button will be done
        click++;
        Toast.makeText(smartShoppingActivity.this, "Working " + click, Toast.LENGTH_SHORT).show();
        Handler handler = new Handler();
        Runnable runnable = () -> click = 0;
        if (click == 1) {
            Toast.makeText(smartShoppingActivity.this, "Working " + click, Toast.LENGTH_SHORT).show();
            tabsSwipeHelper.tabs(activity);
            handler.postDelayed(runnable, 300);
        } else if (click == 2) {
            Toast.makeText(smartShoppingActivity.this, "Working " + click, Toast.LENGTH_SHORT).show();
            Log.i("see", "Double CLick working");
            threadHelper.flashToggleThread();

        } else if (click == 3) {
            click = 0;
        }
    }

    public void mainScreenWork(View view) {
        mainScreen();
    }
}