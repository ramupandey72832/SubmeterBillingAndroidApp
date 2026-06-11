package com.application.bottomnavigationbarui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.application.bottomnavigationbarui.databinding.DashboardItemBillBinding;

import java.util.ArrayList;
import java.util.List;


import com.application.bottomnavigationbarui.R;
import com.github.devfrogora.service.dto.reports.BillReportDto;

public class DashboardBillsAdapter extends RecyclerView.Adapter<DashboardBillsAdapter.DashboardBillViewHolder>  {

    private List<BillReportDto> billList;          // This list will change during filtering
    private List<BillReportDto> billListFull;      // Keeps a copy of the original list
    private final OnBillStatusClickListener clickListener; // 1. Add listener field

    // 2. Define the interface
    public interface OnBillStatusClickListener {
        void onStatusBadgeClicked(BillReportDto bill);
    }

    public DashboardBillsAdapter(List<BillReportDto> billList, OnBillStatusClickListener clickListener) {
        this.billList = billList;
        this.billListFull = new ArrayList<>(billList);
        this.clickListener = clickListener;
    }

    // Call this method whenever you fetch fresh data from your database/API
    public void updateData(List<BillReportDto> newList) {
        this.billList = newList;
        this.billListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        billList.clear();
        if (text.isEmpty()) {
            billList.addAll(billListFull);
        } else {
            String filterPattern = text.toLowerCase().trim();
            for (BillReportDto item : billListFull) {
                // Adjust these getters to match your Bill model class
                if (item.getRoomNumber().toLowerCase().contains(filterPattern) ||
                        item.getTenantName().toLowerCase().contains(filterPattern)) {

                    billList.add(item);
                }
            }
        }
        notifyDataSetChanged();
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
        holder.binding.tvBillMeta.setText("Bill No.: " + currentItem.getBillId() + "  •  " + currentItem.getBillingDate() + "");

        if (currentItem.getPaymentStatus().equalsIgnoreCase("YES")) {
            holder.binding.btnStatusBadge.setText("Paid");
            // Set text color to green and button tint to a soft green
            holder.binding.btnStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.green_text));
            holder.binding.btnStatusBadge.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green_background))
            );
        } else {
            holder.binding.btnStatusBadge.setText("Unpaid");
            // Set text color to red and button tint to a soft red
            holder.binding.btnStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.red_text));
            holder.binding.btnStatusBadge.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red_background))
            );
        }

        holder.binding.btnStatusBadge.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onStatusBadgeClicked(currentItem);
            }
        });

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