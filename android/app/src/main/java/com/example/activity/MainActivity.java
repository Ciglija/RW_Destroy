package com.example.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
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

        initializeComponents();
        initializeEvents();
    }

    private void initializeComponents() {
        btnLoadDatabase = findViewById(R.id.btn_load_database);
        btnScan = findViewById(R.id.btn_scan);
        btnSendReport = findViewById(R.id.btn_create_report);
        btnFinish = findViewById(R.id.btn_finish);
    }

    private void initializeEvents() {
        btnLoadDatabase.setOnClickListener(v -> loadDatabase());
        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScannerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        btnSendReport.setOnClickListener(v -> sendReport());
        btnFinish.setOnClickListener(v -> finish());
    }

    private void loadDatabase() {
        ApiClient.loadDatabase(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greska Internet!", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Uspesno učitana baza!", Toast.LENGTH_SHORT).show());
                response.close();
            }
        });
    }

    private void sendReport() {
        ApiClient.generateReport(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greska Internet!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Report poslat!", Toast.LENGTH_SHORT).show());
                response.close();
            }
        });
    }
}