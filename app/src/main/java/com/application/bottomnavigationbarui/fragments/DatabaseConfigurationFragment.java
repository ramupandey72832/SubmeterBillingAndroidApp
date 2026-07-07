package com.application.bottomnavigationbarui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentDatabaseConfigurationBinding;
import com.github.devfrogora.service.DatabaseSetup;
import com.github.devfrogora.service.utils.CryptoHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DatabaseConfigurationFragment extends Fragment {

    private FragmentDatabaseConfigurationBinding binding;
    public static final String PUBLIC_PREFS = "db_public_config";

    public DatabaseConfigurationFragment() {
        // Required empty constructor bounds
    }

    public static DatabaseConfigurationFragment newInstance() {
        return new DatabaseConfigurationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDatabaseConfigurationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadSavedPreferences();
        showDatabaseList();

        binding.btnSaveConfig.setOnClickListener(v -> saveAndInitializeEngine());
    }

    private void loadSavedPreferences() {
        Context context = getContext();
        if (context == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PUBLIC_PREFS, Context.MODE_PRIVATE);

        // 1. Fetch defaults from XML resources
        String dbPath = context.getFilesDir().getAbsolutePath() + "/" + getString(R.string.default_db_name);
        String defaultDbUrl = getString(R.string.jdbc_prefix) + dbPath;
        String defaultDriver = getString(R.string.default_db_driver);

        // 2. Secret Key stays in CryptoHelper (or a BuildConfig field for better security)
        String defaultSecretKey = CryptoHelper.MY_SECRET_KEY;

        // 3. Hydrate views
        binding.etDbUrl.setText(prefs.getString("db_url", defaultDbUrl));
        binding.etDbDriver.setText(prefs.getString("db_driver", defaultDriver));
        binding.etSecretKey.setText(prefs.getString("crypt_secret_key", defaultSecretKey));
    }

    /**
     * Helper to get the active URL from anywhere in the app
     */
    public static String getDbUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PUBLIC_PREFS, Context.MODE_PRIVATE);
        String dbPath = context.getFilesDir().getAbsolutePath() + "/" + context.getString(R.string.default_db_name);
        String defaultUrl = context.getString(R.string.jdbc_prefix) + dbPath;

        return prefs.getString("db_url", defaultUrl);
    }

    public static String getDbDriver(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PUBLIC_PREFS, Context.MODE_PRIVATE);
        String defaultDriver = context.getString(R.string.default_db_driver);
        return prefs.getString("db_driver", defaultDriver);
    }

    public static String getSecretKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PUBLIC_PREFS, Context.MODE_PRIVATE);
        String defaultSecretKey = CryptoHelper.MY_SECRET_KEY;
        return prefs.getString("crypt_secret_key", defaultSecretKey);
    }


    private void saveAndInitializeEngine() {
        String url = binding.etDbUrl.getText().toString().trim();
        String driver = binding.etDbDriver.getText().toString().trim();
        String secretKey = binding.etSecretKey.getText().toString().trim();

        if (url.isEmpty() || driver.isEmpty() || secretKey.isEmpty()) {
            Toast.makeText(getContext(), "All parameters are mandatory requirements.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Context context = requireContext();

            // Commit parameters to public persistent plain text storage
            SharedPreferences prefs = context.getSharedPreferences(PUBLIC_PREFS, Context.MODE_PRIVATE);
            prefs.edit()
                    .putString("db_url", url)
                    .putString("db_driver", driver)
                    .putString("crypt_secret_key", secretKey)
                    .apply();

            // Fire configuration setups straight up to the centralized data connection context pool
            try {
                DatabaseSetup.initializeDb(url, null, null, driver);
                Toast.makeText(context, "Configurations saved. Connection engine re-initialized.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(context, "Error initializing database: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Write Error Aborted: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDatabaseList() {
        Context context = getContext();
        if (context == null) return;

        File directory = context.getFilesDir();

        // Scan for files explicitly ending with '.db' extension criteria limits
        File[] dbFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".db"));

        List<File> fileList = new ArrayList<>();
        if (dbFiles != null) {
            for (File file : dbFiles) {
                fileList.add(file);
                Log.d("TAG", "File: " + file.getName());
            }
        }

        if (fileList.isEmpty()) {
            binding.tvEmptyDbMessage.setVisibility(View.VISIBLE);
            binding.rvDatabaseList.setVisibility(View.GONE);
        } else {
            binding.tvEmptyDbMessage.setVisibility(View.GONE);
            binding.rvDatabaseList.setVisibility(View.VISIBLE);

            binding.rvDatabaseList.setLayoutManager(new LinearLayoutManager(context));
            binding.rvDatabaseList.setAdapter(new SimpleDbListAdapter(fileList));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class SimpleDbListAdapter extends RecyclerView.Adapter<SimpleDbListAdapter.SimpleDbViewHolder> {
        private final List<File> files;

        public SimpleDbListAdapter(List<File> files) {
            this.files = files;
        }

        @NonNull
        @Override
        public SimpleDbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflates standard single text line row template
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new SimpleDbViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SimpleDbViewHolder holder, int position) {
            File file = files.get(position);

            int primaryColor = com.google.android.material.color.MaterialColors.getColor(
                    holder.itemView.getContext(), // Pass your active layout context
                    android.R.attr.textColor, // Fully resolved material package path
                    android.graphics.Color.BLACK // Safe fallback color if the theme asset fails to resolve
            );

            holder.tvName.setTextColor(primaryColor);

            // Displays ONLY the database file name (e.g., "submeter_bill.db")
            holder.tvName.setText(file.getName());
        }

        @Override
        public int getItemCount() {
            return files.size();
        }

        static class SimpleDbViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            public SimpleDbViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(android.R.id.text1);

                // Layout padding metrics for Material scannability
                itemView.setPadding(40, 36, 40, 36);
            }
        }
    }
}