package com.work.golinko.shkafbonuses;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddBonusesActivity extends AppCompatActivity {

    private final int RequestCameraPermissionID = 1001;
    String responseFromServer = "";
    Runnable QRTask;
    private SurfaceView cameraPreview;
    private TextView txtResult;
    private EditText sumOfMoney;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private RequestQueue requestQueue;
    private String access_token = "use your url";
    private boolean isRun = false;
    private String qrData;
    private String amount;
    private ProgressBar spinner;

    // check camera permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
        barcodeDetector.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bonuses_activity);

        cameraPreview = findViewById(R.id.cameraPreview);
        txtResult = findViewById(R.id.txtResult);
        sumOfMoney = findViewById(R.id.sumOfMoney);
        spinner = findViewById(R.id.progressBar);

        // create request queue for post requests
        requestQueue = Volley.newRequestQueue(this); // 'this' is the Context

        // create qr reader
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        //create cameraSource, set autofocus and change RequestedFps(it will make our preview quite a lot brighter
        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1024, 768)
                .setAutoFocusEnabled(true)
                .setRequestedFps(15.0f)
                .build();

        //Add Event
        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //Request permission
                    ActivityCompat.requestPermissions(AddBonusesActivity.this,
                            new String[]{android.Manifest.permission.CAMERA}, RequestCameraPermissionID);
                    return;
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    Log.e("CAMERA SOURCE", e.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            //receive array of data from qr codes
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();
                if (qrcodes.size() != 0) {

                    //                  this "isRun"  will help us to stop qr reader during post request
                    if (!isRun) {
                        txtResult.post(QRTask = new Runnable() {
                            @Override
                            public void run() {
                                isRun = true;
                                qrData = qrcodes.valueAt(0).displayValue;
                                spinner.setVisibility(View.VISIBLE);
                                jsonPost(qrData);
                            }
                        });
                    }
                }
            }
        });
    }

    // this method check whether our array contains info for getting gifts
    public boolean checkForCorrectQRCode(String[] dataFromQR) {
        if (dataFromQR.length == 5) {
            txtResult.setText(getString(R.string.text_for_gift));
            spinner.setVisibility(View.GONE);
            try {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
                cameraSource.start(cameraPreview.getHolder());
                isRun = false;
            } catch (IOException e) {
                Log.e("CAMERA SOURCE", e.getMessage());
            }
            return false;
        }

        return true;
    }

    // this method sends data to url
    public void jsonPost(String qrData) {
        cameraSource.stop();
        txtResult.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        String url = "use your url";
        final String[] dataFromQR = qrData.split("/");

        if (!checkForCorrectQRCode(dataFromQR)) {
            return;
        }

        // create new StringRequest with our parameters
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("TestTResponseFromServer", response);
                        responseFromServer = response;
                        if (response.contains("success")) {

                            spinner.setVisibility(View.GONE);
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("resultAdd", "" + amount);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();

                        } else if (response.contains("json is required") && ((amount.equals("")) || (amount == null))) {

                            txtResult.setText("Введите суму чека");
                            isRun = false;
                            spinner.setVisibility(View.GONE);

                            try {
                                cameraSource.start(cameraPreview.getHolder());
                            } catch (IOException e) {
                                Log.e("TestTCAMERA SOURCE2", e.getMessage());
                            }

                        } else {

                            txtResult.setText(response);
                            isRun = false;
                            spinner.setVisibility(View.GONE);

                            try {
                                cameraSource.start(cameraPreview.getHolder());
                            } catch (IOException e) {
                                Log.e("TestTCAMERA SOURCE2", e.getMessage());
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("TestTError.Response", error.toString());
                        if (error instanceof TimeoutError) {

                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("result", "Запрос отправлен, но нет ответа от сервера, возможно бонусы начислены");
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();

                        } else if (error instanceof NoConnectionError) {

                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("result", "Нет соединения с сервером");
                            setResult(Activity.RESULT_CANCELED, returnIntent);
                            finish();
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                amount = sumOfMoney.getText().toString();
                if (!amount.equals("") & !(amount == null)) {

                    params.put("json", "{\"customer_id\": \"" + dataFromQR[0] + "\", " +
                            "\"department_id\": \"13\"," +
                            " \"user_id\": \"216\"," +
                            " \"amount\": \"" + amount + "\"," +
                            " \"subject\": \"Продажа по чеку\"," +
                            " \"access_token\":\"" + access_token + "\"}");
                    Log.d("TestTMap", params.toString());
                    Log.d("TestTAmount", amount);
                }
                return params;
            }
        };

        // here we change RetryPolicy because by default Volley will wait for 2.5 seconds and then sends new request
        postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(postRequest);
    }

}