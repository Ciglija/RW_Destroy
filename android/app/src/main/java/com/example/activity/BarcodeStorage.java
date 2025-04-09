package com.example.activity;

import java.util.*;

public class BarcodeStorage {
    private static final LinkedHashSet<String> scannedBarcodes = new LinkedHashSet<>();

    public static boolean addBarcode(String barcode) {
        return scannedBarcodes.add(barcode);
    }

    public static List<String> getBarcodes() {
        return new ArrayList<>(scannedBarcodes);
    }
    public static void clear(){
        scannedBarcodes.clear();
    }
}
