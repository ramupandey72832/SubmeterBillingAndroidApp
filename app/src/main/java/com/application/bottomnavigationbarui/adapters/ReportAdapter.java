package com.application.bottomnavigationbarui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.DashboardItemBillBinding;
import com.application.bottomnavigationbarui.databinding.ReportItemPreviousReportBinding;
import com.github.devfrogora.service.dto.reports.BillReportDto;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final List<BillReportDto> reportList;

    public ReportAdapter(List<BillReportDto> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        ReportItemPreviousReportBinding binding = ReportItemPreviousReportBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ReportAdapter.ReportViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        BillReportDto report = reportList.get(position);

        // TODO: Bind your data to your report_item layout elements here
        // Example: holder.tvDate.setText(report.getBillingDate());
        holder.binding.tvBillMonthName.setText(report.getRoomNumber() + "_Report.csv");

    }

    @Override
    public int getItemCount() {
        // Force the list to show a MAXIMUM of 3 items
        return Math.min(reportList.size(), 3);
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        // Define your view elements from report_item_previous_report here
        final ReportItemPreviousReportBinding binding;

        public ReportViewHolder(@NonNull ReportItemPreviousReportBinding binding) {
            super(binding.getRoot()); // pass the root layout view up to the ViewHolder
            this.binding = binding;
        }
    }
}