package com.example.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class BoxAlertDialog {

    public interface AlertResponseListener {
        void onContinueSelected();
        void onExitSelected();
    }

    public static  void showUnreadBoxesAlert(Context context, String data, AlertResponseListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Obaveštenje");
        builder.setMessage(data);

        builder.setPositiveButton("Nastavi sa radom", (dialog, which) -> {
            if (listener != null) {
                listener.onContinueSelected();
            }
            dialog.dismiss();
        });

        builder.setNegativeButton("Završi", (dialog, which) -> {
            if (listener != null) {
                listener.onExitSelected();
            }
            dialog.dismiss();
        });

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}