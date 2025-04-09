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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONException;
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

    private TextView textView;
    private ArrayAdapter<String> adapter;
    private String token;
    private BroadcastReceiver barcodeReceiver;
    private ScannerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        initializeComponents();
        getUnscannedCount();
        setupBarcodeReceiver();
        setupObservers();
    }

    private void initializeComponents() {
        ListView listView = findViewById(R.id.barcode_list);
        textView = findViewById(R.id.unscanned_count_label);
        token = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("jwt_token", "");
        viewModel = new ViewModelProvider(this).get(ScannerViewModel.class);
        viewModel.updateBarcodesFromStorage();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getScannedBoxesLiveData().observe(this, barcodes -> {
            adapter.clear();
            adapter.addAll(barcodes);
            adapter.notifyDataSetChanged();
        });

        viewModel.getCntUnscanned().observe(this, count ->{
            if(count > 0)
                textView.setText("Broj neočitanih kutija: " + count);
            else
                textView.setText("Završeno!");
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
                    else{
                        String body = response.body().string();
                        JSONObject json = new JSONObject(body);
                        boolean alreadyScanned = json.optBoolean("already_scanned", false);
                        if (!alreadyScanned) {
                            viewModel.updateCnt();
                        }
                    }
                } catch (JSONException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    private void blockScanner() {
        AlertDialogHelper.showAdminAuthDialog(ScannerActivity.this, (username, password) -> checkAdminCredentials(username, password, isCorrect -> {
            if (isCorrect) {
                runOnUiThread(() ->Toast.makeText(ScannerActivity.this, "Rad nastavljen ✅", Toast.LENGTH_SHORT).show());
            } else {
                runOnUiThread(() -> blockScanner());
            }
        }));
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

    private void getUnscannedCount() {
        ApiClient.getUnscannedCount(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ScannerActivity.this, "Greška sa internetom!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    int unscannedCount = json.getInt("unscanned");
                    runOnUiThread(() -> {
                        viewModel.setCntUnscanned(unscannedCount);
                    });
                } catch (Exception e) {
                    Toast.makeText(ScannerActivity.this, "Greška u obradi podataka!", Toast.LENGTH_SHORT).show();
                }
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