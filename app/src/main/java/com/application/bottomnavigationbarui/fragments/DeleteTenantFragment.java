package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentDeleteTenantBinding;
import com.application.bottomnavigationbarui.databinding.FragmentMeterReadingBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.TenancyManagementService;
import com.github.devfrogora.service.dto.RoomDTO;
import com.github.devfrogora.service.dto.TenancyDTO;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.impl.TenancyManagementServiceImpl;

import java.sql.SQLException;
import java.util.Optional;


public class DeleteTenantFragment extends Fragment {
    private UiHelper ui;

    FragmentDeleteTenantBinding binding;

    public DeleteTenantFragment() {
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
        binding = FragmentDeleteTenantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(this.getContext());

        binding.btnDeleteTenant.setOnClickListener(view1 -> {

            String tenantAadhaarNumber = binding.etTenantAadhaarNumber.getText().toString();
            String tenantRoomNumber = binding.etTenantRoomNumber.getText().toString();


            TenancyManagementService tenancyManagementService = new TenancyManagementServiceImpl();
            RoomMeterService roomMeterService = new RoomMeterServiceImpl();
            try {
                boolean isRoomExist = roomMeterService.isRoomExist(tenantRoomNumber);
                if(isRoomExist){

                   TenancyDTO tenancyDTO = tenancyManagementService.findActiveTenancyByTenantAadhar(tenantAadhaarNumber);

                       if(tenancyDTO != null){
                           Toast.makeText(getContext(), "Tenant is using room " + tenancyDTO.getRoomNumber(), Toast.LENGTH_SHORT).show();
                       }else{
                           if(tenancyDTO.getRoomNumber().equals(tenantRoomNumber)){
                               tenancyManagementService.deleteTenantIfNoActiveTenancy(tenantAadhaarNumber);
                               Toast.makeText(getContext(), "Tenant successfully removed", Toast.LENGTH_SHORT).show();
                           }else{
                               Toast.makeText(getContext(), "Tenant have Active Tenancy but not this room: " + tenancyDTO.getRoomNumber(), Toast.LENGTH_SHORT).show();
                           }
                       }
                }
            } catch(Exception e){
                ErrorUtils.handleDatabaseException("Error initializing database", e, ui);
            }
        });

        binding.btnCancel.setOnClickListener(view1 -> {
            binding.etTenantAadhaarNumber.setText("");
            binding.etTenantRoomNumber.setText("");
        });
    }
}