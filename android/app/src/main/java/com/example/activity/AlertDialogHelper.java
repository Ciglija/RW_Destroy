package com.example.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AlertDialogHelper {

    public interface AdminAuthCallback {
        void onCredentialsEntered(String username, String password);
    }

    public static void showAdminAuthDialog(Context context, AdminAuthCallback callback) {
        try {
            if (context == null) {
                return;
            }

            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_admin_auth, null);

            EditText etUsername = dialogView.findViewById(R.id.etUsername);
            EditText etPassword = dialogView.findViewById(R.id.etPassword);

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .setPositiveButton("Potvrdi", (dialog, which) -> {
                        String adminUsername = etUsername.getText().toString().trim();
                        String adminPassword = etPassword.getText().toString().trim();

                        if (adminUsername.isEmpty() || adminPassword.isEmpty()) {
                            Toast.makeText(context, "Unesite oba podatka!", Toast.LENGTH_SHORT).show();
                            showAdminAuthDialog(context, callback);
                        } else {
                            callback.onCredentialsEntered(adminUsername, adminPassword);
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnShowListener(dialogInterface -> etUsername.requestFocus());
            dialog.show();

        } catch (Exception e) {
            Toast.makeText(context, "Greška pri prikazivanju dijaloga!", Toast.LENGTH_SHORT).show();
        }
    }
}