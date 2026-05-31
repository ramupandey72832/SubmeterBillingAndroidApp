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
import com.application.bottomnavigationbarui.databinding.FragmentAddTenantBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class AddTenantFragment extends Fragment {

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

        startingDataPickerUI();
    }

    void startingDataPickerUI(){
        TextInputEditText etStartDate = binding.layoutAddTenant.etStartDate;

// 1. Create the Material Date Picker instance
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Lease Start Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

// 2. Open the picker when clicking the field
        etStartDate.setOnClickListener(v -> {
            datePicker.show(getParentFragmentManager(), "LEASE_DATE_PICKER");
        });

// 3. Format selected timestamp back to the text field
        datePicker.addOnPositiveButtonClickListener(selectionTimestamp -> {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateString = formatter.format(new Date(selectionTimestamp));
            etStartDate.setText(dateString);
        });
    }
}