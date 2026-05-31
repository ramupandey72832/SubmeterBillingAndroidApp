package com.github.devfrogora.data.dao;

import com.github.devfrogora.data.dao.impl.*;
/**
 * Centrally manages DAO instances.
 * This ensures your UI components don't have to keep calling "new SQLiteTenantDao()"
 */
public class DaoManager {
    private static TenantDao tenantDao;
    private static RoomDao roomDao;
    private static SubmeterDao submeterDao;
    private static MeterReadingDao meterReadingDao;
    private static BillDao billDao;
    private static BillSummaryDao billSummaryDao;
    private static TenancyDao tenancyDao;


    // Initialize all implementations at once
    public static void initialize() {
        tenantDao = new SQLiteTenantDao();
        roomDao = new SQLiteRoomDao();
        submeterDao = new SQLiteSubmeterDao();
        meterReadingDao = new SQLiteMeterReadingDao();
        billDao = new SQLiteBillDao();
        billSummaryDao = new SQLiteBillSummaryDao();
        tenancyDao = new SQLiteTenancyDao();
    }

    public static TenantDao getTenantDao() { return tenantDao; }
    public static RoomDao getRoomDao() { return roomDao; }
    public static SubmeterDao getSubmeterDao() { return submeterDao; }
    public static MeterReadingDao getMeterReadingDao() { return meterReadingDao; }
    public static BillDao getBillDao() { return billDao; }
    public static BillSummaryDao getBillSummaryDao() { return billSummaryDao; }

    public static TenancyDao getTenancyDao() {
        return tenancyDao;
    }
}