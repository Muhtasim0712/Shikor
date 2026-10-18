package com.shikor.app.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.Prefs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FamilyTreeView - Interactive Canvas View rendering an authentic living family tree or classic chart.
 *
 * CONCEPT:
 * - Shikor means 'root': oldest ancestors are roots and trunk at the bottom, newer generations grow upward into branches.
 * - Non-overlapping layout calculated off the UI thread via TreeLayoutEngine.
 * - Static layers (trunk, roots, branches, foliage) pre-recorded to android.graphics.Picture display-list cache.
 * - Hardware-accelerated Canvas drawing with Matrix transforms (pinch-zoom 0.4x - 3.0x, pan, fling).
 * - Organic branch sway animation via ValueAnimator (respects system animation scale, pauses offscreen).
 * - Exact tap hit-testing via inverse transformation and sway matrices.
 * - Double-tap to re-center, fitWholeTree() for one-tap overview.
 * - Toggleable between Natural Tree view and Classic Chart view, persisted in Prefs.
 * - Rotation state saved and restored across configuration changes.
 */
public class FamilyTreeView extends View {

    public interface OnPersonClickListener {
        void onPersonClick(String personId);
    }

    private OnPersonClickListener listener;

    // View Modes
    private String viewMode = Prefs.VIEW_MODE_NATURAL_TREE;

    // Transformation Matrix and Gestures
    private final Matrix transformMatrix = new Matrix();
    private final Matrix inverseMatrix = new Matrix();
    private final Matrix swayMatrix = new Matrix();
    private final float[] matrixValues = new float[9];
    private float currentScale = 1.0f;
    private static final float MIN_SCALE = 0.4f;
    private static final float MAX_SCALE = 3.0f;

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private OverScroller scroller;

    // Canvas & Viewport
    private final RectF visibleViewport = new RectF();
    private final float[] touchPoint = new float[2];
    private float density;

    // Data Structures
    private List<PersonEntity> rawPeople = new ArrayList<>();
    private final Map<String, PersonEntity> personMap = new HashMap<>();
    private final Map<String, Bitmap> photoCache = new HashMap<>();
    private TreeLayoutEngine.LayoutResult layoutResult = new TreeLayoutEngine.LayoutResult();
    private String rootPersonId = null;
    private String selectedPersonId = null;

    // Renderer & Static Cache
    private TreePainter treePainter;
    private Picture staticLayerPicture = null;

    // Canopy Sway Animator
    private ValueAnimator swayAnimator;
    private float currentSwayAngle = 0f;

    public FamilyTreeView(Context context) {
        super(context);
        init(context);
    }

    public FamilyTreeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FamilyTreeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        density = getResources().getDisplayMetrics().density;
        viewMode = Prefs.getInstance(context).getTreeViewMode();

        treePainter = new TreePainter(context);
        scroller = new OverScroller(context);

        initGestureDetectors(context);
        initSwayAnimator();
    }

    private void initGestureDetectors(Context context) {
        // Pinch-zoom via ScaleGestureDetector (clamped 0.4x to 3.0x)
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                float factor = detector.getScaleFactor();
                float newScale = currentScale * factor;
                if (newScale >= MIN_SCALE && newScale <= MAX_SCALE) {
                    currentScale = newScale;
                    transformMatrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
                    invalidate();
                }
                return true;
            }
        });

        // Pan, fling, tap, and double tap
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                transformMatrix.postTranslate(-distanceX, -distanceY);
                invalidate();
                return true;
            }

            @Override
            public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                scroller.fling(0, 0, (int) velocityX, (int) velocityY,
                        Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
                postInvalidateOnAnimation();
                return true;
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                reCenterTree();
                return true;
            }

            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent e) {
                handleNodeTap(e.getX(), e.getY());
                return true;
            }
        });
    }

    private void initSwayAnimator() {
        if (!isAnimationScaleEnabled()) {
            currentSwayAngle = 0f;
            return;
        }

        swayAnimator = ValueAnimator.ofFloat(-0.35f, 0.35f);
        swayAnimator.setDuration(3600); // 3.6s gentle dawn breeze oscillation
        swayAnimator.setRepeatMode(ValueAnimator.REVERSE);
        swayAnimator.setRepeatCount(ValueAnimator.INFINITE);
        swayAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        swayAnimator.addUpdateListener(animation -> {
            if (isNaturalTreeMode()) {
                currentSwayAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    private boolean isAnimationScaleEnabled() {
        try {
            float scale = Settings.Global.getFloat(
                    getContext().getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1.0f
            );
            return scale > 0.05f;
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isNaturalTreeMode() {
        return Prefs.VIEW_MODE_NATURAL_TREE.equals(viewMode);
    }

    private boolean isNightMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    public void setOnPersonClickListener(OnPersonClickListener listener) {
        this.listener = listener;
    }

    public void setPeople(List<PersonEntity> people) {
        this.rawPeople = people != null ? new ArrayList<>(people) : new ArrayList<>();
        this.personMap.clear();
        for (PersonEntity p : rawPeople) {
            personMap.put(p.getId(), p);
        }

        loadPhotosInBackground();
        calculateTreeLayout(true);
    }

    public void setRoot(String personId) {
        this.rootPersonId = personId;
        calculateTreeLayout(true);
    }

    public void setSelectedPersonId(String personId) {
        this.selectedPersonId = personId;
        invalidate();
    }

    public String getViewMode() {
        return viewMode;
    }

    public void setViewMode(String mode) {
        if (this.viewMode.equals(mode)) return;
        this.viewMode = mode;
        Prefs.getInstance(getContext()).setTreeViewMode(mode);

        if (isNaturalTreeMode()) {
            startSwayAnimation();
        } else {
            stopSwayAnimation();
            currentSwayAngle = 0f;
        }

        calculateTreeLayout(true);
    }

    /**
     * Calculates the non-overlapping layout off the UI thread and pre-records static display-lists.
     */
    private void calculateTreeLayout(boolean shouldRecenter) {
        if (rawPeople.isEmpty()) {
            layoutResult = new TreeLayoutEngine.LayoutResult();
            staticLayerPicture = null;
            invalidate();
            return;
        }

        final boolean natural = isNaturalTreeMode();
        final String rootId = this.rootPersonId;
        final List<PersonEntity> peopleSnapshot = new ArrayList<>(this.rawPeople);

        AppExecutors.getInstance().diskIO().execute(() -> {
            TreeLayoutEngine.LayoutResult computedResult = TreeLayoutEngine.calculateLayout(
                    peopleSnapshot,
                    rootId,
                    natural,
                    treePainter.NODE_WIDTH,
                    treePainter.NODE_HEIGHT,
                    treePainter.COUPLE_GAP,
                    treePainter.SIBLING_GAP,
                    treePainter.LEVEL_GAP,
                    density
            );

            // Pre-record static layer Picture off UI thread for 60fps performance
            Picture picture = recordStaticLayersPicture(computedResult, natural, isNightMode());

            AppExecutors.getInstance().mainThread().execute(() -> {
                this.layoutResult = computedResult;
                this.staticLayerPicture = picture;

                if (shouldRecenter) {
                    reCenterTree();
                } else {
                    invalidate();
                }

                if (natural) {
                    startSwayAnimation();
                } else {
                    stopSwayAnimation();
                }
            });
        });
    }

    /**
     * Pre-records static tree structure (roots, trunk, branches, foliage) into an android.graphics.Picture.
     */
    private Picture recordStaticLayersPicture(TreeLayoutEngine.LayoutResult layout, boolean natural, boolean nightMode) {
        if (layout.bounds.isEmpty()) return null;

        Picture picture = new Picture();
        int recW = Math.max(1, (int) Math.ceil(layout.bounds.right + (300 * density)));
        int recH = Math.max(1, (int) Math.ceil(layout.bounds.bottom + (300 * density)));

        Canvas picCanvas = picture.beginRecording(recW, recH);
        if (natural) {
            treePainter.drawNaturalTreeStaticStructure(picCanvas, layout, nightMode);
        } else {
            treePainter.drawClassicChartStaticLayers(picCanvas, layout);
        }
        picture.endRecording();
        return picture;
    }

    private void loadPhotosInBackground() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            boolean updated = false;
            for (PersonEntity p : rawPeople) {
                if (p.getPhotoPath() != null && !photoCache.containsKey(p.getId())) {
                    File file = new File(p.getPhotoPath());
                    if (file.exists()) {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inSampleSize = 4;
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
                        if (bitmap != null) {
                            photoCache.put(p.getId(), bitmap);
                            updated = true;
                        }
                    }
                }
            }
            if (updated) {
                postInvalidate();
            }
        });
    }

    /**
     * Fits the entire family tree within the current view bounds.
     */
    public void fitWholeTree() {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (viewWidth <= 0 || viewHeight <= 0 || layoutResult.bounds.isEmpty()) return;

        float padding = 48 * density;
        float availW = Math.max(10, viewWidth - (2 * padding));
        float availH = Math.max(10, viewHeight - (2 * padding));

        float boundsW = Math.max(1, layoutResult.bounds.width());
        float boundsH = Math.max(1, layoutResult.bounds.height());

        float scaleX = availW / boundsW;
        float scaleY = availH / boundsH;
        float targetScale = Math.min(scaleX, scaleY);

        currentScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, targetScale));

        transformMatrix.reset();
        transformMatrix.postTranslate(-layoutResult.bounds.centerX(), -layoutResult.bounds.centerY());
        transformMatrix.postScale(currentScale, currentScale);
        transformMatrix.postTranslate(viewWidth / 2.0f, viewHeight / 2.0f);

        invalidate();
    }

    /**
     * Centers view on the oldest ancestors / trunk base or selected person.
     */
    public void reCenterTree() {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (viewWidth <= 0 || viewHeight <= 0 || layoutResult.allUnits.isEmpty()) return;

        currentScale = 1.0f;
        transformMatrix.reset();

        float targetX;
        float targetY;

        TreeUnit targetUnit = null;
        if (selectedPersonId != null) {
            for (TreeUnit u : layoutResult.allUnits) {
                if (u.primary.getId().equals(selectedPersonId) ||
                        (u.spouse != null && u.spouse.getId().equals(selectedPersonId))) {
                    targetUnit = u;
                    break;
                }
            }
        }

        if (targetUnit != null) {
            targetX = targetUnit.primaryRect.centerX();
            targetY = targetUnit.primaryRect.centerY();
        } else if (isNaturalTreeMode()) {
            targetX = layoutResult.trunkBaseX;
            targetY = layoutResult.trunkTopY;
        } else {
            targetX = layoutResult.rootUnits.isEmpty()
                    ? layoutResult.allUnits.get(0).primaryRect.centerX()
                    : layoutResult.rootUnits.get(0).primaryRect.centerX();
            targetY = layoutResult.rootUnits.isEmpty()
                    ? layoutResult.allUnits.get(0).primaryRect.centerY()
                    : layoutResult.rootUnits.get(0).primaryRect.centerY();
        }

        float dx = (viewWidth / 2.0f) - targetX;
        float dy = isNaturalTreeMode()
                ? (viewHeight * 0.65f) - targetY
                : (viewHeight / 3.0f) - targetY;

        transformMatrix.postTranslate(dx, dy);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean scaleHandled = scaleDetector.onTouchEvent(event);
        boolean gestureHandled = gestureDetector.onTouchEvent(event);
        return scaleHandled || gestureHandled || super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            transformMatrix.postTranslate(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
            postInvalidateOnAnimation();
        }
    }

    /**
     * Hit testing nodes using inverse Matrix transforms, taking organic branch sway into account.
     */
    private void handleNodeTap(float screenX, float screenY) {
        if (listener == null || layoutResult.allUnits.isEmpty()) return;

        transformMatrix.invert(inverseMatrix);
        touchPoint[0] = screenX;
        touchPoint[1] = screenY;
        inverseMatrix.mapPoints(touchPoint);

        // Account for branch sway angle when in natural tree mode
        if (isNaturalTreeMode() && currentSwayAngle != 0f) {
            swayMatrix.reset();
            swayMatrix.setRotate(-currentSwayAngle, layoutResult.trunkBaseX, layoutResult.groundY);
            swayMatrix.mapPoints(touchPoint);
        }

        float virtualX = touchPoint[0];
        float virtualY = touchPoint[1];

        for (TreeUnit unit : layoutResult.allUnits) {
            if (unit.primaryRect.contains(virtualX, virtualY)) {
                selectedPersonId = unit.primary.getId();
                invalidate();
                listener.onPersonClick(unit.primary.getId());
                return;
            }
            if (unit.spouse != null && unit.spouseRect.contains(virtualX, virtualY)) {
                selectedPersonId = unit.spouse.getId();
                invalidate();
                listener.onPersonClick(unit.spouse.getId());
                return;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (layoutResult.allUnits.isEmpty()) return;

        final boolean natural = isNaturalTreeMode();
        final boolean night = isNightMode();

        canvas.save();
        // 1. Apply Matrix transform (scale + translate) to canvas
        canvas.concat(transformMatrix);

        // Calculate visible viewport rectangle for performance optimization
        transformMatrix.invert(inverseMatrix);
        visibleViewport.set(0, 0, getWidth(), getHeight());
        inverseMatrix.mapRect(visibleViewport);

        // 2. Stationary Natural Background (Sky Gradient, Stars, Earth, Grass Line)
        if (natural) {
            treePainter.drawBackground(canvas, layoutResult, night);
        }

        // 3. Tree Sway Transform
        canvas.save();
        if (natural && currentSwayAngle != 0f) {
            canvas.rotate(currentSwayAngle, layoutResult.trunkBaseX, layoutResult.groundY);
        }

        // 4. Pre-recorded Static Tree Structure from Picture Display-List Cache
        if (staticLayerPicture != null) {
            staticLayerPicture.draw(canvas);
        }

        // 5. Dynamic Leaf / Badge Nodes with quickReject
        for (TreeUnit unit : layoutResult.allUnits) {
            if (RectF.intersects(visibleViewport, unit.primaryRect)) {
                treePainter.drawPersonNode(canvas, unit.primary, unit.primaryRect, photoCache, selectedPersonId, natural);
            }
            if (unit.spouse != null && RectF.intersects(visibleViewport, unit.spouseRect)) {
                treePainter.drawPersonNode(canvas, unit.spouse, unit.spouseRect, photoCache, selectedPersonId, natural);
            }
        }

        canvas.restore(); // Restore sway
        canvas.restore(); // Restore transformMatrix
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startSwayAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopSwayAnimation();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            startSwayAnimation();
        } else {
            stopSwayAnimation();
        }
    }

    private void startSwayAnimation() {
        if (swayAnimator != null && !swayAnimator.isRunning() && isAnimationScaleEnabled() && isNaturalTreeMode()) {
            swayAnimator.start();
        }
    }

    private void stopSwayAnimation() {
        if (swayAnimator != null && swayAnimator.isRunning()) {
            swayAnimator.cancel();
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("super_state", super.onSaveInstanceState());
        transformMatrix.getValues(matrixValues);
        bundle.putFloatArray("matrix_values", matrixValues);
        bundle.putFloat("scale", currentScale);
        bundle.putString("root_id", rootPersonId);
        bundle.putString("view_mode", viewMode);
        bundle.putString("selected_id", selectedPersonId);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            Parcelable superState = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    ? bundle.getParcelable("super_state", Parcelable.class)
                    : bundle.getParcelable("super_state");
            super.onRestoreInstanceState(superState);
            float[] values = bundle.getFloatArray("matrix_values");
            if (values != null) {
                transformMatrix.setValues(values);
            }
            currentScale = bundle.getFloat("scale", 1.0f);
            rootPersonId = bundle.getString("root_id");
            viewMode = bundle.getString("view_mode", Prefs.VIEW_MODE_NATURAL_TREE);
            selectedPersonId = bundle.getString("selected_id");
            invalidate();
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    /**
     * Captures and renders the entire family tree canvas into a high-resolution Bitmap.
     * Perfect for PNG and PDF archival document exports.
     */
    public Bitmap exportFullTreeBitmap() {
        if (layoutResult == null || layoutResult.allUnits.isEmpty() || layoutResult.bounds.isEmpty()) {
            return null;
        }

        RectF bounds = new RectF(layoutResult.bounds);
        float padding = 80f * density;
        bounds.inset(-padding, -padding);

        float w = bounds.width();
        float h = bounds.height();
        if (w <= 0 || h <= 0) return null;

        float maxDim = 3200f;
        float scale = Math.min(1.0f, Math.min(maxDim / w, maxDim / h));
        int bmpW = Math.max(300, (int) (w * scale));
        int bmpH = Math.max(300, (int) (h * scale));

        Bitmap bitmap = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        final boolean natural = isNaturalTreeMode();
        final boolean night = isNightMode();

        Matrix m = new Matrix();
        RectF destRect = new RectF(0, 0, bmpW, bmpH);
        m.setRectToRect(bounds, destRect, Matrix.ScaleToFit.CENTER);

        canvas.save();
        canvas.concat(m);

        if (natural) {
            treePainter.drawBackground(canvas, layoutResult, night);
        } else {
            canvas.drawColor(night ? 0xFF121A15 : 0xFFFBF9F3);
        }

        if (staticLayerPicture != null) {
            staticLayerPicture.draw(canvas);
        }

        for (TreeUnit unit : layoutResult.allUnits) {
            treePainter.drawPersonNode(canvas, unit.primary, unit.primaryRect, photoCache, null, natural);
            if (unit.spouse != null) {
                treePainter.drawPersonNode(canvas, unit.spouse, unit.spouseRect, photoCache, null, natural);
            }
        }

        canvas.restore();
        return bitmap;
    }
}

