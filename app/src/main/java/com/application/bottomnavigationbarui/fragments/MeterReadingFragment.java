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
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.NavigationUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.TenancyManagementService;
import com.github.devfrogora.service.dto.TenancyDTO;
import com.github.devfrogora.service.dto.TenantDTO;
import com.github.devfrogora.service.dto.reports.SubmeterDTO;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.impl.TenancyManagementServiceImpl;

import java.sql.SQLException;
import java.util.Optional;


public class MeterReadingFragment extends Fragment {
    private UiHelper ui;
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
    private String tenantName;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(this.getContext());
        binding.etSubmeterSerialNumber.setEnabled(false);
        binding.etSubmeterSerialNumber.setEnabled(false);
        binding.etPreviousMeterReading.setEnabled(false);

        RoomMeterService roomMeterService = new RoomMeterServiceImpl();
        MeterBillingService meterBillingService = new MeterBillingServiceImpl();
        TenancyManagementService tenancyManagementService = new TenancyManagementServiceImpl();
        binding.btnCheck.setOnClickListener(view1 -> {
            String roomNumber = binding.etRoomNumber.getText().toString();
            if(roomNumber.isEmpty()){
                return;
            }
            try {
                SubmeterDTO submeterDTO = roomMeterService.getSubmeterByRoomNumber(roomNumber);
                binding.etSubmeterSerialNumber.setText(submeterDTO.getMeterSerialNumber());
                TenancyDTO tenancyDTO = tenancyManagementService.findActiveTenancyByRoomNumber(roomNumber);
                tenancyDTO.getTenantAaddhar();

                Optional<TenantDTO> tenantDTO = tenancyManagementService.findTenantByAadhar(tenancyDTO.getTenantAaddhar());
                if(tenantDTO.isEmpty()){
                    return;
                }

                tenantName = tenantDTO.get().getName();
                double findPreviousReading = meterBillingService.getLatestReading(submeterDTO.getMeterSerialNumber());

                binding.etPreviousMeterReading.setText(Double.toString(findPreviousReading));
            } catch (Exception e) {
                ErrorUtils.handleDatabaseException("Error : ", e, ui);
            }

        });
        binding.etSubmeterSerialNumber.setText("");


        binding.btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
               String roomNumber = binding.etRoomNumber.getText().toString();
               String submeterSerialNumber = binding.etSubmeterSerialNumber.getText().toString();


              double currentMeterReading = Double.parseDouble(binding.etCurrentMeterReading.getText().toString());
              double previousMeterReading = Double.parseDouble(binding.etPreviousMeterReading.getText().toString());
              double fixedCharge = Double.parseDouble(binding.etFixedCharge.getText().toString());
              double ratePerUnit = Double.parseDouble(binding.etRatePerUnit.getText().toString());

              if(roomNumber.isEmpty() || submeterSerialNumber.isEmpty() || currentMeterReading == 0 || fixedCharge == 0 || ratePerUnit == 0){
                  return;
              }

                Fragment targetFragment =  GenerateBillFragment.newInstance(roomNumber, tenantName , submeterSerialNumber, currentMeterReading,
                        previousMeterReading,  ratePerUnit, fixedCharge);
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), targetFragment);

            }
        });

    }
}