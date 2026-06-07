package com.application.bottomnavigationbarui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.bottomnavigationbarui.adapters.BillingBillsAdapter;
import com.application.bottomnavigationbarui.databinding.FragmentBillsBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.PdfGenerator;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.dto.BillDTO;
import com.github.devfrogora.service.dto.reports.BillReportDto;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class BillsFragment extends Fragment {
    private UiHelper ui;
    private FragmentBillsBinding binding;

    private BillingBillsAdapter adapter;
    private List<BillReportDto> fullBillList; // Holds original data

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBillsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(requireContext());

        MeterBillingService meterBillingService = new MeterBillingServiceImpl();
        try {
            fullBillList =  meterBillingService.getAllBillsReport();
        } catch (SQLException e) {
            ErrorUtils.handleDatabaseException("Error : ", e, ui);
        }

//        // Create dummy list data
//        fullBillList = new ArrayList<>();
//        fullBillList.add(new BillReportDto(1, "105", "Alex", "01 Apr 2026",2.5, "YES"));

        binding.recyclerViewBilling.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BillingBillsAdapter(fullBillList, new BillingBillsAdapter.OnBillClickListener() {
            @Override public void onReceiptClick(BillReportDto bill) {
                try {
                    BillDTO billDTO = meterBillingService.getBillById(bill.getBillId());
                    PdfGenerator.generateBillPdf(requireContext(), bill);
                } catch (Exception e) {
                    ErrorUtils.handleDatabaseException("Error : ", e, ui);
                }

            }
            @Override public void onShareClick(BillReportDto bill) {}
        });


        binding.recyclerViewBilling.setAdapter(adapter);

        // Add real-time text listener to search bar
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }

    // Filter processing logic
    private void filter(String text) {
        List<BillReportDto> filteredList = new ArrayList<>();

        for (BillReportDto item : fullBillList) {
            // Check if input matches room number or name (case-insensitive)
            if (item.getTenantName().toLowerCase().contains(text.toLowerCase()) ||
                    item.getRoomNumber().contains(text)) {
                filteredList.add(item);
            }
        }

        // Pass the updated list to the adapter
        adapter.filterList(filteredList);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevent memory leaks
    }

}