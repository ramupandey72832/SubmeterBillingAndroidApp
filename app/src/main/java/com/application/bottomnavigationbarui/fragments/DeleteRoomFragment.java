package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.application.bottomnavigationbarui.databinding.FragmentDeleteRoomBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;

import java.sql.SQLException;

// 1. Add "implements VerifyMpinDialogFragment.MpinVerificationListener"
public class DeleteRoomFragment extends Fragment implements VerifyMpinDialogFragment.MpinVerificationListener {

    private UiHelper ui;
    private FragmentDeleteRoomBinding binding;

    public DeleteRoomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDeleteRoomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new UiHelper(this.getContext());
        // 2. Click handler on the destructive "Delete Room" action button
        binding.btnDeleteRoom.setOnClickListener(v -> {
            // Optional basic UI validation checks first (e.g. if room field is empty)
            if (binding.etDeleteRoomNumber.getText().toString().trim().isEmpty()) {
                binding.tilDeleteRoomNumber.setError("Room number is required");
                return;
            }

            // 3. LAUNCH THE POPUP DIALOG GATE HERE
            VerifyMpinDialogFragment dialog = new VerifyMpinDialogFragment();

            // Crucial: UsegetChildFragmentManager() because it is being popped up from WITHIN a fragment
            dialog.show(getChildFragmentManager(), "MpinVerifyDialog");
        });
    }

    // 4. THIS CALLBACK RUNS ONLY AFTER THE USER ENTERS THE RIGHT PIN IN THE DIALOG
    @Override
    public void onMpinVerified(boolean isSuccess) {
        if (isSuccess) {
            // Proceed to execute your database code or API calls to delete the room!
            executeRoomDeletionLogic();
        }
    }

    private void executeRoomDeletionLogic() {
        String targetRoom = binding.etDeleteRoomNumber.getText().toString().trim();

        // Execute Room DB / Firebase / API deletion logic here...
        Toast.makeText(getContext(), "Room " + targetRoom + " successfully deleted!", Toast.LENGTH_LONG).show();

        String confirmation = binding.etConfirmDelete.getText().toString().trim();
        if(!confirmation.equals("DELETE")){
            //TODO display error message Show ui error
            return;
        }


        // Navigate away or clean fields up
        binding.etDeleteRoomNumber.setText("");
        binding.etConfirmDelete.setText("");

        RoomMeterService roomMeterService = new RoomMeterServiceImpl();
        try {
            roomMeterService.deleteRoomIfVacant(targetRoom);
        } catch(Exception e){
            ErrorUtils.handleDatabaseException("Error Room is not Empty", e, ui);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}