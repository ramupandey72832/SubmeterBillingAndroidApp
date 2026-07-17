package com.application.bottomnavigationbarui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.application.android_ui_templete1.templates.nav_activity.bottom_nav_activity.BottomNavActivityConstant;
import com.application.baselibrary.ui.utils.NavigationUtils;
import com.application.baselibrary.ui.utils.ToastMessage;
import com.application.baselibrary.utils.GenericPermissionHelper;
import com.application.bottomnavigationbarui.fragments.DatabaseConfigurationFragment;
import com.application.bottomnavigationbarui.fragments.SetupMpinFragment;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.github.devfrogora.service.DatabaseSetup;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ToastMessage ui;
    private static final String PREFS_NAME = "SecurityPrefs";
    private static final String KEY_MPIN = "user_mpin";
    public GenericPermissionHelper permissionHelper;
    final List<String> requiredPermissions = Arrays.asList(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_MEDIA_IMAGES
    );

    // Keep direct references to the structural container items
    private int containerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = new ToastMessage(this);

        // Permissions and Database Initialization
        permissionHelper = new GenericPermissionHelper(this, requiredPermissions, new GenericPermissionHelper.OnPermissionsListener() {
            @Override
            public void onAllPermissionsGranted() {
                ui.showSuccessAlert("All Permission are Granted");
            }
            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                String deniedPermsString = String.join(", ", deniedPermissions);
                ui.showWarningAlert("Permission Not Granted" + deniedPermsString);
            }
        });
        permissionHelper.checkForPermissions();

        try {
            String dburl = DatabaseConfigurationFragment.getDbUrl(this);
            String driver = DatabaseConfigurationFragment.getDbDriver(this);
            DatabaseSetup.initializeDb(dburl, null, null, driver);
        } catch(Exception e){
            ErrorUtils.handleDatabaseException("Error initializing database", e, ui);
        }

        // --- THE EASY SOLUTIONS STARTS HERE ---

        // 1. Inflate the template layout from your template library directly
        setContentView(com.application.android_ui_templete1.R.layout.bottom_navigation_activity_main);

        // 2. Fetch the views using the template module's R file
        BottomNavigationView bottomNavigationView = findViewById(com.application.android_ui_templete1.R.id.bottom_nav_activity_main_bottomNavigationView);
        FloatingActionButton scanMeterFab = findViewById(com.application.android_ui_templete1.R.id.bottom_nav_activity_main_btn_fab);
        containerId = BottomNavActivityConstant.MAIN_CONTAINER;

        // 3. Set bottom navigation menu dynamically from YOUR CURRENT app module resources

//        bottomNavigationView.inflateMenu(R.menu.nav_menu_items);

        // 4. Set the Floating Button Icon dynamically from YOUR CURRENT app module resources
        scanMeterFab.setImageResource(R.drawable.ic_qr_scan);

        // Standard Edge-to-Edge Window Insets setup using root layout ID from template
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(com.application.android_ui_templete1.R.id.bottom_nav_activity_main_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Handle security validation and fragment replacements cleanly
        if (!isMpinConfigured()) {
            NavigationUtils.replaceFragment(this, new SetupMpinFragment(), containerId);
        } else {
            NavigationUtils.replaceFragment(this, new DashboardFragment(), containerId);

            bottomNavigationView.setBackground(null);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.dashboard) {
                    NavigationUtils.replaceFragment(this, new DashboardFragment(), containerId);
                    return true;
                } else if(itemId == R.id.room){
                    NavigationUtils.replaceFragment(this, new RoomsFragment(), containerId);
                    return true;
                } else if(itemId == R.id.report){
                    NavigationUtils.replaceFragment(this, new ReportsFragment(), containerId);
                    return true;
                } else if(itemId == R.id.billing){
                    NavigationUtils.replaceFragment(this, new BillsFragment(), containerId);
                    return true;
                }
                return false;
            });
        }

        // Dynamic click implementation on the floating button instance
        scanMeterFab.setOnClickListener(view -> {
            NavigationUtils.replaceFragment(this, new QrScanFragment(), containerId);
        });
    }

    private boolean isMpinConfigured() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedMpin = sharedPreferences.getString(KEY_MPIN, null);
        return savedMpin != null && !savedMpin.trim().isEmpty();
    }

    public void onMpinSetupComplete() {
        NavigationUtils.replaceFragment(this, new DashboardFragment(), containerId);
    }
}