package com.application.baselibrary.ui.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

public class ToastMessage {
    private final Context context;

    public ToastMessage(Context context){
        this.context = context;
    }

    // Helper method to create a safe custom Toast view for Android 11+
    private Toast createSafeToast(String message, String hexColor, int duration) {
        // 1. Create a TextView programmatically
        TextView textView = new TextView(context);
        textView.setText(message);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        // Add default Android Toast padding
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
        int paddingVerticalDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
        textView.setPadding(paddingDp, paddingVerticalDp, paddingDp, paddingVerticalDp);

        // 2. Set the background shape and apply the color tint safely
        textView.setBackgroundResource(android.R.drawable.toast_frame);
        textView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(hexColor)));

        // 3. Initialize a fresh Toast container and assign the view
        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(textView); // Set the customized view directly

        return toast;
    }

    public void showWarningAlert(String message, Exception e){
        // #FF9800 is Orange
        String fullMsg = "Warning: " + message + " "+e.getMessage();
        createSafeToast(fullMsg, "#FF9800", Toast.LENGTH_LONG).show();
        System.err.println(fullMsg);
        e.printStackTrace();

    }

    public void showErrorAlert(String message, Exception e){
        // #F44336 is Red
        String fullMsg = "Error: " + message + " "+e.getMessage();
        createSafeToast(fullMsg, "#F44336", Toast.LENGTH_LONG).show();
        System.err.println(fullMsg);
        e.printStackTrace();

    }

    public void showSuccessAlert(String message, Exception e){
        // #4CAF50 is Green
        String fulMsg = "Success: " + message + " "+e.getMessage();
        createSafeToast(fulMsg, "#4CAF50", Toast.LENGTH_SHORT).show();
        System.out.println(fulMsg);
        e.printStackTrace();
    }
}
