// File: app/.../fragments/BillsFragment.java
package com.application.bottomnavigationbarui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.application.baselibrary.media.MediaStoreHelper;
import com.application.baselibrary.ui.utils.ToastMessage;
import com.application.bottomnavigationbarui.adapters.BillingBillsAdapter;
import com.application.bottomnavigationbarui.databinding.FragmentBillsBinding;
import com.application.bottomnavigationbarui.fragments.EditBillFragment;
import com.application.bottomnavigationbarui.utils.BillPdfEngine;
import com.application.bottomnavigationbarui.utils.ErrorUtils;

import com.github.devfrogora.service.dto.reports.BillReportDto;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.viewmodel.BillingViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BillsFragment extends Fragment {

    private ToastMessage ui;
    private FragmentBillsBinding binding;

    private BillingBillsAdapter adapter;
    private List<BillReportDto> displayedBillList; // Linked directly inside the layout view adapter

    // Decoupled Business Core presentation manager
    private BillingViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBillsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new ToastMessage(requireContext());

        // Initialize view display collection mapping pointers
        displayedBillList = new ArrayList<>();

        // Establish the Layout Manager mechanics first
        binding.recyclerViewBilling.setLayoutManager(new LinearLayoutManager(getContext()));

        // Wire layout interactions up to the matching events handler adapter
        adapter = new BillingBillsAdapter(displayedBillList, new BillingBillsAdapter.OnBillClickListener() {
            @Override
            public void onReceiptClick(BillReportDto bill) {
                try {
                    String fileName = "Thermal_Bill_" + bill.getBillId() + ".pdf";

                    // 1. Query if it exists already
                    android.net.Uri fileUri = MediaStoreHelper.checkDownloadFileExistence(requireContext(), fileName);

                    if (fileUri != null) {
                        // File exists already! Open it directly
                        android.widget.Toast.makeText(requireContext(), "Opening existing bill slip...", android.widget.Toast.LENGTH_SHORT).show();
                        viewGeneratedBill(fileUri);
                    } else {
                        // 2. File does not exist. Create it and grab the exact returned Uri
                        android.net.Uri freshUri = BillPdfEngine.generateSingleBillSlip(requireContext(), bill, fileName);

                        if (freshUri != null) {
                            viewGeneratedBill(freshUri);
                        } else {
                            android.widget.Toast.makeText(requireContext(), "Error initializing PDF generation file stream.", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    ErrorUtils.handleDatabaseException("PDF Generation Failure", e, ui);
                }
            }

            @Override
            public void onShareClick(BillReportDto bill) {
                try {
                    String filename = "Thermal_Bill_" + bill.getBillId() + ".pdf";
                    BillPdfEngine.generateSingleBillSlip(requireContext(), bill,filename);

                    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File sharedFile = new File(downloadsDir, filename);

                    if (!sharedFile.exists()) {
                        Toast.makeText(requireContext(), "Error: Reference file could not be found to share.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String authority = requireContext().getPackageName() + ".fileprovider";
                    Uri fileUri = FileProvider.getUriForFile(requireContext(), authority, sharedFile);

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/pdf");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Billing Report: " + bill.getRoomNumber());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Attached is the monthly utility billing report");

                    shareIntent.setClipData(android.content.ClipData.newRawUri("", fileUri));
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    Intent chooserIntent = Intent.createChooser(shareIntent, "Share Monthly PDF Via:");
                    chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    requireContext().startActivity(chooserIntent);

                } catch (Exception e) {
                    ErrorUtils.handleDatabaseException("Share Command Failure", e, ui);
                }
            }

            @Override
            public void onBillEditClick(BillReportDto bill) {
                // Replaced inline placeholder comment with structured safe transaction navigation sequence
                Fragment editBillFragment = EditBillFragment.newInstance(bill);

                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out,
                                android.R.anim.slide_in_left,
                                android.R.anim.slide_out_right
                        )
                        .replace(R.id.frame_layout, editBillFragment) // Maps over layout screen viewport
                        .addToBackStack(null) // Restores back stack button navigation traces
                        .commit();
            }
        });

        binding.recyclerViewBilling.setAdapter(adapter);

        // Connect the decoupled ViewModel with dependencies explicitly injected
        viewModel = new BillingViewModel(new MeterBillingServiceImpl());

        // Attach listener handles driving runtime state updates securely
        viewModel.setStateListener(new BillingViewModel.StateListener() {
            @Override
            public void onStateChanged() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState());
                }
            }
        });

        // Add real-time text listener to search layout bars
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Pass filtration actions straight to the business layer logic holder
                viewModel.filterBills(s.toString());
            }
        });

        // Trigger asynchronous retrieval loading sweep
        viewModel.fetchBillsReport();
    }

    /**
     * Single source of truth analyzing primitive updates inside the ViewModel and managing view sets.
     */
    private void renderUiState() {
        // 1. Manage interactive loading screen progress states
        if (viewModel.isLoading()) {
            // binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            // binding.progressBar.setVisibility(View.GONE);
        }

        // 2. Intercept and project failures cleanly through central ErrorUtils mapping layers
        if (viewModel.getErrorMessage() != null) {
            ErrorUtils.handleDatabaseException(
                    "Query Encountered Failures",
                    new Exception(viewModel.getErrorMessage()),
                    ui
            );
        }

        // 3. Sync matching items to adapter cache elements natively upon success flag triggers
        if (viewModel.isOperationSuccess() || viewModel.getFilteredBillList() != null) {
            displayedBillList.clear();
            displayedBillList.addAll(viewModel.getFilteredBillList());
            adapter.notifyDataSetChanged();
        }
    }

    private void viewGeneratedBill(@NonNull android.net.Uri fileUri) {
        try {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");

            // CRUCIAL: Grant temporary read permissions to whatever external PDF reader handles this intent
            intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivity(android.content.Intent.createChooser(intent, "Open Bill via"));
        } catch (android.content.ActivityNotFoundException e) {
            android.widget.Toast.makeText(requireContext(), "No PDF viewer app found on device", android.widget.Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.setStateListener(null); // Evict active listener callbacks to lock down context leaks
        binding = null;
    }
}