package com.application.bottomnavigationbarui.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import com.github.devfrogora.service.dto.reports.BillReportDto;
import java.io.File;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import java.util.List;

public class ExcelGenerator {

    public static void generateBillReport(Context context, List<BillReportDto> bills, String startDate, String endDate) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, "Billing_Report_" + startDate + "_to_" + endDate + ".xls");

        try {
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("Report", 0);

            // 1. Add Headers (column, row, text)
            String[] headers = {"Bill ID", "Room No.", "Tenant Name", "Billing Date", "Meter Serial", "Prev Reading", "Curr Reading", "Rate", "Fixed Charge", "Total", "Status"};
            for (int i = 0; i < headers.length; i++) {
                sheet.addCell(new Label(i, 0, headers[i]));
            }

            // 2. Add Data Rows
            int row = 1;
            for (BillReportDto item : bills) {
                sheet.addCell(new Number(0, row, item.getBillId()));
                sheet.addCell(new Label(1, row, item.getRoomNumber()));
                sheet.addCell(new Label(2, row, item.getTenantName()));
                sheet.addCell(new Label(3, row, item.getBillingDate()));
                sheet.addCell(new Label(4, row, item.getMeterSerialNumber()));
                sheet.addCell(new Number(5, row, item.getPreviousReading()));
                sheet.addCell(new Number(6, row, item.getCurrentReading()));
                sheet.addCell(new Number(7, row, item.getRatePerUnit()));
                sheet.addCell(new Number(8, row, item.getFixedCharge()));
                sheet.addCell(new Number(9, row, item.getTotalAmount()));
                sheet.addCell(new Label(10, row, item.getPaymentStatus()));
                row++;
            }

            workbook.write();
            workbook.close();
            Toast.makeText(context, "Excel file created successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to create file", Toast.LENGTH_SHORT).show();
        }
    }
}