package com.example.eyesyhopefyp.detectObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;

import com.example.eyesyhopefyp.R;
import com.example.eyesyhopefyp.Utility.IntroductionMessageHelper;
import com.example.eyesyhopefyp.Utility.Voice;
import com.example.eyesyhopefyp.detectObject.customview.OverlayView;
import com.example.eyesyhopefyp.detectObject.env.BorderedText;
import com.example.eyesyhopefyp.detectObject.env.ImageUtils;
import com.example.eyesyhopefyp.detectObject.env.Logger;
import com.example.eyesyhopefyp.detectObject.tflite.interpreter.Detector;
import com.example.eyesyhopefyp.detectObject.tflite.interpreter.TFLiteObjectDetectionAPIModel;
import com.example.eyesyhopefyp.detectObject.tracking.MultiBoxTracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


//Main Issue is related to when button is pressed it stop searches but is does not turn searching again

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */

public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();


    ArrayList<String> indoor = new ArrayList<String>(Arrays.asList("book", "clock", "vase", "scissors", "book", "teddy bear", "hair drier", "toothbrush", "chair", "couch", "potted plant", "bed", "dining table", "toilet", "person", "tv", "laptop", "mouse", "remote", "keyboard", "cell phone"));
    ArrayList<String> outdoor = new ArrayList<String>(Arrays.asList("traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat", "person","laptop"));
    ArrayList<String> kitchen = new ArrayList<String>(Arrays.asList("bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "chair", "dining table", "oven", "toaster", "refrigerator", "person", "sink"));
    ArrayList<String> food = new ArrayList<String>(Arrays.asList("banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake"));
    ArrayList<String> furniture = new ArrayList<String>(Arrays.asList("chair", "couch", "potted plant", "bed", "dining table", "toilet"));


    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "labelmap.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.55f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;
    private Detector detector;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;
    private BorderedText borderedText;
    private List<Detector.Recognition> prevrecogs;
    private ArrayList<String> objects = null, prevobject = null;
    //To ensure one time Speak

    HashMap<String, String> detectedIndoorObjects, detectedOutdoorObjects, detectedFurniture, detectedFood, detectedKitchen;

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {

        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            this,
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing Detector!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Detector could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> {
                    tracker.draw(canvas);
                    if (isDebug()) {
                        tracker.drawDebug(canvas);
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }


        runInBackground(new Runnable() {
            @Override
            public void run() {
                LOGGER.i("Running detection on image " + currTimestamp);
                final long startTime = SystemClock.uptimeMillis();
                final List<Detector.Recognition> results = detector.recognizeImage(croppedBitmap);
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);

                final Canvas canvas = new Canvas(cropCopyBitmap);
                final Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStyle(Style.STROKE);
                paint.setStrokeWidth(1.0f);


                float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                if (MODE == DetectorMode.TF_OD_API) {
                    minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                }
                if (objects == null) {
                    objects = new ArrayList<>();
                    prevobject = new ArrayList<>();
                    prevrecogs = new LinkedList<>();
                }
                objects.clear();

                final List<Detector.Recognition> mappedRecognitions =
                        new ArrayList<>();


                for (final Detector.Recognition result : results) {
                    final RectF location = result.getLocation();
                    if (location != null && result.getConfidence() >= minimumConfidence) {
                        String category = CameraActivity.toDetectCategory;
                        Boolean see = CameraActivity.flagToDetect;
                        if (see) {
                            detectedIndoorObjects = new HashMap<String, String>();
                            detectedOutdoorObjects = new HashMap<String, String>();
                            detectedFurniture = new HashMap<String, String>();
                            detectedFood = new HashMap<String, String>();
                            detectedKitchen = new HashMap<String, String>();

                            switch (category) {
                                case "indoor":
                                    if (indoor.contains(result.getTitle())) {
                                        //Add flags to speak once in some time interval
                                        detectedOutdoorObjects.clear();
                                        detectedFurniture.clear();
                                        detectedFood.clear();
                                        detectedKitchen.clear();
                                        ///////////////////////////
                                        detectedIndoorObjects.put(result.getTitle(), result.getTitle());
                                        canvas.drawRect(location, paint);
                                        objects.add(result.getTitle());
                                        cropToFrameTransform.mapRect(location);
                                        result.setLocation(location);
                                        mappedRecognitions.add(result);
                                        callVoiceFunction();

                                    }
                                    break;
                                case "outdoor":
                                    if (outdoor.contains(result.getTitle())) {
                                        /////////////////////////
                                        detectedIndoorObjects.clear();
                                        detectedFurniture.clear();
                                        detectedFood.clear();
                                        detectedKitchen.clear();
                                        ////////////////////
                                        detectedOutdoorObjects.put(result.getTitle(), result.getTitle());
                                        canvas.drawRect(location, paint);
                                        objects.add(result.getTitle());
                                        cropToFrameTransform.mapRect(location);
                                        result.setLocation(location);
                                        mappedRecognitions.add(result);
                                        callVoiceFunction();

                                    }
                                    break;
                                case "kitchen":
                                    if (kitchen.contains(result.getTitle())) {
                                        //////////////////////////////////
                                        detectedIndoorObjects.clear();
                                        detectedFurniture.clear();
                                        detectedFood.clear();
                                        detectedOutdoorObjects.clear();
                                        /////////////////////////////////
                                        detectedKitchen.put(result.getTitle(), result.getTitle());
                                        canvas.drawRect(location, paint);
                                        objects.add(result.getTitle());
                                        cropToFrameTransform.mapRect(location);
                                        result.setLocation(location);
                                        mappedRecognitions.add(result);
                                        callVoiceFunction();

                                    }
                                    break;
                                case "furniture":
                                    if (furniture.contains(result.getTitle())) {
                                        ///////////////////////////////////////
                                        detectedIndoorObjects.clear();
                                        detectedKitchen.clear();
                                        detectedFood.clear();
                                        detectedOutdoorObjects.clear();
                                        //////////////////////////////////////
                                        detectedFurniture.put(result.getTitle(), result.getTitle());
                                        canvas.drawRect(location, paint);
                                        objects.add(result.getTitle());
                                        cropToFrameTransform.mapRect(location);
                                        result.setLocation(location);
                                        mappedRecognitions.add(result);
                                        callVoiceFunction();

                                    }
                                    break;
                                case "food":
                                    if (food.contains(result.getTitle())) {
                                        ////////////////////////////////
                                        detectedIndoorObjects.clear();
                                        detectedKitchen.clear();
                                        detectedFurniture.clear();
                                        detectedOutdoorObjects.clear();
                                        ////////////////////////////////
                                        detectedFood.put(result.getTitle(), result.getTitle());
                                        canvas.drawRect(location, paint);
                                        objects.add(result.getTitle());
                                        cropToFrameTransform.mapRect(location);
                                        result.setLocation(location);
                                        mappedRecognitions.add(result);
                                        callVoiceFunction();
                                    }
                                    break;
                                default:
                                    break;
                            }


                        }
                    }
                    // tracker.trackResults(mappedRecognitions, currTimestamp);
                    // trackingOverlay.postInvalidate();
                    computingDetection = false;


                }
            }

        });

    }

    private void callVoiceFunction() {
        if (!detectedIndoorObjects.isEmpty()) {
            for (String i : detectedIndoorObjects.values()) {
                Voice.speak(DetectorActivity.this, i, false);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            detectedIndoorObjects.clear();
        } else if (!detectedOutdoorObjects.isEmpty()) {
            for (String i : detectedOutdoorObjects.values()) {
                Voice.speak(DetectorActivity.this, i, false);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            detectedOutdoorObjects.clear();
        } else if (!detectedFood.isEmpty()) {
            for (String i : detectedFood.values()) {
                Voice.speak(DetectorActivity.this, i, false);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            detectedFood.clear();
        } else if (!detectedFurniture.isEmpty()) {
            for (String i : detectedFurniture.values()) {
                Voice.speak(DetectorActivity.this, i, false);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            detectedFurniture.clear();
        } else if (!detectedKitchen.isEmpty()) {
            for (String i : detectedKitchen.values()) {
                Voice.speak(DetectorActivity.this, i, false);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            detectedKitchen.clear();
        }


    }


    @Override
    protected int getLayoutId() {
        return R.layout.tfe_od_camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
// checkpoints.
    private enum DetectorMode {
        TF_OD_API
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        runInBackground(
                () -> {
                    try {
                        detector.setUseNNAPI(isChecked);
                    } catch (UnsupportedOperationException e) {
                        LOGGER.e(e, "Failed to set \"Use NNAPI\".");
                        runOnUiThread(
                                () -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                });
    }

    @Override
    protected void setNumThreads(final int numThreads) {
        runInBackground(() -> detector.setNumThreads(numThreads));
    }

    @Override
    public synchronized void onStop() {
        super.onStop();
        Voice.release();
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        Voice.release();
    }


}
