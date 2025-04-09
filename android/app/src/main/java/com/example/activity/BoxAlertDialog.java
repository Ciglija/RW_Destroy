package com.example.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BoxAlertDialog {

    public interface AlertResponseListener {
        void onContinueSelected();
    }

    public static void showUnreadBoxesAlert(Context context, String message, boolean hasNegativeButton, AlertResponseListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_custom, null);

        TextView title = dialogView.findViewById(R.id.dialogTitle);
        TextView msg = dialogView.findViewById(R.id.dialogMessage);
        Button positiveBtn = dialogView.findViewById(R.id.positiveButton);
        Button negativeBtn = dialogView.findViewById(R.id.negativeButton);

        msg.setText(message);
        positiveBtn.setText(hasNegativeButton ? "Da" : "Razumem");

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        positiveBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContinueSelected();
            }
            dialog.dismiss();
        });

        if (hasNegativeButton) {
            negativeBtn.setVisibility(View.VISIBLE);
            negativeBtn.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }
}
