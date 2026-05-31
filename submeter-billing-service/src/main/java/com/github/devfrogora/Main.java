package com.github.devfrogora;

import com.github.devfrogora.data.dao.DaoManager;
import com.github.devfrogora.data.entities.Bill;
import com.github.devfrogora.data.entities.Room;
import com.github.devfrogora.data.entities.Tenancy;
import com.github.devfrogora.data.entities.Tenant;
import com.github.devfrogora.service.DatabaseSetup;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.TenancyManagementService;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.dto.BillReportDto;
import com.github.devfrogora.service.dto.RoomRegistryDto;
import com.github.devfrogora.service.exception.BusinessRuleException;
import com.github.devfrogora.service.exception.ResourceNotFoundException;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.impl.TenancyManagementServiceImpl;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static RoomMeterService infraService;
    static TenancyManagementService tenantService;
    static MeterBillingService billingService;
    public static void main(String[] args)  throws SQLException , Exception {
        // Init frameworks
        DatabaseSetup.initializeDb("jdbc:sqlite:submeter_bill.db", null, null, "org.sqlite.JDBC");

        // Instantiate segregated submeter tracking services
         infraService = new RoomMeterServiceImpl();
         tenantService = new TenancyManagementServiceImpl();
         billingService = new MeterBillingServiceImpl();

        // Execution is completely modular and explicit!
        infraService.addRoomWithMeter("404-B", "2BHK", "MTR-404B-XYZ", 100.0);

        System.out.println("\n--- Executing Screen B: Tenant Onboarding & Placement ---");
        Tenant newTenant = new Tenant();
        newTenant.setName("Amit Sharma");
        newTenant.setPhoneNumber("+919999988888");
        newTenant.setAadharNumber("123456789012");
        newTenant.setAddress("Sector 21, Noida");
        newTenant.setEmail("amit.sharma@example.com");
        newTenant.setActive(true);

        tenantService.addTenantWithTenancy(newTenant, "404-B","2026-05-01");

        System.out.println("\n--- Executing Screen C: Utility Reading and Billing Operations ---");
        // Simulating 250 units consumed (100.0 baseline -> 350.0 current)
        billingService.addMeterReadingWithGenerateBill("404-B", 350.0, 10.50, 150.00);
    }

    void terminateTenancyOfRoom(String roomNumber) throws SQLException {
        System.out.println("\n--- Offboarding Operation for Room: " + roomNumber + " ---");
        try {
             tenantService.terminateTenancyOfRoom(roomNumber);
        }catch (ResourceNotFoundException e){
            System.err.println("Failed to Terminate tenancy :  " + e.getMessage());
        }
    }

    void setPaymentStatus(int billId, boolean isPaid) throws SQLException{
        System.out.println("\n--- Updating Bill Payment ID: " + billId + " ---");
        try {
            billingService.updateBillPaymentStatus(billId, isPaid);
            System.out.println("Success: Payment status updated to " + (isPaid ? "PAID" : "UNPAID"));
        } catch (BusinessRuleException e) {
            System.err.println("Failed to update bill status: " + e.getMessage());
        }
    }

    void getTotalUnit()  throws SQLException{
        double totalUnits = billingService.getTotalUnit();
        System.out.println("\nMetrics Aggregate: Total Units Consumed across ecosystem = " + totalUnits + " Units");
    }

    void getTotalBillsCount()  throws SQLException {
        int totalBills = DaoManager.getBillDao().getAllBills().size();
        System.out.println("\nMetrics Aggregate: Total Invoice Bills Count = " + totalBills);
    }

    void getPendingBills()  throws SQLException{
        System.out.println("\n================ PENDING (UNPAID) INVOICES ================");

        billingService.getAllPendingBills()
                .forEach(b -> System.out.printf("Bill ID: %d | Date: %s | Amount Due: INR %.2f\n",
                        b.getBillId(), b.getBillingDate(), b.getTotalAmount()));
    }


    /**
     * Bill NO , ROOM Number , Tenant Name , Data of Bill , TotalBill ,
     * Bill Payment Status
     */
    void getAllBills()  throws SQLException{
        System.out.println("\n==================================== ALL BILLS SYSTEM LEDGER ====================================");
        System.out.printf("%-10s | %-12s | %-20s | %-12s | %-12s | %-10s\n",
                "BILL NO", "ROOM NUMBER", "TENANT NAME", "BILL DATE", "TOTAL DUE", "STATUS");
        System.out.println("-------------------------------------------------------------------------------------------------");

        // Fetch the pre-bundled data transfer collection
        List<BillReportDto> report = billingService.getAllBillsReport();

        for (BillReportDto dto : report) {
            System.out.printf("%-10d | %-12s | %-20s | %-12s | INR %-8.2f | %-10s\n",
                    dto.getBillId(), dto.getRoomNumber(), dto.getTenantName(),
                    dto.getBillingDate(), dto.getTotalAmount(), dto.getPaymentStatus());
        }
    }

    /**
     *  Get Room Number , Tenant Name
     *  Submeter Serial Number
     *  Vacant : Yes or NO
     */
    void getAllRooms() {
        System.out.println("\n============================ INFRASTRUCTURE PROPERTY STATUS ============================");
        System.out.printf("%-12s | %-20s | %-20s | %-10s\n",
                "ROOM NUMBER", "TENANT NAME", "SUBMETER SERIAL", "VACANT");
        System.out.println("-----------------------------------------------------------------------------------------");

        // Fetch the pre-bundled data transfer collection
        List<RoomRegistryDto> registry = infraService.getRoomRegistryReport();

        for (RoomRegistryDto dto : registry) {
            System.out.printf("%-12s | %-20s | %-20s | %-10s\n",
                    dto.getRoomNumber(), dto.getTenantName(),
                    dto.getSubmeterSerialNumber(), dto.isVacant() ? "YES" : "NO");
        }
    }
}