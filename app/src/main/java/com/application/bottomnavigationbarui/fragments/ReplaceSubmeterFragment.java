package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentMeterReadingBinding;
import com.application.bottomnavigationbarui.databinding.FragmentReplaceSubmeterBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;

import java.sql.SQLException;

public class ReplaceSubmeterFragment extends Fragment implements VerifyMpinDialogFragment.MpinVerificationListener {
    private UiHelper ui;
    private FragmentReplaceSubmeterBinding binding;

    public ReplaceSubmeterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentReplaceSubmeterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ui = new UiHelper(requireActivity());


        binding.btnUpdate.setOnClickListener(view1 -> {
            String roomNumber = binding.etRoomNumber.getText().toString();
            String oldMeterSerialNumber = binding.etOldMeterNumber.getText().toString();
            String newMeterSerialNumber = binding.etNewSerialNumber.getText().toString();

            if(roomNumber.isEmpty() || oldMeterSerialNumber.isEmpty() || newMeterSerialNumber.isEmpty()){
                return;
            }

            // 3. LAUNCH THE POPUP DIALOG GATE HERE
            VerifyMpinDialogFragment dialog = new VerifyMpinDialogFragment();
            dialog.show(getChildFragmentManager(), "MpinVerifyDialog");
        });
    }

    @Override
    public void onMpinVerified(boolean isSuccess) {
        if (isSuccess) {
            // Proceed to execute your database code or API calls to delete the room!
            executeRoomDeletionLogic();

        }
    }

    private void executeRoomDeletionLogic() {
        try {
            String roomNumber = binding.etRoomNumber.getText().toString();
            String oldMeterSerialNumber = binding.etOldMeterNumber.getText().toString();
            String newMeterSerialNumber = binding.etNewSerialNumber.getText().toString();
            RoomMeterService roomMeterService = new RoomMeterServiceImpl();
            roomMeterService.updateSubmeter(roomNumber, oldMeterSerialNumber, newMeterSerialNumber);

        }catch (SQLException e) {
            ErrorUtils.handleDatabaseException("Error : ", e, ui);
        }
    }
}