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
import android.widget.Toast;

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
import com.application.bottomnavigationbarui.fragments.VerifyMpinDialogFragment;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.NavigationUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.dto.reports.BillReportDto;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DashboardFragment extends Fragment implements VerifyMpinDialogFragment.MpinVerificationListener {
    private UiHelper ui;
    private FragmentDashboardBinding binding;
    private BillReportDto selectedBillForProcessing; // Temporary holder variable

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflating Fragment View Binding
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    List<BillReportDto> allBills = new ArrayList<>();

    List<BillReportDto> pendingBills = new ArrayList<>();
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(getContext());
        // 1. Configure LayoutManager for the RecyclerView
        binding.rvPendingBills.setLayoutManager(new LinearLayoutManager(getContext()));

        MeterBillingService meterBillingService = new MeterBillingServiceImpl();

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

        DashboardBillsAdapter billAdapter = new DashboardBillsAdapter(pendingBills,
                new DashboardBillsAdapter.OnBillStatusClickListener() {

                    @Override
                    public void onStatusBadgeClicked(BillReportDto bill) {
                        // 1. Save the clicked bill to a fragment variable if needed
                        selectedBillForProcessing = bill;

                        // 2. Open up the MPIN dialog fragment safely inside the method block
                        String message = "Bill No: "+bill.getBillId()+"  Room: "+bill.getRoomNumber()+" Tenant: "+bill.getTenantName()+" Amount: ₹"+bill.getTotalAmount()+"";
                        VerifyMpinDialogFragment dialog = VerifyMpinDialogFragment.newInstance(message);
                        dialog.show(getChildFragmentManager(), "MpinVerifyDialog");
                    }
                });

        // 4. Bind the adapter to your RecyclerView
        binding.rvPendingBills.setAdapter(billAdapter);

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



        // 3. Add text change listener for real-time filtering
        binding.etSearchBills.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (billAdapter != null) {
                    billAdapter.filter(s.toString());
                }
            }
        });

    }

    private void executeStatusUpdate(BillReportDto bill) {
        MeterBillingService meterBillingService = new MeterBillingServiceImpl();

        // Determine target status. (If it's currently "UNPAID" or "NO", we are setting it to true/PAID)
        boolean currentIsPaid = bill.getPaymentStatus().equalsIgnoreCase("YES") ||
                bill.getPaymentStatus().equalsIgnoreCase("PAID");
        boolean targetStatus = !currentIsPaid;

        try {
            // 1. Execute database/service update operation
            meterBillingService.updateBillPaymentStatus(bill.getBillId(), targetStatus);

            // 2. Update the string state inside our local object model
            bill.setPaymentStatus(targetStatus ? "PAID" : "UNPAID");

            // 3. Since this is a "Pending Bills" list, if it's now paid, remove it from the view!
            if (targetStatus) {
                // Get the current lists from your adapter to safely modify data inside memory
                if (binding.rvPendingBills.getAdapter() instanceof DashboardBillsAdapter) {
                    DashboardBillsAdapter billAdapter = (DashboardBillsAdapter) binding.rvPendingBills.getAdapter();

                    // Get the active mutable collection from the adapter's master records
                    List<BillReportDto> ongoingPendingList = new ArrayList<>(pendingBills);

                    // Find and remove matching bill entry safely via lambda or explicit loop
                    ongoingPendingList.removeIf(item -> item.getBillId() == (bill.getBillId()));

                    // Re-sync local master arrays
                    this.pendingBills = ongoingPendingList;

                    // 4. Force UI refresh via your custom adapter sync tool
                    billAdapter.updateData(ongoingPendingList);
                }
            }

            Toast.makeText(getContext(), "Payment status updated successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            ErrorUtils.handleDatabaseException("Error updating payment status", e, ui);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevent memory leaks
    }

    @Override
    public void onMpinVerified(boolean isSuccess) {
        if (isSuccess && selectedBillForProcessing != null) {

            // Proceed with your action safely using the selected bill!
            executeStatusUpdate(selectedBillForProcessing);
            // Clear the variable when done to prevent duplicate actions
            selectedBillForProcessing = null;
        } else {
            Toast.makeText(getContext(), "Verification failed", Toast.LENGTH_SHORT).show();
        }
    }
}