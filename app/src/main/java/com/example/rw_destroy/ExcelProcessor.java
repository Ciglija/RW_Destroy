package com.example.rw_destroy;

import java.io.FileInputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import android.util.Log;

public abstract class ExcelProcessor {
    protected FileInputStream fis;
    protected Workbook workbook;
    protected String filePath;

    public ExcelProcessor(String filePath) {
        this.filePath = filePath;
    }

    protected void openResources() throws IOException {
        fis = new FileInputStream(filePath);
        workbook = new XSSFWorkbook(fis);
    }

    protected void closeResources() {
        try {
            if (workbook != null) {
                workbook.close();
            }
            if (fis != null) {
                fis.close();
            }
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "Error while closing resources", e);
        }
    }

    public abstract void processFile();

}
