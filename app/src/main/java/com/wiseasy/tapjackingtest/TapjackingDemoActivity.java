package com.wiseasy.tapjackingtest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

public class TapjackingDemoActivity extends Activity {

    private boolean isBackPressed = false;
    private boolean isHomeButtonPressed = false;

    private final BroadcastReceiver homeButtonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String reason = intent.getStringExtra("reason");
            if ("homekey".equals(reason)) {
                isHomeButtonPressed = true;
                Log.d("HomeButton", "Home button pressed.");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Secure the window to prevent screen content from being captured
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        // Register the Home button receiver
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(homeButtonReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed && !isHomeButtonPressed) {
            showError("Error: EA_0016. Atención: Se detecto una pantalla sobrepuesta.");
        }
    }

    @Override
    public void onBackPressed() {
        isBackPressed = true;
        finish();  // Close the activity without throwing an error
    }

    /**
     * Detect overlays when a tap is detected in the application.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isScreenObscured(event)) {
                showError("Error: EA_0016. Atención: Se detecto una pantalla sobrepuesta.");
                return true; // Block further event processing
            }
        }
        return super.dispatchTouchEvent(event); // Allow the event to propagate
    }

    /**
     * Check if the screen is obscured using MotionEvent flags.
     */
    private boolean isScreenObscured(MotionEvent event) {
        boolean isObscured = (event.getFlags() & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0;
        boolean isPartiallyObscured = (event.getFlags() & MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED) != 0;
        return isObscured || isPartiallyObscured;
    }

    /**
     * Show an error message when tapjacking is detected.
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e("TapjackingDemo", message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(homeButtonReceiver);  // Clean up the receiver
    }
}
