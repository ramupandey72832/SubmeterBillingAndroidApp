package com.application.baselibrary.ui.utils;


import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class NavigationUtils {

    public static void navigateTo(FragmentActivity activity, Fragment fragment, @IdRes int containerId, Bundle args) {
        Fragment currentFragment = activity.getSupportFragmentManager()
                .findFragmentById(containerId);

        // Prevent double navigation to the same screen
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return;
        }

        if (args != null) fragment.setArguments(args);

        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerId, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public static void replaceFragment(FragmentActivity activity, Fragment fragment, @IdRes int containerId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerId, fragment).commit();
    }

    public static void replaceFragmentWithBackStack(FragmentActivity activity, Fragment fragment, @IdRes int containerId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerId, fragment)
                .addToBackStack(null)
                .commit();
    }
}