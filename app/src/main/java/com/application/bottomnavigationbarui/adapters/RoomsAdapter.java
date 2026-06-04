package com.application.bottomnavigationbarui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.bottomnavigationbarui.databinding.RoomsItemRoomBinding;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;

import java.util.List;


public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomViewHolder> {

    private final List<RoomRegistryDto> roomList;
    private final OnRoomActionListener actionListener;

    // Define interface to handle button clicks inside your Fragment/Activity
    public interface OnRoomActionListener {
        void onQuickEdit(RoomRegistryDto room);
        void onActionEdit(RoomRegistryDto room);
        void onActionDelete(RoomRegistryDto room, int position);
        void onActionTenant(RoomRegistryDto room);
        void onActionLink(RoomRegistryDto room);
    }

    public RoomsAdapter(List<RoomRegistryDto> roomList, OnRoomActionListener actionListener) {
        this.roomList = roomList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RoomsItemRoomBinding binding = RoomsItemRoomBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RoomViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomRegistryDto room = roomList.get(position);
        holder.bind(room, actionListener);
    }

    @Override
    public int getItemCount() {
        return roomList != null ? roomList.size() : 0;
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        private final RoomsItemRoomBinding binding;

        public RoomViewHolder(@NonNull RoomsItemRoomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RoomRegistryDto room, OnRoomActionListener listener) {
            // Bind Core Text Data
            binding.tvRoomName.setText(room.getRoomNumber());
            binding.tvTenantName.setText("Tenant: " + room.getTenantName());
            binding.tvSubmeterSerial.setText("Submeter Serial: " + room.getSubmeterSerialNumber());

            // Bind Dynamic Status Badge Styling
            if (room.isVacant()) {
                binding.tvStatusBadge.setText("Vacant");
            } else {
                binding.tvStatusBadge.setText("Active");
            }

            // Hook up Click Listeners to interface
            binding.btnQuickEdit.setOnClickListener(v -> listener.onQuickEdit(room));
            binding.btnActionEdit.setOnClickListener(v -> listener.onActionEdit(room));
            binding.btnActionTenant.setOnClickListener(v -> listener.onActionTenant(room));

            binding.btnActionDelete.setOnClickListener(v ->
                    listener.onActionDelete(room, getAdapterPosition())
            );
        }
    }
}