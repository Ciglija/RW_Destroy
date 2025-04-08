package com.example.activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ScannerViewModel extends ViewModel {
    private final MutableLiveData<List<String>> scannedBoxesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public void handleNewBarcode(String barcode) {
        if (BarcodeStorage.addBarcode(barcode)) {
            List<String> current = scannedBoxesLiveData.getValue();
            if (current != null) {
                current.add(barcode);
                scannedBoxesLiveData.postValue(new ArrayList<>(current));
            } else {
                List<String> newList = new ArrayList<>();
                newList.add(barcode);
                scannedBoxesLiveData.postValue(newList);
            }
            showToast("Uspešno skeniranje ✅");
        } else {
            showToast("Barkod vec skeniran.");
        }
    }

    public LiveData<List<String>> getScannedBoxesLiveData() {
        return scannedBoxesLiveData;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public void showToast(String message) {
        toastMessage.postValue(message);
    }

    public void updateBarcodesFromStorage() {
        scannedBoxesLiveData.postValue(BarcodeStorage.getBarcodes());
    }
}
