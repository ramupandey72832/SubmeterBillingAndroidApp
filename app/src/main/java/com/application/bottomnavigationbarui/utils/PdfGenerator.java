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
import java.util.List;
import java.util.Locale;

public class PdfGenerator {

    // Enhanced single thermal bill dimensions (scaled slightly up for readability)
    private static final int PAGE_WIDTH = 220;
    private static final int PAGE_HEIGHT = 340;

    // Standard A4 dimensions at 72 DPI
    private static final int A4_WIDTH = 595;
    private static final int A4_HEIGHT = 842;

    /**
     * Generates a single A4 PDF containing up to 8 bills organized in a 2x4 grid.
     * Typography sizes and vertical paddings have been increased significantly.
     */
    public static void generateMultipleBillsPdf(Context context, List<BillReportDto> bills) {
        if (bills == null || bills.isEmpty()) {
            Toast.makeText(context, "No bills provided to generate", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Grid parameters for 2 Columns x 4 Rows
        final int COLUMNS = 2;

        // Size allocation per bill block within the A4 container
        final int SLOT_WIDTH = A4_WIDTH / COLUMNS;  // ~297 pixels
        final int SLOT_HEIGHT = A4_HEIGHT / 4;      // ~210 pixels

        // --- ENLARGED TYPOGRAPHY CONFIGURATION ---
        final float titleTextSize = 12.0f;   // Increased from 9.5f
        final float regularTextSize = 9.5f;  // Increased from 7.5f
        final int leading = 11;              // Increased from 8 to support larger fonts
        final int indent = 16;

        int totalBillsToPrint = Math.min(bills.size(), 8);

        for (int i = 0; i < totalBillsToPrint; i++) {
            BillReportDto bill = bills.get(i);

            int col = i % COLUMNS;
            int row = i / COLUMNS;

            int offsetX = col * SLOT_WIDTH;
            int offsetY = row * SLOT_HEIGHT;

            int rightMarginEdge = offsetX + SLOT_WIDTH - indent;
            int currentY = offsetY + 16; // Top margin padding

            // --- METADATA HEADER ---
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            paint.setTextSize(titleTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.BLACK);
            canvas.drawText("Room Number: " + bill.getRoomNumber(), offsetX + (SLOT_WIDTH / 2f), currentY, paint);

            // Reset typography to enlarged regular scale
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTypeface(Typeface.MONOSPACE);
            paint.setTextSize(regularTextSize);
            currentY += leading + 3;
            canvas.drawText("Meter No.: " + bill.getMeterSerialNumber(), offsetX + indent, currentY, paint);

            currentY += leading + 3;
            canvas.drawText("Bill Date: " + bill.getBillingDate(), offsetX + indent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("Bill id:#"+bill.getBillId(), rightMarginEdge, currentY, paint);
//            canvas.drawText(, offsetX + indent, currentY, paint);
            paint.setTextAlign(Paint.Align.LEFT);

            currentY += leading + 2;

            // --- TENANT BLOCK ---
            canvas.drawText("Tenant Name: " + bill.getTenantName(), offsetX + indent, currentY, paint);
            currentY += 6;

            paint.setStrokeWidth(0.75f);
            canvas.drawLine(offsetX + indent, currentY, rightMarginEdge, currentY, paint);
            currentY += 11;

            // --- DYNAMIC READINGS SECTION ---
            double unitConsumed = bill.getCurrentReading() - bill.getPreviousReading();

            canvas.drawText("Current Reading", offsetX + indent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.valueOf((int) bill.getCurrentReading()), rightMarginEdge, currentY, paint);
            currentY += leading;

            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Previous Reading", offsetX + indent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.valueOf((int) bill.getPreviousReading()), rightMarginEdge, currentY, paint);
            currentY += leading;

            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Units Consumed", offsetX + indent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.valueOf((int) unitConsumed), rightMarginEdge, currentY, paint);
            currentY += leading;

            currentY += 2;
            canvas.drawLine(offsetX + indent, currentY, rightMarginEdge, currentY, paint);
            currentY += 11;

            // --- DYNAMIC CHARGES SECTION ---
            String chargeFormat = "%.2f";

            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Rate per Unit", offsetX + indent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.getDefault(), chargeFormat, bill.getRatePerUnit()), rightMarginEdge, currentY, paint);
            currentY += leading;

            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Fixed Charges", offsetX + indent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.getDefault(), chargeFormat, bill.getFixedCharge()), rightMarginEdge, currentY, paint);
            currentY += leading + 3;

            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)); // Bold section total
            canvas.drawText("Bill Amount", offsetX + indent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.getDefault(), chargeFormat, bill.getTotalAmount()), rightMarginEdge, currentY, paint);
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)); // Revert
            currentY += 5;

            canvas.drawLine(offsetX + indent, currentY, rightMarginEdge, currentY, paint);
            currentY += 11;

            // --- ARREARS & TOTAL DUE ---
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Arrears Due", offsetX + indent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("0.00", rightMarginEdge, currentY, paint);
            currentY += leading;

            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            canvas.drawText("Total Due", offsetX + indent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.getDefault(), chargeFormat, bill.getTotalAmount()), rightMarginEdge, currentY, paint);
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
            currentY += 5;

            canvas.drawLine(offsetX + indent, currentY, rightMarginEdge, currentY, paint);
            currentY += 12;

            // --- STATUS SYSTEM ---
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("STATUS:", offsetX + indent, currentY, paint);

            if (bill.getPaymentStatus() != null && bill.getPaymentStatus().equalsIgnoreCase("PAID")) {
                paint.setColor(Color.parseColor("#008000"));
            } else {
                paint.setColor(Color.parseColor("#800000"));
            }

            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            canvas.drawText(bill.getPaymentStatus() != null ? bill.getPaymentStatus().toUpperCase() : "UNPAID", offsetX + indent + 50, currentY, paint);


            // Revert settings for the next slot
            paint.setColor(Color.BLACK);
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));

            // Structural cutting lines
            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(0.5f);
            canvas.drawLine(offsetX, offsetY + SLOT_HEIGHT, offsetX + SLOT_WIDTH, offsetY + SLOT_HEIGHT, paint);
            canvas.drawLine(offsetX + SLOT_WIDTH, offsetY, offsetX + SLOT_WIDTH, offsetY + SLOT_HEIGHT, paint);
            paint.setColor(Color.BLACK);
        }

        pdfDocument.finishPage(page);

        // Save File Block
        String fileName = "A4_Combined_8_Bills.pdf";
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File pdfFile = new File(downloadsDir, fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Toast.makeText(context, "8-Grid A4 Document Saved Successfully", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Save Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }

    /**
     * Generates a single dedicated layout slip.
     * All typography and visual structure components have been scaled up.
     */
    public static void generateBillPdf(Context context, BillReportDto bill) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // --- ENLARGED SINGLE SLIP TYPOGRAPHY ---
        final float titleTextSize = 13.0f;   // Increased from 10.5f
        final float regularTextSize = 10.0f; // Increased from 8.5f
        final int leading = 13;              // Increased from 10
        final int indent = 10;
        final int rightMarginEdge = PAGE_WIDTH - indent;

        int currentY = 22;

        // Header Title
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        paint.setTextSize(titleTextSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
        canvas.drawText("Room Number: " + bill.getRoomNumber(), PAGE_WIDTH / 2f, currentY, paint);

        // Restoring Body configuration attributes
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextSize(regularTextSize);

        currentY += leading + 5;
        canvas.drawText("Bill Date  : " + bill.getBillingDate(), indent, currentY, paint);
        currentY += leading + 3;

        canvas.drawText("Tenant Name: " + bill.getTenantName(), indent, currentY, paint);
        currentY += 7;

        paint.setStrokeWidth(1.2f);
        canvas.drawLine(indent, currentY, rightMarginEdge, currentY, paint);
        currentY += 14;

        double unitConsumed = bill.getCurrentReading() - bill.getPreviousReading();

        // Calculations Data Map
        canvas.drawText("Current Reading", indent, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.valueOf((int)bill.getCurrentReading()), rightMarginEdge, currentY, paint);
        currentY += leading;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Previous Reading", indent, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.valueOf((int)bill.getPreviousReading()), rightMarginEdge, currentY, paint);
        currentY += leading;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Units Consumed", indent, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.valueOf((int)unitConsumed), rightMarginEdge, currentY, paint);
        currentY += leading;

        currentY += 3;
        canvas.drawLine(indent, currentY, rightMarginEdge, currentY, paint);
        currentY += 14;

        String chargeFormat = "%.2f";

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Rate per Unit", indent, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), chargeFormat, bill.getRatePerUnit()), rightMarginEdge, currentY, paint);
        currentY += leading;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Fixed Charges", indent, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), chargeFormat, bill.getFixedCharge()), rightMarginEdge, currentY, paint);
        currentY += leading + 5;

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText("Bill Amount", indent, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), chargeFormat, bill.getTotalAmount()), rightMarginEdge, currentY, paint);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        currentY += 10;

        canvas.drawLine(indent, currentY, rightMarginEdge, currentY, paint);
        currentY += 14;

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText("Arrears", indent, currentY, paint);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        currentY += leading;

        canvas.drawText("  Arrears Due", indent, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("0.00", rightMarginEdge, currentY, paint);
        currentY += leading;

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText("  Total Due", indent, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), chargeFormat, bill.getTotalAmount()), rightMarginEdge, currentY, paint);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        currentY += 10;

        canvas.drawLine(indent, currentY, rightMarginEdge, currentY, paint);
        currentY += 15;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("STATUS:", indent, currentY, paint);

        if (bill.getPaymentStatus() != null && bill.getPaymentStatus().equalsIgnoreCase("PAID")) {
            paint.setColor(Color.parseColor("#008000"));
        } else {
            paint.setColor(Color.parseColor("#800000"));
        }

        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText(bill.getPaymentStatus() != null ? bill.getPaymentStatus().toUpperCase() : "UNPAID", indent + 55, currentY, paint);

        pdfDocument.finishPage(page);

        String fileName = "Thermal_Bill_" + bill.getBillId() + ".pdf";
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File pdfFile = new File(downloadsDir, fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Toast.makeText(context, "Receipt Generated Successfully", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pdfDocument.close();
        }
    }
}