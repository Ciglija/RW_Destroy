package com.example.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScannerActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> scannedBoxes = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private static final String SCAN_ACTION = "com.symbol.datawedge.scanner.ACTION";
    private static final String BARCODE_DATA = "com.symbol.datawedge.data_string";
    private String token;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        listView = findViewById(R.id.barcode_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scannedBoxes);
        listView.setAdapter(adapter);

        token = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("jwt_token", "");

        IntentFilter filter = new IntentFilter();
        filter.addAction(SCAN_ACTION);
        registerReceiver(barcodeReceiver, filter);
    }

    private final BroadcastReceiver barcodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(BARCODE_DATA)) {
                String barcode = intent.getStringExtra(BARCODE_DATA);
                scannedBoxes.add(barcode);
                adapter.notifyDataSetChanged();
                sendBarcodeToServer(barcode);
            }
        }
    };

    private void sendBarcodeToServer(String barcode) {
        try {
            JSONObject json = new JSONObject();
            json.put("box_code", barcode);

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("http://192.168.1.100:5000/scan-box")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(ScannerActivity.this, "Error sending barcode", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> Toast.makeText(ScannerActivity.this, "Barcode sent!", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(barcodeReceiver);
    }
}