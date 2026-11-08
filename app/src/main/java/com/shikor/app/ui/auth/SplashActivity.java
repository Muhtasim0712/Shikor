package com.shikor.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.splashscreen.SplashScreen;

import com.shikor.app.data.remote.AuthRepository;
import com.shikor.app.databinding.ActivitySplashBinding;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.ui.main.MainActivity;
import com.shikor.app.util.Prefs;

/**
 * SplashActivity - Entry launcher screen for Shikor with core-splashscreen integration,
 * custom 2D Canvas animated tree intro, background session verification, and gentle screen routing.
 *
 * CONCEPT: Activity Routing, Property Animation, Custom View, SplashScreen API, and Sync.
 */
public class SplashActivity extends BaseActivity {

    private ActivitySplashBinding binding;
    private Runnable pendingNavigationRunnable;
    private boolean isAnimationFinished = false;
    private boolean isSessionCheckFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // STEP A: Install system splashscreen (Android 12+)
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Prefs prefs = Prefs.getInstance(this);
        boolean isIntroEnabled = prefs.isIntroAnimationEnabled();
        boolean hasSeenIntro = prefs.hasSeenIntro();
        boolean isFullIntro = !hasSeenIntro;

        // Run session check in background at the same time as intro animation
        runSessionCheckBackground();

        if (!isIntroEnabled) {
            // Intro animation disabled in settings -> skip intro and navigate immediately when session check resolves
            isAnimationFinished = true;
            tryPerformNavigation();
        } else {
            // Setup IntroTreeView callback
            binding.introTreeView.setOnIntroCompleteListener(() -> {
                isAnimationFinished = true;
                prefs.setHasSeenIntro(true);
                tryPerformNavigation();
            });

            // Tap to skip
            binding.splashRoot.setOnClickListener(v -> binding.introTreeView.skipAnimation());

            // Start Intro Animation
            binding.introTreeView.startAnimation(isFullIntro);
        }
    }

    private void runSessionCheckBackground() {
        AuthRepository authRepo = AuthRepository.getInstance(this);

        if (!authRepo.isUserLoggedIn()) {
            setPendingNavigation(() -> {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        } else {
            String uid = authRepo.getCurrentUser().getUid();
            String cachedFamilyId = Prefs.getInstance(this).getFamilyId();

            if (cachedFamilyId != null && !cachedFamilyId.isEmpty()) {
                setPendingNavigation(this::navigateToMain);
            } else {
                authRepo.fetchUserFamilyData(uid, new AuthRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        String familyId = Prefs.getInstance(SplashActivity.this).getFamilyId();
                        if (familyId != null && !familyId.isEmpty()) {
                            setPendingNavigation(SplashActivity.this::navigateToMain);
                        } else {
                            setPendingNavigation(() -> {
                                Intent intent = new Intent(SplashActivity.this, FamilySetupActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        }
                    }

                    @Override
                    public void onError(String message) {
                        setPendingNavigation(() -> {
                            Intent intent = new Intent(SplashActivity.this, FamilySetupActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }
                });
            }
        }
    }

    private synchronized void setPendingNavigation(Runnable runnable) {
        this.pendingNavigationRunnable = runnable;
        this.isSessionCheckFinished = true;
        tryPerformNavigation();
    }

    private synchronized void tryPerformNavigation() {
        if (isAnimationFinished && isSessionCheckFinished && pendingNavigationRunnable != null && !isFinishing()) {
            pendingNavigationRunnable.run();
            pendingNavigationRunnable = null;
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (binding != null) {
            binding.introTreeView.stopAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binding != null) {
            binding.introTreeView.stopAnimation();
        }
    }
}
