package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentGenerateBillBinding;
import com.application.bottomnavigationbarui.databinding.FragmentMeterReadingBinding;


public class MeterReadingFragment extends Fragment {

    FragmentMeterReadingBinding binding;

    public MeterReadingFragment() {
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
        binding = FragmentMeterReadingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.etRoomNumber.setText("");
        binding.etSubmeterSerialNumber.setText("");


        binding.btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
               String roomNumber = binding.etRoomNumber.getText().toString();
               String submeterSerialNumber = binding.etSubmeterSerialNumber.getText().toString();

               String tenantName ="";

              double currentMeterReading = Double.parseDouble(binding.etMeterReading.getText().toString());
              double fixedCharge = Double.parseDouble(binding.etFixedCharge.getText().toString());
              double ratePerUnit = Double.parseDouble(binding.etRatePerUnit.getText().toString());

                GenerateBillFragment.newInstance(roomNumber, tenantName , submeterSerialNumber, currentMeterReading, ratePerUnit, fixedCharge);
            }
        });

    }
}