// File: app/.../fragments/MeterReadingFragment.java
package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.application.android_ui_templete1.templates.nav_activity.bottom_nav_activity.BottomNavActivityConstant;
import com.application.baselibrary.ui.utils.ToastMessage;
import com.application.bottomnavigationbarui.databinding.FragmentMeterReadingBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.baselibrary.ui.utils.NavigationUtils;

import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.impl.TenancyManagementServiceImpl;
import com.github.devfrogora.service.viewmodel.MeterReadingViewModel;

public class MeterReadingFragment extends Fragment {

    private ToastMessage ui;
    private FragmentMeterReadingBinding binding;

    private MeterReadingViewModel viewModel;

    // Cache the latest UI state packet locally for button click actions
    private MeterReadingViewModel.UiState lastState = MeterReadingViewModel.UiState.initial();

    public MeterReadingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMeterReadingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new ToastMessage(this.getContext());

        // 1. Initialize ViewModel with injected dependencies
        viewModel = new MeterReadingViewModel(
                new RoomMeterServiceImpl(new MeterBillingServiceImpl()),
                new MeterBillingServiceImpl(),
                new TenancyManagementServiceImpl()
        );

        // 2. Clean State Observation (Replaces legacy StateListener interface)
        viewModel.observeState(state -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> renderUiState(state));
            }
        });

        // 3. Parse QR scanner arguments if forwarded
        if (getArguments() != null && getArguments().containsKey("ARG_QR_DATA")) {
            String qrData = getArguments().getString("ARG_QR_DATA");
            binding.etRoomNumber.setText(qrData);
        }

        // 4. Initial Component Lockdown
        setInitialInputLockdown();

        // 5. Button Listeners
        binding.btnCheck.setOnClickListener(v -> {
            String roomNumber = binding.etRoomNumber.getText().toString().trim();
            viewModel.checkRoomDetails(roomNumber);
        });

        binding.btnNext.setOnClickListener(v -> {
            String roomNumber = binding.etRoomNumber.getText().toString().trim();
            String submeterSerialNumber = binding.etSubmeterSerialNumber.getText().toString().trim();
            String currentStr = binding.etCurrentMeterReading.getText().toString().trim();
            String fixedStr = binding.etFixedCharge.getText().toString().trim();
            String rateStr = binding.etRatePerUnit.getText().toString().trim();
            String extraStr = binding.etExtraCharge.getText().toString().trim();
            String notes = binding.etNotes.getText().toString().trim();

            if (roomNumber.isEmpty() || submeterSerialNumber.isEmpty() || currentStr.isEmpty() || fixedStr.isEmpty() || rateStr.isEmpty()) {
                Toast.makeText(getContext(), "Please populate all fields completely.", Toast.LENGTH_SHORT).show();
                return;
            }

            double currentMeterReading = Double.parseDouble(currentStr);
            double previousMeterReading = lastState.previousReading; // Read safely from cached state
            double fixedCharge = Double.parseDouble(fixedStr);
            double ratePerUnit = Double.parseDouble(rateStr);
            double extraCharge = extraStr.isEmpty() ? 0.0 : Double.parseDouble(extraStr);

            // Validation boundary check
            if (currentMeterReading < previousMeterReading) {
                binding.etCurrentMeterReading.setError("Current reading cannot be lower than the previous reading!");
                return;
            }

            Fragment targetFragment = GenerateBillFragment.newInstance(
                    roomNumber,
                    lastState.tenantName,
                    submeterSerialNumber,
                    currentMeterReading,
                    previousMeterReading,
                    ratePerUnit,
                    fixedCharge,
                    extraCharge,
                    notes
            );
            NavigationUtils.replaceFragmentWithBackStack(requireActivity(), targetFragment, BottomNavActivityConstant.MAIN_CONTAINER);
        });
    }

    private void setInitialInputLockdown() {
        binding.etSubmeterSerialNumber.setEnabled(false);
        binding.etPreviousMeterReading.setEnabled(false);
        binding.etRatePerUnit.setEnabled(false);
        binding.etFixedCharge.setEnabled(false);
        binding.etExtraCharge.setEnabled(false);
        binding.etNotes.setEnabled(false);
        binding.btnNext.setEnabled(false);
        binding.etSubmeterSerialNumber.setText("");
    }

    /**
     * Renders UI layout directly using the immutable UiState packet
     */
    private void renderUiState(MeterReadingViewModel.UiState state) {
        this.lastState = state; // Cache state for click listeners

        // 1. Manage button loading lock
        binding.btnCheck.setEnabled(!state.isLoading);

        // 2. Handle errors via central ErrorUtils
        if (state.errorMessage != null) {
            ErrorUtils.handleDatabaseException("Verification Failure", new Exception(state.errorMessage), ui);
        }

        // 3. Unlock or lock inputs depending on verification state
        if (state.isRoomVerified) {
            binding.tvTenantName.setText("Name : " + state.tenantName + " PH.NO.: " + state.tenantContactNumber);
            binding.etSubmeterSerialNumber.setText(state.meterSerialNumber);
            binding.etPreviousMeterReading.setText(String.valueOf(state.previousReading));

            binding.etRatePerUnit.setEnabled(true);
            binding.etFixedCharge.setEnabled(true);
            binding.etExtraCharge.setEnabled(true);
            binding.etNotes.setEnabled(true);
            binding.btnNext.setEnabled(true);
        } else {
            binding.etRatePerUnit.setEnabled(false);
            binding.etFixedCharge.setEnabled(false);
            binding.etExtraCharge.setEnabled(false);
            binding.etNotes.setEnabled(false);
            binding.btnNext.setEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.observeState(null); // Clear subscription to avoid memory leaks
        binding = null;
    }
}