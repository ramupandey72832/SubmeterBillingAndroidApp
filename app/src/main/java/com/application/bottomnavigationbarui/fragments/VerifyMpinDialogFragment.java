package com.application.bottomnavigationbarui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.application.bottomnavigationbarui.databinding.DialogVerifyMpinBinding;

public class VerifyMpinDialogFragment extends DialogFragment {

    private DialogVerifyMpinBinding binding;
    private static final String PREFS_NAME = "SecurityPrefs";
    private static final String KEY_MPIN = "user_mpin";

    // Interface to send the result back to DeleteRoomFragment
    public interface MpinVerificationListener {
        void onMpinVerified(boolean isSuccess);
    }

    private MpinVerificationListener listener;

    public VerifyMpinDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure the parent fragment implements the interface callback
        if (getParentFragment() instanceof MpinVerificationListener) {
            listener = (MpinVerificationListener) getParentFragment();
        } else {
            throw new RuntimeException(getParentFragment().toString()
                    + " must implement MpinVerificationListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogVerifyMpinBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup text watchers to jump from box to box smoothly
        setupAutoAdvance();

        // Handle Cancel Button Click
        binding.btnCancelMpinVerify.setOnClickListener(v -> dismiss());

        // Handle Confirm Button Click
        binding.btnConfirmMpinVerify.setOnClickListener(v -> {
            String enteredPin = binding.etVerifyPin1.getText().toString().trim() +
                    binding.etVerifyPin2.getText().toString().trim() +
                    binding.etVerifyPin3.getText().toString().trim() +
                    binding.etVerifyPin4.getText().toString().trim();

            if (enteredPin.length() < 4) {
                Toast.makeText(getContext(), "Please enter all 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verify pin against preference
            SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String savedMpin = prefs.getString(KEY_MPIN, null);

            if (savedMpin != null && savedMpin.equals(enteredPin)) {
                listener.onMpinVerified(true); // Pass success message to parent fragment
                dismiss();
            } else {
                Toast.makeText(getContext(), "Incorrect MPIN. Try again.", Toast.LENGTH_SHORT).show();
                clearPinInput();
            }
        });
    }

    private void setupAutoAdvance() {
        configureAutoAdvance(binding.etVerifyPin1, binding.etVerifyPin2, null);
        configureAutoAdvance(binding.etVerifyPin2, binding.etVerifyPin3, binding.etVerifyPin1);
        configureAutoAdvance(binding.etVerifyPin3, binding.etVerifyPin4, binding.etVerifyPin2);
        configureAutoAdvance(binding.etVerifyPin4, null, binding.etVerifyPin3);
    }

    private void configureAutoAdvance(final EditText current, final EditText next, final EditText previous) {
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && next != null) next.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        current.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_DEL
                    && event.getAction() == android.view.KeyEvent.ACTION_DOWN
                    && current.getText().length() == 0
                    && previous != null) {
                previous.requestFocus();
                return true;
            }
            return false;
        });
    }

    private void clearPinInput() {
        binding.etVerifyPin1.setText("");
        binding.etVerifyPin2.setText("");
        binding.etVerifyPin3.setText("");
        binding.etVerifyPin4.setText("");
        binding.etVerifyPin1.requestFocus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}