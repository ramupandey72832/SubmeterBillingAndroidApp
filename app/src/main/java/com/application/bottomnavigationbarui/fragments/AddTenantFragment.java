package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.bottomnavigationbarui.databinding.FragmentAddTenantBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.TenancyManagementService;
import com.github.devfrogora.service.dto.TenantDTO;
import com.github.devfrogora.service.impl.TenancyManagementServiceImpl;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class AddTenantFragment extends Fragment implements VerifyMpinDialogFragment.MpinVerificationListener {

    private UiHelper ui;
    private FragmentAddTenantBinding binding;
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
        binding = FragmentAddTenantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(this.getContext());
        startingDataPickerUI();

        demoTenant();


        binding.layoutAddTenant.btnAddTenant.setOnClickListener(view1 -> {
            String tenantName = binding.layoutAddTenant.etTenantName.getText().toString();
            String aadharNumber = binding.layoutAddTenant.etAadharNumber.getText().toString();
            String tenantMobile = binding.layoutAddTenant.etTenantMobile.getText().toString();
            String tenantParentMobile = binding.layoutAddTenant.etTenantParentMobile.getText().toString();
            String tenantAddress = binding.layoutAddTenant.etTenantAddress.getText().toString();
            String roomNumber = binding.layoutAddTenant.etRoomNumber.getText().toString();
            String startDate = binding.layoutAddTenant.etStartDate.getText().toString();

            if(tenantName.isEmpty() || aadharNumber.isEmpty() || tenantMobile.isEmpty() || tenantParentMobile.isEmpty() || tenantAddress.isEmpty() || roomNumber.isEmpty() || startDate.isEmpty())
            {
                return;
            }
            // Perform validation and save logic here

            // 3. LAUNCH THE POPUP DIALOG GATE HERE
            VerifyMpinDialogFragment dialog = new VerifyMpinDialogFragment();
            dialog.show(getChildFragmentManager(), "MpinVerifyDialog");

        });
    }

    void startingDataPickerUI(){
        TextInputEditText etStartDate = binding.layoutAddTenant.etStartDate;

// 1. Create the Material Date Picker instance
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Tenant Start Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

// 2. Open the picker when clicking the field
        etStartDate.setOnClickListener(v -> {
            datePicker.show(getParentFragmentManager(), "TENANT_DATE_PICKER");
        });

// 3. Format selected timestamp back to the text field
        datePicker.addOnPositiveButtonClickListener(selectionTimestamp -> {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateString = formatter.format(new Date(selectionTimestamp));
            etStartDate.setText(dateString);
        });
    }

    @Override
    public void onMpinVerified(boolean isSuccess) {
        if (isSuccess) {
            executeAddTenantLogic();
        }
    }

    private void executeAddTenantLogic() {
        String tenantName = binding.layoutAddTenant.etTenantName.getText().toString();
        String aadharNumber = binding.layoutAddTenant.etAadharNumber.getText().toString();
        String tenantMobile = binding.layoutAddTenant.etTenantMobile.getText().toString();
        String tenantParentMobile = binding.layoutAddTenant.etTenantParentMobile.getText().toString();
        String tenantAddress = binding.layoutAddTenant.etTenantAddress.getText().toString();
        String roomNumber = binding.layoutAddTenant.etRoomNumber.getText().toString();
        String startDate = binding.layoutAddTenant.etStartDate.getText().toString();


        TenancyManagementService tenancyManagementService = new TenancyManagementServiceImpl();
        try {
            TenantDTO tenantDTO = new TenantDTO();
            tenantDTO.setName(tenantName);
            tenantDTO.setAadharNumber(aadharNumber);
            tenantDTO.setPhoneNumber(tenantMobile);
            tenantDTO.setParentPhoneNumber(tenantParentMobile);
            tenantDTO.setAddress(tenantAddress);

            tenancyManagementService.addTenantWithTenancy(tenantDTO, roomNumber, startDate);
        } catch(Exception e){
            ErrorUtils.handleDatabaseException("Error initializing database", e, ui);
        }
    }

    void demoTenant(){
        binding.layoutAddTenant.etTenantName.setText("Himadari");
        binding.layoutAddTenant.etAadharNumber.setText("123456789012");
        binding.layoutAddTenant.etTenantMobile.setText("1234567890");
        binding.layoutAddTenant.etTenantParentMobile.setText("9876543210");
        binding.layoutAddTenant.etTenantAddress.setText("123 Main Street");
        binding.layoutAddTenant.etRoomNumber.setText("301");

    }

}