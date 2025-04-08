package com.example.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ScannerActivity extends AppCompatActivity {

    private static final String TAG = "ScannerActivity";
    private static final String DATAWEDGE_SCANNER_OUTPUT_ACTION = "com.symbol.datawedge.scanner.ACTION";
    private static final String BARCODE_DATA = "com.symbol.datawedge.data_string";

    private Button btnFinish;
    private ArrayAdapter<String> adapter;
    private String token;
    private BroadcastReceiver barcodeReceiver;
    private ScannerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        initializeComponents();
        initializeEvents();
        setupBarcodeReceiver();
        setupObservers();
    }

    private void initializeComponents() {
        ListView listView = findViewById(R.id.barcode_list);
        btnFinish = findViewById(R.id.btn_finish);
        token = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("jwt_token", "");
        viewModel = new ViewModelProvider(this).get(ScannerViewModel.class);
        viewModel.updateBarcodesFromStorage();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);
    }

    private void initializeEvents() {
        btnFinish.setOnClickListener(v -> finishScanning());
    }

    private void setupObservers() {
        viewModel.getScannedBoxesLiveData().observe(this, barcodes -> {
            adapter.clear();
            adapter.addAll(barcodes);
            adapter.notifyDataSetChanged();
        });

        viewModel.getToastMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(ScannerActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBarcodeReceiver() {
        barcodeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && DATAWEDGE_SCANNER_OUTPUT_ACTION.equals(intent.getAction())) {
                    String barcode = intent.getStringExtra(BARCODE_DATA);
                    if (barcode != null && !barcode.isEmpty()) {
                        sendBarcodeToServer(barcode);
                        viewModel.handleNewBarcode(barcode);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(DATAWEDGE_SCANNER_OUTPUT_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(barcodeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }
    }

    private void sendBarcodeToServer(String barcode) {

        ApiClient.sendBarcode(token, barcode, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                viewModel.showToast("Greška sa internetom!");
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (response) {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> blockScanner());
                    }
                }
            }
        });

    }

    private void blockScanner() {
        AlertDialogHelper.showAdminAuthDialog(ScannerActivity.this, new AlertDialogHelper.AdminAuthCallback() {
            @Override
            public void onCredentialsEntered(String username, String password) {
                checkAdminCredentials(username, password, isCorrect -> {
                    if (isCorrect) {
                        runOnUiThread(() ->
                                Toast.makeText(ScannerActivity.this, "Rad nastavljen ✅", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> blockScanner());
                    }
                });
            }

            @Override
            public void onDialogCanceled() {
                runOnUiThread(() ->
                        Toast.makeText(ScannerActivity.this, "Potrebna je administratorska dozvola", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void checkAdminCredentials(String username, String password, PasswordCheckCallback callback) {
        ApiClient.checkAdmin(username, password, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ScannerActivity.this, "Greška sa internetom!", Toast.LENGTH_SHORT).show();
                    callback.onResult(false);
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (response) {
                    runOnUiThread(() -> callback.onResult(response.isSuccessful()));
                }
            }
        });
    }

    private void finishScanning() {
        ApiClient.getUnscannedCount(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ScannerActivity.this, "Greška sa internetom!", Toast.LENGTH_SHORT).show());
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

                            BoxAlertDialog.showUnreadBoxesAlert(ScannerActivity.this,
                                    "Ostalo je još: " + unscannedCount + " neskeniranih kutija.",
                                    "Nastavi skeniranje",
                                    "Završi",
                                    new BoxAlertDialog.AlertResponseListener() {
                                        @Override
                                        public void onContinueSelected() {}

                                        @Override
                                        public void onExitSelected() {
                                            finish();
                                        }
                                    });
                        } else {
                            Toast.makeText(ScannerActivity.this, "Greška u server odgovoru!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(ScannerActivity.this, "Greška u obradi podataka!", Toast.LENGTH_SHORT).show();
                        Log.e("API_RESPONSE", "Parsiranje nije uspelo", e);
                    }
                });
            }

        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (barcodeReceiver != null) {
                unregisterReceiver(barcodeReceiver);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Receiver not registered", e);
        }
    }

    interface PasswordCheckCallback {
        void onResult(boolean isCorrect);
    }
}