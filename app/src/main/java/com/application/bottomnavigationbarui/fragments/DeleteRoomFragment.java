package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.application.bottomnavigationbarui.databinding.FragmentDeleteRoomBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.viewmodel.RoomMeterViewModel;

public class DeleteRoomFragment extends Fragment implements VerifyMpinDialogFragment.MpinVerificationListener {

    private UiHelper ui;
    private FragmentDeleteRoomBinding binding;

    // Decoupled Business Core State Holder
    private RoomMeterViewModel viewModel;

    public DeleteRoomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDeleteRoomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(this.getContext());

        // Initialize dependencies purely using business layer constructs
        RoomMeterServiceImpl service = new RoomMeterServiceImpl(new MeterBillingServiceImpl());
        viewModel = new RoomMeterViewModel(service);

        // Bind layout views directly to ViewModel state change listeners
        viewModel.setStateListener(new RoomMeterViewModel.StateListener() {
            @Override
            public void onStateChanged() {
                // Background processing updates must run on Android's Main Thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState());
                }
            }
        });

        binding.btnDeleteRoom.setOnClickListener(v -> {
            String targetRoom = binding.etDeleteRoomNumber.getText().toString().trim();
            String confirmation = binding.etConfirmDelete.getText().toString().trim();

            if (targetRoom.isEmpty()) {
                binding.tilDeleteRoomNumber.setError("Room number is required");
                return;
            } else {
                binding.tilDeleteRoomNumber.setError(null);
            }

            // Local user intent validation
            if (!confirmation.equals("DELETE")) {
                Toast.makeText(getContext(), "Please type 'DELETE' to confirm action", Toast.LENGTH_SHORT).show();
                return;
            }

            // LAUNCH THE POPUP DIALOG GATE HERE
            VerifyMpinDialogFragment dialog = new VerifyMpinDialogFragment();
            dialog.show(getChildFragmentManager(), "MpinVerifyDialog");
        });
    }

    @Override
    public void onMpinVerified(boolean isSuccess) {
        if (isSuccess) {
            executeRoomDeletionLogic();
        }
    }

    private void executeRoomDeletionLogic() {
        String targetRoom = binding.etDeleteRoomNumber.getText().toString().trim();

        // Pass the structural execution call down to the ViewModel
        viewModel.deleteRoom(targetRoom);
    }

    /**
     * Inspects current state properties inside the ViewModel and changes view markers.
     */
    private void renderUiState() {
        // 1. Manage layout click configurations during active database routines
        if (viewModel.isLoading()) {
            binding.btnDeleteRoom.setEnabled(false);
        } else {
            binding.btnDeleteRoom.setEnabled(true);
        }

        // 2. Safely capture data/business constraints and route them via system ErrorUtils
        if (viewModel.getErrorMessage() != null) {
            ErrorUtils.handleDatabaseException(
                    "Deletion Blocked",
                    new Exception(viewModel.getErrorMessage()),
                    ui
            );
        }

        // 3. Clear interaction inputs exclusively on successful transaction logs
        if (viewModel.isOperationSuccess()) {
            Toast.makeText(getContext(), "Room successfully dropped from systems", Toast.LENGTH_LONG).show();
            clearInputs();
        }
    }

    private void clearInputs() {
        binding.etDeleteRoomNumber.setText("");
        binding.etConfirmDelete.setText("");
        binding.tilDeleteRoomNumber.setError(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Disconnect listener to prevent leak routines or null view bindings when detached
        viewModel.setStateListener(null);
        binding = null;
    }
}