package com.application.bottomnavigationbarui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.application.bottomnavigationbarui.databinding.ReportItemPreviousReportBinding;
import com.application.bottomnavigationbarui.utils.SimplePdfGenerator;
import com.github.devfrogora.service.dto.reports.BillReportDto;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final Context context;
    private final List<String> monthKeys;
    private final Map<String, List<BillReportDto>> groupedData;



    public ReportAdapter(Context context, Map<String, List<BillReportDto>> groupedData) {
        this.context = context;

        this.groupedData = groupedData;

        // Convert map keys to a list so RecyclerView can index them (0, 1, 2)
        this.monthKeys = new ArrayList<>(groupedData.keySet());
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
        String monthKey = monthKeys.get(position); // e.g., "2026-03"
        List<BillReportDto> monthBills = groupedData.get(monthKey);

        // Convert "2026-03" to a clean title like "March 2026_Report.csv"
        String cleanDisplayTitle = formatMonthTitle(monthKey) + "_Report.pdf";
        holder.binding.tvBillMonthName.setText(cleanDisplayTitle);

        // Click listener for the Download action button
        holder.binding.btnDownload.setOnClickListener(v -> {
            // Call your export helper, sending ONLY this month's bills
            SimplePdfGenerator.generateBillReport(context, monthBills, monthKey, monthKey);
        });

        // Click listener for the Share action button
        holder.binding.btnShare.setOnClickListener(v -> {
            // TODO: Optional share logic implementation


            SimplePdfGenerator.generateBillReport(context, monthBills, monthKey, monthKey);

            // 2. Locate the generated file inside the public Downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String filename = "Billing_Report_" + monthKey + "_to_" + monthKey + ".pdf"; // match your generator output extension
            File sharedFile = new File(downloadsDir, filename);

            if (!sharedFile.exists()) {
                Toast.makeText(context, "Error: Reference file could not be found to share.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. Convert the File reference to a secure Content URI
            // Inside your adapter click listener, ensure this maps perfectly:
            String authority = context.getPackageName() + ".fileprovider";
            Uri fileUri = FileProvider.getUriForFile(context, authority, sharedFile);

            // 4. Construct the Standard Android Send Intent Channel
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf"); // FIX: Changed from text/csv to application/pdf
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Billing Report: " + formatMonthTitle(monthKey));
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Attached is the monthly utility billing report for " + formatMonthTitle(monthKey));

// --- CRITICAL PERMISSION SECURING STEPS ---
            // A. Explicitly set ClipData so the Android Chooser knows exactly what file is being bound
            shareIntent.setClipData(android.content.ClipData.newRawUri("", fileUri));

            // B. Add the standard flags to the main intent stream
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // ------------------------------------------

            // 5. Create the Chooser Intent
            Intent chooserIntent = Intent.createChooser(shareIntent, "Share Monthly PDF Via:");

            // C. Grant read permissions directly to the chooser lifecycle container
            chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // 6. Fire the intent sheet UI
            context.startActivity(chooserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return monthKeys.size(); // Will show exactly 3 rows if 3 months of data exist
    }

    // Helper to format "2026-03" into "March 2026"
    private String formatMonthTitle(String monthKey) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            Date date = inputFormat.parse(monthKey);
            return date != null ? outputFormat.format(date) : monthKey;
        } catch (Exception e) {
            return monthKey; // Fallback to raw string if parsing fails
        }
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