package com.example.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class ScannerActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> scannedBoxes = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String token;

    private static final String TAG = "BarcodeScanner";
    private static final String DATAWEDGE_SCANNER_OUTPUT_ACTION = "com.symbol.datawedge.scanner.ACTION";
    private static final String BARCODE_DATA = "com.symbol.datawedge.data_string";

    private BroadcastReceiver barcodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String barcode = intent.getStringExtra(BARCODE_DATA);
            if (barcode != null && !barcode.isEmpty()) {
                Log.d(TAG, "Scanned barcode: " + barcode);
                runOnUiThread(() -> {
                    scannedBoxes.add(barcode);
                    adapter.notifyDataSetChanged();
                });
                sendBarcodeToServer(barcode);

            } else {
                Log.d(TAG, "Received scan intent but no barcode data found");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);;
        setContentView(R.layout.activity_scanner);

        listView = findViewById(R.id.barcode_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scannedBoxes);
        listView.setAdapter(adapter);
        token = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("jwt_token", "");

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

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("http://192.168.0.30:5000/scan-box")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(ScannerActivity.this, "Greska sa internetom!", Toast.LENGTH_SHORT).show());
                    blockScanner();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(ScannerActivity.this, "Uspesno skeniranje!", Toast.LENGTH_SHORT).show());
                    }
                    else blockScanner();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void blockScanner() {
        runOnUiThread(() -> {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScannerActivity.this);
                builder.setTitle("Aplikacija blokirana!");
                builder.setMessage("Unesite admin podatke za nastavak:");

                LinearLayout layout = new LinearLayout(ScannerActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50, 40, 50, 10);

                TextView usernameLabel = new TextView(ScannerActivity.this);
                usernameLabel.setText("Korisničko ime:");
                final EditText usernameInput = new EditText(ScannerActivity.this);
                usernameInput.setInputType(InputType.TYPE_CLASS_TEXT);
                usernameInput.setHint("Unesite korisničko ime");

                TextView passwordLabel = new TextView(ScannerActivity.this);
                passwordLabel.setText("Šifra:");
                final EditText passwordInput = new EditText(ScannerActivity.this);
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordInput.setHint("Unesite šifru");

                layout.addView(usernameLabel);
                layout.addView(usernameInput);
                layout.addView(passwordLabel);
                layout.addView(passwordInput);

                builder.setView(layout);
                builder.setCancelable(false);

                builder.setPositiveButton("Potvrdi", (dialog, which) -> {
                    String adminUsername = usernameInput.getText().toString().trim();
                    String adminPassword = passwordInput.getText().toString().trim();

                    if (adminUsername.isEmpty() || adminPassword.isEmpty()) {
                        Toast.makeText(ScannerActivity.this, "Unesite oba podatka!", Toast.LENGTH_SHORT).show();
                        blockScanner();
                    } else {
                        checkAdminCredentials(adminUsername, adminPassword, isCorrect -> {
                            if (isCorrect) {
                                Toast.makeText(ScannerActivity.this, "Rad nastavljen ✅", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ScannerActivity.this, "Pogrešni podaci!", Toast.LENGTH_SHORT).show();
                                blockScanner();
                            }
                        });
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                
                usernameInput.requestFocus();

            } catch (Exception e) {
                Log.e(TAG, "Error showing dialog", e);
                Toast.makeText(ScannerActivity.this, "Greška pri prikazivanju dijaloga!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAdminCredentials(String username, String password, PasswordCheckCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("admin_username", username);
            json.put("admin_password", password);

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("http://192.168.0.30:5000/admin-auth")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Admin credentials check failed", e);
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
        } catch (Exception e) {
            Log.e(TAG, "Error checking admin credentials", e);
            runOnUiThread(() -> callback.onResult(false));
        }
    }
    interface PasswordCheckCallback {
        void onResult(boolean isCorrect);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(barcodeReceiver);
            Log.d(TAG, "Receiver unregistered");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Receiver not registered", e);
        }
    }
}