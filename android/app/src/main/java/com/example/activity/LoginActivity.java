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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class LoginActivity extends AppCompatActivity {


    private EditText usernameEditText;
    private EditText passwordEditText;
    private AppCompatButton loginButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeComponents();
        initializeEvents();
    }

    private void initializeComponents() {
        usernameEditText = findViewById(R.id.username_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.btn_login);
        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
    }

    private void initializeEvents() {
        loadUsers();
        loginButton.setOnClickListener(v -> loginUserFun());
    }

    private void loadUsers() {

        ApiClient.loadUsers(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Greška u konekciji!", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Korisnici nisu učitani molimo vas da restartujete aplikaciju.", Toast.LENGTH_LONG).show());
                }
                response.close();
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

        ApiClient.loginUser(username, password, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Greška u konekciji!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        prefs.edit().putString("jwt_token", new JSONObject(response.body().string()).getString("access_token")).apply();
                        runOnUiThread(() -> {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        response.close();
                    }
                } else {
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