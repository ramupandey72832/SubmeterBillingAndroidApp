package com.submeter.data.dao;

import com.submeter.data.dao.impl.SqlLiteBillDao;
import com.submeter.data.dao.impl.SqlLiteDbMigrationDao;
import com.submeter.data.dao.impl.SqlLiteMeterReadingDao;
import com.submeter.data.dao.impl.SqlLiteRoomDao;
import com.submeter.data.dao.impl.SqlLiteSubmeterAllocationDao;
import com.submeter.data.dao.impl.SqlLiteSubmeterDao;
import com.submeter.data.dao.impl.SqlLiteTenancyDao;
import com.submeter.data.dao.impl.SqlLiteTenantDao;

public class DaoManager {
    private static TenantDao tenantDao;
    private static RoomDao roomDao;
    private static SubmeterDao submeterDao;
    private static MeterReadingDao meterReadingDao;
    private static BillDao billDao;
    private static TenancyDao tenancyDao;
    private static SubmeterAllocationDao submeterAllocationDao;
    private static DbMigrationDao dbMigrationDao;


    // Initialize all implementations at once
    public static void initialize() {
        tenantDao = new SqlLiteTenantDao();
        roomDao = new SqlLiteRoomDao();
        submeterDao = new SqlLiteSubmeterDao();
        meterReadingDao = new SqlLiteMeterReadingDao();
        billDao = new SqlLiteBillDao();
        tenancyDao = new SqlLiteTenancyDao();

        dbMigrationDao = new SqlLiteDbMigrationDao();
        submeterAllocationDao = new SqlLiteSubmeterAllocationDao();

    }

    public static TenantDao getTenantDao() { return tenantDao; }
    public static RoomDao getRoomDao() { return roomDao; }
    public static SubmeterDao getSubmeterDao() { return submeterDao; }
    public static SubmeterAllocationDao getSubmeterAllocationDao() { return submeterAllocationDao; }
    public static MeterReadingDao getMeterReadingDao() { return meterReadingDao; }
    public static BillDao getBillDao() { return billDao; }
    public static TenancyDao getTenancyDao() {
        return tenancyDao;
    }

    public static DbMigrationDao getDbMigrationDao() { return dbMigrationDao; }
}