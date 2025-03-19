package com.example.rw_destroy;

import android.util.Log;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class BoxCreator extends ExcelProcessor {

    public BoxCreator(String filePath) {
        super(filePath);
    }

    @Override
    public void processFile() {
        try {
            openResources();
            readRowsAndInsertData();
        } catch (IOException e) {
            Log.e("BoxCreator", "Error while processing Excel file", e);
        } finally {
            closeResources();
        }
    }

    private void readRowsAndInsertData() {
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            String client = row.getCell(0).getStringCellValue();
            String barcode = row.getCell(1).getStringCellValue();
            int count = (int) row.getCell(2).getNumericCellValue();

            insertDataToDatabase(client, barcode, count);
        }
    }

    private void insertDataToDatabase(String client, String barcode, int count) {
        String query = "INSERT INTO boxes (client, barcode, count) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, client);
            statement.setString(2, barcode);
            statement.setInt(3, count);

            statement.executeUpdate();
        } catch (Exception e) {
            Log.e("BoxCreator", "Error while inserting data into the database", e);
        }
    }

}
