package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentAddRoomBinding;
import com.application.bottomnavigationbarui.databinding.FragmentBillsBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;


public class AddRoomFragment extends Fragment {

    private FragmentAddRoomBinding binding;
    private UiHelper ui;


    public AddRoomFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddRoomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ui = new UiHelper(this.getContext());
        binding.layoutAddroom.btnAddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String roomNumber = binding.layoutAddroom.etRoomNumber.getText().toString();
                String roomType = binding.layoutAddroom.etRoomType.getText().toString();
                String meterSerial = binding.layoutAddroom.etMeterSerial.getText().toString();
                String initialReading = binding.layoutAddroom.etInitialReading.getText().toString();

                // Perform validation and save logic here

                RoomMeterService roomMeterService = new RoomMeterServiceImpl();
                try {
                  roomMeterService.addRoomWithMeter(roomNumber, roomType, meterSerial, Double.parseDouble(initialReading));
                } catch(Exception e){
                    ErrorUtils.handleDatabaseException("Error initializing database", e, ui);
                }
            }
        });

        binding.layoutAddroom.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.layoutAddroom.etRoomNumber.setText("");
                binding.layoutAddroom.etRoomType.setText("");
                binding.layoutAddroom.etMeterSerial.setText("");
                binding.layoutAddroom.etInitialReading.setText("");
            }
        });
    }
}