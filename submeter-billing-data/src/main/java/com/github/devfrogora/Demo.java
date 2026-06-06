package com.github.devfrogora;

import com.github.devfrogora.data.config.DatabaseConfig;
import com.github.devfrogora.data.config.DatabaseConnection;
import com.github.devfrogora.data.config.SharedSchemaConfigurator;
import com.github.devfrogora.data.entities.Tenant;

import com.github.devfrogora.data.dao.DaoManager;
import com.github.devfrogora.data.entities.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class Demo {

    private static void initializeEnvironment() throws Exception{
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

    public static void main(String[] args) throws SQLException , Exception {
        System.out.println("=== RUNNING PROPERTY MANAGEMENT SYSTEM WORKFLOW DEMO ===");
        initializeEnvironment(); // Ensure database tables & properties are ready

        Demo app = new Demo();

        // 1. SCREEN A: Provision Unit and Linked Hardware
        System.out.println("\n--- Executing Screen A: Infrastructure Setup ---");
        app.addRoomWithMeter("404-B", "2BHK", "MTR-404B-XYZ", 100.0);

        // 2. SCREEN B: Onboard and Check-In Tenant
        System.out.println("\n--- Executing Screen B: Tenant Onboarding & Placement ---");
        Tenant newTenant = new Tenant();
        newTenant.setName("Amit Sharma");
        newTenant.setPhoneNumber("+919999988888");
        newTenant.setAadharNumber("123456789012");
        newTenant.setAddress("Sector 21, Noida");
        newTenant.setEmail("amit.sharma@example.com");
        newTenant.setActive(true);

        app.addTenantWithTenancy(newTenant, "2026-05-01");

        // 3. SCREEN BILL GENERATION: Process Metrics and Formulate Invoice Statements
        System.out.println("\n--- Executing Screen C: Utility Reading and Billing Operations ---");
        // Simulating 250 units consumed (100.0 baseline -> 350.0 current)
        app.addMeterReadingWithGenerateBill("404-B", 350.0, 10.50, 150.00);
    }

    /**
     * Screen A: Registers a room asset and attaches an initial submeter to it.
     */
    public void addRoomWithMeter(String roomNumber, String roomType, String meterSerialNumber, double meterInitialReading)  throws SQLException{
        // Check if room exists; if not, create it
        Optional<Room> existingRoom = DaoManager.getRoomDao().getRoomByNumber(roomNumber);
        int roomId;

        if (existingRoom.isPresent()) {
            roomId = existingRoom.get().getRoomId();
            System.out.println("Screen A: Room " + roomNumber + " already exists (ID: " + roomId + ")");
        } else {
            Room room = new Room();
            room.setRoomNumber(roomNumber);
            room.setRentAmount(roomType.equalsIgnoreCase("2BHK") ? 12000.00 : 7500.00);
            DaoManager.getRoomDao().insertRoom(room);

            roomId = DaoManager.getRoomDao().getRoomByNumber(roomNumber).orElseThrow().getRoomId();
            System.out.println("Screen A: Room " + roomNumber + " provisioned successfully (ID: " + roomId + ")");
        }

        // Connect submeter hardware deployment profile
        Optional<Submeter> linkedMeters = DaoManager.getSubmeterDao().getSubmeterByRoomId(roomId);
        if (linkedMeters.isEmpty()) {
            Submeter meter = new Submeter();
            meter.setRoomId(roomId);
            meter.setMeterSerialNumber(meterSerialNumber);
            meter.setInitialReading(meterInitialReading);

            boolean meterSaved = DaoManager.getSubmeterDao().insertSubmeter(meter);
            System.out.println("Screen A: Submeter hardware attached -> " + meterSaved);
        } else {
            System.out.println("Screen A: Submeter already linked to this unit: " + linkedMeters.get().getMeterSerialNumber());
        }
    }

    /**
     * Screen B: Onboards a tenant profile and records their check-in transaction.
     */
    public void addTenantWithTenancy(Tenant tenant, String startDate) throws SQLException{
        // Save tenant account if profile doesn't exist
        Optional<Tenant> existing = DaoManager.getTenantDao().getTenantByAadhar(tenant.getAadharNumber());
        int tenantId;

        if (existing.isEmpty()) {
            // 1. The box is empty, so it's safe to write a new record
            DaoManager.getTenantDao().insertTenant(tenant);

            // Fetch the newly generated ID safely using orElseThrow
            tenantId = DaoManager.getTenantDao().getTenantByAadhar(tenant.getAadharNumber())
                    .orElseThrow(() -> new IllegalStateException("Failed to retrieve tenant after insert."))
                    .getTenantId();

            System.out.println("Screen B: Registered new tenant account (ID: " + tenantId + ")");
        } else {
            // 2. The tenant exists! Grab the profile safely from the existing variable
            Tenant existingTenant = existing.get();
            tenantId = existingTenant.getTenantId();

            System.out.println("Screen B: Found matching active profile references (ID: " + tenantId + ")");
        }

        // Tie contract placement row to the newest physical room created in demo stream
        List<Room> rooms = DaoManager.getRoomDao().getAllRooms();
        if (rooms.isEmpty()) {
            System.err.println("Screen B Error: Cannot check in tenant. No rooms exist.");
            return;
        }
        int activeRoomId = rooms.get(rooms.size() - 1).getRoomId(); // Targets our latest room asset

        // Check if room is already occupied
        Optional<Tenancy> currentLease = DaoManager.getTenancyDao().getActiveTenancyByRoomId(activeRoomId);
        if (currentLease.isEmpty()) {
            Tenancy lease = new Tenancy();
            lease.setRoomId(activeRoomId);
            lease.setTenantId(tenantId);
            lease.setStartDate(startDate);
            lease.setEndDate(null);

            boolean activeCheckIn = DaoManager.getTenancyDao().insertTenancy(lease);
            System.out.println("Screen B: Tenancy check-in transaction filed successfully -> " + activeCheckIn);
        } else {
            System.out.println("Screen B Notice: Unit is currently occupied by Tenant ID: " + currentLease.get().getTenantId());
        }
    }

    /**
     * Screen Bill Generation: Validates delta updates against historic indexes.
     */
    public void addMeterReadingWithGenerateBill(String roomNumber, double currentMeterReading, double rate, double fixedCharge)throws SQLException  {
        // 1. Resolve room and submeter
        Room room = DaoManager.getRoomDao().getRoomByNumber(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException("Target unit identification code not found: " + roomNumber));

        Optional<Submeter> meters = DaoManager.getSubmeterDao().getSubmeterByRoomId(room.getRoomId());
        if (meters.isEmpty()) {
            System.err.println("Billing Error: No hardware meter profiles bound to unit number " + roomNumber);
            return;
        }
        Submeter submeter = meters.get();

        // 2. GET PREVIOUS READING ID Safely
        Optional<MeterReading> latestDbReading = DaoManager.getMeterReadingDao().getLatestReadingByMeterId(submeter.getMeterId());

        // Fallback to 0 if there's no history (meaning the bill runs against the submeter's initial baseline)
        int previousReadingId = latestDbReading.map(MeterReading::getReadingId).orElse(0);
        double previousValue = latestDbReading.map(MeterReading::getReadingValue).orElse(submeter.getInitialReading());

        // 3. SAVE CURRENT READING AND FETCH ITS GENERATED ID IMMEDIATELY
        String todayIsoString = LocalDate.now().toString();
        MeterReading currentReading = new MeterReading();
        currentReading.setMeterId(submeter.getMeterId());
        currentReading.setReadingValue(currentMeterReading);
        currentReading.setImageUrlOrPath(null);
        currentReading.setReadingDate(todayIsoString);

        DaoManager.getMeterReadingDao().insertReading(currentReading);

        // Re-query the latest reading to get the auto-generated primary key ID safely
        int currentReadingId = DaoManager.getMeterReadingDao().getLatestReadingByMeterId(submeter.getMeterId())
                .orElseThrow(() -> new IllegalStateException("Database write verification failed."))
                .getReadingId();

        // 4. Query active lease profile for tenant details
        Optional<Tenancy> activeLease = DaoManager.getTenancyDao().getActiveTenancyByRoomId(room.getRoomId());
        String tenantName = "Vacant Unit Asset";
        if (activeLease.isPresent()) {
            Tenant currentOccupant = DaoManager.getTenantDao().getTenantById(activeLease.get().getTenantId()).orElseThrow();
            tenantName = currentOccupant.getName();
        }

        // 5. Pass values AND explicit database IDs directly down to the billing engine
        generateBill(
                tenantName,
                roomNumber,
                currentReadingId,
                currentMeterReading,
                previousReadingId,
                previousValue,
                fixedCharge,
                rate
        );
    }

    /**
     * Core Invoice Processing Component: Formulates and prints financial statements.
     */
    public void generateBill(String tenantName, String roomNumber, int currentReadingId,  double currentReading, int previousReadingId , double previousReading, double fixedCharge,double ratePerUnit) throws SQLException {

        double consumption = currentReading - previousReading;
        double usageCost = consumption * ratePerUnit;
        double totalDue = usageCost + fixedCharge;

        // 2. Build and populate the Bill entity object
        Bill bill = new Bill();
        bill.setPreviousReadingId(previousReadingId);
        bill.setCurrentReadingId(currentReadingId);
        bill.setUnitsConsumed(consumption);
        bill.setRatePerUnit(ratePerUnit);
        bill.setTotalAmount(totalDue); // Combined usage cost + fixed charges
        bill.setBillingDate(LocalDate.now().toString());
        bill.setPaid(false); // New bills default to unpaid until processed

        // 3. Write record to the SQLite database via BillDao
        boolean isSaved = DaoManager.getBillDao().insertBill(bill) > 0 ;

        // Visual Presentation Format Engine
        System.out.println("\n==================================================");
        System.out.println("               UTILITY INVOICE STATEMENT           ");
        System.out.println("==================================================");
        System.out.println(" Tenant Profile   : " + tenantName);
        System.out.println(" Unit Address     : Room " + roomNumber);
        System.out.println(" Statement Date   : " + bill.getBillingDate());
        System.out.println(" Database Sync    : " + (isSaved ? "SUCCESS" : "FAILED"));
        System.out.println("--------------------------------------------------");
        System.out.println(" Current Index    : " + currentReading + " kWh (ID: " + currentReadingId + ")");
        System.out.println(" Previous Index   : " + previousReading + " kWh (ID: " + previousReadingId + ")");
        System.out.println(" Net Consumption  : " + consumption + " Units");
        System.out.println("--------------------------------------------------");
        System.out.println(" Power Usage Cost : ₹" + String.format("%.2f", usageCost));
        System.out.println(" Fixed Maintenance: ₹" + String.format("%.2f", (double) fixedCharge));
        System.out.println(" Total Balance Due: ₹" + String.format("%.2f", totalDue));
        System.out.println("==================================================");
        System.out.println("       Generated Automatically via DevFrogOra     ");
        System.out.println("==================================================");
    }
}