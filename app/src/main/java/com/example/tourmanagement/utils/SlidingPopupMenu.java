package com.example.tourmanagement.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;
import android.widget.LinearLayout;
import com.example.tourmanagement.R;

/**
 * Custom PopupMenu helper with beautiful sliding animations
 * and modern Material Design styling.
 */
public class SlidingPopupMenu {

    public interface OnMenuItemClickListener {
        void onProfileClick();
        void onBookingHistoryClick();
        void onSettingsClick();
        void onLogoutClick();
    }

    private Context context;
    private PopupWindow popupWindow;
    private View popupView;
    private OnMenuItemClickListener listener;

    public SlidingPopupMenu(Context context, OnMenuItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        initializePopup();
    }

    private void initializePopup() {
        // Inflate the custom popup layout
        LayoutInflater inflater = LayoutInflater.from(context);
        popupView = inflater.inflate(R.layout.popup_menu_dropdown, null);

        // Create PopupWindow
        popupWindow = new PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true // focusable
        );

        // Set popup properties for smooth animation
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setElevation(16f);

        // Setup click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        LinearLayout menuProfile = popupView.findViewById(R.id.menu_profile);
        LinearLayout menuBookingHistory = popupView.findViewById(R.id.menu_booking_history);
        LinearLayout menuSettings = popupView.findViewById(R.id.menu_settings);
        LinearLayout menuLogout = popupView.findViewById(R.id.menu_logout);

        menuProfile.setOnClickListener(v -> {
            dismissWithDelay();
            if (listener != null) listener.onProfileClick();
        });

        menuBookingHistory.setOnClickListener(v -> {
            dismissWithDelay();
            if (listener != null) listener.onBookingHistoryClick();
        });

        menuSettings.setOnClickListener(v -> {
            dismissWithDelay();
            if (listener != null) listener.onSettingsClick();
        });

        menuLogout.setOnClickListener(v -> {
            dismissWithDelay();
            if (listener != null) listener.onLogoutClick();
        });
    }

    public void show(View anchorView) {
        if (popupWindow != null && !popupWindow.isShowing()) {
            // Load slide-in animation
            Animation slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_right);
            popupView.startAnimation(slideIn);

            // Show popup aligned to the right edge of anchor view
            popupWindow.showAsDropDown(anchorView, -280, 0, Gravity.END);
        }
    }

    private void dismissWithDelay() {
        // Add small delay to show the touch feedback before dismissing
        popupView.postDelayed(() -> dismiss(), 150);
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            // Load slide-out animation
            Animation slideOut = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_right);

            slideOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            popupView.startAnimation(slideOut);
        }
    }

    public boolean isShowing() {
        return popupWindow != null && popupWindow.isShowing();
    }
}
