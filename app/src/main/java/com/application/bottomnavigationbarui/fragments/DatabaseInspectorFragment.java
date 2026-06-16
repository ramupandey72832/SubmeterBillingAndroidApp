package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.application.bottomnavigationbarui.adapters.GenericTableAdapter;
import com.application.bottomnavigationbarui.databinding.FragmentDatabaseInspectorBinding;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.google.android.material.tabs.TabLayout;
import com.github.devfrogora.service.viewmodel.DatabaseInspectorViewModel;

public class DatabaseInspectorFragment extends Fragment {

    private FragmentDatabaseInspectorBinding binding;
    private DatabaseInspectorViewModel viewModel;
    private GenericTableAdapter tableAdapter;

    private final String[] databaseTables = {
            "ROOM", "TENANT", "TENANCY", "SUBMETER", "BILL", "METER_READING"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDatabaseInspectorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup dynamic table tracking adapter rules
        binding.rvDatabaseTable.setLayoutManager(new LinearLayoutManager(getContext()));
        tableAdapter = new GenericTableAdapter();
        binding.rvDatabaseTable.setAdapter(tableAdapter);

        // Instantiate ViewModel
        viewModel = new DatabaseInspectorViewModel();
        viewModel.setStateListener(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::renderTableUIState);
            }
        });

        // Hydrate TabLayout selection headers programmatically
        for (String table : databaseTables) {
            binding.tableTabLayout.addTab(binding.tableTabLayout.newTab().setText(table));
        }

        binding.tableTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.loadTableData(databaseTables[tab.getPosition()]);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Trigger default data load for the first table
        viewModel.loadTableData(databaseTables[0]);
    }

    private void renderTableUIState() {
        if (viewModel.getErrorMessage() != null) {
            Toast.makeText(getContext(), viewModel.getErrorMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if (!viewModel.isLoading()) {
            // Safely pass DTO header data lists into the table renderer
            tableAdapter.updateData(viewModel.getCurrentHeaders(), viewModel.getCurrentRows());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewModel != null) viewModel.setStateListener(null);
        binding = null;
    }
}