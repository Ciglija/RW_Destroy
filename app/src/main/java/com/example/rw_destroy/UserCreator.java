package com.example.rw_destroy;

import android.util.Log;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserCreator extends ExcelProcessor {

    public UserCreator(String filePath) {
        super(filePath);
    }

    @Override
    public void processFile() {
        try {
            openResources();
            readRowsAndInsertData();
        } catch (IOException e) {
            Log.e("UserCreator", "Error while processing Excel file", e);
        } finally {
            closeResources();
        }
    }

    private void readRowsAndInsertData() {
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            String name = row.getCell(0).getStringCellValue();
            String username = row.getCell(1).getStringCellValue();
            String password = row.getCell(2).getStringCellValue();
            String status = row.getCell(3).getStringCellValue();
            String adminPassword = row.getCell(4).getStringCellValue();

            insertDataToDatabase(name, username, password, status, adminPassword);
        }
    }

    private void insertDataToDatabase(String name, String username, String password, String status, String adminPassword) {
        String query = "INSERT INTO users (name, username, password, status, adminPassword) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setString(2, username);
            statement.setString(3, password);
            statement.setString(4, status);
            statement.setString(5, adminPassword);

            statement.executeUpdate();
        } catch (Exception e) {
            Log.e("UserCreator", "Error while inserting data into the database", e);
        }

    }
}
