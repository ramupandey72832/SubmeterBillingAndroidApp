package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentDeleteRoomBinding;
import com.application.bottomnavigationbarui.databinding.FragmentMeterReadingBinding;

public class DeleteRoomFragment extends Fragment {

    FragmentDeleteRoomBinding binding;
    public DeleteRoomFragment() {
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
        binding = FragmentDeleteRoomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}