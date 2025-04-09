package com.example.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AlertDialogHelper {

    public interface AdminAuthCallback {
        void onCredentialsEntered(String username, String password);
    }

    public static void showAdminAuthDialog(Context context, AdminAuthCallback callback) {
        try {
            if (context == null) return;

            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_admin_auth, null);

            EditText etUsername = dialogView.findViewById(R.id.etUsername);
            EditText etPassword = dialogView.findViewById(R.id.etPassword);
            Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            btnConfirm.setOnClickListener(v -> {
                String adminUsername = etUsername.getText().toString().trim();
                String adminPassword = etPassword.getText().toString().trim();

                if (adminUsername.isEmpty() || adminPassword.isEmpty()) {
                    Toast.makeText(context, "Unesite oba podatka!", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    callback.onCredentialsEntered(adminUsername, adminPassword);
                }
            });

            dialog.setOnShowListener(d -> etUsername.requestFocus());
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

        } catch (Exception e) {
            Toast.makeText(context, "Greška pri prikazivanju dijaloga!", Toast.LENGTH_SHORT).show();
        }
    }
}
