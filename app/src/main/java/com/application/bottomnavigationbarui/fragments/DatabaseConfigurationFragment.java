package com.application.bottomnavigationbarui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentDatabaseConfigurationBinding;
import com.github.devfrogora.service.DatabaseSetup;
import com.github.devfrogora.service.utils.CryptoHelper;

public class DatabaseConfigurationFragment extends Fragment {

    private FragmentDatabaseConfigurationBinding binding;
    private static final String PUBLIC_PREFS = "db_public_config";

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}