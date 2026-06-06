package com.github.devfrogora;

import com.github.devfrogora.data.config.DatabaseConfig;
import com.github.devfrogora.data.config.DatabaseConnection;
import com.github.devfrogora.data.config.SharedSchemaConfigurator;
import com.github.devfrogora.data.dao.DaoManager;
import com.github.devfrogora.data.entities.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PropertyManagementSystem {

    public static void main(String[] args) throws SQLException , Exception {
        System.out.println("=== INITIALIZING SYSTEM VIA DAOMANAGER ===");

        // Setup database parameters and schemas
        initializeEnvironment();

        // Step 1: Manage Physical Infrastructure
        int roomId = ensureRoomExists("302-C");
        int meterId = ensureSubmeterExists("MTR-IND-302X", roomId);
        Submeter dbMeter = DaoManager.getSubmeterDao().getSubmeterById(meterId).orElseThrow();

        // Step 2: Customer Onboarding
        int tenantId = ensureTenantOnboarded("Rajesh Kumar", "+919876543210", "[Aadhaar Redacted]", "Flat 12, Sunrise Apartments, Sector 45, Gurgaon");
        if (tenantId == -1) return;

        // Step 3: Allocation & Metric Records
        allocateTenantToRoom(roomId, tenantId);
        recordMeterReadings(meterId, dbMeter.getInitialReading(), "2026-05-01", 385.50, "2026-06-01");

        // Step 4: Invoice Generation
        MeterReading latestReading = generateMonthlyBill(roomId, tenantId, meterId, dbMeter.getInitialReading(), 12.50);

        // Step 5: Live Database Verification Queries & Settlement Demos
        executeSearchAndPaymentDemo(latestReading);
    }

    private static void initializeEnvironment() throws Exception {
        System.out.println("System: Initializing database connection configurations...");
        DatabaseConfig desktopConfig = new DatabaseConfig(
                "jdbc:sqlite:submeter_bill.db",
                null,
                null,
                "org.sqlite.JDBC"
        );
        DatabaseConnection.initialize(desktopConfig);
        SharedSchemaConfigurator.initializeSchema();
        DaoManager.initialize();
    }

    /**
     * Step 1A: Verifies if a room exists by its identifier. If absent, provisions it.
     */
    private static int ensureRoomExists(String roomNumber) throws SQLException{
        System.out.println("\n--- Step 1A: Setting up Room Infrastructure ---");
        Optional<Room> existingRoom = DaoManager.getRoomDao().getRoomByNumber(roomNumber);

        if (existingRoom.isPresent()) {
            System.out.println("Infrastructure: Room " + roomNumber + " already exists. Fetching reference...");
            return existingRoom.get().getRoomId();
        } else {
            Room room = new Room();
            room.setRoomNumber(roomNumber);
            boolean isRoomCreated = DaoManager.getRoomDao().insertRoom(room);
            System.out.println("Infrastructure: Room " + roomNumber + " Registered -> " + isRoomCreated);

            return DaoManager.getRoomDao().getRoomByNumber(roomNumber).orElseThrow().getRoomId();
        }
    }

    /**
     * Step 1B: Verifies if a physical hardware submeter is bound to a room unit.
     */
    private static int ensureSubmeterExists(String serialNumber, int roomId) throws SQLException {
        System.out.println("\n--- Step 1B: Setting up Hardware Submeter ---");
        // FIXED: Using single object lookup via Optional since it checks room binding
        Optional<Submeter> existingMeters = DaoManager.getSubmeterDao().getSubmeterById(roomId);

        // FIXED: Optional uses .isPresent() or .isEmpty(). Changed .get(0) to .get()
        if (existingMeters.isPresent()) {
            System.out.println("Infrastructure: Submeter already linked to Room ID " + roomId + ". Fetching reference...");
            return existingMeters.get().getMeterId();
        } else {
            Submeter submeter = new Submeter();
            submeter.setMeterSerialNumber(serialNumber);
            submeter.setRoomId(roomId);
            submeter.setInitialReading(150.00);
            boolean isMeterInstalled = DaoManager.getSubmeterDao().insertSubmeter(submeter);
            System.out.println("Infrastructure: Submeter " + serialNumber + " Connected -> " + isMeterInstalled);

            return DaoManager.getSubmeterDao().getSubmeterBySerialNumber(serialNumber).orElseThrow().getMeterId();
        }
    }

    /**
     * Step 2: Onboarding for utility consumers utilizing identifying attributes.
     */
    private static int ensureTenantOnboarded(String name, String phone, String aadhaarNumber, String address) throws SQLException{
        System.out.println("\n--- Step 2: Onboarding New Tenant ---");
        // FIXED: Reading from single Optional element context
        Optional<Tenant> existingTenants = DaoManager.getTenantDao().getTenantByAadhar(aadhaarNumber);

        // FIXED: Replaced .get(0) collection patterns with true Optional `.get()` unwrapping
        if (existingTenants.isPresent()) {
            System.out.println("Onboarding: Tenant record already exists. Fetching reference...");
            return existingTenants.get().getTenantId();
        } else {
            Tenant newTenant = new Tenant();
            newTenant.setName(name);
            newTenant.setPhoneNumber(phone);
            newTenant.setAadharNumber(aadhaarNumber);
            newTenant.setAddress(address);
            newTenant.setEmail("tenant." + name.toLowerCase().replace(" ", "") + "@example.com");
            newTenant.setActive(true);

            boolean isTenantInserted = DaoManager.getTenantDao().insertTenant(newTenant);
            System.out.println("Onboarding: New tenant written -> " + isTenantInserted);

            if (!isTenantInserted) {
                System.out.println("System Error: Tenant onboarding database execution failed.");
                return -1;
            }
            return DaoManager.getTenantDao().getTenantByAadhar(aadhaarNumber).orElseThrow().getTenantId();
        }
    }

    /**
     * Step 3A: Maps the legal tenant binding using the clean Tenancies table history log.
     */
    private static void allocateTenantToRoom(int roomId, int tenantId) throws SQLException {
        System.out.println("\n--- Step 3A: Room Allocation ---");

        Optional<Tenancy> activeTenancy = DaoManager.getTenancyDao().getActiveTenancyByRoomId(roomId);

        if (activeTenancy.isPresent()) {
            if (activeTenancy.get().getTenantId() == tenantId) {
                System.out.println("Allocation: Tenant already explicitly checked into this unit.");
                return;
            } else {
                DaoManager.getTenancyDao().endContract(roomId, activeTenancy.get().getTenantId(), "2026-04-30");
            }
        }

        Tenancy tenancy = new Tenancy();
        tenancy.setRoomId(roomId);
        tenancy.setTenantId(tenantId);
        tenancy.setStartDate("2026-05-01");
        tenancy.setEndDate(null);

        boolean isAssigned = DaoManager.getTenancyDao().insertTenancy(tenancy);
        System.out.println("Allocation: Tenant ID " + tenantId + " moved into Room ID " + roomId + " via Tenancies -> " + isAssigned);
    }

    /**
     * Step 3B: Logs chronologically bound baseline indexes and monthly delta metrics.
     */
    private static void recordMeterReadings(int meterId, double initialValue, String baselineDate, double currentVal, String currentDate)throws SQLException {
        System.out.println("\n--- Step 3B: Utility Reading Execution ---");
        List<MeterReading> historicalReadings = DaoManager.getMeterReadingDao().getReadingsByMeterId(meterId);

        if (historicalReadings.isEmpty()) {
            MeterReading baselineReading = new MeterReading();
            baselineReading.setMeterId(meterId);
            baselineReading.setReadingValue(initialValue);
            baselineReading.setImageUrlOrPath(null);
            baselineReading.setReadingDate(baselineDate);
            DaoManager.getMeterReadingDao().insertReading(baselineReading);
        }

        boolean readingExistsForDate = historicalReadings.stream()
                .anyMatch(r -> currentDate.equals(r.getReadingDate()));

        if (!readingExistsForDate) {
            MeterReading currentReading = new MeterReading();
            currentReading.setMeterId(meterId);
            currentReading.setReadingValue(currentVal);
            currentReading.setImageUrlOrPath(null);
            currentReading.setReadingDate(currentDate);
            int readingID = DaoManager.getMeterReadingDao().insertReading(currentReading);
            System.out.println("Utility Track: New monthly meter dial reading stored -> " + readingID);
        } else {
            System.out.println("Utility Track: Monthly reading entry already captured for timestamp: " + currentDate);
        }
    }

    /**
     * Step 4: Analyzes metric consumptions, applying tier rates to formulate transactional statements.
     */
    private static MeterReading generateMonthlyBill(int roomId, int tenantId, int meterId, double baseValue, double ratePerUnit)throws SQLException {
        System.out.println("\n--- Step 4: Compiling Bill Statement ---");

        MeterReading latestReading = DaoManager.getMeterReadingDao().getLatestReadingByMeterId(meterId).orElseThrow();
        double totalUnitsConsumed = latestReading.getReadingValue() - baseValue;
        double calculatedTotalCost = totalUnitsConsumed * ratePerUnit;

        List<MeterReading> readings = DaoManager.getMeterReadingDao().getReadingsByMeterId(meterId);
        int previousReadingId = readings.get(readings.size() - 1).getReadingId();

        Bill finalBill = new Bill();
        finalBill.setPreviousReadingId(previousReadingId);
        finalBill.setCurrentReadingId(latestReading.getReadingId());
        finalBill.setUnitsConsumed(totalUnitsConsumed);
        finalBill.setRatePerUnit(ratePerUnit);
        finalBill.setTotalAmount(calculatedTotalCost);
        finalBill.setBillingDate(latestReading.getReadingDate());
        finalBill.setPaid(false);

        boolean isBillIssued = DaoManager.getBillDao().insertBill(finalBill) > 0 ;
        System.out.println("Billing: Invoice operation executed -> " + isBillIssued);
        System.out.println(">> Total Units Used: " + totalUnitsConsumed + " kWh");
        System.out.println(">> Total Invoice Cost: ₹" + calculatedTotalCost);

        return latestReading;
    }

    /**
     * Step 5: Runs searches via individual identity codes and executes payment transactions.
     */
    private static void executeSearchAndPaymentDemo(MeterReading latestReading) throws SQLException {
        System.out.println("\n--- Step 5: Demonstration of Query/Search Functionality ---");

        System.out.println("Searching Database for identity record matches...");
        Optional<Tenant> searchResults = DaoManager.getTenantDao().getTenantByAadhar("[Aadhaar Redacted]");

        // FIXED: Used standard Optional presentation workflow blocks instead of index lookups
        if (searchResults.isPresent()) {
            Tenant tenant = searchResults.get();
            System.out.println("Found match by identity code: " + tenant.getName() + ", Phone: " + tenant.getPhoneNumber());
        }

        System.out.println("\nProcessing customer payments...");
        List<BillSummary> summaries = DaoManager.getBillSummaryDao().getAllSummaries();
        int activeBillId = summaries.stream()
                .filter(b -> b.getCurrentReading() == latestReading.getReadingValue())
                .map(BillSummary::getBillId)
                .findFirst()
                .orElse(1);

        boolean payStatusUpdate = DaoManager.getBillDao().updatePaymentStatus(activeBillId, true);
        System.out.println("Invoice Update: Bill Index reference (" + activeBillId + ") marked paid -> " + payStatusUpdate);

        System.out.println("\nPulling live combined data from Database View Summary:");
        DaoManager.getBillSummaryDao().getSummaryById(activeBillId).ifPresent(System.out::println);
    }
}