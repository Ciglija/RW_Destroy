package com.example.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class BarcodeAdapter extends BaseAdapter {

    private Context context;
    private List<String> barkodovi;
    private LayoutInflater inflater;

    public BarcodeAdapter(Context context, List<String> barkodovi) {
        this.context = context;
        this.barkodovi = barkodovi;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return barkodovi.size();
    }

    @Override
    public Object getItem(int position) {
        return barkodovi.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        String barcode = barkodovi.get(position);
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(barcode);

        return convertView;
    }
}
