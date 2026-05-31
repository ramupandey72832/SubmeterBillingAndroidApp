package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentAddRoomBinding;
import com.application.bottomnavigationbarui.databinding.FragmentBillsBinding;


public class AddRoomFragment extends Fragment {

    private FragmentAddRoomBinding binding;


    public AddRoomFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddRoomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}