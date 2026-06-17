package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentEditRoomAssetBinding;
import com.github.devfrogora.service.dto.TenantDTO;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.impl.TenancyManagementServiceImpl;
import com.github.devfrogora.service.viewmodel.TenancyViewModel;

public class EditRoomAssetFragment extends Fragment {

    private static final String ARG_ROOM_NUMBER = "target_room_number";
    private FragmentEditRoomAssetBinding binding;
    private TenancyViewModel tenancyViewModel;
    private String passedRoomNumber;

    /**
     * Clean static instantiator passing argument metrics cleanly down the fragment loop stack
     */
    public static EditRoomAssetFragment newInstance(String roomNumber) {
        EditRoomAssetFragment fragment = new EditRoomAssetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_NUMBER, roomNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            passedRoomNumber = getArguments().getString(ARG_ROOM_NUMBER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditRoomAssetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        // Instantiate isolated viewmodel dependencies
        RoomMeterServiceImpl roomService = new RoomMeterServiceImpl(new MeterBillingServiceImpl());
        TenancyManagementServiceImpl tenancyService = new TenancyManagementServiceImpl();

        tenancyViewModel = new TenancyViewModel(tenancyService, roomService, new MeterBillingServiceImpl());
        tenancyViewModel.setStateListener(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::renderAssetState);
            }
        });

        // Trigger asynchronous dataset load sweep
        tenancyViewModel.loadEntireRoomAssetConfiguration(passedRoomNumber);

        // Cancel / Back actions
        binding.btnBack.setOnClickListener(v -> closeFragment());

        binding.btnSaveAssetChanges.setOnClickListener(v -> {
            String updatedRoomNum = binding.etEditRoomNumber.getText().toString().trim();
            String roomType = binding.etEditRoomType.getText().toString().trim();
            String meterSerial = binding.etEditMeterSerial.getText().toString().trim();
            String rawReading = binding.etEditInitialReading.getText().toString().trim();

            if (updatedRoomNum.isEmpty() || meterSerial.isEmpty() || rawReading.isEmpty()) {
                Toast.makeText(getContext(), "Room, Meter Serial, and Initial Reading fields cannot be blank.", Toast.LENGTH_SHORT).show();
                return;
            }

            double readingValue = Double.parseDouble(rawReading);
            String tName = binding.etEditTenantName.getText().toString().trim();
            String tMobile = binding.etEditTenantMobile.getText().toString().trim();
            String tAadhaar = binding.etEditTenantAadhaar.getText().toString().trim();
            String tAddress = binding.etEditTenantAddress.getText().toString().trim();

            // Push configuration payload parameters purely down the transaction pipeline path
            tenancyViewModel.saveAllAssetChanges(
                    updatedRoomNum, roomType, meterSerial, readingValue,
                    tName, tMobile, tAadhaar, tAddress
            );
        });
    }

// Inside EditRoomAssetFragment.java

    private void renderAssetState() {
        if (tenancyViewModel.isLoading()) {
            return;
        }

        if (tenancyViewModel.getFeedbackMessage() != null) {
            Toast.makeText(getContext(), tenancyViewModel.getFeedbackMessage(), Toast.LENGTH_SHORT).show();
        }

        // FIX: Read clean DTO structures safely without cross-module visibility breaks
        if (tenancyViewModel.isOperationSuccess() && tenancyViewModel.getLoadedRoomDto() != null) {
            var room = tenancyViewModel.getLoadedRoomDto(); // Uses RoomDTO
            var submeter = tenancyViewModel.getLoadedSubmeterDto(); // Uses SubmeterDTO
            var tenant = tenancyViewModel.getLoadedTenantDto(); // Uses TenantDTO

            binding.etEditRoomNumber.setText(room.getRoomNumber());
            binding.etEditRoomType.setText(room.getRoomType());

            if (submeter != null) {
                binding.etEditMeterSerial.setText(submeter.getMeterSerialNumber());
                binding.etEditInitialReading.setText(String.valueOf(submeter.getInitialReading()));
            }

            if (tenant != null) {
                binding.cvTenantSection.setVisibility(View.VISIBLE);
                binding.etEditTenantName.setText(tenant.getName());
                binding.etEditTenantMobile.setText(tenant.getPhoneNumber());
                binding.etEditTenantAadhaar.setText(tenant.getAadharNumber());
                binding.etEditTenantAddress.setText(tenant.getAddress());
            } else {
                binding.cvTenantSection.setVisibility(View.GONE);
            }
        }

        if (tenancyViewModel.isOperationSuccess() && tenancyViewModel.getLoadedRoomDto() == null) {
            closeFragment();
        }
    }

    private void closeFragment() {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack(); // Clean exit animation hook
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tenancyViewModel.setStateListener(null);
        binding = null;
    }
}