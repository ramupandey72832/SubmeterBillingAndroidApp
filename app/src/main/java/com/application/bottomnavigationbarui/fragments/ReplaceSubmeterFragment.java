// File: app/.../fragments/ReplaceSubmeterFragment.java
package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.application.bottomnavigationbarui.databinding.FragmentReplaceSubmeterBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.viewmodel.RoomMeterViewModel;

public class ReplaceSubmeterFragment extends Fragment implements VerifyMpinDialogFragment.MpinVerificationListener {

    private UiHelper ui;
    private FragmentReplaceSubmeterBinding binding;

    // Decoupled Business Presentation Core
    private RoomMeterViewModel viewModel;

    public ReplaceSubmeterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReplaceSubmeterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(requireActivity());

        // Initialize pure ViewModel with the service and its required dependency chain
        RoomMeterServiceImpl service = new RoomMeterServiceImpl(new MeterBillingServiceImpl());
        viewModel = new RoomMeterViewModel(service);

        // Track asynchronous state changes securely
        viewModel.setStateListener(new RoomMeterViewModel.StateListener() {
            @Override
            public void onStateChanged() {
                // Background operations must modify layout states on Android's Main Thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState());
                }
            }
        });

        binding.btnUpdate.setOnClickListener(view1 -> {
            String roomNumber = binding.etRoomNumber.getText().toString().trim();
            String oldMeterSerialNumber = binding.etOldMeterNumber.getText().toString().trim();
            String newMeterSerialNumber = binding.etNewSerialNumber.getText().toString().trim();

            if (roomNumber.isEmpty() || oldMeterSerialNumber.isEmpty() || newMeterSerialNumber.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields completely.", Toast.LENGTH_SHORT).show();
                return;
            }

            // LAUNCH THE SECURITY DIALOG GATE HERE
            VerifyMpinDialogFragment dialog = new VerifyMpinDialogFragment();
            dialog.show(getChildFragmentManager(), "MpinVerifyDialog");
        });
    }

    @Override
    public void onMpinVerified(boolean isSuccess) {
        if (isSuccess) {
            executeUpdateSubmeterLogic();
        }
    }

    private void executeUpdateSubmeterLogic() {
        String roomNumber = binding.etRoomNumber.getText().toString().trim();
        String oldMeterSerialNumber = binding.etOldMeterNumber.getText().toString().trim();
        String newMeterSerialNumber = binding.etNewSerialNumber.getText().toString().trim();

        // Forward the operational parameters down to the ViewModel
        viewModel.replaceSubmeter(roomNumber, oldMeterSerialNumber, newMeterSerialNumber);
    }

    /**
     * Single source of truth evaluating current ViewModel state flags and driving lookups.
     */
    private void renderUiState() {
        // 1. Manage layout clickable statuses when database background workers run
        binding.btnUpdate.setEnabled(!viewModel.isLoading());

        // 2. Safely process data mutations or exception messages via central ErrorUtils mapping
        if (viewModel.getErrorMessage() != null) {
            ErrorUtils.handleDatabaseException(
                    "Replacement Failed",
                    new Exception(viewModel.getErrorMessage()),
                    ui
            );
        }

        // 3. Clear data fields exclusively upon safe confirmation from the storage engine
        if (viewModel.isOperationSuccess()) {
            Toast.makeText(getContext(), "Submeter hardware registration swapped successfully.", Toast.LENGTH_LONG).show();
            clearInputs();
        }
    }

    private void clearInputs() {
        binding.etRoomNumber.setText("");
        binding.etOldMeterNumber.setText("");
        binding.etNewSerialNumber.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.setStateListener(null); // Safeguard against background listener leaks
        binding = null;
    }
}