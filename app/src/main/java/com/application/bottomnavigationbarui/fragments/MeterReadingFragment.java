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

import com.application.bottomnavigationbarui.databinding.FragmentMeterReadingBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.NavigationUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.impl.TenancyManagementServiceImpl;
import com.github.devfrogora.service.viewmodel.MeterReadingViewModel;

public class MeterReadingFragment extends Fragment {

    private UiHelper ui;
    private FragmentMeterReadingBinding binding;

    // Pure business presentation layer coordinator
    private MeterReadingViewModel viewModel;

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
        ui = new UiHelper(this.getContext());

        // Initialize our isolated ViewModel with its background dependencies explicitly injected
        viewModel = new MeterReadingViewModel(
                new RoomMeterServiceImpl(new MeterBillingServiceImpl()),
                new MeterBillingServiceImpl(),
                new TenancyManagementServiceImpl()
        );

        // Bind layout behaviors down to structural updates
        viewModel.setStateListener(new MeterReadingViewModel.StateListener() {
            @Override
            public void onStateChanged() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState());
                }
            }
        });

        // Parse explicit scanning arguments forwarded down from barcode layouts
        if (getArguments() != null) {
            if (getArguments().containsKey("ARG_QR_DATA")) {
                String qrData = getArguments().getString("ARG_QR_DATA");
                binding.etRoomNumber.setText(qrData);
            }
        }

        // Initialize immutable visual states on loading profiles

        binding.etSubmeterSerialNumber.setEnabled(false);
        binding.etPreviousMeterReading.setEnabled(false);
        binding.etRatePerUnit.setEnabled(false);
        binding.etFixedCharge.setEnabled(false);
        binding.btnNext.setEnabled(false);
        binding.etSubmeterSerialNumber.setText("");

        binding.btnCheck.setOnClickListener(view1 -> {
            String roomNumber = binding.etRoomNumber.getText().toString().trim();
            viewModel.checkRoomDetails(roomNumber);
        });



        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String roomNumber = binding.etRoomNumber.getText().toString().trim();
                String submeterSerialNumber = binding.etSubmeterSerialNumber.getText().toString().trim();
                String currentStr = binding.etCurrentMeterReading.getText().toString().trim();
                String fixedStr = binding.etFixedCharge.getText().toString().trim();
                String rateStr = binding.etRatePerUnit.getText().toString().trim();

                // NEW FIELDS
                String extraStr = binding.etExtraCharge.getText().toString().trim();
                String notes = binding.etNotes.getText().toString().trim();


                if (roomNumber.isEmpty() || submeterSerialNumber.isEmpty() || currentStr.isEmpty() || fixedStr.isEmpty() || rateStr.isEmpty()) {
                    Toast.makeText(getContext(), "Please populate all fields completely.", Toast.LENGTH_SHORT).show();
                    return;
                }

                double currentMeterReading = Double.parseDouble(currentStr);
                double previousMeterReading = viewModel.getPreviousReading(); // Read straight from state values
                double fixedCharge = Double.parseDouble(fixedStr);
                double ratePerUnit = Double.parseDouble(rateStr);

                // Handle Extra Charge (default to 0 if empty)
                double extraCharge = extraStr.isEmpty() ? 0.0 : Double.parseDouble(extraStr);

                // Quick client side sanity boundary check
                if (currentMeterReading < previousMeterReading) {
                    binding.etCurrentMeterReading.setError("Current reading cannot be lower than the previous reading!");
                    return;
                }

                Fragment targetFragment = GenerateBillFragment.newInstance(
                        roomNumber,
                        viewModel.getTenantName(),
                        submeterSerialNumber,
                        currentMeterReading,
                        previousMeterReading,
                        ratePerUnit,
                        fixedCharge,
                        extraCharge, // Passed here
                        notes        // Passed here
                );
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), targetFragment);
            }
        });
    }

    /**
     * Inspects active state fields inside the ViewModel and renders matching visual components
     */
    private void renderUiState() {
        // 1. Manage interactive locking schemas during background asynchronous sweeps
        binding.btnCheck.setEnabled(!viewModel.isLoading());

        // 2. Intercept and project exceptions using your central system ErrorUtils mechanics
        if (viewModel.getErrorMessage() != null) {
            ErrorUtils.handleDatabaseException("Verification Failure", new Exception(viewModel.getErrorMessage()), ui);
        }

        // 3. Unlock input variables if the data checks confirm success states
        if (viewModel.isRoomVerified()) {
            binding.tvTenantName.setText("Name : "+viewModel.getTenantName()+" PH.NO.: "+viewModel.getTenantContactNumber());
            binding.etSubmeterSerialNumber.setText(viewModel.getMeterSerialNumber());
            binding.etPreviousMeterReading.setText(String.valueOf(viewModel.getPreviousReading()));

            binding.etRatePerUnit.setEnabled(true);
            binding.etFixedCharge.setEnabled(true);

            // ENABLE NEW FIELDS
            binding.etExtraCharge.setEnabled(true);
            binding.etNotes.setEnabled(true);

            binding.btnNext.setEnabled(true);
        } else {
            binding.etRatePerUnit.setEnabled(false);
            binding.etFixedCharge.setEnabled(false);

            // DISABLE NEW FIELDS
            binding.etExtraCharge.setEnabled(false);
            binding.etNotes.setEnabled(false);

            binding.btnNext.setEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.setStateListener(null); // Terminate background listener bindings to dodge leaks
        binding = null;
    }
}