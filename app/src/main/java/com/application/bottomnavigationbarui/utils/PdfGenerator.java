package com.application.bottomnavigationbarui.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import com.github.devfrogora.service.dto.reports.BillReportDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfGenerator {

    public static void generateBillPdf(Context context, BillReportDto bill) {
        // 1. Create a native PdfDocument instance
        PdfDocument pdfDocument = new PdfDocument();

        // 2. Define page size (Standard A4 size is roughly 595 x 842 pixels at 72 DPI)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // --- DRAWING THE PDF CONTENT ---

        // Header / Title
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(24);
        paint.setColor(Color.parseColor("#333333"));
        canvas.drawText("INVOICE / BILL", 40, 60, paint);

        // Divider line
        paint.setStrokeWidth(2f);
        paint.setColor(Color.LTGRAY);
        canvas.drawLine(40, 80, 555, 80, paint);

        // Bill Details (Labels)
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(14);
        paint.setColor(Color.DKGRAY);

        int startXLabel = 40;
        int startXValue = 180;

        canvas.drawText("Bill Number:", startXLabel, 120, paint);
        canvas.drawText("Billing Date:", startXLabel, 150, paint);
        canvas.drawText("Tenant Name:", startXLabel, 180, paint);
        canvas.drawText("Room Number:", startXLabel, 210, paint);
        canvas.drawText("Payment Status:", startXLabel, 240, paint);

        // Bill Details (Values)
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(Color.BLACK);

        canvas.drawText("#" + bill.getBillId(), startXValue, 120, paint);
        canvas.drawText(bill.getBillingDate(), startXValue, 150, paint);
        canvas.drawText(bill.getTenantName(), startXValue, 180, paint);
        canvas.drawText(String.valueOf(bill.getRoomNumber()), startXValue, 210, paint);

        // Color-coded status text
        if (bill.getPaymentStatus().equalsIgnoreCase("PAID")) {
            paint.setColor(Color.parseColor("#008000")); // Solid Green
        } else {
            paint.setColor(Color.parseColor("#800000")); // Solid Red
        }
        canvas.drawText(bill.getPaymentStatus().toUpperCase(), startXValue, 240, paint);

        // Another Divider
        paint.setColor(Color.LTGRAY);
        canvas.drawLine(40, 280, 555, 280, paint);

        // Total Amount Due Section
        paint.setColor(Color.BLACK);
        paint.setTextSize(18);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Total Amount:", 40, 320, paint);
        canvas.drawText("Rs. " + bill.getTotalAmount(), startXValue, 320, paint);

        // Footer Note
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        paint.setTextSize(12);
        paint.setColor(Color.GRAY);
        canvas.drawText("Thank you for your business!", 40, 400, paint);

        // Finish writing the page
        pdfDocument.finishPage(page);

        // 3. Save the document to the Downloads folder
        String fileName = "Bill_" + bill.getBillId() + ".pdf";
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File pdfFile = new File(downloadsDir, fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Toast.makeText(context, "PDF saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Always close the document to avoid memory leaks
            pdfDocument.close();
        }
    }
}