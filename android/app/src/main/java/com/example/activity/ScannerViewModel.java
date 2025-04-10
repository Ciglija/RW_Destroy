package com.example.activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ScannerViewModel extends ViewModel {
    private final MutableLiveData<List<String>> scannedBoxesLiveData = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Integer> cntUnscanned = new MutableLiveData<>(0);

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
        }
    }

    public LiveData<List<String>> getScannedBoxesLiveData() {
        return scannedBoxesLiveData;
    }

    public MutableLiveData<Integer> getCntUnscanned() { return cntUnscanned; }


    public void updateBarcodesFromStorage() {
        scannedBoxesLiveData.postValue(BarcodeStorage.getBarcodes());
    }

    public void setCntUnscanned(int val) {
        cntUnscanned.postValue(val);
    }
    public void updateCnt(){
        cntUnscanned.postValue(cntUnscanned.getValue()-1);
    }

}
