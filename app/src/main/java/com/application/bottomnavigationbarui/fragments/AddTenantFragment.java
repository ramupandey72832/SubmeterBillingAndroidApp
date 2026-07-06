// File: app/.../fragments/AddTenantFragment.java
package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.application.bottomnavigationbarui.RoomsFragment;
import com.application.bottomnavigationbarui.databinding.FragmentAddTenantBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.NavigationUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.impl.TenancyManagementServiceImpl;
import com.github.devfrogora.service.viewmodel.TenantViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTenantFragment extends Fragment implements VerifyMpinDialogFragment.MpinVerificationListener {

    private UiHelper ui;
    private FragmentAddTenantBinding binding;

    // Decoupled presentation core coordinator
    private TenantViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddTenantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(this.getContext());
        startingDataPickerUI();

        // Initialize pure business layer states
        viewModel = new TenantViewModel(new TenancyManagementServiceImpl(),new RoomMeterServiceImpl(new MeterBillingServiceImpl()));

        // Attach listener for processing visual recalculations safely
        viewModel.setStateListener(new TenantViewModel.StateListener() {
            @Override
            public void onStateChanged() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState());
                }
            }
        });



        // 1. Fetch your dataset safely from the ViewModel layer
        List<String> roomNumbers = viewModel.getRoomNumbersList();

// Defensive fallback check: Ensure the view layer handles empty data layer queries gracefully
        if (roomNumbers == null || roomNumbers.isEmpty()) {
            roomNumbers = new ArrayList<>();
            // Optional: Add a temporary placeholder so the user knows why it's empty
            roomNumbers.add("NAN");
        }

// 2. Instantiate a clean Material 3 compliant dropdown list layout item
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line, // Best for M3 styling
                roomNumbers
        );

        AutoCompleteTextView roomDropdown = binding.layoutAddTenant.actvRoomDropdown;

// 3. Attach your adapter payload first
        roomDropdown.setAdapter(adapter);

        roomDropdown.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedRoom = parent.getItemAtPosition(position).toString();

            // Safety handling: Guard against placeholder selection clicks
            if (selectedRoom.equals("NAN")) {
                roomDropdown.setText("", false);
                return;
            }

            // Pass the chosen value safely into your tenant presentation or verification layer
            // viewModel.setSelectedRoomNumber(selectedRoom);

            Toast.makeText(getContext(), "Selected Room: " + selectedRoom, Toast.LENGTH_SHORT).show();

            binding.layoutAddTenant.actvRoomDropdown.setText(selectedRoom);
        });
        roomDropdown.setAdapter(adapter);

        binding.layoutAddTenant.btnAddTenant.setOnClickListener(view1 -> {
            String tenantName = binding.layoutAddTenant.etTenantName.getText().toString();
            String aadharNumber = binding.layoutAddTenant.etAadharNumber.getText().toString();
            String tenantMobile = binding.layoutAddTenant.etTenantMobile.getText().toString();
//            String tenantParentMobile = binding.layoutAddTenant.etTenantParentMobile.getText().toString();
            String tenantAddress = binding.layoutAddTenant.etTenantAddress.getText().toString();
            String roomNumber = binding.layoutAddTenant.actvRoomDropdown.getText().toString();
            String startDate = binding.layoutAddTenant.etStartDate.getText().toString();

            if (tenantName.isEmpty() || aadharNumber.isEmpty() || tenantMobile.isEmpty()
                    || tenantAddress.isEmpty() || roomNumber.isEmpty() || startDate.isEmpty()) {
                Toast.makeText(getContext(), "Please complete all field requirements.", Toast.LENGTH_SHORT).show();
                return;
            }

            // LAUNCH THE SECURITY GATE DIALOG HERE
            VerifyMpinDialogFragment dialog = new VerifyMpinDialogFragment();
            dialog.show(getChildFragmentManager(), "MpinVerifyDialog");
        });
    }

    @Override
    public void onMpinVerified(boolean isSuccess) {
        if (isSuccess) {
            executeAddTenantLogic();
        }
    }

    private void executeAddTenantLogic() {
        String tenantName = binding.layoutAddTenant.etTenantName.getText().toString();
        String aadharNumber = binding.layoutAddTenant.etAadharNumber.getText().toString();
        String tenantMobile = binding.layoutAddTenant.etTenantMobile.getText().toString();
//        String tenantParentMobile = binding.layoutAddTenant.etTenantParentMobile.getText().toString();
        String tenantAddress = binding.layoutAddTenant.etTenantAddress.getText().toString();
        String roomNumber = binding.layoutAddTenant.actvRoomDropdown.getText().toString();
        String startDate = binding.layoutAddTenant.etStartDate.getText().toString();

        // Forward arguments over to ViewModel controls
        viewModel.onboardNewTenant(tenantName, aadharNumber, tenantMobile, "tenantParentMobile", tenantAddress, roomNumber, startDate);
    }

    /**
     * Evaluates state tracking updates inside the ViewModel and renders layout states
     */
    private void renderUiState() {
        // 1. Toggle loading interaction gates during async routines
        binding.layoutAddTenant.btnAddTenant.setEnabled(!viewModel.isLoading());

        // 2. Intercept engine failures and map them via central ErrorUtils helpers
        if (viewModel.getErrorMessage() != null) {
            ErrorUtils.handleDatabaseException("Tenancy Creation Aborted", new Exception(viewModel.getErrorMessage()), ui);
        }

        // 3. Wring out inputs exclusively upon structural commitment confirmations
        if (viewModel.isOperationSuccess()) {
            NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new RoomsFragment());
            String tenantName = binding.layoutAddTenant.etTenantName.getText().toString();
            Toast.makeText(getContext(), "Tenant : " + tenantName + " added successfully", Toast.LENGTH_SHORT).show();
            clearInputs();
        }
    }

    private void clearInputs() {
        binding.layoutAddTenant.etTenantName.setText("");
        binding.layoutAddTenant.etAadharNumber.setText("");
        binding.layoutAddTenant.etTenantMobile.setText("");
//        binding.layoutAddTenant.etTenantParentMobile.setText("");
        binding.layoutAddTenant.etTenantAddress.setText("");
        binding.layoutAddTenant.actvRoomDropdown.setText("");
        binding.layoutAddTenant.etStartDate.setText("");
        binding.layoutAddTenant.actvRoomDropdown.setText("", false);
    }

    void startingDataPickerUI() {
        TextInputEditText etStartDate = binding.layoutAddTenant.etStartDate;

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Tenant Start Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        etStartDate.setOnClickListener(v -> {
            datePicker.show(getChildFragmentManager(), "TENANT_DATE_PICKER");
        });

        datePicker.addOnPositiveButtonClickListener(selectionTimestamp -> {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateString = formatter.format(new Date(selectionTimestamp));
            etStartDate.setText(dateString);
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.setStateListener(null); // Evict background updates to prevent layout memory context leaks
        binding = null;
    }
}