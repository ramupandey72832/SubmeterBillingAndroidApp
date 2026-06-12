package com.application.bottomnavigationbarui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.application.bottomnavigationbarui.adapters.BillingBillsAdapter;
import com.application.bottomnavigationbarui.databinding.FragmentBillsBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.PdfGenerator;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.dto.BillDTO;
import com.github.devfrogora.service.dto.reports.BillReportDto;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
            if (fullBillList != null) {
                Collections.sort(fullBillList, Comparator.comparing(BillReportDto::getBillId).reversed());
            }

        } catch (SQLException e) {
            ErrorUtils.handleDatabaseException("Error : ", e, ui);
        }


        binding.recyclerViewBilling.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BillingBillsAdapter(fullBillList, new BillingBillsAdapter.OnBillClickListener() {
            @Override public void onReceiptClick(BillReportDto bill) {
                try {
                    PdfGenerator.generateBillPdf(requireContext(), bill);
                } catch (Exception e) {
                    ErrorUtils.handleDatabaseException("Error : ", e, ui);
                }

            }
            @Override public void onShareClick(BillReportDto bill) {
                try {
                    PdfGenerator.generateBillPdf(requireContext(), bill);

                    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    String filename = "Thermal_Bill_" + bill.getBillId() + ".pdf"; // match your generator output extension
                    File sharedFile = new File(downloadsDir, filename);

                    if (!sharedFile.exists()) {
                        Toast.makeText(requireContext(), "Error: Reference file could not be found to share.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 3. Convert the File reference to a secure Content URI
                    // Inside your adapter click listener, ensure this maps perfectly:
                    String authority = requireContext().getPackageName() + ".fileprovider";
                    Uri fileUri = FileProvider.getUriForFile(requireContext(), authority, sharedFile);

                    // 4. Construct the Standard Android Send Intent Channel
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/pdf"); // FIX: Changed from text/csv to application/pdf
                    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Billing Report: " + bill.getRoomNumber());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Attached is the monthly utility billing report ");

// --- CRITICAL PERMISSION SECURING STEPS ---
                    // A. Explicitly set ClipData so the Android Chooser knows exactly what file is being bound
                    shareIntent.setClipData(android.content.ClipData.newRawUri("", fileUri));

                    // B. Add the standard flags to the main intent stream
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    // ------------------------------------------

                    // 5. Create the Chooser Intent
                    Intent chooserIntent = Intent.createChooser(shareIntent, "Share Monthly PDF Via:");

                    // C. Grant read permissions directly to the chooser lifecycle container
                    chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    // 6. Fire the intent sheet UI
                    requireContext().startActivity(chooserIntent);

                } catch (Exception e) {
                    ErrorUtils.handleDatabaseException("Error : ", e, ui);
                }
            }
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