package com.example.rw_destroy;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Test BoxCreator
        testBoxCreator();

        // Test UserCreator
        testUserCreator();

        // Test Admin and Worker
        testAdminAndWorker();

        // Test Box
        testBox();

        // Test User
        testUser();
    }

    private void testUserCreator() {
        Log.d("TestActivity", "Testing BoxCreator...");

        String filePath = FileUtil.copyAssetToStorage(this, "Korisnici.xlsx");
        if (filePath != null) {
            UserCreator userCreator = new UserCreator(filePath);
            userCreator.processFile();
            Log.d("TestActivity", "File path: " + filePath);
        } else {
            Log.e("TestActivity", "Failed to copy asset file.");
        }

        Log.d("TestActivity", "BoxCreator test completed.");
    }

    private void testBoxCreator() {
        Log.d("TestActivity", "Testing BoxCreator...");

        String filePath = FileUtil.copyAssetToStorage(this, "Kutija.xlsx");
        if (filePath != null) {
            BoxCreator boxCreator = new BoxCreator(filePath);
            boxCreator.processFile();
            Log.d("TestActivity", "File path: " + filePath);
        } else {
            Log.e("TestActivity", "Failed to copy asset file.");
        }

        Log.d("TestActivity", "BoxCreator test completed.");
    }


    private void testAdminAndWorker() {
        Log.d("TestActivity", "Testing Admin and Worker...");

        // Create an Admin instance
        Admin admin = new Admin("Admin Name", "admin", "admin123", "adminPass");
        Log.d("TestActivity", "Admin isAdmin(): " + admin.isAdmin()); // Should print true

        // Create a Worker instance
        Worker worker = new Worker("Worker Name", "worker", "worker123");
        Log.d("TestActivity", "Worker isAdmin(): " + worker.isAdmin()); // Should print false

        Log.d("TestActivity", "Admin and Worker test completed.");
    }

    private void testBox() {
        Log.d("TestActivity", "Testing Box...");

        // Create a Box instance
        Box box = new Box("12345", "Client1", 300);
        Log.d("TestActivity", "Box Barcode: " + box.getBarcode()); // Should print 12345
        Log.d("TestActivity", "Box Client: " + box.getClient());   // Should print Client1
        Log.d("TestActivity", "Box Count: " + box.getCount());    // Should print 300

        Log.d("TestActivity", "Box test completed.");
    }

    private void testUser() {
        Log.d("TestActivity", "Testing User...");

        // Create a User instance (using Worker as an example)
        User user = new Worker("John Doe", "johndoe", "password123");
        Log.d("TestActivity", "User Name: " + user.getName());       // Should print John Doe
        Log.d("TestActivity", "User Username: " + user.getUsername()); // Should print johndoe
        Log.d("TestActivity", "User Password: " + user.getPassword()); // Should print password123

        Log.d("TestActivity", "User test completed.");
    }
}