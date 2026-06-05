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
import com.application.bottomnavigationbarui.fragments.SetupMpinFragment;
import com.application.bottomnavigationbarui.utils.NavigationUtils;
import com.github.devfrogora.service.dto.reports.BillReportDto;

import java.util.ArrayList;
import java.util.List;


public class DashboardFragment extends Fragment {
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

        // 1. Configure LayoutManager for the RecyclerView
        binding.rvPendingBills.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. Mock a list of data mixing Paid and Unpaid states
        List<BillReportDto> demoBills = new ArrayList<>();
        demoBills.add(new BillReportDto(1, "105", "Alex", "01 Apr 2026",2.5, "YES"));
        demoBills.add(new BillReportDto(2, "104", "Sarah", "01 Apr 2026",8.5, "NO"));
        demoBills.add(new BillReportDto(3, "103", "Alex", "01 Mar 2026", 7.5, "YES"));
        demoBills.add(new BillReportDto(4, "102",  "Alex", "01 Mar 2026",5 ,"NO"));
        demoBills.add(new BillReportDto(5, "101",  "Ramu", "01 Mar 2026", 6,"YES"));
        demoBills.add(new BillReportDto(6, "100",  "Alex", "01 Mar 2026", 10,"NO"));
        demoBills.add(new BillReportDto(7, "109",  "Ramu", "01 Mar 2026", 15,"Yes" ));


        // 3. Initialize your updated adapter with the sample list
        DashboardBillsAdapter adapter = new DashboardBillsAdapter(demoBills);

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

        binding.dashboardSection.addTenant.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Fragment targetFragment = new AddTenantFragment();
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), targetFragment);
            }
        });

        binding.dashboardSection.mpin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Fragment targetFragment = new SetupMpinFragment();
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), targetFragment);
            }
        });

        binding.dashboardSection.deleteRoom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Fragment targetFragment = new DeleteRoomFragment();
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