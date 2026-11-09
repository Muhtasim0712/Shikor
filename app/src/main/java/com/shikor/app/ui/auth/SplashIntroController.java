package com.shikor.app.ui.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.shikor.app.databinding.ViewShikorIntroBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * CONCEPT: lifecycle-aware animation, AnimatorSet, ObjectAnimator, activity transitions.
 *
 * Adds a full-screen intro overlay on top of the existing SplashActivity, plays the animation and
 * tells the Activity when it is finished. It never routes anywhere by itself: the existing
 * routing code stays in SplashActivity and simply runs after onIntroFinished().
 */
public class SplashIntroController {

    public enum Mode {
        /** Full tree animation (first launch, or when the user is signed out). About 2.3 s. */
        FULL,
        /** Logo and text only (later cold starts). About 1 s. */
        SHORT,
        /** Animations disabled in system settings: static logo and text for 600 ms. */
        REDUCED
    }

    public interface Listener {
        void onIntroFinished();
    }

    private final ViewShikorIntroBinding binding;
    private final Listener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final float density;

    private AnimatorSet animator;
    private boolean started;
    private boolean finished;
    private boolean interrupted;

    private final AnimatorListenerAdapter endListener = new AnimatorListenerAdapter() {
        private boolean cancelled;

        @Override
        public void onAnimationStart(Animator animation) {
            cancelled = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            cancelled = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!cancelled) finish();
        }
    };

    private SplashIntroController(@NonNull Activity activity, @NonNull ViewShikorIntroBinding binding,
                                  @NonNull Listener listener) {
        this.binding = binding;
        this.listener = listener;
        this.density = activity.getResources().getDisplayMetrics().density;
        // Tap anywhere to skip.
        binding.getRoot().setOnClickListener(v -> skip());
    }

    /** Inflates the overlay (View Binding) and adds it above the Activity content. */
    public static SplashIntroController attach(@NonNull Activity activity, @NonNull Listener listener) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        ViewShikorIntroBinding binding =
                ViewShikorIntroBinding.inflate(LayoutInflater.from(activity), content, false);
        content.addView(binding.getRoot());
        return new SplashIntroController(activity, binding, listener);
    }

    /** True when the user turned off animations in the system developer settings. */
    public static boolean animationsDisabled(@NonNull Context context) {
        float scale = Settings.Global.getFloat(context.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
        return scale == 0f;
    }

    /** Standard cross-fade to the next screen. Call right after startActivity(...). */
    @SuppressWarnings("deprecation")
    public static void applyFadeTransition(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= 34) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN,
                    android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    public void start(@NonNull Mode mode) {
        if (started) return;
        started = true;
        switch (mode) {
            case FULL:
                startFull();
                break;
            case SHORT:
                startShort();
                break;
            default:
                startReduced();
                break;
        }
    }

    // ---------------------------------------------------------------- modes

    private void startFull() {
        binding.introTree.setAllProgress(0f);
        binding.introTitle.setAlpha(0f);
        binding.introTagline.setAlpha(0f);
        binding.introTagline.setTranslationY(24f * density);

        List<Animator> all = new ArrayList<>();
        all.add(binding.introTree.createGrowAnimator());

        ObjectAnimator titleFade = ObjectAnimator.ofFloat(binding.introTitle, View.ALPHA, 0f, 1f);
        titleFade.setStartDelay(1500);
        titleFade.setDuration(350);
        all.add(titleFade);

        ValueAnimator spacing = ValueAnimator.ofFloat(0f, 0.08f);
        spacing.setStartDelay(1500);
        spacing.setDuration(600);
        spacing.addUpdateListener(a -> binding.introTitle.setLetterSpacing((Float) a.getAnimatedValue()));
        all.add(spacing);

        ObjectAnimator taglineSlide =
                ObjectAnimator.ofFloat(binding.introTagline, View.TRANSLATION_Y, 24f * density, 0f);
        taglineSlide.setStartDelay(1750);
        taglineSlide.setDuration(350);
        all.add(taglineSlide);

        ObjectAnimator taglineFade = ObjectAnimator.ofFloat(binding.introTagline, View.ALPHA, 0f, 1f);
        taglineFade.setStartDelay(1750);
        taglineFade.setDuration(350);
        all.add(taglineFade);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(binding.getRoot(), View.ALPHA, 1f, 0f);
        fadeOut.setStartDelay(2150);
        fadeOut.setDuration(200);
        all.add(fadeOut);

        run(all);
    }

    private void startShort() {
        binding.introTree.setAllProgress(1f);
        binding.introTitle.setAlpha(0f);
        binding.introTagline.setAlpha(0f);
        binding.introTagline.setTranslationY(0f);
        binding.introTitle.setLetterSpacing(0.08f);

        List<Animator> all = new ArrayList<>();
        ObjectAnimator titleFade = ObjectAnimator.ofFloat(binding.introTitle, View.ALPHA, 0f, 1f);
        titleFade.setDuration(300);
        all.add(titleFade);

        ObjectAnimator taglineFade = ObjectAnimator.ofFloat(binding.introTagline, View.ALPHA, 0f, 1f);
        taglineFade.setStartDelay(150);
        taglineFade.setDuration(300);
        all.add(taglineFade);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(binding.getRoot(), View.ALPHA, 1f, 0f);
        fadeOut.setStartDelay(800);
        fadeOut.setDuration(200);
        all.add(fadeOut);

        run(all);
    }

    private void startReduced() {
        // With animations disabled, animators finish instantly, so use a plain timer.
        binding.introTree.setAllProgress(1f);
        binding.introTitle.setAlpha(1f);
        binding.introTagline.setAlpha(1f);
        binding.introTagline.setTranslationY(0f);
        binding.introTitle.setLetterSpacing(0.08f);
        handler.postDelayed(this::finish, 600);
    }

    private void run(List<Animator> all) {
        animator = new AnimatorSet();
        animator.playTogether(all);
        animator.addListener(endListener);
        animator.start();
    }

    // ---------------------------------------------------------------- lifecycle

    /** Call from the Activity's onPause. Stops the animation without finishing the intro. */
    public void pause() {
        if (!started || finished) return;
        cancelAnimator();
        handler.removeCallbacksAndMessages(null);
        interrupted = true;
    }

    /** Call from the Activity's onResume. If the intro was interrupted, finish it now. */
    public void resumeOrFinish() {
        if (interrupted && !finished) finish();
    }

    /** Call from the Activity's onDestroy. */
    public void release() {
        cancelAnimator();
        handler.removeCallbacksAndMessages(null);
    }

    /** Jump straight to the end (tap to skip). */
    public void skip() {
        if (finished) return;
        finish();
    }

    public boolean isFinished() {
        return finished;
    }

    private void finish() {
        if (finished) return;
        finished = true;
        cancelAnimator();
        handler.removeCallbacksAndMessages(null);
        binding.introTree.setAllProgress(1f);
        listener.onIntroFinished();
    }

    private void cancelAnimator() {
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
            animator = null;
        }
    }
}
