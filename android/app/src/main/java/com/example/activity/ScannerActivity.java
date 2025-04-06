package com.example.activity;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScannerActivity extends AppCompatActivity {

    private static final String TAG = "ScannerActivity";
    private static final String DATAWEDGE_SCANNER_OUTPUT_ACTION = "com.symbol.datawedge.scanner.ACTION";
    private static final String BARCODE_DATA = "com.symbol.datawedge.data_string";
    private static final String BASE_URL = "http://192.168.0.30:5000/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private String token;
    private OkHttpClient httpClient;
    private BroadcastReceiver barcodeReceiver;
    private ScannerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        initializeComponents();
        setupHttpClient();
        setupBarcodeReceiver();
        setupObservers();
    }

    private void initializeComponents() {
        BarcodeStorage.loadFromPreferences(this);
        viewModel = new ViewModelProvider(this).get(ScannerViewModel.class);
        viewModel.updateBarcodesFromStorage();
        listView = findViewById(R.id.barcode_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);
        token = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("jwt_token", "");
    }

    private void setupObservers() {
        viewModel.getScannedBoxesLiveData().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> barcodes) {
                adapter.clear();
                adapter.addAll(barcodes);
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getToastMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    Toast.makeText(ScannerActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupHttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    private void setupBarcodeReceiver() {
        barcodeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && DATAWEDGE_SCANNER_OUTPUT_ACTION.equals(intent.getAction())) {
                    String barcode = intent.getStringExtra(BARCODE_DATA);
                    if (barcode != null && !barcode.isEmpty() && !BarcodeStorage.getScannedBarcodes().contains(barcode)) {
                        viewModel.handleNewBarcode(barcode);
                        sendBarcodeToServer(barcode);
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
        try {
            JSONObject json = new JSONObject();
            json.put("box_code", barcode);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "scan-box")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    viewModel.showToast("Greška sa internetom!");
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        if (!response.isSuccessful()) {
                            runOnUiThread(() -> blockScanner());
                        } else {
                            viewModel.showToast("Uspešno skeniranje!");
                        }
                    } finally {
                        response.close();
                    }
                }
            });
        } catch (JSONException e) {
            viewModel.showToast("Greška pri obradi barkoda");
        }
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
                        runOnUiThread(() -> {
                            Toast.makeText(ScannerActivity.this, "Pogrešni podaci!", Toast.LENGTH_SHORT).show();
                            blockScanner();
                        });
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
        try {
            JSONObject json = new JSONObject();
            json.put("admin_username", username);
            json.put("admin_password", password);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "admin-auth")
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(ScannerActivity.this, "Greška sa internetom!", Toast.LENGTH_SHORT).show();
                        callback.onResult(false);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        boolean success = response.isSuccessful();
                        runOnUiThread(() -> callback.onResult(success));
                    } finally {
                        response.close();
                    }
                }
            });
        } catch (JSONException e) {
            runOnUiThread(() -> callback.onResult(false));
        }
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
        BarcodeStorage.saveToPreferences(this);
    }
    interface PasswordCheckCallback {
        void onResult(boolean isCorrect);
    }
}