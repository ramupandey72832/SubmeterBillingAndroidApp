package com.application.bottomnavigationbarui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.bottomnavigationbarui.adapters.DashboardBillsAdapter;
import com.application.bottomnavigationbarui.databinding.FragmentDashboardBinding;
import com.application.bottomnavigationbarui.fragments.AddRoomFragment;
import com.application.bottomnavigationbarui.fragments.AddTenantFragment;
import com.application.bottomnavigationbarui.fragments.DeleteRoomFragment;
import com.application.bottomnavigationbarui.fragments.DeleteTenantFragment;
import com.application.bottomnavigationbarui.fragments.EndRoomTenancyFragment;
import com.application.bottomnavigationbarui.fragments.MeterReadingFragment;
import com.application.bottomnavigationbarui.fragments.ReplaceSubmeterFragment;
import com.application.bottomnavigationbarui.fragments.SetupMpinFragment;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.NavigationUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.dto.reports.BillReportDto;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DashboardFragment extends Fragment {
    private UiHelper ui;
    private FragmentDashboardBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflating Fragment View Binding
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(getContext());
        // 1. Configure LayoutManager for the RecyclerView
        binding.rvPendingBills.setLayoutManager(new LinearLayoutManager(getContext()));

        MeterBillingService meterBillingService = new MeterBillingServiceImpl();
        List<BillReportDto> allBills = new ArrayList<>();
        List<BillReportDto> pendingBills = new ArrayList<>();
        double totalUnits = 0;
        double totalRevenue = 0;
        try {
            allBills = meterBillingService.getAllBillsReport();

            if (allBills != null) {
                for (BillReportDto bill : allBills) {
                    totalUnits += (bill.getCurrentReading() - bill.getPreviousReading());
                    totalRevenue += bill.getTotalAmount();
                }
            }
            pendingBills = meterBillingService.getAllPendingBills();
        } catch (Exception e) {
            ErrorUtils.handleDatabaseException("Error fetching pending bills", e, ui);
        }

        binding.totalUnits.setText(String.format("%.2f", totalUnits));
        binding.totalBills.setText(String.format("%d", allBills.size()));
        binding.totalRevenue.setText(String.format("%.2f", totalRevenue));

        DashboardBillsAdapter adapter = new DashboardBillsAdapter(pendingBills);
        // 4. Bind the adapter to your RecyclerView
        binding.rvPendingBills.setAdapter(adapter);

        binding.dashboardSection.addRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the click event
                Fragment targetFragment = new AddRoomFragment();
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), targetFragment);
            }
        });

        binding.dashboardSection.deleteRoom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Fragment targetFragment = new DeleteRoomFragment();
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), targetFragment);
            }
        });

        binding.dashboardSection.addTenant.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Fragment targetFragment = new AddTenantFragment();
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), targetFragment);
            }
        });

        binding.dashboardSection.deleteTenant.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Fragment targetFragment = new DeleteTenantFragment();
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), targetFragment);
            }
        });

        binding.dashboardSection.replaceSubmeter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Fragment targetFragment = new ReplaceSubmeterFragment();
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), targetFragment);
            }
        });

        binding.dashboardSection.endTenancy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Fragment targetFragment = new EndRoomTenancyFragment();
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), targetFragment);
            }
        });

        binding.btnScanMeter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Fragment targetFragment = new MeterReadingFragment();
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), targetFragment);
            }
        });




        binding.dashboardSection.mpin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Fragment targetFragment = new SetupMpinFragment();
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), targetFragment);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevent memory leaks
    }
}