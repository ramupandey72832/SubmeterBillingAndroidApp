package com.application.bottomnavigationbarui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.application.baselibrary.ui.utils.NavigationUtils;
import com.application.baselibrary.ui.utils.ToastMessage;
import com.application.baselibrary.utils.GenericPermissionHelper;
import com.application.bottomnavigationbarui.databinding.ActivityMainBinding;
import com.application.bottomnavigationbarui.fragments.DatabaseConfigurationFragment;
import com.application.bottomnavigationbarui.fragments.SetupMpinFragment;
import com.application.bottomnavigationbarui.utils.ErrorUtils;


import com.github.devfrogora.service.DatabaseSetup;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ToastMessage ui;
    ActivityMainBinding binding;
    private static final String PREFS_NAME = "SecurityPrefs";
    private static final String KEY_MPIN = "user_mpin";
    public GenericPermissionHelper permissionHelper;
    final List<String> requiredPermissions = Arrays.asList(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_MEDIA_IMAGES
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        ui = new ToastMessage(this);

        // Initialize it immediately while MainActivity is INITIALIZING
        permissionHelper = new GenericPermissionHelper(this, requiredPermissions, new GenericPermissionHelper.OnPermissionsListener() {
            @Override
            public void onAllPermissionsGranted() {
                // Find your active QrScanFragment and let it know
                ui.showSuccessAlert("Permission Granted",new Exception("Permission Granted"));
            }

            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                // Show Dialog
                ui.showWarningAlert("Permission Not Granted",new Exception("Permission Not Granted"));
            }
        });

        permissionHelper.checkForPermissions();

        try{
            // DatabaseSetup.initializeDb("jdbc:sqlite:submeter_bill.db", null, null, "org.sqlite.JDBC"); // Desktop
            String dburl = DatabaseConfigurationFragment.getDbUrl(this);
            String driver = DatabaseConfigurationFragment.getDbDriver(this);
            String username = null;
            String password = null;
            DatabaseSetup.initializeDb( dburl, username, password, driver);
        } catch(Exception e){
            ErrorUtils.handleDatabaseException("Error initializing database", e, ui);
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Now it is safe to handle window insets using the binding reference
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        // 1. Check if the MPIN exists in SharedPreferences
        if (!isMpinConfigured()) {
            // 2. If it does not exist, immediately load the SetupMpinFragment
            NavigationUtils.replaceFragment(this,new SetupMpinFragment(), R.id.frame_layout);
        } else {
            // 3. Optional: Load your default landing fragment (e.g., Home or Dashboard)
            // 3. Display the default fragment on launch
            NavigationUtils.replaceFragment(this,new DashboardFragment(), R.id.frame_layout);

            // 4. Handle navigation item selection
            binding.bottomNavigationView.setBackground(null);
            binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.dashboard) {
                    NavigationUtils.replaceFragment(this,new DashboardFragment(), R.id.frame_layout);
                    return true;
                }else if(itemId == R.id.room){
                    NavigationUtils.replaceFragment(this, new RoomsFragment(), R.id.frame_layout);
                    return true;
                } else if(itemId == R.id.report){
                    NavigationUtils.replaceFragment(this, new ReportsFragment(), R.id.frame_layout);
                    return true;
                } else if(itemId == R.id.billing){
                    NavigationUtils.replaceFragment(this, new BillsFragment(), R.id.frame_layout);
                    return true;
                }
                return false;
            });
        }

        binding.scanMeter.setOnClickListener(view -> {
            NavigationUtils.replaceFragment(this,new QrScanFragment(),R.id.frame_layout);
        });
    }

    private boolean isMpinConfigured() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedMpin = sharedPreferences.getString(KEY_MPIN, null);

        // Returns true if the string exists and is not null/empty
        return savedMpin != null && !savedMpin.trim().isEmpty();
    }

    public void onMpinSetupComplete() {
        // Replace with whatever your primary dashboard or landing fragment is called
      NavigationUtils.replaceFragment(this,new DashboardFragment(),R.id.frame_layout);
    }
}