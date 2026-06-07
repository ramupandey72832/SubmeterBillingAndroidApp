package com.application.bottomnavigationbarui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.bottomnavigationbarui.databinding.FragmentReportsBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.PdfGenerator;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.dto.reports.BillReportDto;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ReportsFragment extends Fragment {

    private UiHelper ui;
    FragmentReportsBinding binding;

    public ReportsFragment() {
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
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(getContext());

        binding.btnGenerateLatestMonthlyBillsReport.setOnClickListener(view1 -> {
            RoomMeterService roomMeterService = new RoomMeterServiceImpl();
            MeterBillingService meterBillingService = new MeterBillingServiceImpl();
            List<RoomRegistryDto> rooms = roomMeterService.getAllRoomReport();
            List<BillReportDto> bills = new ArrayList<>();
            rooms.forEach(room -> {
                try {
                    bills.add( meterBillingService.getLatestBill(room.getRoomNumber()) );
                } catch (Exception e) {
                    ErrorUtils.handleDatabaseException("error: ",e,ui);
                }
            });
            PdfGenerator.generateMultipleBillsPdf(getContext(), bills);
        });
    }
}