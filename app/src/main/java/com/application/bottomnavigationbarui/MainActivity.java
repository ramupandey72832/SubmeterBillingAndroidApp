package com.application.bottomnavigationbarui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.application.bottomnavigationbarui.databinding.ActivityMainBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.DatabaseSetup;
import com.github.devfrogora.service.exception.BusinessRuleException;
import com.github.devfrogora.service.exception.ResourceNotFoundException;
import com.github.devfrogora.service.exception.RoomOccupiedException;

import java.sql.SQLException;

public class MainActivity extends AppCompatActivity {
    private UiHelper ui;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        ui = new UiHelper(this);

        try{
//            DatabaseSetup.initializeDb("jdbc:sqlite:submeter_bill.db", null, null, "org.sqlite.JDBC"); // Desktop
            String dbPath = getFilesDir().getAbsolutePath() + "/submeter_bill.db";
            DatabaseSetup.initializeDb("jdbc:sqldroid:" + dbPath, null, null, "org.sqldroid.SQLDroidDriver");
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
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}