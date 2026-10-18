package com.shikor.app.ui.base;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

/**
 * BaseActivity - Base class for all Activities in Shikor.
 *
 * CONCEPT: Activity lifecycle logging (onCreate, onStart, onResume, onPause,
 * onStop, onRestart, onDestroy, onSaveInstanceState) and edge-to-edge insets.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected final String TAG = getClass().getSimpleName();

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        String lang = com.shikor.app.util.Prefs.getInstance(newBase).getLanguage();
        super.attachBaseContext(com.shikor.app.util.LocaleHelper.wrap(newBase, lang));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Edge-to-edge support for Android 15+
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LIFECYCLE: onCreate(savedInstanceState=" + (savedInstanceState != null) + ")");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "LIFECYCLE: onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "LIFECYCLE: onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "LIFECYCLE: onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "LIFECYCLE: onStop()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "LIFECYCLE: onRestart()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LIFECYCLE: onDestroy()");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "LIFECYCLE: onSaveInstanceState()");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "LIFECYCLE: onRestoreInstanceState()");
    }

    /**
     * Applies system window insets (status bar, navigation bar) to a root view.
     */
    protected void applySystemWindowInsets(View root) {
        if (root == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });
    }

    /**
     * Shows a Snackbar on the current activity's root view.
     */
    public void showSnackbar(String message) {
        View view = findViewById(android.R.id.content);
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Hides the software keyboard.
     */
    public void hideKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }
}
