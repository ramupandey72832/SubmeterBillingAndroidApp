package com.application.bottomnavigationbarui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.application.bottomnavigationbarui.databinding.DashboardItemBillBinding;

import java.util.List;


import com.application.bottomnavigationbarui.R;
import com.github.devfrogora.service.dto.reports.BillReportDto;

public class DashboardBillsAdapter extends RecyclerView.Adapter<DashboardBillsAdapter.DashboardBillViewHolder> {

    private final List<BillReportDto> billList;

    public DashboardBillsAdapter(List<BillReportDto> billList) {
        this.billList = billList;
    }

    @NonNull
    @Override
    public DashboardBillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the generated binding class to inflate the layout
        DashboardItemBillBinding binding = DashboardItemBillBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new DashboardBillViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardBillViewHolder holder, int position) {
        BillReportDto currentItem = billList.get(position);
        Context context = holder.itemView.getContext();

        // Access views safely directly through the binding object
        holder.binding.tvRoomName.setText(currentItem.getRoomNumber());
        holder.binding.tvTenantName.setText("Tenant: " + currentItem.getTenantName());
        holder.binding.tvPrice.setText("₹" + currentItem.getTotalAmount());

        if (currentItem.getPaymentStatus().equalsIgnoreCase("YES")) {
            holder.binding.tvStatusBadge.setText("Paid");
            holder.binding.tvStatusBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_paid));
        } else {
            holder.binding.tvStatusBadge.setText("Unpaid");
            holder.binding.tvStatusBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_unpaid));
        }
    }

    @Override
    public int getItemCount() {
        return billList != null ? billList.size() : 0;
    }

    // ViewHolder holds a reference to the Binding class instead of individual Views
    static class DashboardBillViewHolder extends RecyclerView.ViewHolder {
        final DashboardItemBillBinding binding;

        public DashboardBillViewHolder(@NonNull DashboardItemBillBinding binding) {
            super(binding.getRoot()); // pass the root layout view up to the ViewHolder
            this.binding = binding;
        }
    }
}