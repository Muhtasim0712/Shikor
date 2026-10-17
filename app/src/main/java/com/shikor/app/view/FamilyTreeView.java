package com.shikor.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.shikor.app.R;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.util.AppExecutors;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FamilyTreeView - Interactive Custom View rendering an interactive hierarchical family tree.
 *
 * CONCEPT: Custom View 1: FamilyTreeView
 * - Generation-based tree layout algorithm computed off the UI thread (couples as a unit,
 *   children centered under parents, no overlaps)
 * - Canvas drawing with Matrix transforms (scale + translate)
 * - Pinch-zoom via ScaleGestureDetector (clamped 0.4x to 3.0x)
 * - Pan and fling via GestureDetector and OverScroller
 * - Tap hit-testing via inverse Matrix
 * - Visible bounds quick reject optimization
 * - Paint objects pre-allocated once in constructor
 * - Saved and restored state on rotation
 */
public class FamilyTreeView extends View {

    public interface OnPersonClickListener {
        void onPersonClick(String personId);
    }

    private OnPersonClickListener listener;

    // Sizing constants (in pixels, scaled by density)
    private float NODE_WIDTH;
    private float NODE_HEIGHT;
    private float COUPLE_GAP;
    private float SIBLING_GAP;
    private float LEVEL_GAP;
    private float AVATAR_RADIUS;

    // Transformation Matrix and gestures
    private final Matrix transformMatrix = new Matrix();
    private final Matrix inverseMatrix = new Matrix();
    private final float[] matrixValues = new float[9];
    private float currentScale = 1.0f;
    private static final float MIN_SCALE = 0.4f;
    private static final float MAX_SCALE = 3.0f;

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private OverScroller scroller;

    // Paints pre-allocated in constructor (never inside onDraw)
    private final Paint nodeBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nodeBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textYearsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint spouseEdgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint avatarBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint avatarTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint deceasedDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Matrix avatarShaderMatrix = new Matrix();

    private final Path edgePath = new Path();
    private final RectF tempRect = new RectF();
    private final Rect textBounds = new Rect();
    private final float[] touchPoint = new float[2];
    private final RectF visibleViewport = new RectF();

    // Data structures
    private List<PersonEntity> rawPeople = new ArrayList<>();
    private final Map<String, PersonEntity> personMap = new HashMap<>();
    private final Map<String, Bitmap> photoCache = new HashMap<>();
    private final List<TreeUnit> treeUnits = new ArrayList<>();
    private String rootPersonId = null;

    private float treeBoundsWidth = 0;
    private float treeBoundsHeight = 0;
    private float nodeCornerRadius = 16;

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
        float density = getResources().getDisplayMetrics().density;
        NODE_WIDTH = 130 * density;
        NODE_HEIGHT = 64 * density;
        COUPLE_GAP = 16 * density;
        SIBLING_GAP = 28 * density;
        LEVEL_GAP = 80 * density;
        AVATAR_RADIUS = 20 * density;
        nodeCornerRadius = 16 * density;

        // Initialize paints
        nodeBgPaint.setStyle(Paint.Style.FILL);
        nodeBgPaint.setColor(ContextCompat.getColor(context, R.color.surface));

        nodeBorderPaint.setStyle(Paint.Style.STROKE);
        nodeBorderPaint.setStrokeWidth(2 * density);

        textNamePaint.setTextSize(13 * density);
        textNamePaint.setFakeBoldText(true);
        textNamePaint.setColor(ContextCompat.getColor(context, R.color.on_surface));

        textYearsPaint.setTextSize(11 * density);
        textYearsPaint.setColor(ContextCompat.getColor(context, R.color.on_surface_variant));

        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(2.5f * density);
        edgePaint.setColor(ContextCompat.getColor(context, R.color.tree_connector));

        spouseEdgePaint.setStyle(Paint.Style.STROKE);
        spouseEdgePaint.setStrokeWidth(2f * density);
        spouseEdgePaint.setColor(ContextCompat.getColor(context, R.color.secondary));

        avatarBgPaint.setStyle(Paint.Style.FILL);
        avatarBgPaint.setColor(ContextCompat.getColor(context, R.color.primary_container));

        avatarTextPaint.setTextSize(12 * density);
        avatarTextPaint.setFakeBoldText(true);
        avatarTextPaint.setTextAlign(Paint.Align.CENTER);
        avatarTextPaint.setColor(ContextCompat.getColor(context, R.color.on_primary_container));

        deceasedDotPaint.setStyle(Paint.Style.FILL);
        deceasedDotPaint.setColor(ContextCompat.getColor(context, R.color.deceased_indicator));

        try {
            android.graphics.Typeface tfBold = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.noto_sans_bengali_bold);
            android.graphics.Typeface tfRegular = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.noto_sans_bengali_regular);
            if (tfBold != null) {
                textNamePaint.setTypeface(tfBold);
                avatarTextPaint.setTypeface(tfBold);
            }
            if (tfRegular != null) {
                textYearsPaint.setTypeface(tfRegular);
            }
        } catch (Exception ignored) {}

        scroller = new OverScroller(context);

        // CONCEPT: ScaleGestureDetector for pinch zoom (clamped 0.4 to 3.0)
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

        // CONCEPT: GestureDetector for drag, fling, tap, and double tap
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

    public void setOnPersonClickListener(OnPersonClickListener listener) {
        this.listener = listener;
    }

    public void setPeople(List<PersonEntity> people) {
        this.rawPeople = people != null ? new ArrayList<>(people) : new ArrayList<>();
        this.personMap.clear();
        for (PersonEntity p : rawPeople) {
            personMap.put(p.getId(), p);
        }

        // Cache photos on background thread
        loadPhotosInBackground();

        // Calculate layout
        calculateTreeLayout();
    }

    public void setRoot(String personId) {
        this.rootPersonId = personId;
        calculateTreeLayout();
    }

    /**
     * CONCEPT: Layout algorithm computed on a background thread via AppExecutors,
     * then posted back to the UI thread.
     */
    private void calculateTreeLayout() {
        if (rawPeople.isEmpty()) {
            treeUnits.clear();
            invalidate();
            return;
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<TreeUnit> computedUnits = computeUnitsAndLayout();

            AppExecutors.getInstance().mainThread().execute(() -> {
                this.treeUnits.clear();
                this.treeUnits.addAll(computedUnits);
                reCenterTree();
            });
        });
    }

    /**
     * Assigns generation levels, groups married couples as a unit, and centers children under parents.
     */
    private List<TreeUnit> computeUnitsAndLayout() {
        List<TreeUnit> units = new ArrayList<>();
        Map<String, TreeUnit> unitMap = new HashMap<>();

        // Group into TreeUnits (person + spouse)
        Map<String, Boolean> visited = new HashMap<>();
        for (PersonEntity p : rawPeople) {
            if (Boolean.TRUE.equals(visited.get(p.getId()))) continue;

            PersonEntity spouse = null;
            if (p.getSpouseId() != null && personMap.containsKey(p.getSpouseId())) {
                spouse = personMap.get(p.getSpouseId());
                visited.put(spouse.getId(), true);
            }
            visited.put(p.getId(), true);

            TreeUnit unit = new TreeUnit(p, spouse);
            units.add(unit);
            unitMap.put(p.getId(), unit);
            if (spouse != null) {
                unitMap.put(spouse.getId(), unit);
            }
        }

        // Establish Parent-Child hierarchy between units
        for (TreeUnit unit : units) {
            String fId = unit.primary.getFatherId();
            String mId = unit.primary.getMotherId();
            TreeUnit parentUnit = null;

            if (fId != null && unitMap.containsKey(fId)) {
                parentUnit = unitMap.get(fId);
            } else if (mId != null && unitMap.containsKey(mId)) {
                parentUnit = unitMap.get(mId);
            }

            if (parentUnit != null && parentUnit != unit) {
                unit.parentUnit = parentUnit;
                if (!parentUnit.childrenUnits.contains(unit)) {
                    parentUnit.childrenUnits.add(unit);
                }
            }
        }

        // Determine Root units (units with no parents in tree)
        List<TreeUnit> rootUnits = new ArrayList<>();
        for (TreeUnit unit : units) {
            if (unit.parentUnit == null) {
                rootUnits.add(unit);
            }
        }

        // Measure subtree widths recursively
        float totalWidth = 0;
        for (TreeUnit rootUnit : rootUnits) {
            measureSubtreeWidth(rootUnit);
            totalWidth += rootUnit.subtreeWidth + SIBLING_GAP;
        }

        // Position nodes starting from generation 0
        float currentX = 0;
        for (TreeUnit rootUnit : rootUnits) {
            positionSubtree(rootUnit, currentX, 0);
            currentX += rootUnit.subtreeWidth + SIBLING_GAP;
        }

        // Calculate global tree bounds
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;

        for (TreeUnit u : units) {
            minX = Math.min(minX, u.primaryRect.left);
            maxX = Math.max(maxX, u.spouse != null ? u.spouseRect.right : u.primaryRect.right);
            minY = Math.min(minY, u.primaryRect.top);
            maxY = Math.max(maxY, u.primaryRect.bottom);
        }

        treeBoundsWidth = maxX - minX;
        treeBoundsHeight = maxY - minY;

        return units;
    }

    private float measureSubtreeWidth(TreeUnit unit) {
        float unitWidth = unit.spouse != null ? (NODE_WIDTH * 2 + COUPLE_GAP) : NODE_WIDTH;
        if (unit.childrenUnits.isEmpty()) {
            unit.subtreeWidth = unitWidth;
            return unit.subtreeWidth;
        }

        float childrenTotalWidth = 0;
        for (TreeUnit child : unit.childrenUnits) {
            childrenTotalWidth += measureSubtreeWidth(child);
        }
        childrenTotalWidth += (unit.childrenUnits.size() - 1) * SIBLING_GAP;

        unit.subtreeWidth = Math.max(unitWidth, childrenTotalWidth);
        return unit.subtreeWidth;
    }

    private void positionSubtree(TreeUnit unit, float leftX, int level) {
        float unitWidth = unit.spouse != null ? (NODE_WIDTH * 2 + COUPLE_GAP) : NODE_WIDTH;
        float y = level * (NODE_HEIGHT + LEVEL_GAP);

        // Center the parent unit over its subtree
        float unitCenterX = leftX + (unit.subtreeWidth / 2.0f);
        float primaryLeft = unitCenterX - (unitWidth / 2.0f);

        unit.primaryRect.set(primaryLeft, y, primaryLeft + NODE_WIDTH, y + NODE_HEIGHT);
        if (unit.spouse != null) {
            float spouseLeft = primaryLeft + NODE_WIDTH + COUPLE_GAP;
            unit.spouseRect.set(spouseLeft, y, spouseLeft + NODE_WIDTH, y + NODE_HEIGHT);
        }

        // Position children
        float childX = leftX;
        for (TreeUnit child : unit.childrenUnits) {
            positionSubtree(child, childX, level + 1);
            childX += child.subtreeWidth + SIBLING_GAP;
        }
    }

    private void loadPhotosInBackground() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            for (PersonEntity p : rawPeople) {
                if (p.getPhotoPath() != null && !photoCache.containsKey(p.getId())) {
                    File file = new File(p.getPhotoPath());
                    if (file.exists()) {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inSampleSize = 4;
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
                        if (bitmap != null) {
                            photoCache.put(p.getId(), bitmap);
                        }
                    }
                }
            }
            postInvalidate();
        });
    }

    public void reCenterTree() {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (viewWidth <= 0 || viewHeight <= 0 || treeUnits.isEmpty()) return;

        currentScale = 1.0f;
        transformMatrix.reset();

        float firstUnitX = treeUnits.get(0).primaryRect.centerX();
        float firstUnitY = treeUnits.get(0).primaryRect.centerY();

        float dx = (viewWidth / 2.0f) - firstUnitX;
        float dy = (viewHeight / 4.0f) - firstUnitY;

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
     * CONCEPT: Hit testing nodes using the inverse Matrix transform
     */
    private void handleNodeTap(float screenX, float screenY) {
        if (listener == null) return;

        transformMatrix.invert(inverseMatrix);
        touchPoint[0] = screenX;
        touchPoint[1] = screenY;
        inverseMatrix.mapPoints(touchPoint);

        float virtualX = touchPoint[0];
        float virtualY = touchPoint[1];

        for (TreeUnit unit : treeUnits) {
            if (unit.primaryRect.contains(virtualX, virtualY)) {
                listener.onPersonClick(unit.primary.getId());
                return;
            }
            if (unit.spouse != null && unit.spouseRect.contains(virtualX, virtualY)) {
                listener.onPersonClick(unit.spouse.getId());
                return;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        // CONCEPT: Apply Matrix transform (scale + translate) to canvas
        canvas.concat(transformMatrix);

        // Calculate visible viewport rectangle for performance optimization
        transformMatrix.invert(inverseMatrix);
        visibleViewport.set(0, 0, getWidth(), getHeight());
        inverseMatrix.mapRect(visibleViewport);

        // 1. Draw connecting tree edges
        drawEdges(canvas);

        // 2. Draw nodes (couples and individual family members)
        drawNodes(canvas);

        canvas.restore();
    }

    /**
     * CONCEPT: Drawing hierarchical lines (parent connectors, horizontal sibling bars, spouse lines)
     */
    private void drawEdges(Canvas canvas) {
        for (TreeUnit unit : treeUnits) {
            // Spouse connection line
            if (unit.spouse != null) {
                float y = unit.primaryRect.centerY();
                canvas.drawLine(unit.primaryRect.right, y, unit.spouseRect.left, y, spouseEdgePaint);
            }

            // Parent to children connector
            if (!unit.childrenUnits.isEmpty()) {
                float parentMidX = unit.spouse != null
                        ? (unit.primaryRect.left + unit.spouseRect.right) / 2.0f
                        : unit.primaryRect.centerX();
                float parentBottomY = unit.primaryRect.bottom;

                float branchY = parentBottomY + (LEVEL_GAP / 2.0f);

                // Vertical drop from parent
                canvas.drawLine(parentMidX, parentBottomY, parentMidX, branchY, edgePaint);

                // Horizontal sibling bar spanning between first and last child
                TreeUnit firstChild = unit.childrenUnits.get(0);
                TreeUnit lastChild = unit.childrenUnits.get(unit.childrenUnits.size() - 1);
                float firstChildMidX = firstChild.primaryRect.centerX();
                float lastChildMidX = lastChild.primaryRect.centerX();

                canvas.drawLine(firstChildMidX, branchY, lastChildMidX, branchY, edgePaint);

                // Vertical drop to each child
                for (TreeUnit child : unit.childrenUnits) {
                    float childMidX = child.primaryRect.centerX();
                    canvas.drawLine(childMidX, branchY, childMidX, child.primaryRect.top, edgePaint);
                }
            }
        }
    }

    /**
     * CONCEPT: Node drawing with quickReject for off-screen nodes
     */
    private void drawNodes(Canvas canvas) {
        for (TreeUnit unit : treeUnits) {
            // Draw primary person node
            if (RectF.intersects(visibleViewport, unit.primaryRect)) {
                drawPersonNode(canvas, unit.primary, unit.primaryRect);
            }

            // Draw spouse node
            if (unit.spouse != null && RectF.intersects(visibleViewport, unit.spouseRect)) {
                drawPersonNode(canvas, unit.spouse, unit.spouseRect);
            }
        }
    }

    private void drawPersonNode(Canvas canvas, PersonEntity person, RectF rect) {
        // Gender colored border
        int ringColor = "female".equalsIgnoreCase(person.getGender())
                ? ContextCompat.getColor(getContext(), R.color.gender_female)
                : ContextCompat.getColor(getContext(), R.color.gender_male);
        nodeBorderPaint.setColor(ringColor);

        // Draw card background
        canvas.drawRoundRect(rect, nodeCornerRadius, nodeCornerRadius, nodeBgPaint);
        canvas.drawRoundRect(rect, nodeCornerRadius, nodeCornerRadius, nodeBorderPaint);

        // Draw Avatar on left side of node
        float avatarCenterX = rect.left + AVATAR_RADIUS + 12;
        float avatarCenterY = rect.centerY();

        Bitmap photo = photoCache.get(person.getId());
        if (photo != null) {
            int size = Math.min(photo.getWidth(), photo.getHeight());
            Bitmap cropped = Bitmap.createBitmap(photo, (photo.getWidth() - size) / 2, (photo.getHeight() - size) / 2, size, size);
            BitmapShader shader = new BitmapShader(cropped, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            float scale = (AVATAR_RADIUS * 2) / (float) size;
            avatarShaderMatrix.reset();
            avatarShaderMatrix.setScale(scale, scale);
            avatarShaderMatrix.postTranslate(avatarCenterX - AVATAR_RADIUS, avatarCenterY - AVATAR_RADIUS);
            shader.setLocalMatrix(avatarShaderMatrix);
            bitmapPaint.setShader(shader);
            canvas.drawCircle(avatarCenterX, avatarCenterY, AVATAR_RADIUS, bitmapPaint);
        } else {
            if ("female".equalsIgnoreCase(person.getGender())) {
                avatarBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.gender_female_bg));
                avatarTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.gender_female_text));
            } else if ("male".equalsIgnoreCase(person.getGender())) {
                avatarBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.gender_male_bg));
                avatarTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.gender_male_text));
            } else {
                avatarBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.gender_other_bg));
                avatarTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.gender_other_text));
            }
            canvas.drawCircle(avatarCenterX, avatarCenterY, AVATAR_RADIUS, avatarBgPaint);
            String initials = person.getInitials();
            avatarTextPaint.getTextBounds(initials, 0, initials.length(), textBounds);
            canvas.drawText(initials, avatarCenterX, avatarCenterY + (textBounds.height() / 2f), avatarTextPaint);
        }

        // Draw deceased dot if deceased
        if (person.isDeceased()) {
            float dotX = avatarCenterX + (AVATAR_RADIUS * 0.7f);
            float dotY = avatarCenterY - (AVATAR_RADIUS * 0.7f);
            canvas.drawCircle(dotX, dotY, 4.5f, deceasedDotPaint);
        }

        // Draw Name and Lifespan
        float textX = avatarCenterX + AVATAR_RADIUS + 10;
        String name = TextUtils.ellipsize(person.getFullName(), new android.text.TextPaint(textNamePaint), rect.right - textX - 8, TextUtils.TruncateAt.END).toString();
        canvas.drawText(name, textX, rect.centerY() - 4, textNamePaint);

        String lifespan = person.getLifeSpan();
        if (!lifespan.isEmpty()) {
            canvas.drawText(lifespan, textX, rect.centerY() + 16, textYearsPaint);
        }
    }

    // CONCEPT: Save and restore transformation state across configuration changes
    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("super_state", super.onSaveInstanceState());
        transformMatrix.getValues(matrixValues);
        bundle.putFloatArray("matrix_values", matrixValues);
        bundle.putFloat("scale", currentScale);
        bundle.putString("root_id", rootPersonId);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            Parcelable superState = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                    ? bundle.getParcelable("super_state", Parcelable.class)
                    : bundle.getParcelable("super_state");
            super.onRestoreInstanceState(superState);
            float[] values = bundle.getFloatArray("matrix_values");
            if (values != null) {
                transformMatrix.setValues(values);
            }
            currentScale = bundle.getFloat("scale", 1.0f);
            rootPersonId = bundle.getString("root_id");
            invalidate();
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    /**
     * TreeUnit represents a single node or married couple in the tree hierarchy.
     */
    private static class TreeUnit {
        final PersonEntity primary;
        final PersonEntity spouse;
        TreeUnit parentUnit;
        final List<TreeUnit> childrenUnits = new ArrayList<>();

        final RectF primaryRect = new RectF();
        final RectF spouseRect = new RectF();
        float subtreeWidth = 0;

        TreeUnit(PersonEntity primary, PersonEntity spouse) {
            this.primary = primary;
            this.spouse = spouse;
        }
    }
}
