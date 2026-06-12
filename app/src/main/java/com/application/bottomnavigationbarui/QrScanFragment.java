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
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;

import com.application.bottomnavigationbarui.camera.CameraHelper;
import com.application.bottomnavigationbarui.fragments.MeterReadingFragment;
import com.application.bottomnavigationbarui.utils.LocalPermissionHelper;
import com.application.bottomnavigationbarui.utils.NavigationUtils;

import java.util.List;

public class QrScanFragment extends Fragment {

    private static final String TAG = "QrScanFragment";

    private LocalPermissionHelper localPermissionHelper;
    private CameraHelper cameraHelper;
    private ImageButton btnFlashlight;

    // Keep constructors completely empty for Fragments
    public QrScanFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize CameraHelper safely after view creation
        cameraHelper = new CameraHelper(new CameraHelper.QrResultListener() {
            @Override
            public void onQrDetected(String data, Uri uri) {
                sendToResultFragment(data, uri);
            }
        });

        // 2. Bind top bar views
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnFlashlight = view.findViewById(R.id.btnFlashlight);
        ImageButton btnRefresh = view.findViewById(R.id.btnRefresh);

        // 3. Set click listeners
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        if (btnFlashlight != null) {
            btnFlashlight.setOnClickListener(v -> onFlashClicked());
        }

        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(this::refreshCamera);
        }

        // 4. Set up permissions helper
        localPermissionHelper = new LocalPermissionHelper(requireActivity(), new LocalPermissionHelper.OnPermissionsListener() {
            @Override
            public void onAllPermissionsGranted() {
                // If permissions are granted mid-session, safely start camera setup
                setupCameraPreview(view);
            }

            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Camera Permission Required")
                        .setMessage("This application cannot run because it does not have the camera permission required for scanning. Please enable the permission.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        // 5. Trigger automated permission verification pipeline
        localPermissionHelper.checkForPermissions();
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null && localPermissionHelper != null && localPermissionHelper.hasAllPermissions()) {
            setupCameraPreview(view);
        }
    }

    private void setupCameraPreview(@NonNull View view) {
        // Use a small delay to allow transition animations to complete smoothly
        view.postDelayed(() -> {
            if (isAdded() && getContext() != null) {
                PreviewView previewView = view.findViewById(R.id.previewView);
                if (previewView != null) {
                    cameraHelper.startCamera(requireContext(), previewView, this);
                }
            }
        }, 300);
    }

    private void onFlashClicked() {
        if (cameraHelper == null) return;

        boolean isNowOn = cameraHelper.toggleTorch();

        // Update the flashlight UI button look based on state
        if (btnFlashlight != null) {
            if (isNowOn) {
                btnFlashlight.setColorFilter(android.graphics.Color.YELLOW);
            } else {
                btnFlashlight.setColorFilter(android.graphics.Color.WHITE);
            }
        }
    }

    public void refreshCamera(View v) {
        View root = getView();
        if (root == null || cameraHelper == null || getContext() == null) return;

        PreviewView previewView = root.findViewById(R.id.previewView);
        if (previewView != null) {
            cameraHelper.restartCamera(requireContext(), previewView, this);

            // Nice 360-degree rotation animation on the refresh icon
            v.animate().rotationBy(360).setDuration(500).start();
            Log.d(TAG, "Refresh button triggered successfully");
        }
    }

    public void sendToResultFragment(String result, Uri uri) {
        if (result == null || uri == null || getActivity() == null) return;

        Bundle bundle = new Bundle();
        bundle.putString("ARG_QR_DATA", result);
        bundle.putString("ARG_IMAGE_URI", uri.toString());

        MeterReadingFragment resultFragment = new MeterReadingFragment();
        NavigationUtils.navigateTo(getActivity(), resultFragment, bundle);
    }
}