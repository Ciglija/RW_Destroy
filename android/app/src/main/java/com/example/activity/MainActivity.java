package com.example.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private Button btnLoadDatabase;
    private Button btnScan;
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
    }

    private void initializeEvents() {
        btnLoadDatabase.setOnClickListener(v -> unscannedCount(() -> checkClient(this::loadDatabase)));
        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScannerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        btnSendReport.setOnClickListener(v -> unscannedCount(this::sendReport));
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
    private void checkClient(Runnable onSuccess) {
        ApiClient.getClientName(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greška sa internetom!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                String responseBody = null;

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        responseBody = response.body().string();
                    }
                } catch (Exception e) {
                    Log.e("API_RESPONSE", "Greska prilikom citanja odgovora", e);
                }

                String finalResponseBody = responseBody;
                runOnUiThread(() -> {
                    try {
                        if (finalResponseBody != null) {
                            String clientName = new JSONObject(finalResponseBody).getString("client_name");

                            BoxAlertDialog.showUnreadBoxesAlert(MainActivity.this,
                                    "Da li se uništava klijent pod nazivom: " + clientName,
                                    new BoxAlertDialog.AlertResponseListener() {

                                        @Override
                                        public void onContinueSelected() {
                                            onSuccess.run();
                                        }

                                        @Override
                                        public void onExitSelected() {
                                        }
                                    });
                        } else {
                            Toast.makeText(MainActivity.this, "Greška u server odgovoru!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Greška u obradi podataka!", Toast.LENGTH_SHORT).show();
                        Log.e("API_RESPONSE", "Parsiranje nije uspelo", e);
                    }
                });
                response.close();
            }
        });
    }

    private void unscannedCount(Runnable onSuccess) {
        ApiClient.getUnscannedCount(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greška sa internetom!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                String responseBody = null;

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        responseBody = response.body().string();
                    }
                } catch (Exception e) {
                    Log.e("API_RESPONSE", "Greska prilikom citanja odgovora", e);
                }

                String finalResponseBody = responseBody;
                runOnUiThread(() -> {
                    try {
                        if (finalResponseBody != null) {
                            int unscannedCount = new JSONObject(finalResponseBody).getInt("unscanned");

                            BoxAlertDialog.showUnreadBoxesAlert(MainActivity.this,
                                    "Ostalo je još: " + unscannedCount + " neskeniranih kutija.",
                                    new BoxAlertDialog.AlertResponseListener() {
                                        @Override
                                        public void onContinueSelected() {
                                            onSuccess.run();
                                        }

                                        @Override
                                        public void onExitSelected() {
                                        }
                                    });
                        } else {
                            Toast.makeText(MainActivity.this, "Greška u server odgovoru!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Greška u obradi podataka!", Toast.LENGTH_SHORT).show();
                        Log.e("API_RESPONSE", "Parsiranje nije uspelo", e);
                    }
                });
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