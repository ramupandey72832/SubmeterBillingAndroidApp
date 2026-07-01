// File: app/.../fragments/QrScanFragment.java
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
import com.application.bottomnavigationbarui.utils.NavigationUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;
import com.github.devfrogora.service.impl.RoomMeterServiceImpl;
import com.github.devfrogora.service.viewmodel.QrScanViewModel;

public class QrScanFragment extends Fragment {

    private static final String TAG = "QrScanFragment";
    private UiHelper ui;
    private FragmentQrScanBinding binding;
    private CameraHelper cameraHelper;
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
        ui = new UiHelper(getContext());

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
        });

        // Initialize CameraHelper safely after view creation
        cameraHelper = new CameraHelper(new CameraHelper.QrResultListener() {
            @Override
            public void onQrDetected(String data, Uri uri) {
                temporaryImageUri = uri;
                // Forward raw scan text directly to the ViewModel state machine
                viewModel.processScannedData(data);
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
    }

    /**
     * Inspects current state properties inside the ViewModel and handles updates safely.
     */
    private void renderUiState() {
        // 1. Toggle progress indicator or custom views during active background threads
        if (viewModel.isLoading()) {
            // binding.scanProgress.setVisibility(View.VISIBLE);
        } else {
            // binding.scanProgress.setVisibility(View.GONE);
        }

        // 2. Intercept verification exceptions or format complaints via system ErrorUtils mechanics
        if (viewModel.getErrorMessage() != null) {
            ErrorUtils.handleDatabaseException(
                    viewModel.getErrorMessage(),
                    new Exception(viewModel.getErrorMessage()),
                    ui
            );
            // Reset the verification state so user can tap refresh and scan another asset
            viewModel.resetVerificationState();
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
            viewModel.resetVerificationState();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent background updates from causing crashes when switching fragments
        viewModel.setStateListener(null);
        binding = null;
    }
}