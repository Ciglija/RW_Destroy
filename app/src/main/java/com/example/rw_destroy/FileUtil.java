package com.example.rw_destroy;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    public static String copyAssetToStorage(Context context, String fileName) {
        File outFile = new File(context.getFilesDir(), fileName); // Kreira putanju u internal storage
        try (InputStream is = context.getAssets().open(fileName);
             FileOutputStream fos = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
            }
            return outFile.getAbsolutePath(); // Vraća apsolutni put do kopiranog fajla
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

