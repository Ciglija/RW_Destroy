package com.example.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://192.168.0.30:5000/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private Button btnLoadDatabase;
    private Button btnScan;
    private Button btnFinish;
    private Button btnSendReport;
    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeComponents();
        initializeEvents();
        setupHttpClient();
    }

    private void initializeComponents() {
        btnLoadDatabase = findViewById(R.id.btn_load_database);
        btnScan =findViewById(R.id.btn_scan);
        btnSendReport = findViewById(R.id.btn_create_report);
        btnFinish = findViewById(R.id.btn_finish);
    }

    private void initializeEvents(){
        btnLoadDatabase.setOnClickListener(v -> loadDatabase());
        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScannerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        btnSendReport.setOnClickListener(v -> sendReport());
        btnFinish.setOnClickListener(v -> finish());
    }

    private void setupHttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }
    private void loadDatabase() {
        String token = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("jwt_token", "");

        Request request = new Request.Builder()
                .url(BASE_URL + "load-database")
                .post(RequestBody.create("", JSON))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greska Internet!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Uspesno učitana baza!", Toast.LENGTH_SHORT).show());
                response.close();
            }
        });
    }

    private void sendReport() {
        httpClient.newCall(new Request.Builder().url(BASE_URL + "generate-report").get().build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greska Internet!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response){
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Report poslat!", Toast.LENGTH_SHORT).show());
                response.close();
            }
        });
    }
}