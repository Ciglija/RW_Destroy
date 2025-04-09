package com.example.activity;

import android.app.AlertDialog;
import android.content.Context;

public class BoxAlertDialog {

    public interface AlertResponseListener {
        void onContinueSelected();
    }

    public static void showUnreadBoxesAlert(Context context, String message, boolean hasNegativeButton, AlertResponseListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Upozorenje!");
        builder.setMessage(message);

        builder.setPositiveButton(hasNegativeButton ? "Da" : "Razumem", (dialog, which) -> {
            if (listener != null) {
                listener.onContinueSelected();
            }
            dialog.dismiss();
        });

        if (hasNegativeButton) {
            builder.setNegativeButton("Ne", (dialog, which) -> dialog.dismiss());
        }

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
