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

    private static final int PAGE_WIDTH = 220;
    private static final int PAGE_HEIGHT = 340;

    // Standard A4 dimensions at 72 DPI
    private static final int A4_WIDTH = 595;
    private static final int A4_HEIGHT = 842;

    /**
     * Generates an A4 PDF Document with dynamic pagination,
     * organizing exactly 8 bills per page in a 2x4 grid arrangement.
     */
    public static void generateMultipleBillsPdf(Context context, List<BillReportDto> bills) {
        if (bills == null || bills.isEmpty()) {
            Toast.makeText(context, "No bills provided to generate", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();

        // Grid structural tracking variables
        final int COLUMNS = 2;
        final int MAX_BILLS_PER_PAGE = 8;
        final int SLOT_WIDTH = A4_WIDTH / COLUMNS;  // ~297 pixels
        final int SLOT_HEIGHT = A4_HEIGHT / 4;      // ~210 pixels

        // Typography settings
        final float titleTextSize = 12.0f;
        final float regularTextSize = 9.5f;
        final int leading = 11;
        final int indent = 16;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        PdfDocument.Page currentPage = null;
        Canvas canvas = null;
        int pageNumber = 0;

        for (int i = 0; i < bills.size(); i++) {
            // FIXED: Dynamically instantiate a fresh page every time index strikes a multiple of 8
            if (i % MAX_BILLS_PER_PAGE == 0) {
                if (currentPage != null) {
                    pdfDocument.finishPage(currentPage);
                }
                pageNumber++;
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, pageNumber).create();
                currentPage = pdfDocument.startPage(pageInfo);
                canvas = currentPage.getCanvas();
            }

            BillReportDto bill = bills.get(i);

            // Calculate item placement position relative to the local page index layout
            int localIndex = i % MAX_BILLS_PER_PAGE;
            int col = localIndex % COLUMNS;
            int row = localIndex / COLUMNS;

            int offsetX = col * SLOT_WIDTH;
            int offsetY = row * SLOT_HEIGHT;

            int rightMarginEdge = offsetX + SLOT_WIDTH - indent;
            int currentY = offsetY + 16;

            // --- METADATA HEADER ---
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            paint.setTextSize(titleTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.BLACK);
            canvas.drawText("Room Number: " + bill.getRoomNumber(), offsetX + (SLOT_WIDTH / 2f), currentY, paint);

            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTypeface(Typeface.MONOSPACE);
            paint.setTextSize(regularTextSize);
            currentY += leading + 3;
            canvas.drawText("Meter No.: " + bill.getMeterSerialNumber(), offsetX + indent, currentY, paint);

            currentY += leading + 3;
            canvas.drawText("Bill Date: " + bill.getBillingDate(), offsetX + indent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("Bill id:#" + bill.getBillId(), rightMarginEdge, currentY, paint);
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
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            canvas.drawText("Bill Amount", offsetX + indent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.getDefault(), chargeFormat, bill.getTotalAmount()), rightMarginEdge, currentY, paint);
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
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

            // Revert basic settings
            paint.setColor(Color.BLACK);
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));

            // Structural layout cutting grid lines
            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(0.5f);
            canvas.drawLine(offsetX, offsetY + SLOT_HEIGHT, offsetX + SLOT_WIDTH, offsetY + SLOT_HEIGHT, paint);
            canvas.drawLine(offsetX + SLOT_WIDTH, offsetY, offsetX + SLOT_WIDTH, offsetY + SLOT_HEIGHT, paint);
            paint.setColor(Color.BLACK);
        }

        // Close out the final remaining active page tracking container
        if (currentPage != null) {
            pdfDocument.finishPage(currentPage);
        }

        // Save File Block
        String fileName = "A4_All_Combined_Bills.pdf";
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File pdfFile = new File(downloadsDir, fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Toast.makeText(context, "Multi-page A4 Document Saved Successfully (" + pageNumber + " Pages)", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Save Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }

    /**
     * Generates a single dedicated layout slip.
     */
    public static void generateBillPdf(Context context, BillReportDto bill) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        final float titleTextSize = 13.0f;
        final float regularTextSize = 10.0f;
        final int leading = 13;
        final int indent = 10;
        final int rightMarginEdge = PAGE_WIDTH - indent;

        int currentY = 22;

        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        paint.setTextSize(titleTextSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
        canvas.drawText("Room Number: " + bill.getRoomNumber(), PAGE_WIDTH / 2f, currentY, paint);

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

        // ADDED: Extra Charges
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Extra Charges", indent, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), chargeFormat, bill.getExtraCharge()), rightMarginEdge, currentY, paint);
        currentY += leading + 5;

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText("Total Amount", indent, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), chargeFormat, bill.getTotalAmount()), rightMarginEdge, currentY, paint);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        currentY += 10;

        canvas.drawLine(indent, currentY, rightMarginEdge, currentY, paint);
        currentY += 14;

//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
//        canvas.drawText("Arrears", indent, currentY, paint);
//        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
//        currentY += leading;
//
//        canvas.drawText("  Arrears Due", indent, currentY, paint);
//        paint.setTextAlign(Paint.Align.RIGHT);
//        canvas.drawText("0.00", rightMarginEdge, currentY, paint);
//        currentY += leading;
//
//        paint.setTextAlign(Paint.Align.LEFT);
//        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
//        canvas.drawText("  Total Due", indent, currentY, paint);
//        paint.setTextAlign(Paint.Align.RIGHT);
//        canvas.drawText(String.format(Locale.getDefault(), chargeFormat, bill.getTotalAmount()), rightMarginEdge, currentY, paint);
//        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
//        currentY += 10;
//
//        canvas.drawLine(indent, currentY, rightMarginEdge, currentY, paint);
//        currentY += 15;

        paint.setTextAlign(Paint.Align.LEFT);
        // ADDED: Notes/Remarks
        if (bill.getNote() != null) {
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC));
            canvas.drawText("Notes: " + bill.getNote(), indent, currentY, paint);
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
            currentY += leading + 5;
            canvas.drawLine(indent, currentY, rightMarginEdge, currentY, paint);
            currentY += 15;
        }

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