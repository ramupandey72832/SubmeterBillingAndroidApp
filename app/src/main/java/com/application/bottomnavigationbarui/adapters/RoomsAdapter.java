package com.application.bottomnavigationbarui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.application.bottomnavigationbarui.databinding.RoomsItemRoomBinding;
import com.application.bottomnavigationbarui.qr.QrCodeHelper;
import com.github.devfrogora.service.dto.reports.RoomRegistryDto;
import com.github.devfrogora.service.utils.CryptoHelper;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.itextpdf.text.pdf.qrcode.BitMatrix;
import com.itextpdf.text.pdf.qrcode.QRCodeWriter;

import java.util.ArrayList;
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
            String tempEncrypted = "";

            binding.tvRoomName.setText(room.getRoomNumber());
            binding.tvTenantName.setText("Tenant: " + room.getTenantName());
            binding.tvSubmeterSerial.setText("Submeter Serial: " + room.getSubmeterSerialNumber());
            try {
                tempEncrypted = CryptoHelper.encryptToBase64("ROOM_NUMBER_"+room.getRoomNumber(), CryptoHelper.MY_SECRET_KEY);
                binding.etEncryptedRoomNumber.setText(tempEncrypted);
                binding.etEncryptedRoomNumber.setEnabled(false);
                android.util.Log.d("CryptoTest", "Encrypted string for QR: " + tempEncrypted);
            } catch (Exception e) {
                // Log the error instead of crashing the application
                android.util.Log.e("CryptoTest", "Encryption failed", e);
            }

            final String encryptedRoomNumber = tempEncrypted;

            binding.tilQrCode.setEndIconOnClickListener(v -> {
                if(!encryptedRoomNumber.isEmpty()) showQrPopup(v.getContext(), encryptedRoomNumber, room.getRoomNumber());
            });

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
        private void showQrPopup(Context context, String qrString, String roomTitle) {
            try {
                // 1. Generate QR Bitmap
                Bitmap bmp = new QrCodeHelper().generateQrCode(qrString, 500, 500);

                // 2. Create ImageView for Dialog
                ImageView imageView = new ImageView(context);
                imageView.setImageBitmap(bmp);
                imageView.setPadding(40, 40, 40, 40);
                imageView.setAdjustViewBounds(true);

                // 3. Show in Material Dialog
                new AlertDialog.Builder(context)
                        .setTitle("QR Code: " + roomTitle)
                        .setView(imageView)
                        .setPositiveButton("Close", null)
                        .show();

            } catch (Exception e) {
                android.util.Log.e("QR_ERR", "Could not generate QR code", e);
            }
        }
    }


}