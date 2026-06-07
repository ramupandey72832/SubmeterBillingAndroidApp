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
import com.application.bottomnavigationbarui.databinding.FragmentEndRoomTenancyBinding;
import com.application.bottomnavigationbarui.databinding.FragmentMeterReadingBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.TenancyManagementService;
import com.github.devfrogora.service.dto.TenancyDTO;
import com.github.devfrogora.service.dto.TenantDTO;
import com.github.devfrogora.service.dto.reports.MeterReadingDTO;
import com.github.devfrogora.service.dto.reports.SubmeterDTO;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.impl.TenancyManagementServiceImpl;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * How to handle this in your Backend/Database:
 * To properly execute this function without losing data, do not run a DELETE query. Instead, update your database logic using these guidelines:
 *
 * Step 1: Update the Tenant Status: Add an is_active boolean or a status text column to your Tenant table. When this form is submitted, switch their status from Active to Archived (or Past Tenant).
 *
 * Step 2: Unlink the Room: Clear the tenant_id column from that particular Room row so the room shows up as vacant and ready for a new occupant.
 *
 * Step 3: Save the Move-Out Date: Record the date from etMoveOutDate into a lease_end_date column in your history files for future financial reporting.
 *
 * Step 4: Use a Date Picker: In your Java/Kotlin activity code, attach a MaterialDatePicker to the etMoveOutDate input field. Setting android:focusable="false" ensures the keyboard won't pop up and annoy the user when they tap the date field to open the calendar picker.
 */
public class EndRoomTenancyFragment extends Fragment  implements VerifyMpinDialogFragment.MpinVerificationListener {

    FragmentEndRoomTenancyBinding binding;
    private UiHelper ui;

    public EndRoomTenancyFragment() {
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
        binding = FragmentEndRoomTenancyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(this.getContext());
        startingDataPickerUI();

        binding.llTenantDetailsContainer.setVisibility(View.GONE);
        // TODO Show Caution to user: please check latest Reading Before Terminating Tenancy ;
       // and Remove Final Reading from here


        binding.btnCheck.setOnClickListener(view1 -> {
            String roomNumber =  binding.etRoomNumber.getText().toString();
            if(roomNumber.isEmpty()){
                //TODO show empty error

                return;
            }
            TenancyManagementService tenancyManagementService = new TenancyManagementServiceImpl();
            try {
                binding.llTenantDetailsContainer.setVisibility(View.VISIBLE);
                TenancyDTO tenancyDTO = tenancyManagementService.findActiveTenancyByRoomNumber(roomNumber);
                if(tenancyDTO == null){
                    return;
                }
                tenancyManagementService.findTenantByAadhar(tenancyDTO.getTenantAaddhar()).ifPresentOrElse(tenantDTO -> {
                            binding.tvTenantName.setText(tenantDTO.getName());
                            binding.tvAadhaarNumber.setText(tenantDTO.getAadharNumber());
                            binding.tvMobileNumber.setText(tenantDTO.getPhoneNumber());
                        }, () -> {
                        // show error , not tenant is present in that room
                    Toast.makeText(getContext(), "Tenant not found", Toast.LENGTH_SHORT).show();

                });
                MeterBillingService meterReadingService = new MeterBillingServiceImpl();
                RoomMeterService roomMeterService = new RoomMeterServiceImpl();
                SubmeterDTO submeterDTO= roomMeterService.getSubmeterByRoomNumber(roomNumber);
                double latestReading = meterReadingService.getLatestReading(submeterDTO.getMeterSerialNumber());

                binding.tvLastMeterReading.setText(String.valueOf(latestReading));
            }catch (Exception e){}
        });


        binding.btnEndTenancy.setOnClickListener(view1 -> {
            String roomNumber =  binding.etRoomNumber.getText().toString();
            String endDate = binding.etEndDate.getText().toString();
            if(roomNumber.isEmpty() || endDate.isEmpty()){
                return;
            }
//            binding.etFinalReading.getText().toString();

            // 3. LAUNCH THE POPUP DIALOG GATE HERE
            VerifyMpinDialogFragment dialog = new VerifyMpinDialogFragment();

            // Crucial: UsegetChildFragmentManager() because it is being popped up from WITHIN a fragment
            dialog.show(getChildFragmentManager(), "MpinVerifyDialog");


        });
    }

    @Override
    public void onMpinVerified(boolean isSuccess) {
        if (isSuccess) {
            TenancyManagementService tenancyManagementService = new TenancyManagementServiceImpl();
            try {

                String roomNumber =  binding.etRoomNumber.getText().toString();
                String endDate = binding.etEndDate.getText().toString();
                tenancyManagementService.terminateTenancyOfRoom(roomNumber,endDate);

            } catch (Exception e) {
                ErrorUtils.handleDatabaseException("Error : ", e, ui);
            }
        }
    }

    void startingDataPickerUI(){
        TextInputEditText etStartDate = binding.etEndDate;

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
}