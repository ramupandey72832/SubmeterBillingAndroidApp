// File Location: app/.../fragments/RoomsFragment.java
package com.application.bottomnavigationbarui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.application.bottomnavigationbarui.adapters.RoomsAdapter;
import com.application.bottomnavigationbarui.databinding.DialogQuickEditTenantBinding;
import com.application.bottomnavigationbarui.databinding.FragmentRoomsBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.impl.TenancyManagementServiceImpl;
import com.github.devfrogora.service.viewmodel.RoomMeterViewModel;
import com.github.devfrogora.service.viewmodel.TenancyViewModel;

import java.util.ArrayList;
import java.util.List;

public class RoomsFragment extends Fragment implements RoomsAdapter.OnRoomActionListener {

    private UiHelper ui;
    private FragmentRoomsBinding binding;
    private DialogQuickEditTenantBinding dialogBinding; // Dialog view binding reference
    private RoomsAdapter adapter;
    private List<RoomRegistryDto> roomList;

    // ViewModels (Completely isolated state machines)
    private RoomMeterViewModel roomMeterViewModel;
    private TenancyViewModel tenancyViewModel;

    private AlertDialog editDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRoomsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(getContext());
        roomList = new ArrayList<>();

        binding.rvRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RoomsAdapter(roomList, this);
        binding.rvRooms.setAdapter(adapter);

        // Initialize Room Metrics ViewModel
        RoomMeterServiceImpl roomService = new RoomMeterServiceImpl(new MeterBillingServiceImpl());
        roomMeterViewModel = new RoomMeterViewModel(roomService);
        roomMeterViewModel.setStateListener(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::renderRoomListState);
            }
        });

        // Initialize Tenancy Management ViewModel
        TenancyManagementServiceImpl tenancyService = new TenancyManagementServiceImpl();
        tenancyViewModel = new TenancyViewModel(tenancyService,roomService,new MeterBillingServiceImpl());
        tenancyViewModel.setStateListener(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::renderTenancyMutationState);
            }
        });

        // Load dataset
        roomMeterViewModel.loadRoomReports();
    }

    private void renderRoomListState() {
        if (roomMeterViewModel.getErrorMessage() != null) {
            ErrorUtils.handleDatabaseException("Report Failure", new Exception(roomMeterViewModel.getErrorMessage()), ui);
        }

        if (roomMeterViewModel.isOperationSuccess()) {
            roomList.clear();
            roomList.addAll(roomMeterViewModel.getRoomReportsList());
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Observes Tenancy changes and updates layout UI states elegantly
     */
    private void renderTenancyMutationState() {
        if (tenancyViewModel.isLoading()) {
            return;
        }

        // Check for anomalies and report errors safely to UI via Toast or dialog utilities
        if (tenancyViewModel.getFeedbackMessage() != null) {
            Toast.makeText(getContext(), tenancyViewModel.getFeedbackMessage(), Toast.LENGTH_SHORT).show();
        }

        // If a tenant payload returns successfully, populate fields inside the active dialog
        if (tenancyViewModel.isOperationSuccess() && tenancyViewModel.getCurrentEditingTenant() != null && dialogBinding != null) {
            var tenant = tenancyViewModel.getCurrentEditingTenant();
            dialogBinding.etEditName.setText(tenant.getName());
            dialogBinding.etEditMobile.setText(tenant.getPhoneNumber());
            dialogBinding.etEditAadhaar.setText(tenant.getAadharNumber());
            dialogBinding.etEditAddress.setText(tenant.getAddress());
        }

        // If updating succeeds completely, clear references and update datasets
        if (tenancyViewModel.isOperationSuccess() && tenancyViewModel.getCurrentEditingTenant() == null) {
            if (editDialog != null && editDialog.isShowing()) {
                editDialog.dismiss();
            }
            roomMeterViewModel.loadRoomReports(); // Dynamic update sweep
        }
    }

    @Override
    public void onQuickEdit(RoomRegistryDto room) {
        if (room.isVacant() || room.getTenantName().equalsIgnoreCase("N/A")) {
            Toast.makeText(getContext(), "Cannot edit details: Room is currently vacant.", Toast.LENGTH_SHORT).show();
            return;
        }

        dialogBinding = DialogQuickEditTenantBinding.inflate(LayoutInflater.from(getContext()));

        editDialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext())
                .setView(dialogBinding.getRoot())
                .create();

        if (editDialog.getWindow() != null) {
            editDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogBinding.tvRoomSubtitle.setText("Room " + room.getRoomNumber() + " • Profile Lock Active");

        // Ask the ViewModel to load the current details from the database safely
        tenancyViewModel.loadTenantForRoom(room.getRoomNumber());

        dialogBinding.btnSaveTenantChanges.setOnClickListener(v -> {
            String name = dialogBinding.etEditName.getText().toString().trim();
            String mobile = dialogBinding.etEditMobile.getText().toString().trim();
            String aadhaarNumber = dialogBinding.etEditAadhaar.getText().toString().trim();
            String address = dialogBinding.etEditAddress.getText().toString().trim();

            if (name.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(getContext(), "Name and Mobile fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Command invocation down the lifecycle flow pipeline
            tenancyViewModel.quickUpdateTenantProfile(name, mobile, aadhaarNumber, address);
        });

        dialogBinding.btnCancelEdit.setOnClickListener(v -> editDialog.dismiss());
        editDialog.show();
    }

    @Override public void onActionEdit(RoomRegistryDto room) {}
    @Override public void onActionTenant(RoomRegistryDto room) {}
    @Override public void onActionLink(RoomRegistryDto room) {}

    @Override
    public void onActionDelete(RoomRegistryDto room, int position) {
        roomList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        roomMeterViewModel.setStateListener(null);
        tenancyViewModel.setStateListener(null);
        dialogBinding = null;
        binding = null;
    }
}