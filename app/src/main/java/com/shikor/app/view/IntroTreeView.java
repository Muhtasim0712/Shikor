package com.shikor.app.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.shikor.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * CONCEPT: Custom View + Canvas drawing + property animation (ValueAnimator, AnimatorSet,
 * interpolators, PathMeasure).
 *
 * Draws the Shikor logo (tree with roots, canopy, branches and gold family nodes) and grows it
 * step by step. All geometry lives in a 1024 x 1024 design space and is scaled to the view size,
 * so it works on phones and tablets. Paint, Path and PathMeasure objects are created once.
 */
public class IntroTreeView extends View {

    // Region of the 1024 design space that contains the whole symbol.
    private static final float DESIGN_LEFT = 190f;
    private static final float DESIGN_TOP = 190f;
    private static final float DESIGN_SIZE = 644f;

    // Roots: stroke widths. Paths are built in buildPaths().
    private static final float[] ROOT_WIDTH = {26f, 22f, 24f, 22f, 26f};

    // Canopy circles: cx, cy, r.
    private static final float[][] CANOPY = {
            {512f, 350f, 135f}, {400f, 430f, 105f}, {624f, 430f, 105f}, {512f, 452f, 120f}};
    private static final float[][] HIGHLIGHT = {{470f, 300f, 62f}, {352f, 395f, 44f}};
    private static final float[][] SHADE = {{578f, 468f, 46f}};
    // People nodes: cx, cy, r.
    private static final float[][] NODES = {
            {512f, 300f, 30f}, {404f, 394f, 26f}, {620f, 394f, 26f}, {386f, 480f, 24f}, {638f, 480f, 24f}};

    private interface FloatSetter {
        void set(float value);
    }

    private final Path[] rootPaths = new Path[5];
    private final PathMeasure[] rootMeasures = new PathMeasure[5];
    private final float[] rootLengths = new float[5];
    private final Path[] branchPaths = new Path[5];
    private final PathMeasure[] branchMeasures = new PathMeasure[5];
    private final float[] branchLengths = new float[5];
    private final Path trunkPath = new Path();
    private final Path trunkLine = new Path();
    private final Path segment = new Path();

    private final Paint rootPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trunkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trunkLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint seedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint canopyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint branchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nodeFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nodeRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final TimeInterpolator overshoot = new OvershootInterpolator(2f);
    private final TimeInterpolator decelerate = new DecelerateInterpolator();

    // Animation progress values (0 = hidden, 1 = fully drawn).
    private float seedP;
    private final float[] rootP = new float[5];
    private float trunkP;
    private final float[] canopyP = new float[4];
    private float detailP;
    private final float[] branchP = new float[5];
    private final float[] nodeP = new float[5];

    public IntroTreeView(@NonNull Context context) {
        this(context, null);
    }

    public IntroTreeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IntroTreeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaints(context);
        buildPaths();
    }

    private void initPaints(Context c) {
        int green = ContextCompat.getColor(c, R.color.intro_green);
        int greenLight = ContextCompat.getColor(c, R.color.intro_green_light);
        int greenDark = ContextCompat.getColor(c, R.color.intro_green_dark);
        int brown = ContextCompat.getColor(c, R.color.intro_brown);
        int brownDark = ContextCompat.getColor(c, R.color.intro_brown_dark);
        int gold = ContextCompat.getColor(c, R.color.intro_gold);
        int cream = ContextCompat.getColor(c, R.color.intro_cream);

        rootPaint.setStyle(Paint.Style.STROKE);
        rootPaint.setStrokeCap(Paint.Cap.ROUND);
        rootPaint.setStrokeJoin(Paint.Join.ROUND);
        rootPaint.setColor(brown);

        trunkPaint.setStyle(Paint.Style.FILL);
        trunkPaint.setColor(brown);

        trunkLinePaint.setStyle(Paint.Style.STROKE);
        trunkLinePaint.setStrokeCap(Paint.Cap.ROUND);
        trunkLinePaint.setStrokeWidth(6f);
        trunkLinePaint.setColor(brownDark);
        trunkLinePaint.setAlpha(128);

        seedPaint.setStyle(Paint.Style.FILL);
        seedPaint.setColor(gold);

        canopyPaint.setStyle(Paint.Style.FILL);
        canopyPaint.setColor(green);

        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setColor(greenLight);

        shadePaint.setStyle(Paint.Style.FILL);
        shadePaint.setColor(greenDark);

        branchPaint.setStyle(Paint.Style.STROKE);
        branchPaint.setStrokeCap(Paint.Cap.ROUND);
        branchPaint.setStrokeJoin(Paint.Join.ROUND);
        branchPaint.setStrokeWidth(13f);
        branchPaint.setColor(cream);

        nodeFillPaint.setStyle(Paint.Style.FILL);
        nodeFillPaint.setColor(gold);

        nodeRingPaint.setStyle(Paint.Style.STROKE);
        nodeRingPaint.setStrokeWidth(8f);
        nodeRingPaint.setColor(cream);
    }

    private void buildPaths() {
        for (int i = 0; i < 5; i++) {
            rootPaths[i] = new Path();
            branchPaths[i] = new Path();
        }
        // Roots
        rootPaths[0].moveTo(480f, 648f);
        rootPaths[0].cubicTo(440f, 684f, 398f, 706f, 348f, 764f);
        rootPaths[1].moveTo(495f, 655f);
        rootPaths[1].cubicTo(470f, 702f, 452f, 742f, 432f, 792f);
        rootPaths[2].moveTo(512f, 655f);
        rootPaths[2].lineTo(512f, 802f);
        rootPaths[3].moveTo(529f, 655f);
        rootPaths[3].cubicTo(554f, 702f, 572f, 742f, 592f, 792f);
        rootPaths[4].moveTo(544f, 648f);
        rootPaths[4].cubicTo(584f, 684f, 626f, 706f, 676f, 764f);

        // Trunk
        trunkPath.moveTo(484f, 540f);
        trunkPath.lineTo(540f, 540f);
        trunkPath.cubicTo(538f, 590f, 550f, 624f, 576f, 654f);
        trunkPath.lineTo(448f, 654f);
        trunkPath.cubicTo(474f, 624f, 486f, 590f, 484f, 540f);
        trunkPath.close();
        trunkLine.moveTo(512f, 560f);
        trunkLine.lineTo(512f, 640f);

        // Branches inside the canopy
        branchPaths[0].moveTo(512f, 562f);
        branchPaths[0].cubicTo(512f, 480f, 512f, 380f, 512f, 304f);
        branchPaths[1].moveTo(512f, 530f);
        branchPaths[1].cubicTo(500f, 478f, 452f, 432f, 408f, 398f);
        branchPaths[2].moveTo(512f, 530f);
        branchPaths[2].cubicTo(524f, 478f, 572f, 432f, 616f, 398f);
        branchPaths[3].moveTo(512f, 552f);
        branchPaths[3].cubicTo(470f, 524f, 430f, 498f, 390f, 480f);
        branchPaths[4].moveTo(512f, 552f);
        branchPaths[4].cubicTo(554f, 524f, 594f, 498f, 634f, 480f);

        for (int i = 0; i < 5; i++) {
            rootMeasures[i] = new PathMeasure(rootPaths[i], false);
            rootLengths[i] = rootMeasures[i].getLength();
            branchMeasures[i] = new PathMeasure(branchPaths[i], false);
            branchLengths[i] = branchMeasures[i].getLength();
        }
    }

    // ---------------------------------------------------------------- drawing

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        float s = Math.min(w, h) / DESIGN_SIZE;
        float dx = (w - DESIGN_SIZE * s) / 2f - DESIGN_LEFT * s;
        float dy = (h - DESIGN_SIZE * s) / 2f - DESIGN_TOP * s;

        canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(s, s);

        // 1. roots grow downward
        for (int i = 0; i < 5; i++) {
            rootPaint.setStrokeWidth(ROOT_WIDTH[i]);
            drawGrown(canvas, rootPaths[i], rootMeasures[i], rootLengths[i], rootP[i], rootPaint);
        }

        // 2. trunk rises from the roots (revealed bottom to top)
        float t = clamp(trunkP);
        if (t > 0f) {
            canvas.save();
            float top = 654f - 114f * t;
            canvas.clipRect(400f, top, 620f, 700f);
            canvas.drawPath(trunkPath, trunkPaint);
            canvas.drawPath(trunkLine, trunkLinePaint);
            canvas.restore();
        }

        // 3. gold seed, fades as the trunk appears
        float seedAlpha = 1f - clamp(trunkP * 2f);
        if (seedP > 0f && seedAlpha > 0f) {
            seedPaint.setAlpha((int) (255f * seedAlpha));
            canvas.drawCircle(512f, 650f, 22f * seedP, seedPaint);
        }

        // 4. canopy circles pop in one after another
        for (int i = 0; i < CANOPY.length; i++) {
            if (canopyP[i] <= 0f) continue;
            canvas.drawCircle(CANOPY[i][0], CANOPY[i][1], CANOPY[i][2] * canopyP[i], canopyPaint);
        }

        // 5. soft highlight and shade
        float d = clamp(detailP);
        if (d > 0f) {
            highlightPaint.setAlpha((int) (255f * 0.55f * d));
            for (float[] c : HIGHLIGHT) canvas.drawCircle(c[0], c[1], c[2], highlightPaint);
            shadePaint.setAlpha((int) (255f * 0.35f * d));
            for (float[] c : SHADE) canvas.drawCircle(c[0], c[1], c[2], shadePaint);
        }

        // 6. cream branches grow outward
        for (int i = 0; i < 5; i++) {
            drawGrown(canvas, branchPaths[i], branchMeasures[i], branchLengths[i], branchP[i], branchPaint);
        }

        // 7. gold people nodes pop in
        for (int i = 0; i < NODES.length; i++) {
            if (nodeP[i] <= 0f) continue;
            float r = NODES[i][2] * nodeP[i];
            canvas.drawCircle(NODES[i][0], NODES[i][1], r, nodeFillPaint);
            canvas.drawCircle(NODES[i][0], NODES[i][1], r, nodeRingPaint);
        }

        canvas.restore();
    }

    private void drawGrown(Canvas canvas, Path full, PathMeasure pm, float length, float p, Paint paint) {
        float c = clamp(p);
        if (c <= 0f) return;
        if (c >= 1f) {
            canvas.drawPath(full, paint);
        } else {
            segment.rewind();
            pm.getSegment(0f, length * c, segment, true);
            canvas.drawPath(segment, paint);
        }
    }

    private static float clamp(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    // ---------------------------------------------------------------- animation

    /** Jump to the finished state (used for the short intro, skip, and reduced motion). */
    public void setAllProgress(float value) {
        seedP = value;
        trunkP = value;
        detailP = value;
        for (int i = 0; i < 5; i++) {
            rootP[i] = value;
            branchP[i] = value;
            nodeP[i] = value;
        }
        for (int i = 0; i < canopyP.length; i++) canopyP[i] = value;
        invalidate();
    }

    /**
     * Builds the full grow animation (about 1.9 seconds): seed, roots, trunk, canopy,
     * highlights, branches, then gold nodes. Call setAllProgress(0f) before starting it.
     */
    public AnimatorSet createGrowAnimator() {
        List<Animator> list = new ArrayList<>();

        list.add(anim(0, 350, overshoot, v -> seedP = v));
        for (int i = 0; i < 5; i++) {
            final int index = i;
            list.add(anim(200 + i * 60L, 550, decelerate, v -> rootP[index] = v));
        }
        list.add(anim(500, 500, decelerate, v -> trunkP = v));
        for (int i = 0; i < canopyP.length; i++) {
            final int index = i;
            list.add(anim(800 + i * 80L, 320, overshoot, v -> canopyP[index] = v));
        }
        list.add(anim(1100, 400, decelerate, v -> detailP = v));
        for (int i = 0; i < 5; i++) {
            final int index = i;
            list.add(anim(1100 + i * 70L, 350, decelerate, v -> branchP[index] = v));
        }
        for (int i = 0; i < 5; i++) {
            final int index = i;
            list.add(anim(1350 + i * 70L, 300, overshoot, v -> nodeP[index] = v));
        }

        AnimatorSet set = new AnimatorSet();
        set.playTogether(list);
        return set;
    }

    private ValueAnimator anim(long delay, long duration, TimeInterpolator interpolator,
                               final FloatSetter setter) {
        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setStartDelay(delay);
        a.setDuration(duration);
        a.setInterpolator(interpolator);
        a.addUpdateListener(animation -> {
            setter.set((Float) animation.getAnimatedValue());
            invalidate();
        });
        return a;
    }
}
