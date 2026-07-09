// File: app/.../fragments/EndRoomTenancyFragment.java
package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.application.baselibrary.ui.utils.ToastMessage;
import com.application.bottomnavigationbarui.DashboardFragment;
import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentEndRoomTenancyBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.baselibrary.ui.utils.NavigationUtils;

import com.github.devfrogora.service.dto.TenantDTO;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.impl.TenancyManagementServiceImpl;
import com.github.devfrogora.service.viewmodel.TenancyViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EndRoomTenancyFragment extends Fragment implements VerifyMpinDialogFragment.MpinVerificationListener {

    private FragmentEndRoomTenancyBinding binding;
    private ToastMessage ui;

    // Decoupled Business Presentation layer
    private TenancyViewModel viewModel;

    public EndRoomTenancyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEndRoomTenancyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new ToastMessage(this.getContext());
        startingDataPickerUI();

        // 1. Initialize ViewModel with its required operational services injected
        viewModel = new TenancyViewModel(
                new TenancyManagementServiceImpl(),
                new RoomMeterServiceImpl(new MeterBillingServiceImpl()),
                new MeterBillingServiceImpl()
        );

        // 2. Wire up state tracking listener
        viewModel.setStateListener(new TenancyViewModel.StateListener() {
            @Override
            public void onStateChanged() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState());
                }
            }
        });


        binding.btnBack.setOnClickListener(v -> {
            clearInputs();
            NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new DashboardFragment(),R.id.frame_layout);
        });

        // Initialize display container visibility conditions based on state default checks
        binding.cvTenantDetailsContainer.setVisibility(View.GONE);

        binding.btnCheck.setOnClickListener(v -> {
            String roomNumber = binding.etRoomNumber.getText().toString().trim();
            viewModel.verifyAndFetchActiveTenant(roomNumber);
        });

        binding.btnEndTenancy.setOnClickListener(v -> {
            String roomNumber = binding.etRoomNumber.getText().toString().trim();
            String endDate = binding.etEndDate.getText().toString().trim();

            if (roomNumber.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(getContext(), "Room number and End date are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            // LAUNCH THE SECURITY DIALOG GATE HERE
            String msg = "Are you sure you want to terminate tenancy for room " + roomNumber + " on " + endDate + "?" ;
            VerifyMpinDialogFragment dialog =  VerifyMpinDialogFragment.newInstance(msg);
            dialog.show(getChildFragmentManager(), "MpinVerifyDialog");
        });
    }

    @Override
    public void onMpinVerified(boolean isSuccess) {
        if (isSuccess) {
            String roomNumber = binding.etRoomNumber.getText().toString().trim();
            String endDate = binding.etEndDate.getText().toString().trim();

            // Forward execution request down to business state machine safely
            viewModel.terminateTenancy(roomNumber, endDate);
        }
    }

    /**
     * Single source of truth rendering procedure for this layout screen.
     */
    private void renderUiState() {
        // 1. Toggle interaction lock state during async actions
        binding.btnCheck.setEnabled(!viewModel.isLoading());
        binding.btnEndTenancy.setEnabled(!viewModel.isLoading());

        // 2. Capture and route any service layer exceptions safely through your system ErrorUtils helper
        if (viewModel.getErrorMessage() != null) {
            ErrorUtils.handleDatabaseException(
                    "Operation Error",
                    new Exception(viewModel.getErrorMessage()),
                    ui
            );
        }

        // 3. Populate container details if valid tenant configurations are present
        if (viewModel.areDetailsLoaded() && viewModel.getLoadedTenant() != null) {
            TenantDTO tenant = viewModel.getLoadedTenant();
            binding.tvTenantName.setText(tenant.getName());
            binding.tvAadhaarNumber.setText(tenant.getAadharNumber());
            binding.tvMobileNumber.setText(tenant.getPhoneNumber());
            binding.tvLastMeterReading.setText(String.valueOf(viewModel.getLatestMeterReading()));

            binding.cvTenantDetailsContainer.setVisibility(View.VISIBLE);
        } else {
            binding.cvTenantDetailsContainer.setVisibility(View.GONE);
        }

        // 4. Handle successful tenancy drop loops
        if (viewModel.isTerminationSuccess()) {
            viewModel.resetTerminationFlag();
            Toast.makeText(getContext(), "Tenancy successfully terminated.", Toast.LENGTH_LONG).show();
            clearInputs();
        }
    }

    private void clearInputs() {
        binding.etRoomNumber.setText("");
        binding.etEndDate.setText("");
        binding.cvTenantDetailsContainer.setVisibility(View.GONE);
    }

    void startingDataPickerUI() {
        TextInputEditText etEndDate = binding.etEndDate;

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Termination End Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        etEndDate.setOnClickListener(v -> {
            datePicker.show(getChildFragmentManager(), "TENANT_DATE_PICKER");
        });

        datePicker.addOnPositiveButtonClickListener(selectionTimestamp -> {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateString = formatter.format(new Date(selectionTimestamp));
            etEndDate.setText(dateString);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.setStateListener(null); // Prevent leaks
        binding = null;
    }
}