// File: app/.../fragments/RoomsFragment.java
package com.application.bottomnavigationbarui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.application.bottomnavigationbarui.adapters.RoomsAdapter;
import com.application.bottomnavigationbarui.databinding.FragmentRoomsBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.viewmodel.RoomMeterViewModel;

import java.util.ArrayList;
import java.util.List;

public class RoomsFragment extends Fragment implements RoomsAdapter.OnRoomActionListener {

    private UiHelper ui;
    private FragmentRoomsBinding binding;
    private RoomsAdapter adapter;
    private List<RoomRegistryDto> roomList;

    // Decoupled clean business layer state machine
    private RoomMeterViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRoomsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(getContext());

        // Initialize local list structure container
        roomList = new ArrayList<>();

        // Establish the layout manager constraints first
        binding.rvRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RoomsAdapter(roomList, this);
        binding.rvRooms.setAdapter(adapter);

        // Instantiate isolated view model with background services explicitly injected
        RoomMeterServiceImpl service = new RoomMeterServiceImpl(new MeterBillingServiceImpl());
        viewModel = new RoomMeterViewModel(service);

        // Sync and render states upon processing complete signals
        viewModel.setStateListener(new RoomMeterViewModel.StateListener() {
            @Override
            public void onStateChanged() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState());
                }
            }
        });

        // Trigger asynchronous report generation sweep
        viewModel.loadRoomReports();
    }

    /**
     * Single source of truth analyzing primitive flags and hydrating your adapter dataset safely
     */
    private void renderUiState() {
        // 1. Manage interactive progress loaders
        if (viewModel.isLoading()) {
            // binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            // binding.progressBar.setVisibility(View.GONE);
        }

        // 2. Safely capture exceptions or structural lookup timeouts via ErrorUtils
        if (viewModel.getErrorMessage() != null) {
            ErrorUtils.handleDatabaseException(
                    "Report Failure",
                    new Exception(viewModel.getErrorMessage()),
                    ui
            );
        }

        // 3. Hydrate matching target layout adapter data references upon success flags
        if (viewModel.isOperationSuccess()) {
            roomList.clear();
            roomList.addAll(viewModel.getRoomReportsList());
            adapter.notifyDataSetChanged();
        }
    }

    // --- OnRoomActionListener Interface Methods ---

    @Override
    public void onQuickEdit(RoomRegistryDto room) {
        Toast.makeText(getContext(), "Quick Edit: " + room.getRoomNumber(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionEdit(RoomRegistryDto room) {
        Toast.makeText(getContext(), "Editing Details for " + room.getRoomNumber(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionDelete(RoomRegistryDto room, int position) {
        // Note: For a professional system, pass this request down to the ViewModel via a service delete,
        // which then triggers an automatic reload. Here is the local optimization view check:
        roomList.remove(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(getContext(), room.getRoomNumber() + " deleted from view tracking", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionTenant(RoomRegistryDto room) {
        Toast.makeText(getContext(), "Opening Profile: " + room.getTenantName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionLink(RoomRegistryDto room) {
        Toast.makeText(getContext(), "Linking submeter for " + room.getRoomNumber(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.setStateListener(null); // Clear out active callback references to block context runtime leaks
        binding = null;
    }
}