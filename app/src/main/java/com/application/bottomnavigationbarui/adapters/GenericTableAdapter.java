package com.application.bottomnavigationbarui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.application.bottomnavigationbarui.R;

import java.util.ArrayList;
import java.util.List;

public class GenericTableAdapter extends RecyclerView.Adapter<GenericTableAdapter.TableViewHolder> {

    private final List<String> headers = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();
    private static final int COLUMN_WIDTH_DP = 140;

    public void updateData(List<String> newHeaders, List<List<String>> newRows) {
        this.headers.clear();
        this.headers.addAll(newHeaders);
        this.rows.clear();
        this.rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_database_row, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        holder.container.removeAllViews();

        List<String> columnsData = (position == 0) ? headers : rows.get(position - 1);
        float scale = context.getResources().getDisplayMetrics().density;
        int widthPixels = (int) (COLUMN_WIDTH_DP * scale + 0.5f);

        for (int i = 0; i < columnsData.size(); i++) {
            TextView cell = new TextView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPixels, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins((int)(8 * scale), 0, (int)(8 * scale), 0);
            cell.setLayoutParams(params);

            String cellText = columnsData.get(i);
            cell.setText(cellText != null ? cellText : "NULL");
            cell.setTextSize(14);

            // Dynamically resolve theme colors to support Dark Mode seamlessly
            TypedValue typedValue = new TypedValue();

            if (position == 0) {
                // Header style rules
                cell.setTypeface(null, Typeface.BOLD);

                // FIXED: Resolve dynamic high-contrast color for Header text (e.g., colorPrimary)
                context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
                cell.setTextColor(typedValue.data);
            } else {
                // Normal Row data text style rules
                cell.setTypeface(null, Typeface.NORMAL);

                // FIXED: Resolve dynamic readable body color (e.g., colorOnSurface)
                context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
                cell.setTextColor(typedValue.data);
            }
            holder.container.addView(cell);
        }

        // Apply dynamic theme-aware background tints for alternating rows
        TypedValue bgValue = new TypedValue();
        if (position == 0) {
            // Header Row Background (e.g., colorSurfaceVariant)
            context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, bgValue, true);
            holder.itemView.setBackgroundColor(bgValue.data);
        } else if (position % 2 == 0) {
            // Even rows remain transparent to inherit container surface colors
            holder.itemView.setBackgroundColor(context.getColor(android.R.color.transparent));
        } else {
            // Odd rows get a subtle, theme-compliant color shade tint (e.g., colorSurfaceContainerLow / colorSurfaceInverse with alpha)
            context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, bgValue, true);
            // Add a tiny bit of opacity separation or use a darker neutral if required
            holder.itemView.setBackgroundColor(bgValue.data);
        }
    }

    @Override
    public int getItemCount() {
        if (headers.isEmpty()) return 0;
        return rows.size() + 1;
    }

    static class TableViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TableViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.rowContainer);
        }
    }
}