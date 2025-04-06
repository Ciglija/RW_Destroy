package com.example.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class BoxAlertDialog {

    public interface AlertResponseListener {
        void onContinueSelected();
        void onExitSelected();
    }

    public static void showUnreadBoxesAlert(Context context, int unreadCount, AlertResponseListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Obaveštenje");
        builder.setMessage("Ostalo je još: " + unreadCount + " neskeniranih kutija,");

        builder.setPositiveButton("Nastavi sa radom", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onContinueSelected();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Završi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onExitSelected();
                }
                dialog.dismiss();
            }
        });

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}