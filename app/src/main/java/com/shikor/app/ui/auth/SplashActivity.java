package com.shikor.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.shikor.app.data.remote.AuthRepository;
import com.shikor.app.databinding.ActivitySplashBinding;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.ui.main.MainActivity;
import com.shikor.app.util.Prefs;

/**
 * SplashActivity - Entry launcher screen for Shikor.
 *
 * CONCEPT: Explicit Intents & Activity routing based on session/auth state
 */
public class SplashActivity extends BaseActivity {

    private ActivitySplashBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        // Slight delay to display branding and verify authentication session
        handler.postDelayed(this::checkSessionAndNavigate, 1000);
    }

    private void checkSessionAndNavigate() {
        AuthRepository authRepo = AuthRepository.getInstance(this);

        if (!authRepo.isUserLoggedIn()) {
            // Not logged in -> Go to LoginActivity
            // CONCEPT: Explicit Intent to LoginActivity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            // User is logged in -> Check if they have joined or created a family space
            String uid = authRepo.getCurrentUser().getUid();
            String cachedFamilyId = Prefs.getInstance(this).getFamilyId();

            if (cachedFamilyId != null && !cachedFamilyId.isEmpty()) {
                // Already part of a family -> Go to MainActivity
                navigateToMain();
            } else {
                // Verify with Firestore in case familyId was set on another device
                authRepo.fetchUserFamilyData(uid, new AuthRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        String familyId = Prefs.getInstance(SplashActivity.this).getFamilyId();
                        if (familyId != null && !familyId.isEmpty()) {
                            navigateToMain();
                        } else {
                            // User needs to create or join a family space
                            Intent intent = new Intent(SplashActivity.this, FamilySetupActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        // Fallback to FamilySetupActivity
                        Intent intent = new Intent(SplashActivity.this, FamilySetupActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }
    }

    private void navigateToMain() {
        // CONCEPT: Explicit Intent to MainActivity
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
