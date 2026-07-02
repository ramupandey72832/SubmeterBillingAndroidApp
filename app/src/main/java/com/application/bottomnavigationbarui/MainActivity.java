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

import com.application.bottomnavigationbarui.databinding.ActivityMainBinding;
import com.application.bottomnavigationbarui.fragments.DatabaseConfigurationFragment;
import com.application.bottomnavigationbarui.fragments.SetupMpinFragment;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.LocalPermissionHelper;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.DatabaseSetup;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private UiHelper ui;
    ActivityMainBinding binding;
    private static final String PREFS_NAME = "SecurityPrefs";
    private static final String KEY_MPIN = "user_mpin";
    public LocalPermissionHelper localPermissionHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        ui = new UiHelper(this);



        // Initialize it immediately while MainActivity is INITIALIZING
        localPermissionHelper = new LocalPermissionHelper(this, new LocalPermissionHelper.OnPermissionsListener() {
            @Override
            public void onAllPermissionsGranted() {
                // Find your active QrScanFragment and let it know
                System.out.println("Permission Granted");
            }

            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                // Show Dialog
                System.out.println("Permission not Granted");
            }
        });

        localPermissionHelper.checkForPermissions();

        try{
//            DatabaseSetup.initializeDb("jdbc:sqlite:submeter_bill.db", null, null, "org.sqlite.JDBC"); // Desktop
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
            loadFragment(new SetupMpinFragment());
        } else {
            // 3. Optional: Load your default landing fragment (e.g., Home or Dashboard)
            // 3. Display the default fragment on launch
            replaceFragment(new DashboardFragment());

            // 4. Handle navigation item selection
            binding.bottomNavigationView.setBackground(null);
            binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.dashboard) {
                    replaceFragment(new DashboardFragment());
                    return true;
                }else if(itemId == R.id.room){
                    replaceFragment(new RoomsFragment());
                    return true;
                } else if(itemId == R.id.report){
                    replaceFragment(new ReportsFragment());
                    return true;
                } else if(itemId == R.id.billing){
                    replaceFragment(new BillsFragment());
                    return true;
                }
                return false;
            });
        }

        binding.scanMeter.setOnClickListener(view -> {
            replaceFragment(new QrScanFragment());
        });

    }

    private boolean isMpinConfigured() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedMpin = sharedPreferences.getString(KEY_MPIN, null);

        // Returns true if the string exists and is not null/empty
        return savedMpin != null && !savedMpin.trim().isEmpty();
    }

    /**
     * Helper method to handle smooth fragment transactions
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace 'R.id.fragment_container' with the actual ID of your FrameLayout/FragmentContainerView in activity_main.xml
        transaction.replace(R.id.frame_layout, fragment);

        // Do NOT add to backstack here, because we don't want the user to press 'Back'
        // and exit to an empty screen while bypassing setup!
        transaction.commit();
    }

    public void onMpinSetupComplete() {
        // Replace with whatever your primary dashboard or landing fragment is called
        replaceFragment(new DashboardFragment());
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}