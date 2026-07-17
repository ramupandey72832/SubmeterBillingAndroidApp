// File: app/.../fragments/DeleteTenantFragment.java
package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.application.android_ui_templete1.templates.nav_activity.bottom_nav_activity.BottomNavActivityConstant;
import com.application.baselibrary.ui.utils.ToastMessage;
import com.application.bottomnavigationbarui.DashboardFragment;
import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentDeleteTenantBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.baselibrary.ui.utils.NavigationUtils;

import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.impl.TenancyManagementServiceImpl;
import com.github.devfrogora.service.viewmodel.TenantViewModel;

public class DeleteTenantFragment extends Fragment implements VerifyMpinDialogFragment.MpinVerificationListener {

    private ToastMessage ui;
    private FragmentDeleteTenantBinding binding;

    // Decoupled clean business layer coordinator
    private TenantViewModel viewModel;

    public DeleteTenantFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDeleteTenantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new ToastMessage(this.getContext());

        // Instantiate components with explicit injection parameters
        viewModel = new TenantViewModel(
                new TenancyManagementServiceImpl(),
                new RoomMeterServiceImpl(new MeterBillingServiceImpl())
        );

        // Track and render states synchronously
        viewModel.setStateListener(new TenantViewModel.StateListener() {
            @Override
            public void onStateChanged() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState());
                }
            }
        });

        binding.btnBack.setOnClickListener(v -> {
            clearInputs();
            NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new DashboardFragment(), BottomNavActivityConstant.MAIN_CONTAINER);
        });

        binding.btnDeleteTenant.setOnClickListener(view1 -> {
            String tenantAadhaarNumber = binding.etTenantAadhaarNumber.getText().toString().trim();

            if (tenantAadhaarNumber.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all mandatory field strings.", Toast.LENGTH_SHORT).show();
                return;
            }

            // LAUNCH THE POPUP GATE DIALOG HERE
            String msg = "Are you sure you want to delete tenant " + tenantAadhaarNumber + " from systems?" ;
            VerifyMpinDialogFragment dialog =  VerifyMpinDialogFragment.newInstance(msg);
            dialog.show(getChildFragmentManager(), "MpinVerifyDialog");
        });

    }

    @Override
    public void onMpinVerified(boolean isSuccess) {
        if (isSuccess) {
            executeDeleteTenantLogic();
        }
    }

    private void executeDeleteTenantLogic() {
        String tenantAadhaarNumber = binding.etTenantAadhaarNumber.getText().toString().trim();

        // Forward deletion actions over to ViewModel operations
        viewModel.deleteTenant(tenantAadhaarNumber);
    }

    /**
     * Re-evaluates primitive flags on updates and updates UI elements.
     */
    private void renderUiState() {
        // 1. Manage layout click handling criteria when processing actions
        binding.btnDeleteTenant.setEnabled(!viewModel.isLoading());
        binding.btnBack.setEnabled(!viewModel.isLoading());

        // 2. Intercept structural failures and project them using ErrorUtils layout mechanics
        if (viewModel.getErrorMessage() != null) {
            ErrorUtils.handleDatabaseException(
                    "Removal Action Intercepted",
                    new Exception(viewModel.getErrorMessage()),
                    ui
            );
        }

        // 3. Clear data fields exclusively on confirmation from data tables
        if (viewModel.isOperationSuccess()) {
            if (viewModel.getFeedbackMessage() != null) {
                Toast.makeText(getContext(), viewModel.getFeedbackMessage(), Toast.LENGTH_SHORT).show();
            }
            clearInputs();
        }
    }

    private void clearInputs() {
        binding.etTenantAadhaarNumber.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.setStateListener(null); // Evict callback listener pointers to prevent leaks
        binding = null;
    }
}