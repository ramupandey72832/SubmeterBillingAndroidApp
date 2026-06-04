package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.bottomnavigationbarui.R;

/**
 * How to handle this in your Backend/Database:
 * To properly execute this function without losing data, do not run a DELETE query. Instead, update your database logic using these guidelines:
 *
 * Step 1: Update the Tenant Status: Add an is_active boolean or a status text column to your Tenant table. When this form is submitted, switch their status from Active to Archived (or Past Tenant).
 *
 * Step 2: Unlink the Room: Clear the tenant_id column from that particular Room row so the room shows up as vacant and ready for a new occupant.
 *
 * Step 3: Save the Move-Out Date: Record the date from etMoveOutDate into a lease_end_date column in your history files for future financial reporting.
 *
 * Step 4: Use a Date Picker: In your Java/Kotlin activity code, attach a MaterialDatePicker to the etMoveOutDate input field. Setting android:focusable="false" ensures the keyboard won't pop up and annoy the user when they tap the date field to open the calendar picker.
 */
public class EndRoomTenancyFragment extends Fragment {



    public EndRoomTenancyFragment() {
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
        return inflater.inflate(R.layout.fragment_end_room_tenancy, container, false);
    }


}