package com.example.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

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
        btnLoadDatabase.setOnClickListener(v -> getUnscannedCount(() -> checkClient(this::loadDatabase), "Nije moguće učitati novu bazu.\n"));
        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScannerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        btnSendReport.setOnClickListener(v -> getUnscannedCount(this::sendReport, "Nije moguće kreirati izveštaj.\n"));
    }

    private void loadDatabase() {

        ApiClient.loadDatabase(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greška sa internetom!!", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                BarcodeStorage.clear();
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
                try (response) {
                    assert response.body() != null;
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    String clientName = json.getString("client_name");
                    runOnUiThread(()->{
                    BoxAlertDialog.showUnreadBoxesAlert(MainActivity.this,
                            "Da li se uništava klijent pod nazivom: " + clientName + "?",
                            true,
                            onSuccess::run);
                    });
                } catch (JSONException | IOException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greška u odgovoru!", Toast.LENGTH_SHORT).show());
                }

            }
        });
    }

    private void getUnscannedCount(Runnable onSuccess, String msgStr) {
        ApiClient.getUnscannedCount(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greška sa internetom!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {

                try (response) {
                    assert response.body() != null;
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    int unscannedCount = json.getInt("unscanned");
                    runOnUiThread(()->{
                        if (unscannedCount > 0) {
                            BoxAlertDialog.showUnreadBoxesAlert(MainActivity.this,
                                    msgStr + "Imate još: " + unscannedCount + "  kutija iz prethodne baze.",
                                    false,
                                    () -> {});
                        } else {
                            onSuccess.run();
                        }
                    });
                } catch (JSONException | IOException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greška u odgovoru!", Toast.LENGTH_SHORT).show());
                }

            }
        });
    }

    private void sendReport() {
        ApiClient.generateReport(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Greška sa internetom!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Report poslat!", Toast.LENGTH_SHORT).show());
                response.close();
            }
        });
    }
}