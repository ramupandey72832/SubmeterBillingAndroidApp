package com.application.bottomnavigationbarui;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.application.bottomnavigationbarui.adapters.ReportAdapter;
import com.application.bottomnavigationbarui.databinding.FragmentReportsBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.ExcelGenerator;
import com.application.bottomnavigationbarui.utils.PdfGenerator;
import com.application.bottomnavigationbarui.utils.SimplePdfGenerator;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.dto.reports.BillReportDto;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ReportsFragment extends Fragment {

    private UiHelper ui;
    FragmentReportsBinding binding;

    // Track selected dates
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    // Right way for standard Year-Month-Day: yyyy-MM-dd
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
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

        RoomMeterService roomMeterService = new RoomMeterServiceImpl();
        MeterBillingService meterBillingService = new MeterBillingServiceImpl();

        List<BillReportDto> historyList = new ArrayList<>();
        try {
            // Replace with your actual method that retrieves historical data
            historyList = meterBillingService.getLatestThreeMonthBills();
        } catch (Exception e) {
            ErrorUtils.handleDatabaseException("error: ", e, ui);
        }
        // 2. Set up the RecyclerView with your custom adapter
        ReportAdapter adapter = new ReportAdapter(historyList);
        binding.rvPreviousReports.setAdapter(adapter);


// Initialize default dates into views matching your XML defaults
        binding.tvStartDate.setText(dateFormat.format(startCalendar.getTime()));
        binding.tvEndDate.setText(dateFormat.format(endCalendar.getTime()));

        // Start Date Picker Click Listener
        binding.btnStartDate.setOnClickListener(v -> showDatePicker(startCalendar, (view1, year, month, dayOfMonth) -> {
            startCalendar.set(Calendar.YEAR, year);
            startCalendar.set(Calendar.MONTH, month);
            startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            binding.tvStartDate.setText(dateFormat.format(startCalendar.getTime()));
        }));

        // End Date Picker Click Listener
        binding.btnEndDate.setOnClickListener(v -> showDatePicker(endCalendar, (view1, year, month, dayOfMonth) -> {
            endCalendar.set(Calendar.YEAR, year);
            endCalendar.set(Calendar.MONTH, month);
            endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            binding.tvEndDate.setText(dateFormat.format(endCalendar.getTime()));
        }));

        // Generate Monthly Report (CSV/Excel/Data Export) Click Listener
        binding.btnGenerateReport.setOnClickListener(v -> {
            Date startDate = startCalendar.getTime();
            Date endDate = endCalendar.getTime();

            // Validation: Ensure Start Date is not after End Date
            if (startDate.after(endDate)) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Start date cannot be after end date", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // Convert Date objects to nicely formatted database strings ("yyyy-MM-dd")
            String startString = dateFormat.format(startDate);
            String endString = dateFormat.format(endDate);

            generateFilteredReport(startString, endString);
        });


        binding.btnGenerateLatestMonthlyBillsReport.setOnClickListener(view1 -> {
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

    private void generateFilteredReport(String start, String end) {
        MeterBillingService meterBillingService = new MeterBillingServiceImpl();
        try {
            // Now passing correctly formatted "yyyy-MM-dd" strings to your service
            List<BillReportDto> filteredBills = meterBillingService.getBillsByRange(start, end);
            if(filteredBills.isEmpty()){
                Toast.makeText(getContext(), "No Bills Found", Toast.LENGTH_SHORT).show();
                return;
            }
            filteredBills.forEach(System.out::println);
            ExcelGenerator.generateBillReport(requireContext(), filteredBills, start, end);
            SimplePdfGenerator.generateBillReport(requireContext(), filteredBills, start, end);
        } catch (Exception e) {
            ErrorUtils.handleDatabaseException("error: ", e, ui);
        }

        if (getContext() != null) {
            // FIX: Print the plain strings here directly instead of re-formatting them
            Toast.makeText(requireContext(), "Generating report from " + start + " to " + end, Toast.LENGTH_LONG).show();
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
}