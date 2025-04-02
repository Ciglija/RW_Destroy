package com.example.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class ScannerActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> scannedBoxes = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String token;

    private static final String TAG = "BarcodeScanner";
    private static final String DATAWEDGE_SCANNER_OUTPUT_ACTION = "com.symbol.datawedge.scanner.ACTION";
    private static final String BARCODE_DATA = "com.symbol.datawedge.data_string";

    private BroadcastReceiver barcodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String barcode = intent.getStringExtra(BARCODE_DATA);
            if (barcode != null && !barcode.isEmpty()) {
                Log.d(TAG, "Scanned barcode: " + barcode);
                sendBarcodeToServer(barcode);

            } else {
                Log.d(TAG, "Received scan intent but no barcode data found");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);;
        setContentView(R.layout.activity_scanner);

        listView = findViewById(R.id.barcode_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scannedBoxes);
        listView.setAdapter(adapter);
        token = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("jwt_token", "");

        IntentFilter filter = new IntentFilter();
        filter.addAction(DATAWEDGE_SCANNER_OUTPUT_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(barcodeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        }
    }

    private void sendBarcodeToServer(String barcode) {
        try {
            JSONObject json = new JSONObject();
            json.put("box_code", barcode);

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("http://192.168.0.30:5000/scan-box")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(ScannerActivity.this, "Greska sa internetom!", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> Toast.makeText(ScannerActivity.this, "Uspesno skeniranje!", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(barcodeReceiver);
            Log.d(TAG, "Receiver unregistered");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Receiver not registered", e);
        }
    }
}