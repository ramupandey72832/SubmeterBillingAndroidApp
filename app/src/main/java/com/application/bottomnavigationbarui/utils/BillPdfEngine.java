package com.application.bottomnavigationbarui.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.devfrogora.service.dto.reports.BillReportDto;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

public class BillPdfEngine {

    private static final int SLIP_WIDTH = 220;
    private static final int SLIP_HEIGHT = 340;
    private static final int A4_WIDTH = 595;
    private static final int A4_HEIGHT = 842;

    /**
     * Deletes any pre-existing duplicate file entry in MediaStore to avoid "(1).pdf" duplicate naming bugs.
     */
    private static void deleteExistingFile(@NonNull Context context, @NonNull Uri collectionUri, @NonNull String fileName) {
        try {
            ContentResolver resolver = context.getContentResolver();
            String selection = MediaStore.MediaColumns.DISPLAY_NAME + " = ?";
            String[] selectionArgs = new String[]{fileName};
            resolver.delete(collectionUri, selection, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper to resolve the correct platform collection Uri.
     */
    @NonNull
    private static Uri getCollectionUri() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        } else {
            return MediaStore.Files.getContentUri("external");
        }
    }

    /**
     * Generates a single individual thermal-style bill slip.
     */
    /**
     * Generates a single individual thermal-style bill slip.
     * @return The exact Uri where the PDF was written, or null if generation failed.
     */
    public static Uri generateSingleBillSlip(@NonNull Context context, @NonNull BillReportDto targetBill, @NonNull String outputFileName) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(SLIP_WIDTH, SLIP_HEIGHT, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        final float titleTextSize = 13.0f;
        final float regularTextSize = 10.0f;
        final int leadingSpace = 13;
        final int sidePadding = 10;
        final int rightMarginEdge = SLIP_WIDTH - sidePadding;

        int currentY = 22;

        // --- Bill Header ---
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        paint.setTextSize(titleTextSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
        canvas.drawText("Room Number: " + targetBill.getRoomNumber(), SLIP_WIDTH / 2f, currentY, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextSize(regularTextSize);

        currentY += leadingSpace + 5;
        canvas.drawText("Bill Date  : " + targetBill.getBillingDate(), sidePadding, currentY, paint);
        currentY += leadingSpace + 3;
        canvas.drawText("Bill No.  : " + targetBill.getBillId(), sidePadding, currentY, paint);
        currentY += leadingSpace + 3;
        canvas.drawText("Meter No.  : " + targetBill.getMeterSerialNumber(), sidePadding, currentY, paint);
        currentY += leadingSpace + 3;
        canvas.drawText("Tenant Name: " + targetBill.getTenantName(), sidePadding, currentY, paint);
        currentY += 7;

        paint.setStrokeWidth(1.2f);
        canvas.drawLine(sidePadding, currentY, rightMarginEdge, currentY, paint);
        currentY += 14;

        double unitsConsumed = targetBill.getCurrentReading() - targetBill.getPreviousReading();
        canvas.drawText("Current Reading", sidePadding, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.valueOf((int) targetBill.getCurrentReading()), rightMarginEdge, currentY, paint);
        currentY += leadingSpace;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Previous Reading", sidePadding, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.valueOf((int) targetBill.getPreviousReading()), rightMarginEdge, currentY, paint);
        currentY += leadingSpace;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Units Consumed", sidePadding, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.valueOf((int) unitsConsumed), rightMarginEdge, currentY, paint);
        currentY += leadingSpace;

        currentY += 3;
        canvas.drawLine(sidePadding, currentY, rightMarginEdge, currentY, paint);
        currentY += 14;

        paint.setTextAlign(Paint.Align.LEFT);
        String amountFormat = "%.2f";
        canvas.drawText("Rate per Unit", sidePadding, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), amountFormat, targetBill.getRatePerUnit()), rightMarginEdge, currentY, paint);
        currentY += leadingSpace;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Fixed Charges", sidePadding, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), amountFormat, targetBill.getFixedCharge()), rightMarginEdge, currentY, paint);
        currentY += leadingSpace + 5;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Extra Charges", sidePadding, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), amountFormat, targetBill.getExtraCharge()), rightMarginEdge, currentY, paint);
        currentY += leadingSpace + 5;

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText("Total Amount", sidePadding, currentY, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), amountFormat, targetBill.getTotalAmount()), rightMarginEdge, currentY, paint);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        currentY += 10;

        canvas.drawLine(sidePadding, currentY, rightMarginEdge, currentY, paint);
        currentY += 14;

        paint.setTextAlign(Paint.Align.LEFT);
        if (targetBill.getNote() != null && !targetBill.getNote().trim().isEmpty()) {
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC));
            canvas.drawText("Notes: " + targetBill.getNote(), sidePadding, currentY, paint);
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
            currentY += leadingSpace + 5;
            canvas.drawLine(sidePadding, currentY, rightMarginEdge, currentY, paint);
            currentY += 15;
        }

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("STATUS:", sidePadding, currentY, paint);
        if (targetBill.getPaymentStatus() != null && targetBill.getPaymentStatus().equalsIgnoreCase("PAID")) {
            paint.setColor(Color.parseColor("#008000"));
        } else {
            paint.setColor(Color.parseColor("#800000"));
        }

        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText(targetBill.getPaymentStatus() != null ? targetBill.getPaymentStatus().toUpperCase() : "UNPAID", sidePadding + 55, currentY, paint);

        pdfDocument.finishPage(page);

        Uri collectionUri = getCollectionUri();

        // CRUCIAL: Delete the old index first so MediaStore doesn't create (1).pdf
        deleteExistingFile(context, collectionUri, outputFileName);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        } else {
            java.io.File legacyDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            java.io.File targetLegacyFile = new java.io.File(legacyDownloadDir, outputFileName);
            contentValues.put(MediaStore.MediaColumns.DATA, targetLegacyFile.getAbsolutePath());
        }

        Uri targetUri = context.getContentResolver().insert(collectionUri, contentValues);
        if (targetUri == null) {
            pdfDocument.close();
            return null;
        }

        try (OutputStream outputStream = context.getContentResolver().openOutputStream(targetUri)) {
            if (outputStream != null) {
                pdfDocument.writeTo(outputStream);
                Toast.makeText(context, "Bill Slip Generated Successfully", Toast.LENGTH_LONG).show();
                return targetUri; // Return the exact newly generated file URI!
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pdfDocument.close();
        }
        return null;
    }
    /**
     * Packages a collection of items into an A4 document grid arrangement (Exactly 8 bills per page).
     */
    public static void generateBulkInvoicesGridPdf(@NonNull Context context, @Nullable List<BillReportDto> billCollection, @NonNull String outputFileName) {
        if (billCollection == null || billCollection.isEmpty()) {
            Toast.makeText(context, "No bills provided to batch generate", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        final int TOTAL_COLUMNS = 2;
        final int MAX_BILLS_PER_PAGE = 8;
        final int ITEM_SLOT_WIDTH = A4_WIDTH / TOTAL_COLUMNS;
        final int ITEM_SLOT_HEIGHT = A4_HEIGHT / 4;

        final float titleTextSize = 12.0f;
        final float regularTextSize = 9.5f;
        final int leadingSpace = 11;
        final int cellIndent = 16;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        PdfDocument.Page currentA4Page = null;
        Canvas canvas = null;
        int sequentialPageCounter = 0;

        for (int i = 0; i < billCollection.size(); i++) {
            if (i % MAX_BILLS_PER_PAGE == 0) {
                if (currentA4Page != null) {
                    pdfDocument.finishPage(currentA4Page);
                }
                sequentialPageCounter++;
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, sequentialPageCounter).create();
                currentA4Page = pdfDocument.startPage(pageInfo);
                canvas = currentA4Page.getCanvas();
            }

            BillReportDto specificBill = billCollection.get(i);
            int subPageIndex = i % MAX_BILLS_PER_PAGE;
            int columnIndex = subPageIndex % TOTAL_COLUMNS;
            int rowIndex = subPageIndex / TOTAL_COLUMNS;

            int horizontalOffset = columnIndex * ITEM_SLOT_WIDTH;
            int verticalOffset = rowIndex * ITEM_SLOT_HEIGHT;
            int cellRightEdge = horizontalOffset + ITEM_SLOT_WIDTH - cellIndent;
            int currentY = verticalOffset + 16;

            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            paint.setTextSize(titleTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.BLACK);
            canvas.drawText("Room Number: " + specificBill.getRoomNumber(), horizontalOffset + (ITEM_SLOT_WIDTH / 2f), currentY, paint);

            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTypeface(Typeface.MONOSPACE);
            paint.setTextSize(regularTextSize);
            currentY += leadingSpace + 3;
            canvas.drawText("Meter No.: " + specificBill.getMeterSerialNumber(), horizontalOffset + cellIndent, currentY, paint);

            currentY += leadingSpace + 3;
            canvas.drawText("Bill Date: " + specificBill.getBillingDate(), horizontalOffset + cellIndent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("Bill id:#" + specificBill.getBillId(), cellRightEdge, currentY, paint);
            paint.setTextAlign(Paint.Align.LEFT);

            currentY += leadingSpace + 2;
            canvas.drawText("Tenant Name: " + specificBill.getTenantName(), horizontalOffset + cellIndent, currentY, paint);
            currentY += 6;

            paint.setStrokeWidth(0.75f);
            canvas.drawLine(horizontalOffset + cellIndent, currentY, cellRightEdge, currentY, paint);
            currentY += 11;

            double calculationUnits = specificBill.getCurrentReading() - specificBill.getPreviousReading();
            canvas.drawText("Current Reading", horizontalOffset + cellIndent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.valueOf((int) specificBill.getCurrentReading()), cellRightEdge, currentY, paint);
            currentY += leadingSpace;

            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Previous Reading", horizontalOffset + cellIndent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.valueOf((int) specificBill.getPreviousReading()), cellRightEdge, currentY, paint);
            currentY += leadingSpace;

            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Units Consumed", horizontalOffset + cellIndent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.valueOf((int) calculationUnits), cellRightEdge, currentY, paint);
            currentY += leadingSpace;

            currentY += 2;
            canvas.drawLine(horizontalOffset + cellIndent, currentY, cellRightEdge, currentY, paint);
            currentY += 11;

            paint.setTextAlign(Paint.Align.LEFT);
            String cellAmountFormat = "%.2f";
            canvas.drawText("Rate per Unit", horizontalOffset + cellIndent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.getDefault(), cellAmountFormat, specificBill.getRatePerUnit()), cellRightEdge, currentY, paint);
            currentY += leadingSpace;

            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Fixed Charges", horizontalOffset + cellIndent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.getDefault(), cellAmountFormat, specificBill.getFixedCharge()), cellRightEdge, currentY, paint);
            currentY += leadingSpace + 3;

            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Extra Charges", horizontalOffset + cellIndent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.getDefault(), cellAmountFormat, specificBill.getExtraCharge()), cellRightEdge, currentY, paint);
            currentY += leadingSpace + 3;

            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            canvas.drawText("Total Amount", horizontalOffset + cellIndent, currentY, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(Locale.getDefault(), cellAmountFormat, specificBill.getTotalAmount()), cellRightEdge, currentY, paint);
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
            currentY += 5;

            canvas.drawLine(horizontalOffset + cellIndent, currentY, cellRightEdge, currentY, paint);
            currentY += 11;
            paint.setTextAlign(Paint.Align.LEFT);

            if (specificBill.getNote() != null && !specificBill.getNote().trim().isEmpty()) {
                paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC));
                canvas.drawText("Notes: " + specificBill.getNote(), horizontalOffset + cellIndent, currentY, paint);
                paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
                currentY += leadingSpace + 10;
            }

            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("STATUS:", horizontalOffset + cellIndent, currentY, paint);
            if (specificBill.getPaymentStatus() != null && specificBill.getPaymentStatus().equalsIgnoreCase("PAID")) {
                paint.setColor(Color.parseColor("#008000"));
            } else {
                paint.setColor(Color.parseColor("#800000"));
            }

            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            canvas.drawText(specificBill.getPaymentStatus() != null ? specificBill.getPaymentStatus().toUpperCase() : "UNPAID", horizontalOffset + cellIndent + 50, currentY, paint);

            paint.setColor(Color.BLACK);
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));

            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(0.5f);
            canvas.drawLine(horizontalOffset, verticalOffset + ITEM_SLOT_HEIGHT, horizontalOffset + ITEM_SLOT_WIDTH, verticalOffset + ITEM_SLOT_HEIGHT, paint);
            canvas.drawLine(horizontalOffset + ITEM_SLOT_WIDTH, verticalOffset, horizontalOffset + ITEM_SLOT_WIDTH, verticalOffset + ITEM_SLOT_HEIGHT, paint);
            paint.setColor(Color.BLACK);
        }

        if (currentA4Page != null) {
            pdfDocument.finishPage(currentA4Page);
        }

        Uri collectionUri = getCollectionUri();
        deleteExistingFile(context, collectionUri, outputFileName);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        }

        Uri targetUri = context.getContentResolver().insert(collectionUri, contentValues);
        if (targetUri == null) return;

        try (OutputStream stream = context.getContentResolver().openOutputStream(targetUri)) {
            if (stream != null) {
                pdfDocument.writeTo(stream);
                Toast.makeText(context, "Batch Invoices Grid Document Exported", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pdfDocument.close();
        }
    }

    /**
     * Generates a Landscape Table Report covering comprehensive accounting items (via iText).
     */
    public static void generateBillingSummaryReport(@NonNull Context context, @Nullable List<BillReportDto> fullBillList, @NonNull String queryStartDate, @NonNull String queryEndDate) {
        if (fullBillList == null || fullBillList.isEmpty()) return;

        Document textDocumentInstance = new Document(PageSize.A4.rotate());
        String outputFileName = "Billing_Report_" + queryStartDate + "_to_" + queryEndDate + ".pdf";

        Uri collectionUri = getCollectionUri();
        deleteExistingFile(context, collectionUri, outputFileName);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        }

        Uri targetUri = context.getContentResolver().insert(collectionUri, contentValues);
        if (targetUri == null) return;

        try (OutputStream outputStream = context.getContentResolver().openOutputStream(targetUri)) {
            PdfWriter.getInstance(textDocumentInstance, outputStream);
            textDocumentInstance.open();

            Font headerTitleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph mainTitleBlock = new Paragraph("METER BILLING & UTILITY REPORT", headerTitleFont);
            mainTitleBlock.setSpacingAfter(6);
            textDocumentInstance.add(mainTitleBlock);

            Font filteringMetaFont = new Font(Font.FontFamily.HELVETICA, 11, Font.ITALIC);
            Paragraph dateSpanSubtitle = new Paragraph("Generated Range: " + queryStartDate + " to " + queryEndDate, filteringMetaFont);
            dateSpanSubtitle.setSpacingAfter(20);
            textDocumentInstance.add(dateSpanSubtitle);

            PdfPTable summaryGridTable = new PdfPTable(11);
            summaryGridTable.setWidthPercentage(100);
            summaryGridTable.setSpacingBefore(10f);

            String[] gridHeaderTitles = {"ID", "Room", "Tenant Name", "Bill Date", "Serial No.", "Prev", "Curr", "Rate", "Fixed", "Total", "Status"};
            Font tabularHeaderFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);

            for (String columnHeader : gridHeaderTitles) {
                PdfPCell headerCell = new PdfPCell(new Phrase(columnHeader, tabularHeaderFont));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                headerCell.setPadding(6);
                summaryGridTable.addCell(headerCell);
            }

            Font itemBodyFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
            for (BillReportDto processingItem : fullBillList) {
                summaryGridTable.addCell(createSummaryTableCell(String.valueOf(processingItem.getBillId()), itemBodyFont, Element.ALIGN_CENTER));
                summaryGridTable.addCell(createSummaryTableCell(processingItem.getRoomNumber(), itemBodyFont, Element.ALIGN_CENTER));
                summaryGridTable.addCell(createSummaryTableCell(processingItem.getTenantName(), itemBodyFont, Element.ALIGN_LEFT));
                summaryGridTable.addCell(createSummaryTableCell(processingItem.getBillingDate(), itemBodyFont, Element.ALIGN_CENTER));
                summaryGridTable.addCell(createSummaryTableCell(processingItem.getMeterSerialNumber(), itemBodyFont, Element.ALIGN_CENTER));
                summaryGridTable.addCell(createSummaryTableCell(String.valueOf(processingItem.getPreviousReading()), itemBodyFont, Element.ALIGN_RIGHT));
                summaryGridTable.addCell(createSummaryTableCell(String.valueOf(processingItem.getCurrentReading()), itemBodyFont, Element.ALIGN_RIGHT));
                summaryGridTable.addCell(createSummaryTableCell(String.valueOf(processingItem.getRatePerUnit()), itemBodyFont, Element.ALIGN_RIGHT));
                summaryGridTable.addCell(createSummaryTableCell(String.valueOf(processingItem.getFixedCharge()), itemBodyFont, Element.ALIGN_RIGHT));
                summaryGridTable.addCell(createSummaryTableCell(String.valueOf(processingItem.getTotalAmount()), itemBodyFont, Element.ALIGN_RIGHT));
                summaryGridTable.addCell(createSummaryTableCell(processingItem.getPaymentStatus(), itemBodyFont, Element.ALIGN_CENTER));
            }

            textDocumentInstance.add(summaryGridTable);
            Toast.makeText(context, "Billing summary report compiled successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (textDocumentInstance.isOpen()) {
                textDocumentInstance.close();
            }
        }
    }

    private static PdfPCell createSummaryTableCell(String structuralText, Font fontStyle, int spatialAlignment) {
        PdfPCell dynamicDataCell = new PdfPCell(new Phrase(structuralText != null ? structuralText : "", fontStyle));
        dynamicDataCell.setHorizontalAlignment(spatialAlignment);
        dynamicDataCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        dynamicDataCell.setPadding(5);
        return dynamicDataCell;
    }
}