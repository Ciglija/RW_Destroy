package com.example.rw_destroy;

import android.content.Context;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserCreatorTest {

    private UserCreator userCreator;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private Context context;

    @BeforeEach
    void setUp() throws Exception {


        // Mock the database connection and statement
        mockConnection = Mockito.mock(Connection.class);
        mockStatement = Mockito.mock(PreparedStatement.class);

        // Mock the DatabaseConnection to return the mock connection
        DatabaseConnection databaseConnection = Mockito.mock(DatabaseConnection.class);
        when(databaseConnection.connect()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        // Load the Excel file from the assets folder
        InputStream inputStream = context.getAssets().open("user_data.xlsx"); // Place the file in assets
        userCreator = new UserCreator("dummy_path") {
            @Override
            protected void openResources() throws IOException {
                this.workbook = new XSSFWorkbook(inputStream);
            }
        };
    }

    @Test
    void testProcessFile() throws Exception {
        // Execute the method under test
        userCreator.processFile();

        // Verify that the database insert was called with the correct values
        verify(mockStatement).setString(1, "John Doe");       // Example name
        verify(mockStatement).setString(2, "johndoe");       // Example username
        verify(mockStatement).setString(3, "password123");   // Example password
        verify(mockStatement).setString(4, "admin");         // Example status
        verify(mockStatement).setString(5, "adminPass");     // Example admin password
        verify(mockStatement).executeUpdate();
    }
}