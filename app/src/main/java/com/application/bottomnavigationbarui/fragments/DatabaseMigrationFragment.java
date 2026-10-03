package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.application.android_ui_templete1.templates.nav_activity.bottom_nav_activity.BottomNavActivityConstant;
import com.application.baselibrary.ui.utils.NavigationUtils;
import com.application.baselibrary.ui.utils.ToastMessage;
import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.RoomsFragment;
import com.application.bottomnavigationbarui.databinding.FragmentDatabaseInspectorBinding;
import com.application.bottomnavigationbarui.databinding.FragmentDatabaseMigrationBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.github.devfrogora.service.DatabaseMigrationService;
import com.github.devfrogora.service.impl.DatabaseMigratonServiceImpl;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.viewmodel.DatabaseMigrationViewModel;
import com.github.devfrogora.service.viewmodel.RoomMeterViewModel;


public class DatabaseMigrationFragment extends Fragment {

    private FragmentDatabaseMigrationBinding binding;
    private DatabaseMigrationViewModel viewModel;

    public DatabaseMigrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDatabaseMigrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    private ToastMessage ui;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new ToastMessage(this.getContext());
        viewModel = new DatabaseMigrationViewModel(new DatabaseMigratonServiceImpl());


        binding.btnMigrateDb.setOnClickListener( v ->{
                viewModel.runMigrationDb();
        });

        viewModel.setStateListener( new RoomMeterViewModel.StateListener() {
            @Override
            public void onStateChanged() {
                // Ensure UI mutations always run on Android's Main Thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState());
                }
            }
        });
    }

    private void renderUiState() {
        // 1. Toggle progress bar state or update submission button loading visualization
        if (viewModel.isLoading()) {
            // e.g., binding.layoutAddroom.progressBar.setVisibility(View.VISIBLE);
            binding.btnMigrateDb.setEnabled(false);
        } else {
            // e.g., binding.layoutAddroom.progressBar.setVisibility(View.GONE);
            binding.btnMigrateDb.setEnabled(true);
        }

        // 2. Intercept exceptions or database constraints and render them cleanly using ErrorUtils/UiHelper
        if (viewModel.getErrorMessage() != null) {
            // Employs your system logic wrapper to show error diagnostics to the user
            ErrorUtils.handleDatabaseException("Registration Failed", new Exception(viewModel.getErrorMessage()), ui);
        }

        // 3. Clear data input layouts only on successful processing confirmation
        if (viewModel.isOperationSuccess()) {
            Toast.makeText(getContext(), "Migrated successfully", Toast.LENGTH_SHORT).show();
        }

        if (viewModel.getMigrationStatus() != null) {
            Toast.makeText(getContext(), viewModel.getMigrationStatus(), Toast.LENGTH_SHORT).show();
            // Clear it so it doesn't toast again on next state change
            viewModel.clearMigrationStatus();
        }
    }
}