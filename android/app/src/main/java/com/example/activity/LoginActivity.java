package com.example.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://192.168.0.30:5000/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private EditText usernameEditText;
    private EditText passwordEditText;
    private AppCompatButton loginButton;
    private SharedPreferences prefs;

    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeComponents();
        initializeEvents();
        setupHttpClient();
    }
    private void initializeComponents() {
        usernameEditText = findViewById(R.id.username_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.btn_login);
        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
    }

    private void initializeEvents(){
        loadUsers();
        loginButton.setOnClickListener(v -> loginUserFun());
    }
    private void setupHttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    private void loadUsers() {
        RequestBody body = RequestBody.create("{}",JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "import-users")
                .post(body)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Greška u konekciji!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Korisnici nisu učitani molimo vas da restartujete aplikaciju.", Toast.LENGTH_SHORT).show());
                }
                //response.close();
            }
        });
    }

    private void loginUserFun() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Unesite korisničko ime i lozinku!", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder().url(BASE_URL + "login").post(body).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Greška u konekciji!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        String token = jsonResponse.getString("access_token");

                        prefs.edit().putString("jwt_token", token).apply();
                        runOnUiThread(() -> {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        response.close();
                    }
                }
                else{
                    passwordEditText.setText("");
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Pogrešni kredencijali!", Toast.LENGTH_SHORT).show();
                    });
                    response.close();
                }
            }
        });
    }
}