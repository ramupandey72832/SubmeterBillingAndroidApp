// File: app/.../fragments/QrScanFragment.java
package com.application.bottomnavigationbarui;

import android.graphics.Color;
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


import com.application.baselibrary.libs.qr.QrCameraScanner;
import com.application.baselibrary.ui.utils.ToastMessage;
import com.application.bottomnavigationbarui.databinding.FragmentQrScanBinding;
import com.application.bottomnavigationbarui.fragments.MeterReadingFragment;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.baselibrary.ui.utils.NavigationUtils;

import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.viewmodel.QrScanViewModel;

public class QrScanFragment extends Fragment {

    private static final String TAG = "QrScanFragment";
    private ToastMessage ui;
    private FragmentQrScanBinding binding;
    private QrCameraScanner cameraHelper;
    private ImageButton btnFlashlight;

    // Decoupled Business Presentation core coordinator
    private QrScanViewModel viewModel;

    // Keep a temporary reference to the latest image URI for fragment forwarding
    private Uri temporaryImageUri;

    public QrScanFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQrScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new ToastMessage(getContext());

        // Initialize pure ViewModel with the service and its required dependency chain
        RoomMeterServiceImpl service = new RoomMeterServiceImpl(new MeterBillingServiceImpl());
        viewModel = new QrScanViewModel(service);

        // Bind layout views directly to ViewModel state change listeners
        viewModel.setStateListener(new QrScanViewModel.StateListener() {
            @Override
            public void onStateChanged() {
                // Background operations must modify layout states on Android's Main Thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState());
                }
            }

            @Override
            public void onManualRoomNumber( ) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> renderUiState("manual"));
                }
            }
        });

        // Initialize CameraHelper safely after view creation
        cameraHelper = new QrCameraScanner(new QrCameraScanner.QrResultListener() {
            @Override
            public void onQrDetected(String data, Uri uri) {
                temporaryImageUri = uri;
                // Forward raw scan text directly to the ViewModel state machine
                viewModel.processScannedData(data,false);
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


        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String roomNumber = binding.etRoomNumber.getText().toString();
                if (roomNumber.isEmpty()) {
                    ErrorUtils.handleDatabaseException(
                            "Please enter a room number",
                            new Exception("Invalid Room Number"),
                            ui
                    );
                    return;
                }else{
                    viewModel.processScannedData(roomNumber,true);
                }
            }

        });

    }

    private boolean isHandlingError = false;
    /**
     * Inspects current state properties inside the ViewModel and handles updates safely.
     */
    private void renderUiState() {
        // 1. Toggle progress indicator or custom views during active background threads
        if (viewModel.isLoading()) {
//             binding.scanProgress.setVisibility(View.VISIBLE);
        } else {
            // binding.scanProgress.setVisibility(View.GONE);
        }


        // 2. Intercept verification exceptions or format complaints via system ErrorUtils mechanics
        if (viewModel.getErrorMessage() != null && !isHandlingError) {
            isHandlingError = true;
            String msg = viewModel.getErrorMessage();

            // Show Custom Popup
            binding.tvErrorMessage.setText(msg);
            binding.layoutErrorPopup.setVisibility(View.VISIBLE);
            binding.layoutErrorPopup.setAlpha(0f);
            binding.layoutErrorPopup.animate().alpha(1f).setDuration(300).start();

            // Dismiss after 3 seconds
            binding.layoutErrorPopup.postDelayed(() -> {
                if (binding != null) {
                    binding.layoutErrorPopup.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                        if (binding != null) {
                            binding.layoutErrorPopup.setVisibility(View.GONE);
                            isHandlingError = false;
                            viewModel.resetVerificationState();
                            refreshCamera(binding.btnRefresh);
                        }
                    }).start();
                }
            }, 3000);
        }



        // 3. Navigate away exclusively on successful database/asset verification confirmation
        if (viewModel.getVerifiedRoomNumber() != null && temporaryImageUri != null) {
            String targetRoom = viewModel.getVerifiedRoomNumber();
            Uri targetUri = temporaryImageUri;

            // Clear tracking variables before leaving the fragment instance context
            viewModel.resetVerificationState();
            temporaryImageUri = null;

            sendToResultFragment(targetRoom, targetUri);
        }
    }

    private void renderUiState(String anyString) {
        // 1. Toggle progress indicator or custom views during active background threads
        if (viewModel.isLoading()) {
//             binding.scanProgress.setVisibility(View.VISIBLE);
        } else {
            // binding.scanProgress.setVisibility(View.GONE);
        }


        // 2. Intercept verification exceptions or format complaints via system ErrorUtils mechanics
        if (viewModel.getErrorMessage() != null && !isHandlingError) {
            isHandlingError = true;
            String msg = viewModel.getErrorMessage();

            // Show Custom Popup
            binding.tvErrorMessage.setText(msg);
            binding.layoutErrorPopup.setVisibility(View.VISIBLE);
            binding.layoutErrorPopup.setAlpha(0f);
            binding.layoutErrorPopup.animate().alpha(1f).setDuration(300).start();

            // Dismiss after 3 seconds
            binding.layoutErrorPopup.postDelayed(() -> {
                if (binding != null) {
                    binding.layoutErrorPopup.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                        if (binding != null) {
                            binding.layoutErrorPopup.setVisibility(View.GONE);
                            isHandlingError = false;
                            viewModel.resetVerificationState();
                            refreshCamera(binding.btnRefresh);
                        }
                    }).start();
                }
            }, 3000);
        }



        // 3. Navigate away exclusively on successful database/asset verification confirmation
        if (viewModel.getVerifiedRoomNumber() != null) {
            Log.d(TAG, "renderUiState: " + viewModel.getVerifiedRoomNumber());
            String targetRoom = viewModel.getVerifiedRoomNumber();

            // Clear tracking variables before leaving the fragment instance context
            viewModel.resetVerificationState();

            sendToResultFragment(targetRoom);
        }
    }

    /**
     * Shows a message for 3 seconds using a Snackbar (standard Material approach)
     * or you can use a custom overlay View.
     */
    private void showTimedInvalidScanPopup(String message) {
        if (binding == null) return;

        // Option A: Using Google Material Snackbar (Easiest & Best for 3 sec)
        com.google.android.material.snackbar.Snackbar snackbar =
                com.google.android.material.snackbar.Snackbar.make(
                        binding.getRoot(),
                        message,
                        3000 // 3000 milliseconds = 3 seconds
                );

        // Optional: Style it to look like an error
        snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark));
        snackbar.setTextColor(getResources().getColor(android.R.color.white));
        snackbar.show();

        // Option B: If you prefer a centered custom UI, toggle a View visibility
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            View view = getView();
            if (view != null && mainActivity.permissionHelper != null && mainActivity.permissionHelper.hasAllPermissions()) {
                setupCameraPreview(view);
            }
        }
    }

    private void setupCameraPreview(@NonNull View view) {
        view.postDelayed(() -> {
            if (isAdded() && getContext() != null) {
                androidx.camera.view.PreviewView previewView = binding.previewView;
                if (previewView != null) {
                    cameraHelper.startScanning(requireContext(), previewView, this);
                }
            }
        }, 300);
    }

    private void onFlashClicked() {
        if (cameraHelper == null) return;
        boolean isNowOn = cameraHelper.toggleTorch();
        if (btnFlashlight != null) {
            btnFlashlight.setColorFilter(isNowOn ? Color.YELLOW : Color.BLUE);
        }
    }

    public void refreshCamera(View v) {
        View root = getView();
        if (root == null || cameraHelper == null || getContext() == null) return;

        androidx.camera.view.PreviewView previewView = root.findViewById(R.id.previewView);
        if (previewView != null) {
            viewModel.resetVerificationState();
            cameraHelper.restartScanning(requireContext(), previewView, this);
            v.animate().rotationBy(360).setDuration(500).start();
        }
    }

    public void sendToResultFragment(String result, Uri uri) {
        if (result == null || uri == null || getActivity() == null) return;

        Bundle bundle = new Bundle();
        bundle.putString("ARG_QR_DATA", result);

        MeterReadingFragment resultFragment = new MeterReadingFragment();
        NavigationUtils.navigateTo(getActivity(), resultFragment, R.id.frame_layout, bundle);
    }

    public void sendToResultFragment(String result) {
        if (result == null || getActivity() == null) return;

        Bundle bundle = new Bundle();
        bundle.putString("ARG_QR_DATA", result);

        MeterReadingFragment resultFragment = new MeterReadingFragment();
        NavigationUtils.navigateTo(getActivity(), resultFragment, R.id.frame_layout, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent background updates from causing crashes when switching fragments
        viewModel.setStateListener(null);
        binding = null;
    }
}