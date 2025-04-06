package com.example.activity;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BarcodeStorage {
    private static final String PREF_NAME = "BarcodePrefs";
    private static final String BARCODE_KEY = "scanned_barcodes";

    private static final Set<String> scannedBarcodes = new HashSet<>();

    public static void addBarcode(String barcode) {
        scannedBarcodes.add(barcode);
    }
    public static final Set<String> getScannedBarcodes() { return scannedBarcodes; }
    public static List<String> getBarcodes() {
        return new ArrayList<>(scannedBarcodes);
    }

    public static void clear(Context context) {
        scannedBarcodes.clear();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(BARCODE_KEY).apply();
    }

    public static void saveToPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray(scannedBarcodes);
        prefs.edit().putString(BARCODE_KEY, jsonArray.toString()).apply();
    }

    public static void loadFromPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(BARCODE_KEY, null);
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                scannedBarcodes.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    scannedBarcodes.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
