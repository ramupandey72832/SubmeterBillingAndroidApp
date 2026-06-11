package com.application.bottomnavigationbarui.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

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

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class SimplePdfGenerator {

    public static void generateBillReport(Context context, List<BillReportDto> bills, String startDate, String endDate) {
        if (bills == null || bills.isEmpty()) return;

        // 1. Create a Document Instance rotated to Landscape (A4) so all 11 columns fit easily
        Document document = new Document(PageSize.A4.rotate());

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File pdfFile = new File(downloadsDir, "Billing_Report_" + startDate + "_to_" + endDate + ".pdf");

        try {
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // 2. Setup Document Header
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("METER BILLING & UTILITY REPORT", titleFont);
            title.setSpacingAfter(6);
            document.add(title);

            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 11, Font.ITALIC);
            Paragraph subtitle = new Paragraph("Generated Range: " + startDate + " to " + endDate, subtitleFont);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // 3. Create a clean Data Table with 11 structural columns
            PdfPTable table = new PdfPTable(11);
            table.setWidthPercentage(100); // Stretch cleanly across the full landscape page
            table.setSpacingBefore(10f);

            // Table Header Row
            String[] headers = {"ID", "Room", "Tenant Name", "Bill Date", "Serial No.", "Prev", "Curr", "Rate", "Fixed", "Total", "Status"};
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);

            for (String headerText : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(headerText, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);
            }

            // 4. Fill Data Rows smoothly (Auto-wraps long text and manages multi-page boundaries)
            Font bodyFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
            for (BillReportDto item : bills) {
                table.addCell(createCell(String.valueOf(item.getBillId()), bodyFont, Element.ALIGN_CENTER));
                table.addCell(createCell(item.getRoomNumber(), bodyFont, Element.ALIGN_CENTER));
                table.addCell(createCell(item.getTenantName(), bodyFont, Element.ALIGN_LEFT));
                table.addCell(createCell(item.getBillingDate(), bodyFont, Element.ALIGN_CENTER));
                table.addCell(createCell(item.getMeterSerialNumber(), bodyFont, Element.ALIGN_CENTER));
                table.addCell(createCell(String.valueOf(item.getPreviousReading()), bodyFont, Element.ALIGN_RIGHT));
                table.addCell(createCell(String.valueOf(item.getCurrentReading()), bodyFont, Element.ALIGN_RIGHT));
                table.addCell(createCell(String.valueOf(item.getRatePerUnit()), bodyFont, Element.ALIGN_RIGHT));
                table.addCell(createCell(String.valueOf(item.getFixedCharge()), bodyFont, Element.ALIGN_RIGHT));
                table.addCell(createCell(String.valueOf(item.getTotalAmount()), bodyFont, Element.ALIGN_RIGHT));
                table.addCell(createCell(item.getPaymentStatus(), bodyFont, Element.ALIGN_CENTER));
            }

            // Complete document assembly
            document.add(table);
            Toast.makeText(context, "PDF Report saved to Downloads!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private static PdfPCell createCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        return cell;
    }
}