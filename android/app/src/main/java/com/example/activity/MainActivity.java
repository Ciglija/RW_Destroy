package com.example.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private Button btnLoadDatabase;
    private Button btnScan;
    private Button btnFinish;
    private Button btnSendReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLoadDatabase = findViewById(R.id.btn_load_database);
        btnScan =findViewById(R.id.btn_scan);
        btnSendReport = findViewById(R.id.btn_create_report);
        btnFinish = findViewById(R.id.btn_finish);

        btnLoadDatabase.setOnClickListener(v -> loadDatabase());
        btnScan.setOnClickListener(v -> startActivity(new Intent(this, ScannerActivity.class)));
        btnSendReport.setOnClickListener(v -> sendReport());
        btnFinish.setOnClickListener(v -> finish());

    }
    private void loadDatabase() {
        String SERVER_URL = "http://192.168.0.30:5000/load-database";
        String token = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("jwt_token", "");

        Request request = new Request.Builder()
                .url(SERVER_URL)
                .post(RequestBody.create("", MediaType.get("application/json; charset=utf-8")))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greska Internet!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Uspesno učitana baza!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void sendReport() {
        String SERVER_URL = "http://192.168.0.30:5000/generate-report";
        new OkHttpClient().newCall(new Request.Builder().url(SERVER_URL).get().build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greska Internet!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response){
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Report poslat!", Toast.LENGTH_SHORT).show());
            }
        });
    }
}