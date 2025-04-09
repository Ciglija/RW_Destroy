package com.example.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class BoxAlertDialog {

    public interface AlertResponseListener {
        void onContinueSelected();
        void onExitSelected();
    }

    public static  void showUnreadBoxesAlert(Context context, String data,String positiveBtnText, String negativeBtnText, AlertResponseListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Upozorenje!");
        builder.setMessage(data);

        builder.setPositiveButton(positiveBtnText, (dialog, which) -> {
            if (listener != null) {
                listener.onContinueSelected();
            }
            dialog.dismiss();
        });

        builder.setNegativeButton(negativeBtnText, (dialog, which) -> {
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