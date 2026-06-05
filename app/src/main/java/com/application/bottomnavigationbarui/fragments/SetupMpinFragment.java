package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.application.bottomnavigationbarui.MainActivity;
import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentSetupMpinBinding;


public class SetupMpinFragment extends Fragment {

    FragmentSetupMpinBinding binding;

    private static final String PREFS_NAME = "SecurityPrefs";
    private static final String KEY_MPIN = "user_mpin";

    public SetupMpinFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSetupMpinBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
// Initialize the focus listeners
        setupPinAutoAdvance();

        // Handle Save Button click
        binding.btnSaveMpin.setOnClickListener(v -> {
            // 1. Gather Row 1 (Setup MPIN)
            String setupMpin = binding.etSetupPin1.getText().toString().trim() +
                    binding.etSetupPin2.getText().toString().trim() +
                    binding.etSetupPin3.getText().toString().trim() +
                    binding.etSetupPin4.getText().toString().trim();

            // 2. Gather Row 2 (Confirm MPIN)
            String confirmMpin = binding.etConfirmPin1.getText().toString().trim() +
                    binding.etConfirmPin2.getText().toString().trim() +
                    binding.etConfirmPin3.getText().toString().trim() +
                    binding.etConfirmPin4.getText().toString().trim();

            // 3. Validation Rules
            if (setupMpin.length() < 4 || confirmMpin.length() < 4) {
                Toast.makeText(getContext(), "Please complete all 4 digits in both rows", Toast.LENGTH_LONG).show();
                return;
            }

            if (!setupMpin.equals(confirmMpin)) {
                Toast.makeText(getContext(), "MPINs do not match. Please try again.", Toast.LENGTH_LONG).show();
                clearConfirmRow(); // Utility method to wipe the second row
                return;
            }

            // 4. Save to SharedPreferences
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(KEY_MPIN, setupMpin); // Storing the 4-digit numeric string
            editor.apply(); // Asynchronous write to file

            Toast.makeText(getContext(), "Security MPIN set successfully!", Toast.LENGTH_LONG).show();

            // Optional: Navigate back or close fragment

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onMpinSetupComplete();
            }
        });
    }

    // Helper to wipe confirm boxes on an incorrect match match
    private void clearConfirmRow() {
        binding.etConfirmPin1.setText("");
        binding.etConfirmPin2.setText("");
        binding.etConfirmPin3.setText("");
        binding.etConfirmPin4.setText("");
        binding.etConfirmPin1.requestFocus();
    }

    // Helper method to setup auto-advance behavior between digits
    private void setupPinAutoAdvance() {
        // Row 1: Setup MPIN
        configureAutoAdvance(binding.etSetupPin1, binding.etSetupPin2, null);
        configureAutoAdvance(binding.etSetupPin2, binding.etSetupPin3, binding.etSetupPin1);
        configureAutoAdvance(binding.etSetupPin3, binding.etSetupPin4, binding.etSetupPin2);
        configureAutoAdvance(binding.etSetupPin4, binding.etConfirmPin1, binding.etSetupPin3);

        // Row 2: Confirm MPIN
        configureAutoAdvance(binding.etConfirmPin1, binding.etConfirmPin2, binding.etSetupPin4);
        configureAutoAdvance(binding.etConfirmPin2, binding.etConfirmPin3, binding.etConfirmPin1);
        configureAutoAdvance(binding.etConfirmPin3, binding.etConfirmPin4, binding.etConfirmPin2);
        configureAutoAdvance(binding.etConfirmPin4, null, binding.etConfirmPin3);
    }

    private void configureAutoAdvance(final EditText current, final EditText next, final EditText previous) {
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Move forward when a digit is entered
                if (s.length() == 1 && next != null) {
                    next.requestFocus();
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        // Optional: Handle backspace to move backward (highly recommended for good UX)
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
}