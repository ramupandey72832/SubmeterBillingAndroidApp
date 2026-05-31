package com.application.bottomnavigationbarui.utils;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.application.bottomnavigationbarui.R;


public class NavigationUtils {

    public static void navigateTo(FragmentActivity activity, Fragment fragment,Bundle args) {
        Fragment currentFragment = activity.getSupportFragmentManager()
                .findFragmentById(R.id.frame_layout);

        // Prevent double navigation to the same screen
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return;
        }

        if (args != null) fragment.setArguments(args);

        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public static void replaceFragment(FragmentActivity activity,Fragment fragment) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment).commit();
    }

    public static void replaceFragmentWithBackStack(FragmentActivity activity, Fragment fragment) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null) // This is the key line
                .commit();
    }
}