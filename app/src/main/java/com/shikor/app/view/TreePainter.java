package com.shikor.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.shikor.app.R;
import com.shikor.app.data.local.entities.PersonEntity;

import java.util.Map;
import java.util.Random;

/**
 * TreePainter - High performance Canvas renderer for FamilyTreeView.
 *
 * CONCEPT: Pre-allocates all Paint, Path, Shader, and Matrix objects once in constructor.
 * Draws natural living tree (sky gradient, grass/ground, roots, textured trunk, tapered cubic branches,
 * spouse vines, leaf badges with readable contrast pills, and decorative canopy foliage)
 * and classic top-down hierarchy charts.
 */
public class TreePainter {

    private final Context context;
    private final float density;

    // Paints (all pre-allocated)
    private final Paint skyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint groundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grassLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint trunkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barkLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint branchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint spouseVinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint decorativeLeafPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint nodeBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nodeBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectionGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pillBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pillStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint avatarBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint avatarTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textYearsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint deceasedMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint deceasedRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    // Reusable Paths & Rects (never inside onDraw)
    private final Path tempBranchPath = new Path();
    private final Path tempRootPath = new Path();
    private final Path tempLeafPath = new Path();
    private final RectF tempRect = new RectF();
    private final RectF tempPillRect = new RectF();
    private final Rect tempTextBounds = new Rect();
    private final Matrix avatarShaderMatrix = new Matrix();

    // Fixed deterministic stars for night mode
    private static final int STAR_COUNT = 32;
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starRadius = new float[STAR_COUNT];
    private final int[] starAlpha = new int[STAR_COUNT];

    // Dimension constants
    public final float NODE_WIDTH;
    public final float NODE_HEIGHT;
    public final float AVATAR_RADIUS;
    public final float COUPLE_GAP;
    public final float SIBLING_GAP;
    public final float LEVEL_GAP;

    public TreePainter(Context context) {
        this.context = context;
        this.density = context.getResources().getDisplayMetrics().density;

        NODE_WIDTH = 136 * density;
        NODE_HEIGHT = 70 * density;
        AVATAR_RADIUS = 20 * density;
        COUPLE_GAP = 18 * density;
        SIBLING_GAP = 32 * density;
        LEVEL_GAP = 96 * density;

        initPaints();
        initStars();
    }

    private void initPaints() {
        // Sky & Ground
        skyPaint.setStyle(Paint.Style.FILL);
        groundPaint.setStyle(Paint.Style.FILL);
        grassLinePaint.setStyle(Paint.Style.STROKE);
        grassLinePaint.setStrokeWidth(3 * density);
        grassLinePaint.setStrokeCap(Paint.Cap.ROUND);

        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setColor(ContextCompat.getColor(context, R.color.tertiary));

        // Trunk & Roots
        trunkPaint.setStyle(Paint.Style.FILL);
        barkLinePaint.setStyle(Paint.Style.STROKE);
        barkLinePaint.setStrokeWidth(1.5f * density);
        barkLinePaint.setStrokeCap(Paint.Cap.ROUND);
        barkLinePaint.setColor(ContextCompat.getColor(context, R.color.secondary_variant));

        // Branches & Leaves
        branchPaint.setStyle(Paint.Style.STROKE);
        branchPaint.setStrokeCap(Paint.Cap.ROUND);
        branchPaint.setStrokeJoin(Paint.Join.ROUND);
        branchPaint.setColor(ContextCompat.getColor(context, R.color.secondary));

        spouseVinePaint.setStyle(Paint.Style.STROKE);
        spouseVinePaint.setStrokeWidth(2.5f * density);
        spouseVinePaint.setStrokeCap(Paint.Cap.ROUND);
        spouseVinePaint.setColor(ContextCompat.getColor(context, R.color.tertiary));

        decorativeLeafPaint.setStyle(Paint.Style.FILL);

        // Nodes
        nodeBgPaint.setStyle(Paint.Style.FILL);
        nodeBorderPaint.setStyle(Paint.Style.STROKE);
        nodeBorderPaint.setStrokeWidth(2.5f * density);

        selectionGlowPaint.setStyle(Paint.Style.STROKE);
        selectionGlowPaint.setStrokeWidth(5f * density);
        selectionGlowPaint.setColor(ContextCompat.getColor(context, R.color.primary));
        selectionGlowPaint.setAlpha(140);

        pillBgPaint.setStyle(Paint.Style.FILL);
        pillStrokePaint.setStyle(Paint.Style.STROKE);
        pillStrokePaint.setStrokeWidth(0.5f * density);
        pillStrokePaint.setColor(ContextCompat.getColor(context, R.color.outline));

        avatarBgPaint.setStyle(Paint.Style.FILL);
        avatarTextPaint.setStyle(Paint.Style.FILL);
        avatarTextPaint.setTextAlign(Paint.Align.CENTER);
        avatarTextPaint.setTextSize(12 * density);

        deceasedMarkerPaint.setStyle(Paint.Style.FILL);
        deceasedMarkerPaint.setColor(ContextCompat.getColor(context, R.color.tertiary));

        deceasedRingPaint.setStyle(Paint.Style.STROKE);
        deceasedRingPaint.setStrokeWidth(2f * density);
        deceasedRingPaint.setColor(ContextCompat.getColor(context, R.color.surface));

        textNamePaint.setStyle(Paint.Style.FILL);
        textNamePaint.setTextSize(12 * density);
        textNamePaint.setColor(ContextCompat.getColor(context, R.color.on_surface));

        textYearsPaint.setStyle(Paint.Style.FILL);
        textYearsPaint.setTextSize(10 * density);
        textYearsPaint.setColor(ContextCompat.getColor(context, R.color.on_surface_variant));

        // Bundle Noto Sans Bengali font for all Canvas text
        try {
            Typeface tfBold = ResourcesCompat.getFont(context, R.font.noto_sans_bengali_bold);
            Typeface tfRegular = ResourcesCompat.getFont(context, R.font.noto_sans_bengali_regular);
            if (tfBold != null) {
                textNamePaint.setTypeface(tfBold);
                avatarTextPaint.setTypeface(tfBold);
            }
            if (tfRegular != null) {
                textYearsPaint.setTypeface(tfRegular);
            }
        } catch (Exception ignored) {}
    }

    private void initStars() {
        Random rand = new Random(1971); // Fixed seed for reproducible stars
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = rand.nextFloat();
            starY[i] = rand.nextFloat();
            starRadius[i] = (1.5f + (rand.nextFloat() * 2.0f)) * density;
            starAlpha[i] = 70 + rand.nextInt(160);
        }
    }

    /**
     * Draws static natural background: sky gradient, night stars, ground earth band, and grass line.
     */
    public void drawBackground(Canvas canvas, TreeLayoutEngine.LayoutResult layout, boolean isNightMode) {
        if (layout.bounds.isEmpty()) return;

        RectF bounds = layout.bounds;
        float groundY = layout.groundY;

        // 1. Sky Gradient
        int skyStart = isNightMode ? Color.parseColor("#0C140E") : Color.parseColor("#E6EFE5");
        int skyEnd = isNightMode ? Color.parseColor("#17241B") : Color.parseColor("#F6F3EA");
        skyPaint.setShader(new LinearGradient(bounds.centerX(), bounds.top, bounds.centerX(), groundY,
                skyStart, skyEnd, Shader.TileMode.CLAMP));
        canvas.drawRect(bounds.left - (400 * density), bounds.top - (400 * density),
                bounds.right + (400 * density), groundY, skyPaint);

        // 2. Night Stars (if dark mode)
        if (isNightMode) {
            float width = bounds.width() + (800 * density);
            float height = groundY - bounds.top + (400 * density);
            for (int i = 0; i < STAR_COUNT; i++) {
                float sx = (bounds.left - 400 * density) + (starX[i] * width);
                float sy = (bounds.top - 200 * density) + (starY[i] * height * 0.75f);
                starPaint.setAlpha(starAlpha[i]);
                canvas.drawCircle(sx, sy, starRadius[i], starPaint);
            }
        }

        // 3. Ground Earth & Grass Line
        int groundStart = isNightMode ? Color.parseColor("#1A261D") : Color.parseColor("#E7DDCD");
        int groundEnd = isNightMode ? Color.parseColor("#121A15") : Color.parseColor("#D4C7B0");
        groundPaint.setShader(new LinearGradient(bounds.centerX(), groundY, bounds.centerX(), bounds.bottom + (400 * density),
                groundStart, groundEnd, Shader.TileMode.CLAMP));
        canvas.drawRect(bounds.left - (400 * density), groundY, bounds.right + (400 * density),
                bounds.bottom + (400 * density), groundPaint);

        // Subtle wavy grass line
        grassLinePaint.setColor(isNightMode ? Color.parseColor("#2D4433") : Color.parseColor("#5A8057"));
        canvas.drawLine(bounds.left - (400 * density), groundY, bounds.right + (400 * density), groundY, grassLinePaint);
    }

    /**
     * Draws static natural living tree structure: trunk, roots, curved branches, spouse vines, and foliage.
     */
    public void drawNaturalTreeStaticStructure(Canvas canvas, TreeLayoutEngine.LayoutResult layout, boolean isNightMode) {
        if (layout.bounds.isEmpty()) return;

        float groundY = layout.groundY;

        // 1. Roots and Trunk below generation 0
        drawTrunkAndRoots(canvas, layout, groundY, isNightMode);

        // 2. Curved, Tapered Branches & Spouse Vines
        for (TreeUnit unit : layout.allUnits) {
            // Spouse connection vine
            if (unit.spouse != null) {
                float midY = unit.primaryRect.centerY();
                float x1 = unit.primaryRect.right;
                float x2 = unit.spouseRect.left;
                canvas.drawLine(x1, midY, x2, midY, spouseVinePaint);

                // Small golden union marker in center
                float midX = (x1 + x2) / 2f;
                canvas.drawCircle(midX, midY, 3.5f * density, spouseVinePaint);
            }

            // Branches from parent to children
            if (!unit.childrenUnits.isEmpty()) {
                float parentStartX = unit.spouse != null
                        ? (unit.primaryRect.left + unit.spouseRect.right) / 2f
                        : unit.primaryRect.centerX();
                float parentStartY = unit.primaryRect.top; // Branch reaches upward from top of parent

                for (TreeUnit child : unit.childrenUnits) {
                    float childEndX = child.spouse != null
                            ? (child.primaryRect.left + child.spouseRect.right) / 2f
                            : child.primaryRect.centerX();
                    float childEndY = child.primaryRect.bottom; // Connects to bottom of child

                    drawCurvedBranch(canvas, parentStartX, parentStartY, childEndX, childEndY,
                            unit.level, child.primary.getId(), isNightMode);
                }
            }
        }
    }

    /**
     * Draws complete static natural background and tree structure (for combined rendering).
     */
    public void drawNaturalTreeStaticLayers(Canvas canvas, TreeLayoutEngine.LayoutResult layout, boolean isNightMode) {
        drawBackground(canvas, layout, isNightMode);
        drawNaturalTreeStaticStructure(canvas, layout, isNightMode);
    }

    /**
     * Draws a thick trunk and curved roots spreading into the ground with LinearGradient bark texture.
     */
    private void drawTrunkAndRoots(Canvas canvas, TreeLayoutEngine.LayoutResult layout, float groundY, boolean isNightMode) {
        float trunkX = layout.trunkBaseX;
        float trunkTopY = layout.trunkTopY;
        float trunkHeight = Math.max(80 * density, groundY - trunkTopY);

        int barkLight = isNightMode ? Color.parseColor("#7D5E43") : Color.parseColor("#8A6A4B");
        int barkDark = isNightMode ? Color.parseColor("#443021") : Color.parseColor("#5C4029");

        trunkPaint.setShader(new LinearGradient(trunkX, trunkTopY, trunkX, groundY,
                barkLight, barkDark, Shader.TileMode.CLAMP));

        // Trunk polygon (flares wider as it meets the ground)
        tempRootPath.reset();
        float topHalfWidth = 14 * density;
        float bottomHalfWidth = 28 * density;

        tempRootPath.moveTo(trunkX - topHalfWidth, trunkTopY);
        tempRootPath.quadTo(trunkX - topHalfWidth - (4 * density), trunkTopY + (trunkHeight * 0.5f),
                trunkX - bottomHalfWidth, groundY);
        tempRootPath.lineTo(trunkX + bottomHalfWidth, groundY);
        tempRootPath.quadTo(trunkX + topHalfWidth + (4 * density), trunkTopY + (trunkHeight * 0.5f),
                trunkX + topHalfWidth, trunkTopY);
        tempRootPath.close();
        canvas.drawPath(tempRootPath, trunkPaint);

        // Bark vertical contour lines
        barkLinePaint.setColor(isNightMode ? Color.parseColor("#38271A") : Color.parseColor("#4A3320"));
        canvas.drawLine(trunkX - (4 * density), trunkTopY + (10 * density),
                trunkX - (8 * density), groundY - (5 * density), barkLinePaint);
        canvas.drawLine(trunkX + (5 * density), trunkTopY + (14 * density),
                trunkX + (10 * density), groundY - (5 * density), barkLinePaint);

        // 4 Spreading Roots into the ground
        drawSingleRoot(canvas, trunkX - (16 * density), groundY, trunkX - (65 * density), groundY + (45 * density), 10 * density);
        drawSingleRoot(canvas, trunkX - (8 * density), groundY, trunkX - (30 * density), groundY + (60 * density), 8 * density);
        drawSingleRoot(canvas, trunkX + (16 * density), groundY, trunkX + (65 * density), groundY + (45 * density), 10 * density);
        drawSingleRoot(canvas, trunkX + (8 * density), groundY, trunkX + (30 * density), groundY + (60 * density), 8 * density);
    }

    private void drawSingleRoot(Canvas canvas, float startX, float startY, float endX, float endY, float width) {
        tempRootPath.reset();
        tempRootPath.moveTo(startX, startY);
        float cpX = (startX + endX) / 2f + ((startX < endX) ? (12 * density) : (-12 * density));
        float cpY = (startY + endY) / 2f;
        tempRootPath.quadTo(cpX, cpY, endX, endY);

        branchPaint.setStrokeWidth(width);
        canvas.drawPath(tempRootPath, branchPaint);
    }

    /**
     * Draws an organic cubic bezier branch connecting parent to child, tapering by generation,
     * with deterministic subtle curvature and decorative canopy leaves.
     */
    private void drawCurvedBranch(
            Canvas canvas,
            float startX, float startY,
            float endX, float endY,
            int generationLevel,
            String childId,
            boolean isNightMode
    ) {
        tempBranchPath.reset();
        tempBranchPath.moveTo(startX, startY);

        float dy = startY - endY; // Upward travel (positive)
        int seed = Math.abs(childId != null ? childId.hashCode() : 42);
        float waveOffset = ((seed % 17) - 8) * density * 1.6f;

        float cp1X = startX + waveOffset;
        float cp1Y = startY - (dy * 0.38f);
        float cp2X = endX - waveOffset;
        float cp2Y = startY - (dy * 0.68f);

        tempBranchPath.cubicTo(cp1X, cp1Y, cp2X, cp2Y, endX, endY);

        // Taper branch thickness by generation level
        float strokeWidth = Math.max(3.5f * density, (12.0f - (generationLevel * 2.2f)) * density);
        branchPaint.setStrokeWidth(strokeWidth);
        canvas.drawPath(tempBranchPath, branchPaint);

        // 2-3 Decorative canopy leaves sprouting along the branch
        drawDecorativeLeavesAlongBranch(canvas, cp1X, cp1Y, cp2X, cp2Y, seed, isNightMode);
    }

    private void drawDecorativeLeavesAlongBranch(Canvas canvas, float x1, float y1, float x2, float y2, int seed, boolean isNightMode) {
        int leafColor = isNightMode ? Color.parseColor("#4D7A58") : Color.parseColor("#5B8C67");
        decorativeLeafPaint.setColor(leafColor);
        decorativeLeafPaint.setAlpha(180);

        // Leaf 1 near first curve point
        float lx1 = x1 + (((seed % 7) - 3) * density * 2f);
        float ly1 = y1 + (((seed % 5) - 2) * density * 2f);
        drawSingleLeafBadge(canvas, lx1, ly1, 8 * density, 5 * density, (seed % 60) - 30);

        // Leaf 2 near second curve point
        float lx2 = x2 - (((seed % 9) - 4) * density * 2f);
        float ly2 = y2 - (((seed % 7) - 3) * density * 2f);
        drawSingleLeafBadge(canvas, lx2, ly2, 7 * density, 4.5f * density, (seed % 80) - 40);
    }

    private void drawSingleLeafBadge(Canvas canvas, float cx, float cy, float w, float h, float angle) {
        canvas.save();
        canvas.rotate(angle, cx, cy);
        tempLeafPath.reset();
        tempLeafPath.moveTo(cx - w, cy);
        tempLeafPath.quadTo(cx, cy - h, cx + w, cy);
        tempLeafPath.quadTo(cx, cy + h, cx - w, cy);
        tempLeafPath.close();
        canvas.drawPath(tempLeafPath, decorativeLeafPaint);
        canvas.restore();
    }

    /**
     * Draws individual person badges on top of static layers.
     * In Natural Tree mode: renders as leaf/blossom badges with readable text pills.
     * In Classic Chart mode: renders as structured family cards.
     */
    public void drawPersonNode(
            Canvas canvas,
            PersonEntity person,
            RectF rect,
            Map<String, Bitmap> photoCache,
            String selectedPersonId,
            boolean isNaturalTreeMode
    ) {
        boolean isSelected = person.getId().equals(selectedPersonId);
        boolean isDeceased = person.isDeceased();

        if (isNaturalTreeMode) {
            drawNaturalLeafNode(canvas, person, rect, photoCache, isSelected, isDeceased);
        } else {
            drawClassicCardNode(canvas, person, rect, photoCache, isSelected, isDeceased);
        }
    }

    private void drawNaturalLeafNode(
            Canvas canvas,
            PersonEntity person,
            RectF rect,
            Map<String, Bitmap> photoCache,
            boolean isSelected,
            boolean isDeceased
    ) {
        float centerX = rect.centerX();
        float topY = rect.top;

        // Leaf/Blossom Badge Frame (Top part has avatar badge, bottom has name pill)
        float avatarCenterX = centerX;
        float avatarCenterY = topY + AVATAR_RADIUS + (4 * density);

        // Selection Glow Ring
        if (isSelected) {
            canvas.drawCircle(avatarCenterX, avatarCenterY, AVATAR_RADIUS + (6 * density), selectionGlowPaint);
        }

        // Circular Leaf/Blossom Badge Border
        int borderColor = isDeceased
                ? ContextCompat.getColor(context, R.color.tertiary)
                : ContextCompat.getColor(context, R.color.primary);
        nodeBorderPaint.setColor(borderColor);
        nodeBorderPaint.setStrokeWidth(3f * density);

        // Avatar photo or initials
        Bitmap photo = photoCache != null ? photoCache.get(person.getId()) : null;
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
            // Tint initials background by gender
            if ("female".equalsIgnoreCase(person.getGender())) {
                avatarBgPaint.setColor(ContextCompat.getColor(context, R.color.gender_female_bg));
                avatarTextPaint.setColor(ContextCompat.getColor(context, R.color.gender_female_text));
            } else if ("male".equalsIgnoreCase(person.getGender())) {
                avatarBgPaint.setColor(ContextCompat.getColor(context, R.color.gender_male_bg));
                avatarTextPaint.setColor(ContextCompat.getColor(context, R.color.gender_male_text));
            } else {
                avatarBgPaint.setColor(ContextCompat.getColor(context, R.color.gender_other_bg));
                avatarTextPaint.setColor(ContextCompat.getColor(context, R.color.gender_other_text));
            }
            canvas.drawCircle(avatarCenterX, avatarCenterY, AVATAR_RADIUS, avatarBgPaint);
            String initials = person.getInitials();
            avatarTextPaint.getTextBounds(initials, 0, initials.length(), tempTextBounds);
            canvas.drawText(initials, avatarCenterX, avatarCenterY + (tempTextBounds.height() / 2f), avatarTextPaint);
        }

        // Leaf ring outline
        canvas.drawCircle(avatarCenterX, avatarCenterY, AVATAR_RADIUS, nodeBorderPaint);

        // Deceased remembrance marker (golden dot with surface ring)
        if (isDeceased) {
            float dotX = avatarCenterX + (AVATAR_RADIUS * 0.72f);
            float dotY = avatarCenterY - (AVATAR_RADIUS * 0.72f);
            canvas.drawCircle(dotX, dotY, 5 * density, deceasedMarkerPaint);
            canvas.drawCircle(dotX, dotY, 5 * density, deceasedRingPaint);
        }

        // Guaranteed Readable Semi-Transparent Name Pill below avatar
        String fullName = person.getFullName();
        float maxPillWidth = rect.width();
        CharSequence ellipsized = TextUtils.ellipsize(fullName, new android.text.TextPaint(textNamePaint),
                maxPillWidth - (16 * density), TextUtils.TruncateAt.END);

        textNamePaint.getTextBounds(ellipsized.toString(), 0, ellipsized.length(), tempTextBounds);
        float pillWidth = Math.min(maxPillWidth, tempTextBounds.width() + (18 * density));
        float pillHeight = 22 * density;
        float pillLeft = centerX - (pillWidth / 2f);
        float pillTop = avatarCenterY + AVATAR_RADIUS + (3 * density);

        tempPillRect.set(pillLeft, pillTop, pillLeft + pillWidth, pillTop + pillHeight);

        // Pill background: semi-transparent surface with subtle outline
        pillBgPaint.setColor(ContextCompat.getColor(context, R.color.surface));
        pillBgPaint.setAlpha(235);
        canvas.drawRoundRect(tempPillRect, 11 * density, 11 * density, pillBgPaint);
        canvas.drawRoundRect(tempPillRect, 11 * density, 11 * density, pillStrokePaint);

        // Name text centered in pill
        textNamePaint.setTextAlign(Paint.Align.CENTER);
        float textY = tempPillRect.centerY() + (tempTextBounds.height() / 2f) - (1 * density);
        canvas.drawText(ellipsized.toString(), centerX, textY, textNamePaint);

        // Lifespan text in small font below pill
        String lifespan = person.getLifeSpan();
        if (lifespan != null && !lifespan.isEmpty()) {
            textYearsPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(lifespan, centerX, tempPillRect.bottom + (11 * density), textYearsPaint);
        }
    }

    private void drawClassicCardNode(
            Canvas canvas,
            PersonEntity person,
            RectF rect,
            Map<String, Bitmap> photoCache,
            boolean isSelected,
            boolean isDeceased
    ) {
        // Gender colored border
        int ringColor = "female".equalsIgnoreCase(person.getGender())
                ? ContextCompat.getColor(context, R.color.gender_female)
                : ContextCompat.getColor(context, R.color.gender_male);
        nodeBorderPaint.setColor(ringColor);
        nodeBorderPaint.setStrokeWidth(2 * density);

        // Draw card background
        nodeBgPaint.setColor(ContextCompat.getColor(context, R.color.surface));
        canvas.drawRoundRect(rect, 16 * density, 16 * density, nodeBgPaint);
        canvas.drawRoundRect(rect, 16 * density, 16 * density, nodeBorderPaint);

        if (isSelected) {
            canvas.drawRoundRect(rect, 16 * density, 16 * density, selectionGlowPaint);
        }

        // Draw Avatar on left side of node
        float avatarCenterX = rect.left + AVATAR_RADIUS + (10 * density);
        float avatarCenterY = rect.centerY();

        Bitmap photo = photoCache != null ? photoCache.get(person.getId()) : null;
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
                avatarBgPaint.setColor(ContextCompat.getColor(context, R.color.gender_female_bg));
                avatarTextPaint.setColor(ContextCompat.getColor(context, R.color.gender_female_text));
            } else if ("male".equalsIgnoreCase(person.getGender())) {
                avatarBgPaint.setColor(ContextCompat.getColor(context, R.color.gender_male_bg));
                avatarTextPaint.setColor(ContextCompat.getColor(context, R.color.gender_male_text));
            } else {
                avatarBgPaint.setColor(ContextCompat.getColor(context, R.color.gender_other_bg));
                avatarTextPaint.setColor(ContextCompat.getColor(context, R.color.gender_other_text));
            }
            canvas.drawCircle(avatarCenterX, avatarCenterY, AVATAR_RADIUS, avatarBgPaint);
            String initials = person.getInitials();
            avatarTextPaint.getTextBounds(initials, 0, initials.length(), tempTextBounds);
            canvas.drawText(initials, avatarCenterX, avatarCenterY + (tempTextBounds.height() / 2f), avatarTextPaint);
        }

        // Deceased dot if deceased
        if (isDeceased) {
            float dotX = avatarCenterX + (AVATAR_RADIUS * 0.72f);
            float dotY = avatarCenterY - (AVATAR_RADIUS * 0.72f);
            canvas.drawCircle(dotX, dotY, 4.5f * density, deceasedMarkerPaint);
        }

        // Member Name & Lifespan to right of avatar
        textNamePaint.setTextAlign(Paint.Align.LEFT);
        float textX = avatarCenterX + AVATAR_RADIUS + (10 * density);
        String name = TextUtils.ellipsize(person.getFullName(), new android.text.TextPaint(textNamePaint),
                rect.right - textX - (8 * density), TextUtils.TruncateAt.END).toString();
        canvas.drawText(name, textX, rect.centerY() - (4 * density), textNamePaint);

        String lifespan = person.getLifeSpan();
        if (lifespan != null && !lifespan.isEmpty()) {
            textYearsPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(lifespan, textX, rect.centerY() + (14 * density), textYearsPaint);
        }
    }

    /**
     * Draws static layers for Classic Chart mode (orthogonal connecting lines).
     */
    public void drawClassicChartStaticLayers(Canvas canvas, TreeLayoutEngine.LayoutResult layout) {
        branchPaint.setStrokeWidth(2f * density);
        branchPaint.setColor(ContextCompat.getColor(context, R.color.tree_connector));

        for (TreeUnit unit : layout.allUnits) {
            // Spouse connector
            if (unit.spouse != null) {
                float y = unit.primaryRect.centerY();
                canvas.drawLine(unit.primaryRect.right, y, unit.spouseRect.left, y, spouseVinePaint);
            }

            // Parent to children orthogonal connectors
            if (!unit.childrenUnits.isEmpty()) {
                float parentMidX = unit.spouse != null
                        ? (unit.primaryRect.left + unit.spouseRect.right) / 2.0f
                        : unit.primaryRect.centerX();
                float parentBottomY = unit.primaryRect.bottom;
                float branchY = parentBottomY + (LEVEL_GAP / 2.0f);

                // Vertical drop from parent
                canvas.drawLine(parentMidX, parentBottomY, parentMidX, branchY, branchPaint);

                // Horizontal distribution line across children
                TreeUnit firstChild = unit.childrenUnits.get(0);
                TreeUnit lastChild = unit.childrenUnits.get(unit.childrenUnits.size() - 1);
                float firstChildMidX = firstChild.spouse != null
                        ? (firstChild.primaryRect.left + firstChild.spouseRect.right) / 2.0f
                        : firstChild.primaryRect.centerX();
                float lastChildMidX = lastChild.spouse != null
                        ? (lastChild.primaryRect.left + lastChild.spouseRect.right) / 2.0f
                        : lastChild.primaryRect.centerX();

                canvas.drawLine(firstChildMidX, branchY, lastChildMidX, branchY, branchPaint);

                // Vertical drop to each child
                for (TreeUnit child : unit.childrenUnits) {
                    float childMidX = child.spouse != null
                            ? (child.primaryRect.left + child.spouseRect.right) / 2.0f
                            : child.primaryRect.centerX();
                    canvas.drawLine(childMidX, branchY, childMidX, child.primaryRect.top, branchPaint);
                }
            }
        }
    }
}
