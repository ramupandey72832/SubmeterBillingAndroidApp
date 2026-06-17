// File Location: submeter-billing-service/.../service/viewmodel/TenancyViewModel.java
package com.github.devfrogora.service.viewmodel;

import com.github.devfrogora.data.config.DatabaseConnection;
import com.github.devfrogora.data.dao.DaoManager;
import com.github.devfrogora.data.entities.Room;
import com.github.devfrogora.data.entities.Submeter;
import com.github.devfrogora.data.entities.Tenancy;
import com.github.devfrogora.data.entities.Tenant;
import com.github.devfrogora.service.TenancyManagementService;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.dto.TenancyDTO;
import com.github.devfrogora.service.dto.RoomDTO; // Imported DTO
import com.github.devfrogora.service.dto.SubmeterDTO; // Imported DTO
import com.github.devfrogora.service.dto.TenantDTO;
import com.github.devfrogora.service.utils.OperationResult;

import java.sql.SQLException;
import java.util.Optional;

public class TenancyViewModel {

    private final TenancyManagementService tenancyService;
    private final RoomMeterService roomMeterService;
    private final MeterBillingService meterService;

    // Direct UI State Fields
    private boolean isLoading = false;
    private String errorMessage = null;

    // Decoupled architecture data flags
    private boolean isOperationSuccess = false;
    private String feedbackMessage = null;
    private boolean isTerminationSuccess = false;

    // Fetched Detail Data Fields
    private TenantDTO loadedTenant = null;
    private double latestMeterReading = 0.0;
    private boolean detailsLoaded = false;

    // Holds currently loaded tenant details for form binding
    private TenantDTO currentEditingTenant = null;
    private int internalTenantId = -1;

    // FIX: Expose UI-Safe DTOs instead of raw Data Layer Entities
    private RoomDTO loadedRoomDto = null;
    private SubmeterDTO loadedSubmeterDto = null;
    private TenantDTO loadedTenantDto = null;

    // Hidden backing primitives keeping data keys private to the layer context
    private int internalRoomId = -1;
    private int internalSubmeterId = -1;
    private int trackingTenantId = -1;
    private String trackingOriginalRoomNumber = null;

    public interface StateListener {
        void onStateChanged();
    }

    private StateListener stateListener;

    public TenancyViewModel(TenancyManagementService tenancyService, RoomMeterService roomMeterService, MeterBillingService meterService) {
        this.tenancyService = tenancyService;
        this.roomMeterService = roomMeterService;
        this.meterService = meterService;
    }

    public void setStateListener(StateListener listener) {
        this.stateListener = listener;
    }

    private void notifyStateChanged() {
        if (stateListener != null) {
            stateListener.onStateChanged();
        }
    }

    // --- State Getters ---
    public boolean isLoading() { return isLoading; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isTerminationSuccess() { return isTerminationSuccess; }
    public TenantDTO getLoadedTenant() { return loadedTenant; }
    public double getLatestMeterReading() { return latestMeterReading; }
    public boolean areDetailsLoaded() { return detailsLoaded; }
    public boolean isOperationSuccess() { return isOperationSuccess; }
    public String getFeedbackMessage() { return feedbackMessage; }
    public TenantDTO getCurrentEditingTenant() { return currentEditingTenant; }

    // FIX: Return clean DTO signatures to the View layer
    public RoomDTO getLoadedRoomDto() { return loadedRoomDto; }
    public SubmeterDTO getLoadedSubmeterDto() { return loadedSubmeterDto; }
    public TenantDTO getLoadedTenantDto() { return loadedTenantDto; }

    // --- UI Actions ---

    public void verifyAndFetchActiveTenant(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            this.errorMessage = "Please enter a valid room number.";
            notifyStateChanged();
            return;
        }

        this.isLoading = true;
        this.errorMessage = null;
        this.detailsLoaded = false;
        notifyStateChanged();

        new Thread(() -> {
            try {
                TenancyDTO tenancyDTO = tenancyService.findActiveTenancyByRoomNumber(roomNumber.trim());
                if (tenancyDTO == null) {
                    this.errorMessage = "No active tenancy found for Room " + roomNumber;
                    this.isLoading = false;
                    notifyStateChanged();
                    return;
                }

                Optional<TenantDTO> tenantOpt = tenancyService.findTenantByAadhar(tenancyDTO.getTenantAaddhar());
                if (tenantOpt.isEmpty()) {
                    this.errorMessage = "Active tenant records are missing for this tenancy profile.";
                    this.isLoading = false;
                    notifyStateChanged();
                    return;
                }

                OperationResult<SubmeterDTO> submeterOpt = roomMeterService.getSubmeterByRoomNumber(roomNumber.trim());
                if (submeterOpt.isSuccess()) {
                    this.latestMeterReading = meterService.getLatestReading(submeterOpt.getData().getMeterSerialNumber());
                } else {
                    this.latestMeterReading = 0.0;
                }

                this.loadedTenant = tenantOpt.get();
                this.detailsLoaded = true;

            } catch (Exception e) {
                this.errorMessage = "Error querying tenant information: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyStateChanged();
            }
        }).start();
    }

    public void terminateTenancy(String roomNumber, String endDate) {
        if (roomNumber == null || roomNumber.trim().isEmpty() || endDate == null || endDate.trim().isEmpty()) {
            this.errorMessage = "Room number and end date are mandatory fields.";
            notifyStateChanged();
            return;
        }

        this.isLoading = true;
        this.errorMessage = null;
        this.isTerminationSuccess = false;
        notifyStateChanged();

        new Thread(() -> {
            try {
                tenancyService.terminateTenancyOfRoom(roomNumber.trim(), endDate.trim());
                this.isTerminationSuccess = true;
                this.detailsLoaded = false;
            } catch (Exception e) {
                this.errorMessage = e.getMessage();
            } finally {
                this.isLoading = false;
                notifyStateChanged();
            }
        }).start();
    }

    public void loadTenantForRoom(String roomNumber) {
        this.isLoading = true;
        this.feedbackMessage = null;
        this.currentEditingTenant = null;
        this.internalTenantId = -1;
        notifyStateChanged();

        new Thread(() -> {
            try {
                Optional<Room> roomOpt = DaoManager.getRoomDao().getRoomByNumber(roomNumber);
                if (roomOpt.isPresent()) {
                    Optional<Tenancy> activeLease = DaoManager.getTenancyDao().getActiveTenancyByRoomId(roomOpt.get().getRoomId());
                    if (activeLease.isPresent()) {
                        Optional<Tenant> tenantOpt = DaoManager.getTenantDao().getTenantById(activeLease.get().getTenantId());
                        if (tenantOpt.isPresent()) {
                            Tenant tenant = tenantOpt.get();
                            this.internalTenantId = tenant.getTenantId();

                            this.currentEditingTenant = new TenantDTO(
                                    tenant.getName(),
                                    tenant.getAadharNumber(),
                                    tenant.getPhoneNumber(),
                                    tenant.getAddress()
                            );
                            this.isOperationSuccess = true;
                        } else {
                            this.feedbackMessage = "Tenant identity profile record missing.";
                        }
                    } else {
                        this.feedbackMessage = "No active occupancy found for Room " + roomNumber;
                    }
                } else {
                    this.feedbackMessage = "Room details not found.";
                }
            } catch (SQLException e) {
                this.feedbackMessage = "Database Fetch Failure: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyStateChanged();
            }
        }).start();
    }

    public void quickUpdateTenantProfile(String name, String mobile, String aaddharNumber, String address) {
        if (internalTenantId == -1) {
            this.feedbackMessage = "No active profile loaded to update.";
            notifyStateChanged();
            return;
        }

        this.isLoading = true;
        this.isOperationSuccess = false;
        this.feedbackMessage = null;
        notifyStateChanged();

        new Thread(() -> {
            try {
                Tenant tenant = DaoManager.getTenantDao().getTenantById(internalTenantId)
                        .orElseThrow(() -> new SQLException("Tenant record mapping not found."));

                tenant.setName(name);
                tenant.setPhoneNumber(mobile);
                tenant.setAddress(address);
                tenant.setAadharNumber(aaddharNumber);

                boolean isUpdated = DaoManager.getTenantDao().updateTenant(tenant);
                if (isUpdated) {
                    this.isOperationSuccess = true;
                    this.currentEditingTenant = null;
                    this.internalTenantId = -1;
                    this.feedbackMessage = "Tenant details updated successfully.";
                } else {
                    this.feedbackMessage = "Storage mutation request rejected.";
                }
            } catch (SQLException e) {
                this.feedbackMessage = "Database Write Failure: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyStateChanged();
            }
        }).start();
    }

    // =========================================================================
    // --- FIX: Map database elements cleanly to UI-Visible DTO profiles ---
    // =========================================================================

    public void loadEntireRoomAssetConfiguration(String roomNumber) {
        this.isLoading = true;
        this.feedbackMessage = null;
        this.loadedRoomDto = null;
        this.loadedSubmeterDto = null;
        this.loadedTenantDto = null;
        this.internalRoomId = -1;
        this.internalSubmeterId = -1;
        this.trackingTenantId = -1;
        this.trackingOriginalRoomNumber = roomNumber;
        notifyStateChanged();

        new Thread(() -> {
            try {
                Optional<Room> roomOpt = DaoManager.getRoomDao().getRoomByNumber(roomNumber);
                if (roomOpt.isPresent()) {
                    Room room = roomOpt.get();
                    this.internalRoomId = room.getRoomId();

                    // Map to DTO
                    this.loadedRoomDto = new RoomDTO(room.getRoomNumber(), room.getRentAmount(),room.getRoomType());

                    Optional<Submeter> submeterOpt = DaoManager.getSubmeterDao().getSubmeterByRoomId(internalRoomId);
                    if (submeterOpt.isPresent()) {
                        Submeter submeter = submeterOpt.get();
                        this.internalSubmeterId = submeter.getMeterId();

                        // Map to DTO
                        this.loadedSubmeterDto = new SubmeterDTO(
                                submeter.getMeterId(),
                                submeter.getRoomId(),
                                submeter.getMeterSerialNumber(),
                                submeter.getInitialReading()
                        );
                    }

                    Optional<Tenancy> activeLease = DaoManager.getTenancyDao().getActiveTenancyByRoomId(internalRoomId);
                    if (activeLease.isPresent()) {
                        Optional<Tenant> tenantOpt = DaoManager.getTenantDao().getTenantById(activeLease.get().getTenantId());
                        if (tenantOpt.isPresent()) {
                            Tenant tenant = tenantOpt.get();
                            this.trackingTenantId = tenant.getTenantId();

                            // Map to DTO
                            this.loadedTenantDto = new TenantDTO(
                                    tenant.getName(),
                                    tenant.getAadharNumber(),
                                    tenant.getPhoneNumber(),
                                    tenant.getAddress()
                            );
                        }
                    }
                    this.isOperationSuccess = true;
                } else {
                    this.feedbackMessage = "Target room asset configuration lookup failed.";
                }
            } catch (SQLException e) {
                this.feedbackMessage = "Database Fetch Exception: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyStateChanged();
            }
        }).start();
    }

    public void saveAllAssetChanges(String targetRoomNum, String roomType, String meterSerial, double initialReading,
                                    String tenantName, String tenantMobile, String tenantAadhaar, String tenantAddress) {
        if (internalRoomId == -1) {
            this.feedbackMessage = "Cannot save: No active configuration instance initialized.";
            notifyStateChanged();
            return;
        }

        this.isLoading = true;
        this.isOperationSuccess = false;
        this.feedbackMessage = null;
        notifyStateChanged();

        new Thread(() -> {
            try {
                DatabaseConnection.beginTransaction();

                // 1. Re-fetch and update Room Entity via cached primary key index
                Room room = DaoManager.getRoomDao().getRoomByNumber(trackingOriginalRoomNumber)
                        .orElseThrow(() -> new SQLException("Original Room structural mapping lost."));
                room.setRoomNumber(targetRoomNum);
                room.setRoomType(roomType);
                DaoManager.getRoomDao().updateRoom(room);

                // 2. Re-fetch and update Submeter Entity via cached primary key index
                if (internalSubmeterId != -1) {
                    Optional<Submeter> submeterOpt = DaoManager.getSubmeterDao().getSubmeterByRoomId(room.getRoomId());
                    if (submeterOpt.isPresent()) {
                        Submeter submeter = submeterOpt.get();
                        submeter.setMeterSerialNumber(meterSerial);
                        submeter.setInitialReading(initialReading);
                        DaoManager.getSubmeterDao().updateSubmeter(submeter);
                    }
                }

                // 3. Re-fetch and update Tenant Entity via cached primary key index
                if (trackingTenantId != -1) {
                    Optional<Tenant> tenantOpt = DaoManager.getTenantDao().getTenantById(trackingTenantId);
                    if (tenantOpt.isPresent()) {
                        Tenant t = tenantOpt.get();
                        t.setName(tenantName);
                        t.setPhoneNumber(tenantMobile);
                        t.setAadharNumber(tenantAadhaar);
                        t.setAddress(tenantAddress);
                        DaoManager.getTenantDao().updateTenant(t);
                    }
                }

                DatabaseConnection.commitTransaction();
                this.isOperationSuccess = true;
                this.feedbackMessage = "Asset configuration parameters modified cleanly.";

                // Reset structural variables
                this.loadedRoomDto = null;
                this.loadedSubmeterDto = null;
                this.loadedTenantDto = null;
                this.internalRoomId = -1;
                this.internalSubmeterId = -1;
                this.trackingTenantId = -1;

            } catch (SQLException e) {
                DatabaseConnection.rollbackTransaction();
                this.feedbackMessage = "Database Structural Mutation Aborted: " + e.getMessage();
            } finally {
                this.isLoading = false;
                notifyStateChanged();
            }
        }).start();
    }
}