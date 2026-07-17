// File: app/.../fragments/DashboardFragment.java
package com.application.bottomnavigationbarui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.application.baselibrary.ui.utils.ToastMessage;
import com.application.bottomnavigationbarui.adapters.DashboardBillsAdapter;
import com.application.bottomnavigationbarui.databinding.FragmentDashboardBinding;
import com.application.bottomnavigationbarui.fragments.AddRoomFragment;
import com.application.bottomnavigationbarui.fragments.AddTenantFragment;
import com.application.bottomnavigationbarui.fragments.DatabaseConfigurationFragment;
import com.application.bottomnavigationbarui.fragments.DatabaseInspectorFragment;
import com.application.bottomnavigationbarui.fragments.DeleteRoomFragment;
import com.application.bottomnavigationbarui.fragments.DeleteTenantFragment;
import com.application.bottomnavigationbarui.fragments.EndRoomTenancyFragment;
import com.application.bottomnavigationbarui.fragments.ReplaceSubmeterFragment;
import com.application.bottomnavigationbarui.fragments.SetupMpinFragment;
import com.application.bottomnavigationbarui.fragments.VerifyMpinDialogFragment;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.baselibrary.ui.utils.NavigationUtils;

import com.github.devfrogora.service.dto.reports.BillReportDto;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.viewmodel.DashboardViewModel;

import com.application.android_ui_templete1.R;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment implements VerifyMpinDialogFragment.MpinVerificationListener {

    private ToastMessage ui;
    private FragmentDashboardBinding binding;
    private DashboardBillsAdapter billAdapter;
    private List<BillReportDto> pendingBills;
    private BillReportDto selectedBillForProcessing;

    // Decoupled Business Core presentation layer coordinator
    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new ToastMessage(getContext());

        binding.tvActiveDatabaseName.setText("Connected: " + DatabaseConfigurationFragment.getDbUrl(getContext()));

        pendingBills = new ArrayList<>();
        binding.rvPendingBills.setLayoutManager(new LinearLayoutManager(getContext()));

        billAdapter = new DashboardBillsAdapter(pendingBills, new DashboardBillsAdapter.OnBillStatusClickListener() {
            @Override
            public void onStatusBadgeClicked(BillReportDto bill) {
                selectedBillForProcessing = bill;
                String message = "Bill No: " + bill.getBillId() + "  Room: " + bill.getRoomNumber() +
                        " Tenant: " + bill.getTenantName() + " Amount: ₹" + bill.getTotalAmount();
                VerifyMpinDialogFragment dialog = VerifyMpinDialogFragment.newInstance(message);
                dialog.show(getChildFragmentManager(), "MpinVerifyDialog");
            }
        });
        binding.rvPendingBills.setAdapter(billAdapter);

        // Instantiate isolated ViewModel with operational dependencies explicitly injected
        viewModel = new DashboardViewModel(new MeterBillingServiceImpl());

        // Secure state modification triggers across context layers
        viewModel.setStateListener(new DashboardViewModel.StateListener() {
            @Override
            public void onStateChanged() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState());
                }
            }
        });

        // Click listeners driving routing destinations
        binding.dashboardSection.addRoom.setOnClickListener(v ->
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new AddRoomFragment(),R.id.bottom_nav_activity_main_frame_layout));

        binding.dashboardSection.deleteRoom.setOnClickListener(v ->
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new DeleteRoomFragment(),R.id.bottom_nav_activity_main_frame_layout));

        binding.dashboardSection.addTenant.setOnClickListener(v ->
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new AddTenantFragment(),R.id.bottom_nav_activity_main_frame_layout));

        binding.dashboardSection.deleteTenant.setOnClickListener(v ->
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new DeleteTenantFragment(),R.id.bottom_nav_activity_main_frame_layout));

        binding.dashboardSection.replaceSubmeter.setOnClickListener(v ->
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new ReplaceSubmeterFragment(),R.id.bottom_nav_activity_main_frame_layout));

        binding.dashboardSection.endTenancy.setOnClickListener(v ->
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new EndRoomTenancyFragment(),R.id.bottom_nav_activity_main_frame_layout));

        binding.btnScanMeter.setOnClickListener(v ->
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new QrScanFragment(),R.id.bottom_nav_activity_main_frame_layout));

        binding.dashboardSection.mpin.setOnClickListener(v ->
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new SetupMpinFragment(),R.id.bottom_nav_activity_main_frame_layout));

        binding.dashboardSection.databaseConfiguration.setOnClickListener(v ->
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new DatabaseConfigurationFragment(),R.id.bottom_nav_activity_main_frame_layout));


        binding.dashboardSection.dbTables.setOnClickListener(v->{
            NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new DatabaseInspectorFragment(),R.id.bottom_nav_activity_main_frame_layout);
        });

        // Search text filtration listeners
        binding.etSearchBills.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (billAdapter != null) {
                    billAdapter.filter(s.toString());
                }
            }
        });

        // Fire metrics evaluation fetch loops
        viewModel.loadDashboardSummary();
//        viewModel.performDatabaseCheck();  // Run only if one want to check or alter
    }

    /**
     * Single source of truth evaluation analyzer driving visual elements from layout values
     */
    private void renderUiState() {
        // 1. Manage layout click statuses during background async task execution loops
        binding.btnScanMeter.setEnabled(!viewModel.isLoading());

        // 2. Intercept and isolate system error logging strings via central ErrorUtils mapping layers
        if (viewModel.getErrorMessage() != null) {
            ErrorUtils.handleDatabaseException("Dashboard Query Failure", new Exception(viewModel.getErrorMessage()), ui);
        }

        // 3. Build text mapping targets directly from evaluated numerical model stats
        if (viewModel.isDataLoaded()) {
            binding.totalUnits.setText(String.format(java.util.Locale.US, "%.2f", viewModel.getTotalUnits()));
            binding.totalBills.setText(String.format(java.util.Locale.US, "%d", viewModel.getTotalBillsCount()));
            binding.totalRevenue.setText(String.format(java.util.Locale.US, "%.2f", viewModel.getTotalRevenue()));

            pendingBills.clear();
            pendingBills.addAll(viewModel.getPendingBillsList());
            billAdapter.notifyDataSetChanged();
        }

        // 4. Capture success path status triggers
        if (viewModel.isStatusUpdateSuccess()) {
            Toast.makeText(getContext(), "Payment status updated successfully!", Toast.LENGTH_SHORT).show();
            // Refresh adapters lists dynamically using core model arrays
            pendingBills.clear();
            pendingBills.addAll(viewModel.getPendingBillsList());
            billAdapter.notifyDataSetChanged();
        }

        if (viewModel.getMigrationStatus() != null) {
            Toast.makeText(getContext(), viewModel.getMigrationStatus(), Toast.LENGTH_SHORT).show();
            // Clear it so it doesn't toast again on next state change
             viewModel.clearMigrationStatus();
        }
    }

    @Override
    public void onMpinVerified(boolean isSuccess) {
        if (isSuccess && selectedBillForProcessing != null) {
            viewModel.changeBillPaymentStatus(selectedBillForProcessing);
            selectedBillForProcessing = null;
        } else {
            Toast.makeText(getContext(), "Verification failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.setStateListener(null); // Prevent listener memory context leaks
        binding = null;
    }
}