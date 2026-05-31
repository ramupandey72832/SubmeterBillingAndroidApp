package com.application.bottomnavigationbarui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;

import com.application.bottomnavigationbarui.adapters.RoomsAdapter;
import com.application.bottomnavigationbarui.databinding.FragmentRoomsBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.dto.RoomRegistryDto;
import com.github.devfrogora.service.exception.BusinessRuleException;
import com.github.devfrogora.service.exception.ResourceNotFoundException;
import com.github.devfrogora.service.exception.RoomOccupiedException;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;


public class RoomsFragment extends Fragment implements RoomsAdapter.OnRoomActionListener {
    private UiHelper ui;
    private FragmentRoomsBinding binding;
    private RoomsAdapter adapter;
    private List<RoomRegistryDto> roomList;

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

        try {
            RoomMeterService roomMeterService = new RoomMeterServiceImpl();
            List<RoomRegistryDto> rooms = roomMeterService.getRoomRegistryReport();
            // 1. Setup mock data
            roomList = new ArrayList<>();
            for (RoomRegistryDto room : rooms) {
                roomList.add(new RoomRegistryDto(room.getRoomNumber(), room.getTenantName(), room.getSubmeterSerialNumber(), room.isVacant()));
            }
            // 2. Setup layout manager & pass 'this' to handle callbacks
            binding.rvRooms.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new RoomsAdapter(roomList, this);
            binding.rvRooms.setAdapter(adapter);
        } catch(Exception e){
            ErrorUtils.handleDatabaseException("Not handling Exception: ",e,ui);
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
        roomList.remove(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(getContext(), room.getRoomNumber() + " deleted", Toast.LENGTH_SHORT).show();
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
        binding = null; // Prevent memory leaks
    }
}