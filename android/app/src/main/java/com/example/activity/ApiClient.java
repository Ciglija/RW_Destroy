package com.example.activity;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ApiClient {

    private static final String BASE_URL = "http://192.168.0.30:5000/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    public static void loadUsers(Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "import-users")
                .post(RequestBody.create("", JSON))
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void loginUser(String username, String password, Callback callback)  {
        try{
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);
            Request request = new Request.Builder()
                    .url(BASE_URL + "login")
                    .post(RequestBody.create(json.toString(), JSON))
                    .build();
            client.newCall(request).enqueue(callback);
        } catch(JSONException ignored){}
    }

    public static void loadDatabase(Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "load-database")
                .post(RequestBody.create("", JSON))
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void generateReport(Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "generate-report")
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void sendBarcode(String token, String barcode, Callback callback) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("box_code", barcode);
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "scan-box")
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        client.newCall(request).enqueue(callback);
    }
    public static void checkAdmin(String username, String password, Callback callback) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("admin_username", username);
        json.put("admin_password", password);
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "admin-auth")
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
