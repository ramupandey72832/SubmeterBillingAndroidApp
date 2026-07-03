package com.application.bottomnavigationbarui.fragments;

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

import com.application.bottomnavigationbarui.databinding.FragmentEditBillBinding;
import com.github.devfrogora.service.dto.reports.BillReportDto;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.viewmodel.BillingViewModel;

public class EditBillFragment extends Fragment {

    private FragmentEditBillBinding binding;
    private BillingViewModel viewModel;
    private BillReportDto selectedBill;

    public static EditBillFragment newInstance(BillReportDto bill) {
        EditBillFragment fragment = new EditBillFragment();
        Bundle args = new Bundle();
        args.putSerializable("selected_bill_dto", bill);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedBill = (BillReportDto) getArguments().getSerializable("selected_bill_dto");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditBillBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.setNavigationOnClickListener(v -> popScreen());

        viewModel = new BillingViewModel(new MeterBillingServiceImpl());
        viewModel.setStateListener(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::handleUiStateFeedback);
            }
        });

        // Hydrate Read-Only display items
        if (selectedBill != null) {
            binding.tvReadOnlyBillId.setText("Bill #" + selectedBill.getBillId());
            binding.tvReadOnlyTenant.setText(selectedBill.getTenantName() + " (" + selectedBill.getRoomNumber() + ")");
            binding.tvReadOnlyMeter.setText(selectedBill.getMeterSerialNumber());
            binding.tvReadOnlyPrevReading.setText(selectedBill.getPreviousReading() + " kWh");

            // Hydrate Editable forms inputs initialization parameters
            binding.etEditCurrentReading.setText(String.valueOf(selectedBill.getCurrentReading()));
            binding.etEditRatePerUnit.setText(String.valueOf(selectedBill.getRatePerUnit()));
            binding.etEditFixedCharges.setText(String.valueOf(selectedBill.getFixedCharge()));
            binding.etEditTotalAmount.setText(String.valueOf(selectedBill.getTotalAmount()));
            binding.etEditNote.setText(selectedBill.getNote());
            binding.etEditExtraCharges.setText(String.valueOf(selectedBill.getExtraCharge()));

            // Attach Auto-Calculation dynamic structural triggers
            TextWatcher continuousMathWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) { executeLiveRecalculation(); }
            };

            binding.etEditCurrentReading.addTextChangedListener(continuousMathWatcher);
            binding.etEditRatePerUnit.addTextChangedListener(continuousMathWatcher);
            binding.etEditFixedCharges.addTextChangedListener(continuousMathWatcher);
            binding.etEditExtraCharges.addTextChangedListener(continuousMathWatcher);
        }

        binding.btnSaveBillChanges.setOnClickListener(v -> submitBillAdjustments());
    }

    /**
     * Live Math Aggregator Engine running in real-time on input state changes
     */
    private void executeLiveRecalculation() {
        try {
            String currentRaw = binding.etEditCurrentReading.getText().toString().trim();
            String rateRaw = binding.etEditRatePerUnit.getText().toString().trim();
            String fixedRaw = binding.etEditFixedCharges.getText().toString().trim();
            String extraRaw = binding.etEditExtraCharges.getText().toString().trim();

            double current = currentRaw.isEmpty() ? 0.0 : Double.parseDouble(currentRaw);
            double rate = rateRaw.isEmpty() ? 0.0 : Double.parseDouble(rateRaw);
            double fixed = fixedRaw.isEmpty() ? 0.0 : Double.parseDouble(fixedRaw);
            double extra = extraRaw.isEmpty() ? 0.0 : Double.parseDouble(extraRaw);
            double previous = selectedBill != null ? selectedBill.getPreviousReading() : 0.0;

            double consumed = Math.max(0.0, current - previous);
            double calculatedTotal = (consumed * rate) + fixed + extra;

            // Update display natively without breaking infinite loops flags
            binding.etEditTotalAmount.setText(String.format(java.util.Locale.US, "%.2f", calculatedTotal));
        } catch (NumberFormatException ignored) {}
    }

    private void submitBillAdjustments() {
        if (selectedBill == null) return;

        String curRaw = binding.etEditCurrentReading.getText().toString().trim();
        String ratRaw = binding.etEditRatePerUnit.getText().toString().trim();
        String fixRaw = binding.etEditFixedCharges.getText().toString().trim();
        String totRaw = binding.etEditTotalAmount.getText().toString().trim();
        String extraRaw = binding.etEditExtraCharges.getText().toString().trim();
        String note = binding.etEditNote.getText().toString().trim();

        if (curRaw.isEmpty() || ratRaw.isEmpty() || fixRaw.isEmpty() || totRaw.isEmpty()) {
            Toast.makeText(getContext(), "All computational metric fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        double current = Double.parseDouble(curRaw);
        double rate = Double.parseDouble(ratRaw);
        double fixed = Double.parseDouble(fixRaw);
        double extra = Double.parseDouble(extraRaw);
        double total = Double.parseDouble(totRaw);

        if (current < selectedBill.getPreviousReading()) {
            Toast.makeText(getContext(), "Error: Current reading cannot drop below the previous reading value.", Toast.LENGTH_LONG).show();
            return;
        }

        viewModel.updateExistingBillMetrics(selectedBill.getBillId(), current, rate, fixed, extra ,total,note);
    }

    private void handleUiStateFeedback() {
        if (viewModel.getErrorMessage() != null) {
            Toast.makeText(getContext(), viewModel.getErrorMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if (viewModel.isOperationSuccess()) {
            Toast.makeText(getContext(), "Bill specifications adjusted cleanly.", Toast.LENGTH_SHORT).show();
            popScreen();
        }
    }

    private void popScreen() {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewModel != null) viewModel.setStateListener(null);
        binding = null;
    }
}