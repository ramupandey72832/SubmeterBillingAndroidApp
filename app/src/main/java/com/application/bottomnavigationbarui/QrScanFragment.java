package com.application.bottomnavigationbarui;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.application.bottomnavigationbarui.camera.CameraHelper;
import com.application.bottomnavigationbarui.databinding.FragmentQrScanBinding;
import com.application.bottomnavigationbarui.fragments.MeterReadingFragment;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.LocalPermissionHelper;
import com.application.bottomnavigationbarui.utils.NavigationUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.RoomMeterService;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;

import java.sql.SQLException;
import java.util.List;

public class QrScanFragment extends Fragment {

    private static final String TAG = "QrScanFragment";
    private UiHelper ui;
    FragmentQrScanBinding binding;
    private CameraHelper cameraHelper;
    private ImageButton btnFlashlight;

    public QrScanFragment() {}

    // 1. INITIALIZE HERE instead of onViewCreated
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This is safe because requireActivity() is available here,
        // and the Activity lifecycle state is still early enough.
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQrScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RoomMeterService roomMeterService = new RoomMeterServiceImpl();

        // Initialize CameraHelper safely after view creation
        cameraHelper = new CameraHelper(new CameraHelper.QrResultListener() {
            @Override
            public void onQrDetected(String data, Uri uri) {
                // Check if the string contains the specific substring
                if (data != null && data.contains("ROOM_NUMBER_")) {
                    // True: The data contains the substring
                    Log.d("SCAN_CHECK", "Valid room data found!");
                    // Optional: Extract just the number (301) by removing the prefix
                    String roomNumber = data.replace("ROOM_NUMBER_", "");
                    try {
                        if(roomMeterService.isRoomExist(roomNumber)){
                            sendToResultFragment(roomNumber, uri);
                            Log.d("SCAN_CHECK", "Extracted Number: " + roomNumber);
                        }else{
                            ErrorUtils.handleDatabaseException("Room not found", new SQLException(), ui);
                        }
                    } catch (Exception e) {
                        ErrorUtils.handleDatabaseException("Error: ", e, ui);
                    }
                } else {
                    // False: Substring not found
                    Log.w("SCAN_CHECK", "Invalid data format.");
                }
//                System.out.println("Data: "+data);
            }
        });

        setupCameraPreview(view);

        // Bind toolbar UI views
        ImageButton btnBack = binding.btnBack;
        btnFlashlight = binding.btnFlashlight;
        ImageButton btnRefresh = binding.btnRefresh;

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }
        if (btnFlashlight != null) btnFlashlight.setOnClickListener(v -> onFlashClicked());
        if (btnRefresh != null) btnRefresh.setOnClickListener(this::refreshCamera);

        // 2. CHECK PERMISSIONS HERE
        // The helper was already safely registered in onCreate, now we just fire the check logic
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            View view = getView();
            if (view != null && mainActivity.localPermissionHelper != null && mainActivity.localPermissionHelper.hasAllPermissions()) {
                setupCameraPreview(view);
            }
        }
    }


    private void setupCameraPreview(@NonNull View view) {
        view.postDelayed(() -> {
            if (isAdded() && getContext() != null) {
                androidx.camera.view.PreviewView previewView = binding.previewView;
                if (previewView != null) {
                    cameraHelper.startCamera(requireContext(), previewView, this);
                }
            }
        }, 300);
    }

    private void onFlashClicked() {
        if (cameraHelper == null) return;
        boolean isNowOn = cameraHelper.toggleTorch();
        if (btnFlashlight != null) {
            btnFlashlight.setColorFilter(isNowOn ? android.graphics.Color.YELLOW : android.graphics.Color.WHITE);
        }
    }

    public void refreshCamera(View v) {
        View root = getView();
        if (root == null || cameraHelper == null || getContext() == null) return;

        androidx.camera.view.PreviewView previewView = root.findViewById(R.id.previewView);
        if (previewView != null) {
            cameraHelper.restartCamera(requireContext(), previewView, this);
            v.animate().rotationBy(360).setDuration(500).start();
        }
    }

    public void sendToResultFragment(String result, Uri uri) {
        if (result == null || uri == null || getActivity() == null) return;

        Bundle bundle = new Bundle();
        bundle.putString("ARG_QR_DATA", result);

        MeterReadingFragment resultFragment = new MeterReadingFragment();
        NavigationUtils.navigateTo(getActivity(), resultFragment, bundle);
    }
}