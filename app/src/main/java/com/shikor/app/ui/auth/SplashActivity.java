package com.shikor.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.splashscreen.SplashScreen;

import com.shikor.app.data.remote.AuthRepository;
import com.shikor.app.databinding.ActivitySplashBinding;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.util.IntroPrefs;
import com.shikor.app.util.Prefs;

/**
 * SplashActivity - Entry launcher screen for Shikor.
 *
 * CONCEPT: Explicit Intents & Activity routing, SplashScreen API, and SplashIntroController.
 */
public class SplashActivity extends BaseActivity {

    private ActivitySplashBinding binding;
    private SplashIntroController introController;
    private Runnable pendingRoute;
    private boolean introFinished = false;
    private boolean sessionCheckFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // STEP A: Call SplashScreen.installSplashScreen(this) before super.onCreate
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        AuthRepository authRepo = AuthRepository.getInstance(this);
        boolean signedIn = authRepo.isUserLoggedIn();
        SplashIntroController.Mode mode = IntroPrefs.chooseMode(this, signedIn);

        if (mode != null && savedInstanceState == null) {
            introController = SplashIntroController.attach(this, () -> {
                introFinished = true;
                if (mode == SplashIntroController.Mode.FULL) {
                    IntroPrefs.markFullSeen(this);
                }
                tryRoute();
            });
            introController.start(mode);
        } else {
            introFinished = true;
        }

        // Always show Login page on open
        checkSessionAndNavigate();
    }

    private void checkSessionAndNavigate() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
        Prefs.getInstance(this).clearSession();

        setRoute(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            SplashIntroController.applyFadeTransition(SplashActivity.this);
            finish();
        });
    }

    private synchronized void setRoute(Runnable route) {
        this.pendingRoute = route;
        this.sessionCheckFinished = true;
        tryRoute();
    }

    private synchronized void tryRoute() {
        if (introFinished && sessionCheckFinished && pendingRoute != null && !isFinishing()) {
            pendingRoute.run();
            pendingRoute = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (introController != null) {
            introController.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (introController != null) {
            introController.resumeOrFinish();
        }
    }

    @Override
    protected void onDestroy() {
        if (introController != null) {
            introController.release();
        }
        super.onDestroy();
    }
}
