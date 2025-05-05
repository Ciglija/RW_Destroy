package com.example.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
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
        btnLoadDatabase.setOnClickListener(v -> getUnscannedCount(() -> checkClient(this::loadDatabase), getString(R.string.ldb_error)));
        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScannerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        btnSendReport.setOnClickListener(v -> getUnscannedCount(this::sendReport, getString(R.string.crp_error)));
    }

    private void loadDatabase() {

        ApiClient.loadDatabase(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> StylishToast.show(MainActivity.this, getString(R.string.internet_error)));
            }

            @Override
            public void onResponse(Call call, Response response) {
                BarcodeStorage.clear();
                runOnUiThread(() -> StylishToast.show(MainActivity.this, getString(R.string.ldb_successful)));
                response.close();
            }
        });
    }
    private void checkClient(Runnable onSuccess) {
        ApiClient.getClientName(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> StylishToast.show(MainActivity.this, getString(R.string.internet_error)));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (response) {
                    assert response.body() != null;
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    String clientName = json.getString("client_name");
                    runOnUiThread(()-> BoxAlertDialog.showUnreadBoxesAlert(MainActivity.this,
                            "Da li se uništava klijent pod nazivom: " + clientName + "?",
                            true,
                            onSuccess::run));
                } catch (JSONException | IOException e) {
                    runOnUiThread(() -> StylishToast.show(MainActivity.this, getString(R.string.internet_error)));
                }

            }
        });
    }

    private void getUnscannedCount(Runnable onSuccess, String msgStr) {
        ApiClient.getUnscannedCount(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> StylishToast.show(MainActivity.this, getString(R.string.internet_error)));
            }

            @Override
            public void onResponse(Call call, Response response) {

                try (response) {
                    assert response.body() != null;
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    int unscannedCount = json.getInt("unscanned");

                        if (unscannedCount > 0) {
                            runOnUiThread(()-> BoxAlertDialog.showUnreadBoxesAlert(MainActivity.this,
                                    msgStr + "Imate još: " + unscannedCount + "  kutija iz prethodne baze.",
                                    false,
                                    () -> {}));
                        } else {
                            onSuccess.run();
                        }

                } catch (JSONException | IOException e) {
                    runOnUiThread(() -> StylishToast.show(MainActivity.this, getString(R.string.internet_error)));
                }

            }
        });
    }

    private void sendReport() {
        runOnUiThread(() -> {
            btnSendReport.setEnabled(false);
            btnSendReport.setText(R.string.crating_report_btn_txt);
        });
        ApiClient.generateReport(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    StylishToast.show(MainActivity.this, getString(R.string.internet_error));
                    btnSendReport.setText(R.string.btn_create_report_text);
                    btnSendReport.setEnabled(true);
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    StylishToast.show(MainActivity.this, getString(R.string.crp_successful));
                    btnSendReport.setText(R.string.btn_create_report_text);
                    btnSendReport.setEnabled(true);
                });
                response.close();
            }
        });

    }
}