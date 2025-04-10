package com.example.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SuccessDialog {
    public interface OnReportClickListener {
        void onReportClick();
    }
    public static void showSuccessDialog(Context context, OnReportClickListener listener) {
        try {
            if (context == null) return;

            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_finish, null);
            Button btnDialogReport = dialogView.findViewById(R.id.btnDialogReport);

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            btnDialogReport.setOnClickListener(v -> {
                dialog.dismiss();
                if (listener != null) {
                    listener.onReportClick();
                }
            });

            dialog.show();

        } catch (Exception e) {
            Toast.makeText(context, R.string.error_dialog_box_text, Toast.LENGTH_SHORT).show();
        }
    }
}
