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
        setupPinAutoAdvance();
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