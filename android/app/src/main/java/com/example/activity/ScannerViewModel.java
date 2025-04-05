package com.example.activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScannerViewModel extends ViewModel {
    private final Set<String> scannedBarcodes = new HashSet<>();
    private final MutableLiveData<List<String>> scannedBoxesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> blockScanner = new MutableLiveData<>(false);

    public void handleNewBarcode(String barcode) {
        if (!scannedBarcodes.contains(barcode)) {
            scannedBarcodes.add(barcode);
            scannedBoxesLiveData.postValue(new ArrayList<>(scannedBarcodes));
        }
    }

    public LiveData<List<String>> getScannedBoxesLiveData() {
        return scannedBoxesLiveData;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public LiveData<Boolean> getBlockScanner() {
        return blockScanner;
    }

    public void showToast(String message) {
        toastMessage.postValue(message);
    }

    public void setBlockScanner(boolean block) {
        blockScanner.postValue(block);
    }
}