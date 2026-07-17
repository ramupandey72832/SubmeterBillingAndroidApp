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
import com.application.bottomnavigationbarui.DashboardFragment;
import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.RoomsFragment;
import com.application.bottomnavigationbarui.databinding.FragmentAddRoomBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.baselibrary.ui.utils.NavigationUtils;

import com.application.bottomnavigationbarui.validation.RoomValidator;
import com.application.bottomnavigationbarui.validation.ValidationResult;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.viewmodel.RoomMeterViewModel;

public class AddRoomFragment extends Fragment implements VerifyMpinDialogFragment.MpinVerificationListener {

    private FragmentAddRoomBinding binding;
    private ToastMessage ui;

    // Encapsulated pure business state holder
    private RoomMeterViewModel viewModel;

    public AddRoomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddRoomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ui = new ToastMessage(this.getContext());

        // Initialize Service with dependencies injected, and connect to ViewModel
        RoomMeterServiceImpl service = new RoomMeterServiceImpl(new MeterBillingServiceImpl());
        viewModel = new RoomMeterViewModel(service);



        // Set up the listener to track state transitions
        viewModel.setStateListener(new RoomMeterViewModel.StateListener() {
            @Override
            public void onStateChanged() {
                // Ensure UI mutations always run on Android's Main Thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState());
                }
            }
        });

        binding.layoutAddroom.btnBack.setOnClickListener(v -> {
            clearInputs();
            NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new DashboardFragment(),BottomNavActivityConstant.MAIN_CONTAINER);
        });


        binding.layoutAddroom.btnAddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String roomNumber = binding.layoutAddroom.etRoomNumber.getText().toString();
                String roomType = binding.layoutAddroom.etRoomType.getText().toString();
                String meterSerial = binding.layoutAddroom.etMeterSerial.getText().toString();
                String initialReading = binding.layoutAddroom.etInitialReading.getText().toString();

                ValidationResult validationResult = RoomValidator.validate(roomNumber, roomType, meterSerial, initialReading);
                if (!validationResult.isValid()) {
                    Toast.makeText(getContext(), validationResult.getJoinedErrors(), Toast.LENGTH_SHORT).show();
                    return;
                }

//                if (roomNumber.isEmpty() || roomType.isEmpty() || meterSerial.isEmpty() || initialReading.isEmpty()) {
//                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                String message = "Room: " + roomNumber + "\nRoom Type: " + roomType +
                        "\nMeter Serial: " + meterSerial + "\nInitial Reading: " + initialReading;
                // LAUNCH THE POPUP DIALOG GATE HERE
                VerifyMpinDialogFragment dialog =  VerifyMpinDialogFragment.newInstance(message);
                dialog.show(getChildFragmentManager(), "MpinVerifyDialog");
            }
        });

    }

    @Override
    public void onMpinVerified(boolean isSuccess) {
        if (isSuccess) {
            executeAddRoomLogic();
        }
    }

    private void executeAddRoomLogic() {
        String roomNumber = binding.layoutAddroom.etRoomNumber.getText().toString();
        String roomType = binding.layoutAddroom.etRoomType.getText().toString();
        String meterSerial = binding.layoutAddroom.etMeterSerial.getText().toString();
        String initialReading = binding.layoutAddroom.etInitialReading.getText().toString();

        // Hand over execution control directly to the ViewModel
        viewModel.createRoom(roomNumber, roomType, meterSerial,Double.parseDouble(initialReading));
    }

    /**
     * Evaluates state variables inside the ViewModel and changes layout visual markers accordingly
     */
    private void renderUiState() {
        // 1. Toggle progress bar state or update submission button loading visualization
        if (viewModel.isLoading()) {
            // e.g., binding.layoutAddroom.progressBar.setVisibility(View.VISIBLE);
            binding.layoutAddroom.btnAddRoom.setEnabled(false);
        } else {
            // e.g., binding.layoutAddroom.progressBar.setVisibility(View.GONE);
            binding.layoutAddroom.btnAddRoom.setEnabled(true);
        }

        // 2. Intercept exceptions or database constraints and render them cleanly using ErrorUtils/UiHelper
        if (viewModel.getErrorMessage() != null) {
            // Employs your system logic wrapper to show error diagnostics to the user
            ErrorUtils.handleDatabaseException("Registration Failed", new Exception(viewModel.getErrorMessage()), ui);
        }

        // 3. Clear data input layouts only on successful processing confirmation
        if (viewModel.isOperationSuccess()) {
            NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new RoomsFragment(), BottomNavActivityConstant.MAIN_CONTAINER);
            Toast.makeText(getContext(), "Room added successfully", Toast.LENGTH_SHORT).show();
            clearInputs();
        }
    }

    private void clearInputs() {
        binding.layoutAddroom.etRoomNumber.setText("");
        binding.layoutAddroom.etRoomType.setText("");
        binding.layoutAddroom.etMeterSerial.setText("");
        binding.layoutAddroom.etInitialReading.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent background updates from causing null layouts when switching fragments
        viewModel.setStateListener(null);
        binding = null;
    }
}