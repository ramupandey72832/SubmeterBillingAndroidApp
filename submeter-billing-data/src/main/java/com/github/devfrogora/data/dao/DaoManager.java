package com.github.devfrogora.data.dao;


import com.github.devfrogora.data.dao.impl.SQLiteDbMigrationDao;
import com.github.devfrogora.data.dao.impl.SQLiteMeterReadingDao;
import com.submeter.data.dao.impl.SqlLiteRoomDao;
import com.submeter.data.dao.impl.SqlLiteSubmeterAllocationDao;
import com.submeter.data.dao.impl.SqlLiteSubmeterDao;
import com.submeter.data.dao.SubmeterDao;
import com.github.devfrogora.data.dao.impl.SQLiteSubmeterAllocationDao;
import com.github.devfrogora.data.dao.impl.SQLiteTenancyDao;
import com.github.devfrogora.data.dao.impl.SQLiteTenantDao;
import com.submeter.data.dao.impl.SqlLiteBillDao;
import com.submeter.data.dao.impl.SqlLiteTenantDao;
import com.submeter.data.dao.impl.SqlLiteMeterReadingDao;
import com.submeter.data.dao.impl.SqlLiteTenancyDao;

/**
 * Centrally manages DAO instances.
 * This ensures your UI components don't have to keep calling "new SQLiteTenantDao()"
 */


public class DaoManager {
    private static com.submeter.data.dao.TenantDao tenantDao;
    private static com.submeter.data.dao.RoomDao roomDao;
    private static com.submeter.data.dao.SubmeterDao submeterDao;
    private static com.submeter.data.dao.MeterReadingDao meterReadingDao;
    private static com.submeter.data.dao.BillDao billDao;
    private static com.submeter.data.dao.TenancyDao tenancyDao;
    private static com.submeter.data.dao.SubmeterAllocationDao submeterAllocationDao;



    // Initialize all implementations at once
    public static void initialize() {
        tenantDao = new SqlLiteTenantDao();
        roomDao = new SqlLiteRoomDao();
        submeterDao = new SqlLiteSubmeterDao();
        meterReadingDao = new SqlLiteMeterReadingDao();
        billDao = new SqlLiteBillDao();
        tenancyDao = new SqlLiteTenancyDao();

        submeterAllocationDao = new SqlLiteSubmeterAllocationDao();

    }

    public static com.submeter.data.dao.TenantDao getTenantDao() { return tenantDao; }
    public static com.submeter.data.dao.RoomDao getRoomDao() { return roomDao; }
    public static com.submeter.data.dao.SubmeterDao getSubmeterDao() { return submeterDao; }
    public static com.submeter.data.dao.SubmeterAllocationDao getSubmeterAllocationDao() { return submeterAllocationDao; }
    public static com.submeter.data.dao.MeterReadingDao getMeterReadingDao() { return meterReadingDao; }
    public static com.submeter.data.dao.BillDao getBillDao() { return billDao; }
    public static com.submeter.data.dao.TenancyDao getTenancyDao() {
        return tenancyDao;
    }

}