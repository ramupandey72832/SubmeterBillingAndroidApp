package com.application.bottomnavigationbarui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.bottomnavigationbarui.databinding.BillingItemBillBinding;
import com.github.devfrogora.service.dto.BillReportDto;

import java.util.List;

public class BillingBillsAdapter extends RecyclerView.Adapter<BillingBillsAdapter.BillViewHolder> {

    private  List<BillReportDto> billList;
    private final OnBillClickListener clickListener;
    public interface OnBillClickListener {
        void onReceiptClick(BillReportDto bill);
        void onShareClick(BillReportDto bill);
    }
    public BillingBillsAdapter(List<BillReportDto> billList, OnBillClickListener clickListener) {
        this.billList = billList;
        this.clickListener = clickListener;
    }

    public void filterList(List<BillReportDto> filteredList) {
        this.billList = filteredList;
        notifyDataSetChanged(); // Refreshes the UI with the filtered results
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        BillingItemBillBinding binding = BillingItemBillBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new BillViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        BillReportDto currentBill =  billList.get(position);

        holder.binding.tvBillNumber.setText("Bill #" + currentBill.getBillId());
        holder.binding.tvRoomAndName.setText("(Room " + currentBill.getRoomNumber() + ", " + currentBill.getTenantName() + ")");
        holder.binding.tvDate.setText(currentBill.getBillingDate());
        holder.binding.tvAmount.setText("₹" + currentBill.getTotalAmount());

        // UI Toggle logic for view receipt / large amount
        if (currentBill.getPaymentStatus().equalsIgnoreCase("YES")) {
            holder.binding.btnViewReceipt.setVisibility(View.VISIBLE);
            holder.binding.tvLargeAmount.setVisibility(View.GONE);
        } else {
            holder.binding.btnViewReceipt.setVisibility(View.GONE);
            holder.binding.tvLargeAmount.setVisibility(View.VISIBLE);
            holder.binding.tvLargeAmount.setText("₹" + currentBill.getTotalAmount());
        }

        // Action listeners
        holder.binding.btnViewReceipt.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onReceiptClick(currentBill);
        });

        holder.binding.btnShare.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onShareClick(currentBill);
        });
    }


    @Override
    public int getItemCount() {
        return billList != null ? billList.size() : 0;
    }

    public static class BillViewHolder extends RecyclerView.ViewHolder {
        final BillingItemBillBinding binding;

        public BillViewHolder(@NonNull BillingItemBillBinding binding) {
            super(binding.getRoot()); // pass the root layout view up to the ViewHolder
            this.binding = binding;
        }
    }
}