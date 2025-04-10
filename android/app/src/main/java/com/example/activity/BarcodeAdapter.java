package com.example.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BarcodeAdapter extends RecyclerView.Adapter<BarcodeAdapter.BarcodeViewHolder> {
    private List<String> barcodes = new ArrayList<>();

    public static class BarcodeViewHolder extends RecyclerView.ViewHolder {
        TextView barcodeText;

        public BarcodeViewHolder(View itemView) {
            super(itemView);
            barcodeText = itemView.findViewById(R.id.barcode_item_text);
        }
    }

    @Override
    public BarcodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_barcode, parent, false);
        return new BarcodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BarcodeViewHolder holder, int position) {
        holder.barcodeText.setText(barcodes.get(position));
    }

    @Override
    public int getItemCount() {
        return barcodes.size();
    }
    public void updateBarcodes(List<String> newBarcodes) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new BarcodeDiffCallback(barcodes, newBarcodes));
        barcodes = new ArrayList<>(newBarcodes);
        diffResult.dispatchUpdatesTo(this);
    }
}
class BarcodeDiffCallback extends DiffUtil.Callback {
    private final List<String> oldList;
    private final List<String> newList;

    public BarcodeDiffCallback(List<String> oldList, List<String> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
