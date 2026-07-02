package com.github.devfrogora.service.utils;

import com.github.devfrogora.data.dao.BackupDao;
import com.github.devfrogora.service.dto.reports.BillReportDto;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ExcelGenerator {

    public static OperationResult<Void> generateBillReport(OutputStream outputStream, List<BillReportDto> bills) {
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(outputStream);
            WritableSheet sheet = workbook.createSheet("Report", 0);

            // 1. Add Headers
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
            return OperationResult.success(null,"");

        } catch (Exception e) {
            e.printStackTrace();
            return OperationResult.failure("Failed to write Excel billing rows: " + e.getMessage());
        }
    }
    /**
     * Pure file converter binding the database stream consumer interface
     */
    public static OperationResult<Void> exportAllTablesToExcel(OutputStream outputStream) {
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(outputStream);

            BackupDao.exportDatabaseRecords(new BackupDao.TableDataConsumer() {
                private WritableSheet currentSheet;
                private int sheetIndex = 0;
                private int currentRow = 0;

                @Override
                public void acceptTableMetaData(String tableName, List<String> columnNames) throws Exception {
                    currentSheet = workbook.createSheet(tableName, sheetIndex++);
                    currentRow = 0;
                    for (int i = 0; i < columnNames.size(); i++) {
                        currentSheet.addCell(new Label(i, currentRow, columnNames.get(i)));
                    }
                    currentRow++;
                }

                @Override
                public void acceptRowData(List<String> rowValues) throws Exception {
                    for (int i = 0; i < rowValues.size(); i++) {
                        currentSheet.addCell(new Label(i, currentRow, rowValues.get(i)));
                    }
                    currentRow++;
                }

                @Override
                public void endTable() {
                    // Sheet finished processing safely
                }
            });

            workbook.write();
            workbook.close();
            return OperationResult.success(null, "Export completed successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return OperationResult.failure("Export framework failure: " + e.getMessage());
        }
    }

    /**
     * Parses the InputStream using JXL and loads an in-memory map structure to feed the Data Layer
     */
    public static OperationResult<Void> importAllTablesFromExcel(InputStream excelInputStream) {
        try {
            Workbook workbook = Workbook.getWorkbook(excelInputStream);

            // Temporary structure maps table names to column lists and rows matrix
            Map<String, List<String>> headersMap = new LinkedHashMap<>();
            Map<String, List<List<String>>> rowsMap = new LinkedHashMap<>();

            for (String tableName : BackupDao.TABLES_TO_BACKUP) {
                Sheet sheet = workbook.getSheet(tableName);
                if (sheet == null) continue;

                int rows = sheet.getRows();
                int cols = sheet.getColumns();
                if (rows <= 1) continue;

                List<String> columns = new ArrayList<>();
                for (int c = 0; c < cols; c++) {
                    columns.add(sheet.getCell(c, 0).getContents());
                }
                headersMap.put(tableName, columns);

                List<List<String>> dataRows = new ArrayList<>();
                for (int r = 1; r < rows; r++) {
                    List<String> rowValues = new ArrayList<>();
                    for (int c = 0; c < cols; c++) {
                        rowValues.add(sheet.getCell(c, r).getContents());
                    }
                    dataRows.add(rowValues);
                }
                rowsMap.put(tableName, dataRows);
            }
            workbook.close();

            // Fire the transaction processing on the decoupled data layer safely
            BackupDao.importDatabaseRecords(new BackupDao.TableDataProvider() {
                @Override
                public List<String> getColumnsForTable(String tableName) {
                    return headersMap.get(tableName);
                }

                @Override
                public int getRowCount(String tableName) {
                    return rowsMap.containsKey(tableName) ? rowsMap.get(tableName).size() : 0;
                }

                @Override
                public List<String> getRowAt(String tableName, int rowIndex) {
                    return rowsMap.get(tableName).get(rowIndex);
                }
            });

            return OperationResult.success(null, "Import completed successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return OperationResult.failure("Import validation engine failed: " + e.getMessage());
        }
    }
}