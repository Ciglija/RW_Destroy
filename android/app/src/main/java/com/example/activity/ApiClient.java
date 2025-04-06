package com.example.activity;

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


    private static Request buildPostRequest(String endpoint, JSONObject jsonBody) {
        RequestBody body = RequestBody.create(
                jsonBody != null ? jsonBody.toString() : "", JSON
        );
        return new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body)
                .build();
    }

    private static Request buildGetRequest(String endpoint) {
        return new Request.Builder()
                .url(BASE_URL + endpoint)
                .get()
                .build();
    }

    private static Request buildAuthorizedPostRequest(String endpoint, JSONObject jsonBody, String token) {
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        return new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }


    public static void loadUsers(Callback callback) {
        client.newCall(buildPostRequest("import-users", null)).enqueue(callback);
    }

    public static void loginUser(String username, String password, Callback callback) {
        try {
            JSONObject json = new JSONObject()
                    .put("username", username)
                    .put("password", password);
            client.newCall(buildPostRequest("login", json)).enqueue(callback);
        } catch (JSONException ignored) {
        }
    }

    public static void loadDatabase(Callback callback) {
        client.newCall(buildPostRequest("load-database", null)).enqueue(callback);
    }

    public static void generateReport(Callback callback) {
        client.newCall(buildGetRequest("generate-report")).enqueue(callback);
    }

    public static void sendBarcode(String token, String barcode, Callback callback) {
        try {
            JSONObject json = new JSONObject().put("box_code", barcode);
            client.newCall(buildAuthorizedPostRequest("scan-box", json, token)).enqueue(callback);
        } catch (JSONException ignored) {
        }
    }

    public static void checkAdmin(String username, String password, Callback callback) {
        try {
            JSONObject json = new JSONObject()
                    .put("admin_username", username)
                    .put("admin_password", password);
            client.newCall(buildPostRequest("admin-auth", json)).enqueue(callback);
        } catch (JSONException ignored) {
        }
    }
    public static void getUnscannedCount(Callback callback) {
        client.newCall(buildGetRequest("get-missing-count")).enqueue(callback);
    }
}
