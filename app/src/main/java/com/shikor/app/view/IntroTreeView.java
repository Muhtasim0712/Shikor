package com.shikor.app.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.PathParser;

import com.shikor.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * IntroTreeView - Custom 2D Canvas animated view rendering the Shikor botanical tree logo,
 * roots growth, trunk rising, canopy expansion, branch drawing, nodes popping, and tagline fading.
 *
 * CONCEPT: Custom View, Canvas 2D, PathMeasure, Property Animation, Hardware Acceleration, and Accessibility.
 */
public class IntroTreeView extends View {

    public interface OnIntroCompleteListener {
        void onIntroComplete();
    }

    private OnIntroCompleteListener listener;
    private AnimatorSet animatorSet;

    // Design Space: 1024 x 1024
    private static final float DESIGN_SIZE = 1024f;

    // Paints
    private Paint seedPaint;
    private Paint rootPaint;
    private Paint trunkPaint;
    private Paint canopyPaint;
    private Paint highlightPaint;
    private Paint shadePaint;
    private Paint branchPaint;
    private Paint nodeFillPaint;
    private Paint nodeStrokePaint;
    private Paint titleTextPaint;
    private Paint taglineTextPaint;

    // Path & Geometry Structures
    private static final String[] ROOT_PATH_DATA = {
            "M480,648 C440,684 398,706 348,764",
            "M495,655 C470,702 452,742 432,792",
            "M512,655 L512,802",
            "M529,655 C554,702 572,742 592,792",
            "M544,648 C584,684 626,706 676,764"
    };
    private static final float[] ROOT_WIDTHS = {26f, 22f, 24f, 22f, 26f};

    private static final String TRUNK_PATH_DATA = "M484,540 L540,540 C538,590 550,624 576,654 L448,654 C474,624 486,590 484,540 Z";

    private static final String[] BRANCH_PATH_DATA = {
            "M512,562 C512,480 512,380 512,304",
            "M512,530 C500,478 452,432 408,398",
            "M512,530 C524,478 572,432 616,398",
            "M512,552 C470,524 430,498 390,480",
            "M512,552 C554,524 594,498 634,480"
    };

    private final List<Path> rootPaths = new ArrayList<>();
    private final List<PathMeasure> rootMeasures = new ArrayList<>();
    private final List<Path> rootPartialPaths = new ArrayList<>();

    private Path trunkPath;
    private final RectF trunkBounds = new RectF();

    private final List<Path> branchPaths = new ArrayList<>();
    private final List<PathMeasure> branchMeasures = new ArrayList<>();
    private final List<Path> branchPartialPaths = new ArrayList<>();

    // Circles (cx, cy, r)
    private static final float[][] CANOPY_CIRCLES = {
            {512f, 350f, 135f},
            {400f, 430f, 105f},
            {624f, 430f, 105f},
            {512f, 452f, 120f}
    };
    private static final float[][] HIGHLIGHT_CIRCLES = {
            {470f, 300f, 62f},
            {352f, 395f, 44f}
    };
    private static final float[] SHADE_CIRCLE = {578f, 468f, 46f};

    private static final float[][] PEOPLE_NODES = {
            {512f, 300f, 30f},
            {404f, 394f, 26f},
            {620f, 394f, 26f},
            {386f, 480f, 24f},
            {638f, 480f, 24f}
    };

    // Animation progress values
    private float seedProgress = 0f;
    private float rootProgress = 0f;
    private float trunkProgress = 0f;
    private float canopyProgress = 0f;
    private float branchProgress = 0f;
    private float nodeProgress = 0f;
    private float textProgress = 0f;

    private boolean isCompleted = false;
    private String appNameStr;
    private String taglineStr;

    public IntroTreeView(Context context) {
        super(context);
        init(context);
    }

    public IntroTreeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IntroTreeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) return;
        appNameStr = context.getString(R.string.app_name);
        taglineStr = context.getString(R.string.app_tagline);

        int greenColor = ContextCompat.getColor(context, R.color.primary);
        int lightGreenColor = ContextCompat.getColor(context, R.color.primary_container);
        int darkGreenColor = ContextCompat.getColor(context, R.color.primary_variant);
        int brownColor = ContextCompat.getColor(context, R.color.secondary);
        int darkBrownColor = ContextCompat.getColor(context, R.color.secondary_variant);
        int goldColor = ContextCompat.getColor(context, R.color.tertiary);
        int creamColor = ContextCompat.getColor(context, R.color.background);
        int textOnBg = ContextCompat.getColor(context, R.color.on_background);

        // Seed Paint
        seedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        seedPaint.setColor(goldColor);
        seedPaint.setStyle(Paint.Style.FILL);

        // Root Paint
        rootPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rootPaint.setColor(brownColor);
        rootPaint.setStyle(Paint.Style.STROKE);
        rootPaint.setStrokeCap(Paint.Cap.ROUND);

        // Trunk Paint
        trunkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trunkPaint.setColor(brownColor);
        trunkPaint.setStyle(Paint.Style.FILL);

        // Canopy Paints
        canopyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canopyPaint.setColor(greenColor);
        canopyPaint.setStyle(Paint.Style.FILL);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(lightGreenColor);
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setAlpha(140); // ~55% alpha

        shadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadePaint.setColor(darkGreenColor);
        shadePaint.setStyle(Paint.Style.FILL);
        shadePaint.setAlpha(90); // ~35% alpha

        // Branch Paint
        branchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        branchPaint.setColor(creamColor);
        branchPaint.setStyle(Paint.Style.STROKE);
        branchPaint.setStrokeWidth(13f);
        branchPaint.setStrokeCap(Paint.Cap.ROUND);

        // Node Paints
        nodeFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodeFillPaint.setColor(goldColor);
        nodeFillPaint.setStyle(Paint.Style.FILL);

        nodeStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodeStrokePaint.setColor(creamColor);
        nodeStrokePaint.setStyle(Paint.Style.STROKE);
        nodeStrokePaint.setStrokeWidth(8f);

        // Text Paints
        Typeface typeface = ResourcesCompat.getFont(context, R.font.noto_sans_bengali);

        titleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titleTextPaint.setColor(greenColor);
        titleTextPaint.setTextAlign(Paint.Align.CENTER);
        titleTextPaint.setTextSize(72f);
        titleTextPaint.setFakeBoldText(true);
        if (typeface != null) titleTextPaint.setTypeface(typeface);

        taglineTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        taglineTextPaint.setColor(textOnBg);
        taglineTextPaint.setTextAlign(Paint.Align.CENTER);
        taglineTextPaint.setTextSize(34f);
        if (typeface != null) taglineTextPaint.setTypeface(typeface);

        // Parse & Store Paths
        for (int i = 0; i < ROOT_PATH_DATA.length; i++) {
            Path p = PathParser.createPathFromPathData(ROOT_PATH_DATA[i]);
            rootPaths.add(p);
            rootMeasures.add(new PathMeasure(p, false));
            rootPartialPaths.add(new Path());
        }

        trunkPath = PathParser.createPathFromPathData(TRUNK_PATH_DATA);
        trunkPath.computeBounds(trunkBounds, true);

        for (int i = 0; i < BRANCH_PATH_DATA.length; i++) {
            Path p = PathParser.createPathFromPathData(BRANCH_PATH_DATA[i]);
            branchPaths.add(p);
            branchMeasures.add(new PathMeasure(p, false));
            branchPartialPaths.add(new Path());
        }

        // Tap to skip
        setOnClickListener(v -> skipAnimation());
    }

    public void setOnIntroCompleteListener(OnIntroCompleteListener listener) {
        this.listener = listener;
    }

    public void startAnimation(boolean isFullIntro) {
        if (animatorSet != null) {
            animatorSet.cancel();
        }

        // Check if system animations are disabled
        float animScale = 1.0f;
        try {
            animScale = Settings.Global.getFloat(getContext().getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f);
        } catch (Exception ignored) {}

        if (animScale == 0f) {
            // Fast 600ms fade for disabled animations
            showEndState();
            postDelayed(this::notifyComplete, 600);
            return;
        }

        if (!isFullIntro) {
            // Short intro (1.0s logo and text fade)
            startShortAnimation();
            return;
        }

        // Full Intro Animation Sequence (~2.2s)
        ValueAnimator seedAnim = ValueAnimator.ofFloat(0f, 1f);
        seedAnim.setDuration(350);
        seedAnim.setInterpolator(new OvershootInterpolator(1.2f));
        seedAnim.addUpdateListener(a -> {
            seedProgress = (float) a.getAnimatedValue();
            invalidate();
        });

        ValueAnimator rootAnim = ValueAnimator.ofFloat(0f, 1f);
        rootAnim.setStartDelay(200);
        rootAnim.setDuration(600);
        rootAnim.setInterpolator(new DecelerateInterpolator());
        rootAnim.addUpdateListener(a -> {
            rootProgress = (float) a.getAnimatedValue();
            invalidate();
        });

        ValueAnimator trunkAnim = ValueAnimator.ofFloat(0f, 1f);
        trunkAnim.setStartDelay(500);
        trunkAnim.setDuration(500);
        trunkAnim.setInterpolator(new DecelerateInterpolator());
        trunkAnim.addUpdateListener(a -> {
            trunkProgress = (float) a.getAnimatedValue();
            invalidate();
        });

        ValueAnimator canopyAnim = ValueAnimator.ofFloat(0f, 1f);
        canopyAnim.setStartDelay(800);
        canopyAnim.setDuration(500);
        canopyAnim.setInterpolator(new OvershootInterpolator(1.1f));
        canopyAnim.addUpdateListener(a -> {
            canopyProgress = (float) a.getAnimatedValue();
            invalidate();
        });

        ValueAnimator branchAnim = ValueAnimator.ofFloat(0f, 1f);
        branchAnim.setStartDelay(1100);
        branchAnim.setDuration(500);
        branchAnim.addUpdateListener(a -> {
            branchProgress = (float) a.getAnimatedValue();
            nodeProgress = (float) a.getAnimatedValue();
            invalidate();
        });

        ValueAnimator textAnim = ValueAnimator.ofFloat(0f, 1f);
        textAnim.setStartDelay(1500);
        textAnim.setDuration(500);
        textAnim.addUpdateListener(a -> {
            textProgress = (float) a.getAnimatedValue();
            invalidate();
        });

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(seedAnim, rootAnim, trunkAnim, canopyAnim, branchAnim, textAnim);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isCompleted) {
                    isCompleted = true;
                    notifyComplete();
                }
            }
        });
        animatorSet.start();
    }

    private void startShortAnimation() {
        seedProgress = 1f;
        rootProgress = 1f;
        trunkProgress = 1f;
        canopyProgress = 1f;
        branchProgress = 1f;
        nodeProgress = 1f;

        ValueAnimator fadeAnim = ValueAnimator.ofFloat(0f, 1f);
        fadeAnim.setDuration(1000);
        fadeAnim.addUpdateListener(a -> {
            textProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        fadeAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isCompleted) {
                    isCompleted = true;
                    notifyComplete();
                }
            }
        });

        animatorSet = new AnimatorSet();
        animatorSet.play(fadeAnim);
        animatorSet.start();
    }

    public void skipAnimation() {
        if (isCompleted) return;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        showEndState();
        notifyComplete();
    }

    public void showEndState() {
        seedProgress = 1f;
        rootProgress = 1f;
        trunkProgress = 1f;
        canopyProgress = 1f;
        branchProgress = 1f;
        nodeProgress = 1f;
        textProgress = 1f;
        isCompleted = true;
        invalidate();
    }

    private void notifyComplete() {
        if (listener != null) {
            listener.onIntroComplete();
        }
    }

    public void stopAnimation() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        float scale = Math.min(w, h) * 0.62f / DESIGN_SIZE;
        float translateX = (w - DESIGN_SIZE * scale) / 2f;
        float translateY = (h - DESIGN_SIZE * scale) / 2f - 40f * scale;

        int saveCount = canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scale, scale);

        // 1. Seed (0.0 -> 0.35s)
        if (seedProgress > 0f) {
            float seedScale = Math.min(1.0f, seedProgress);
            canvas.drawCircle(512f, 640f, 16f * seedScale, seedPaint);
        }

        // 2. Roots (0.2 -> 0.8s)
        if (rootProgress > 0f) {
            for (int i = 0; i < rootPaths.size(); i++) {
                float startDelay = i * 0.12f;
                float p = Math.max(0f, Math.min(1.0f, (rootProgress - startDelay) / (1.0f - startDelay)));
                if (p > 0f) {
                    PathMeasure pm = rootMeasures.get(i);
                    Path partial = rootPartialPaths.get(i);
                    partial.reset();
                    pm.getSegment(0f, pm.getLength() * p, partial, true);

                    rootPaint.setStrokeWidth(ROOT_WIDTHS[i]);
                    canvas.drawPath(partial, rootPaint);
                }
            }
        }

        // 3. Trunk (0.5 -> 1.0s)
        if (trunkProgress > 0f) {
            int clipSave = canvas.save();
            float topY = trunkBounds.bottom - (trunkBounds.height() * trunkProgress);
            canvas.clipRect(0f, topY, DESIGN_SIZE, trunkBounds.bottom + 10f);
            canvas.drawPath(trunkPath, trunkPaint);
            canvas.restoreToCount(clipSave);
        }

        // 4. Canopy Circles (0.8 -> 1.3s)
        if (canopyProgress > 0f) {
            for (int i = 0; i < CANOPY_CIRCLES.length; i++) {
                float delay = i * 0.12f;
                float p = Math.max(0f, Math.min(1.0f, (canopyProgress - delay) / (1.0f - delay)));
                if (p > 0f) {
                    float cx = CANOPY_CIRCLES[i][0];
                    float cy = CANOPY_CIRCLES[i][1];
                    float r = CANOPY_CIRCLES[i][2] * p;
                    canvas.drawCircle(cx, cy, r, canopyPaint);
                }
            }

            // Highlights & Shade
            if (canopyProgress > 0.5f) {
                float alphaP = (canopyProgress - 0.5f) / 0.5f;
                highlightPaint.setAlpha((int) (140 * alphaP));
                shadePaint.setAlpha((int) (90 * alphaP));

                for (float[] hc : HIGHLIGHT_CIRCLES) {
                    canvas.drawCircle(hc[0], hc[1], hc[2], highlightPaint);
                }
                canvas.drawCircle(SHADE_CIRCLE[0], SHADE_CIRCLE[1], SHADE_CIRCLE[2], shadePaint);
            }
        }

        // 5. Branches (1.1 -> 1.6s)
        if (branchProgress > 0f) {
            for (int i = 0; i < branchPaths.size(); i++) {
                PathMeasure pm = branchMeasures.get(i);
                Path partial = branchPartialPaths.get(i);
                partial.reset();
                pm.getSegment(0f, pm.getLength() * branchProgress, partial, true);
                canvas.drawPath(partial, branchPaint);
            }
        }

        // 6. People Nodes (1.1 -> 1.6s)
        if (nodeProgress > 0f) {
            for (int i = 0; i < PEOPLE_NODES.length; i++) {
                float delay = i * 0.15f;
                float p = Math.max(0f, Math.min(1.0f, (nodeProgress - delay) / (1.0f - delay)));
                if (p > 0f) {
                    float cx = PEOPLE_NODES[i][0];
                    float cy = PEOPLE_NODES[i][1];
                    float r = PEOPLE_NODES[i][2] * p;
                    canvas.drawCircle(cx, cy, r, nodeFillPaint);
                    canvas.drawCircle(cx, cy, r, nodeStrokePaint);
                }
            }
        }

        // 7. Text & Tagline (1.5 -> 2.0s)
        if (textProgress > 0f) {
            titleTextPaint.setAlpha((int) (255 * textProgress));
            taglineTextPaint.setAlpha((int) (220 * textProgress));

            float slideY = (1.0f - textProgress) * 20f;
            canvas.drawText(appNameStr, 512f, 910f + slideY, titleTextPaint);
            canvas.drawText(taglineStr, 512f, 975f + slideY, taglineTextPaint);
        }

        canvas.restoreToCount(saveCount);
    }
}
