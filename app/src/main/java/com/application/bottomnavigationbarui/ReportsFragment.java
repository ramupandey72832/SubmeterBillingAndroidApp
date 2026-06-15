// File: app/.../fragments/ReportsFragment.java
package com.application.bottomnavigationbarui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.application.bottomnavigationbarui.adapters.ReportAdapter;
import com.application.bottomnavigationbarui.databinding.FragmentReportsBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.ExcelGenerator;
import com.application.bottomnavigationbarui.utils.PdfGenerator;
import com.application.bottomnavigationbarui.utils.SimplePdfGenerator;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.viewmodel.ReportsViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private UiHelper ui;
    private FragmentReportsBinding binding;

    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Decoupled Business Core state holder
    private ReportsViewModel viewModel;

    public ReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(getContext());

        // Initialize pure ViewModel with the required operational services explicitly injected
        viewModel = new ReportsViewModel(
                new MeterBillingServiceImpl(),
                new RoomMeterServiceImpl(new MeterBillingServiceImpl())
        );

        // Track and map state transitions securely
        viewModel.setStateListener(new ReportsViewModel.StateListener() {
            @Override
            public void onStateChanged() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState());
                }
            }
        });

        // Initialize default dates into views matching layouts
        binding.tvStartDate.setText(dateFormat.format(startCalendar.getTime()));
        binding.tvEndDate.setText(dateFormat.format(endCalendar.getTime()));

        binding.btnStartDate.setOnClickListener(v -> showDatePicker(startCalendar, (view1, year, month, dayOfMonth) -> {
            startCalendar.set(Calendar.YEAR, year);
            startCalendar.set(Calendar.MONTH, month);
            startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            binding.tvStartDate.setText(dateFormat.format(startCalendar.getTime()));
        }));

        binding.btnEndDate.setOnClickListener(v -> showDatePicker(endCalendar, (view1, year, month, dayOfMonth) -> {
            endCalendar.set(Calendar.YEAR, year);
            endCalendar.set(Calendar.MONTH, month);
            endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            binding.tvEndDate.setText(dateFormat.format(endCalendar.getTime()));
        }));

        binding.btnGenerateReport.setOnClickListener(v -> {
            Date startDate = startCalendar.getTime();
            Date endDate = endCalendar.getTime();

            if (startDate.after(endDate)) {
                Toast.makeText(requireContext(), "Start date cannot be after end date", Toast.LENGTH_LONG).show();
                return;
            }

            String startString = dateFormat.format(startDate);
            String endString = dateFormat.format(endDate);

            // Hand over the search task parameters straight to the ViewModel
            viewModel.fetchBillsByRange(startString, endString);
        });

        binding.btnGenerateLatestMonthlyBillsReport.setOnClickListener(view1 -> {
            // Forward action call down to background workers
            viewModel.compileLatestMonthlyBills();
        });

        // Trigger asynchronous initialization sweep for past records
        viewModel.loadHistoricalThreeMonthReports();
    }

    /**
     * Single source of truth evaluating current primitive flags and triggering file renders
     */
    private void renderUiState() {
        // 1. Manage layout click statuses during background operations
        binding.btnGenerateReport.setEnabled(!viewModel.isLoading());
        binding.btnGenerateLatestMonthlyBillsReport.setEnabled(!viewModel.isLoading());

        // 2. Intercept and isolate system failures cleanly through ErrorUtils layers
        if (viewModel.getErrorMessage() != null) {
            ErrorUtils.handleDatabaseException("Reporting Failure", new Exception(viewModel.getErrorMessage()), ui);
        }

        // 3. Hydrate past list groupings exclusively when processing confirms data success states
        if (viewModel.isHistoryLoaded()) {
            ReportAdapter adapter = new ReportAdapter(getContext(), viewModel.getGroupedPreviousReports());
            binding.rvPreviousReports.setAdapter(adapter);
        }

        // 4. Handle date-range results ready signals
        if (viewModel.isRangeExportReady()) {
            if (viewModel.getRangeFilteredBills().isEmpty()) {
                Toast.makeText(getContext(), "No Bills Found", Toast.LENGTH_SHORT).show();
            } else {
                String start = dateFormat.format(startCalendar.getTime());
                String end = dateFormat.format(endCalendar.getTime());

                // Execute UI/Android document writer engines cleanly
                ExcelGenerator.generateBillReport(requireContext(), viewModel.getRangeFilteredBills(), start, end);
                SimplePdfGenerator.generateBillReport(requireContext(), viewModel.getRangeFilteredBills(), start, end);

                Toast.makeText(requireContext(), "Generating report from " + start + " to " + end, Toast.LENGTH_LONG).show();
            }
        }

        // 5. Handle multi-bill compilation ready signals
        if (viewModel.isMultiPdfReady()) {
            PdfGenerator.generateMultipleBillsPdf(getContext(), viewModel.getLatestMonthlyBills());
        }
    }

    private void showDatePicker(Calendar calendar, DatePickerDialog.OnDateSetListener listener) {
        if (getContext() == null) return;
        new DatePickerDialog(
                getContext(),
                listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.setStateListener(null); // Clear out active callbacks to block layout leaks
        binding = null;
    }
}