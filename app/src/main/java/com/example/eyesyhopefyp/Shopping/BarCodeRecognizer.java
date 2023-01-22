package com.example.eyesyhopefyp.Shopping;

import static android.content.Context.CAMERA_SERVICE;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.eyesyhopefyp.Shopping.DataBase.BarcodeRepository;
import com.example.eyesyhopefyp.Utility.Voice;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


import java.util.List;
import java.util.concurrent.ExecutionException;

public class BarCodeRecognizer {

    InputImage image;
    BarcodeScanner detector;
    Task<List<Barcode>> result;
    Task<Text> resultOCR;
    BarcodeRepository barcodeRepository;
    BarcodeScannerOptions options;
    TextRecognizer textRecognizer;
    int rotation = 0;
    boolean flag;


    public BarCodeRecognizer() {
        options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        detector = BarcodeScanning.getClient(options);
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        flag = true;
    }


    public void getBarcode(Bitmap bitmap, final Activity activity, final Application application) {

        image = InputImage.fromBitmap(bitmap, 0);
        resultOCR = textRecognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        String resultText = visionText.getText();
                        Voice.speak(activity, resultText, false);
                        callBarCodeToDetect(bitmap,activity,rotation,application);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Voice.speak(activity, Voice.getCanNot(), false);
                            }
                        });

    }

    private void callBarCodeToDetect(Bitmap bitmap, Activity activity, int rotation, Application application) {
        Log.i("IN OCR", "callOCR");
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        result = detector.process(image).addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
            @Override
            public void onSuccess(List<Barcode> barcodes) {

                for (Barcode barcode : barcodes) {
                    barcodeRepository = new BarcodeRepository(application);
                    int valueType = barcode.getValueType();
                    switch (valueType) {
                        case Barcode.TYPE_WIFI:
                            String ssid = barcode.getWifi().getSsid();
                            String password = barcode.getWifi().getPassword();
                            int type = barcode.getWifi().getEncryptionType();
                            String result = "SSID is " + ssid + "\n" + "Password is " + password + "\nType of Wifi Encryption is " + type;
                            Voice.speak(activity, result, false);
                            break;
                        case Barcode.TYPE_URL:
                            String title = barcode.getUrl().getTitle();
                            String url = barcode.getUrl().getUrl();
                            String result2 = "Title of Website is " + title + "\nUrl of website is: " + url;
                            Voice.speak(activity, result2, false);
                            break;
                        case Barcode.TYPE_PRODUCT:
                            try {
                                if (barcodeRepository.getNameCode(barcode.getRawValue()) != null && barcodeRepository.getNameCode(barcode.getRawValue()).size() != 0) {
                                    Voice.speak(activity, barcodeRepository.getNameCode(barcode.getRawValue()).get(0).getBarcodeName(), false);
                                } else {
                                    Voice.speak(activity, Voice.getCanNot(), false);
                                }
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            String data = barcode.getDisplayValue();
                            Voice.speak(activity, data, false);
                            break;
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Voice.speak(activity, "shah g", false);
            }
        });

    }


}
