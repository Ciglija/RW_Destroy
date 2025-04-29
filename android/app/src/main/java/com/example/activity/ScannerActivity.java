package com.example.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ScannerActivity extends AppCompatActivity {

    private static final String TAG = "ScannerActivity";
    private static final String DATAWEDGE_SCANNER_OUTPUT_ACTION = "com.symbol.datawedge.scanner.ACTION";
    private static final String BARCODE_DATA = "com.symbol.datawedge.data_string";

    private TextView textView;
    private BarcodeAdapter adapter;
    private String token;
    private BroadcastReceiver barcodeReceiver;
    private ScannerViewModel viewModel;
    private MediaPlayer mpError;



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
        RecyclerView recyclerView = findViewById(R.id.barcode_list);
        textView = findViewById(R.id.unscanned_count_label);
        token = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("jwt_token", "");
        viewModel = new ViewModelProvider(this).get(ScannerViewModel.class);
        viewModel.updateBarcodesFromStorage();
        mpError = MediaPlayer.create(this, R.raw.error);

        adapter = new BarcodeAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }
    private void setupObservers() {
        viewModel.getScannedBoxesLiveData().observe(this, barcodes -> adapter.updateBarcodes(barcodes));

        viewModel.getCntUnscanned().observe(this, count ->{
            if(count > 0)
                textView.setText(getString(R.string.unscanned_count_text, count));
            else{
                textView.setText(R.string.finished_scanning);
                SuccessDialog.showSuccessDialog(this, () -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
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
                runOnUiThread(() -> StylishToast.show(ScannerActivity.this, getString(R.string.internet_error)));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (response) {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> blockScanner());
                        viewModel.handleNewBarcode(barcode);
                    }
                    else{
                        assert response.body() != null;
                        String body = response.body().string();
                        JSONObject json = new JSONObject(body);
                        boolean alreadyScanned = json.optBoolean("already_scanned", false);

                        if (!alreadyScanned) {
                            runOnUiThread(() -> StylishToast.show(ScannerActivity.this, getString(R.string.good_scan)));
                            int cnt = json.getInt("unscanned");
                            viewModel.setCntUnscanned(cnt);
                            viewModel.handleNewBarcode(barcode);
                        }else{
                            mpError.start();
                            runOnUiThread(() -> StylishToast.show(ScannerActivity.this, getString(R.string.already_scanned_text)));
                        }
                    }
                } catch (JSONException | IOException e) {
                    runOnUiThread(() -> StylishToast.show(ScannerActivity.this, getString(R.string.internet_error)));
                }
            }
        });

    }

    private void blockScanner() {
        mpError.start();
        AlertDialogHelper.showAdminAuthDialog(ScannerActivity.this, (username, password) -> checkAdminCredentials(username, password, isCorrect -> {
            if (isCorrect) {
                runOnUiThread(() -> StylishToast.show(ScannerActivity.this, getString(R.string.continue_work_text)));
            } else {
                runOnUiThread(this::blockScanner);
            }
        }));
    }

    private void checkAdminCredentials(String username, String password, PasswordCheckCallback callback) {
        ApiClient.checkAdmin(username, password, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    runOnUiThread(() -> StylishToast.show(ScannerActivity.this, getString(R.string.internet_error)));
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
                runOnUiThread(() -> StylishToast.show(ScannerActivity.this, getString(R.string.internet_error)));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    assert response.body() != null;
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    int unscannedCount = json.getInt("unscanned");
                    runOnUiThread(()-> viewModel.setCntUnscanned(unscannedCount));
                } catch (Exception e) {
                    runOnUiThread(() -> StylishToast.show(ScannerActivity.this, getString(R.string.internet_error)));
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
        if (mpError != null) {
            mpError.release();
            mpError = null;
        }
    }

    interface PasswordCheckCallback {
        void onResult(boolean isCorrect);
    }
}