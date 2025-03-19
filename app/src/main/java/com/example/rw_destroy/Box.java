package com.example.rw_destroy;

public class Box {
    private String barcode;
    private String client;
    private int count;

    public Box(String barcode, String client, int count) {
        this.barcode = barcode;
        this.client = client;
        this.count = count;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getClient() {
        return client;
    }

    public int getCount() {
        return count;
    }

}