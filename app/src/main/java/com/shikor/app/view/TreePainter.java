package com.shikor.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
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

    public enum DiurnalPhase {
        DAY("Day / Morning"),
        AFTERNOON("Afternoon"),
        EVENING("Sunset / Godhuli"),
        NIGHT("Night");

        public final String displayName;

        DiurnalPhase(String displayName) {
            this.displayName = displayName;
        }

        public static DiurnalPhase fromCurrentTime() {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
            int minute = cal.get(java.util.Calendar.MINUTE);
            int totalMins = hour * 60 + minute;

            if (totalMins >= 360 && totalMins < 840) {
                return DAY;       // 06:00 to 13:59 (Daylight & Sun)
            } else if (totalMins >= 840 && totalMins < 1020) {
                return AFTERNOON; // 14:00 to 16:59 (Golden hour & Warm sun)
            } else if (totalMins >= 1020 && totalMins < 1170) {
                return EVENING;   // 17:00 to 19:29 (Fiery Sunset & Godhuli)
            } else {
                return NIGHT;     // 19:30 to 05:59 (Crescent Moon & Stars)
            }
        }
    }

    private final Context context;
    private final float density;

    // Paints (all pre-allocated)
    private final Paint skyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint groundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grassLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Diurnal Celestial & Atmosphere Paints
    private final Paint celestialCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint celestialGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint celestialRaysPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cloudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cloudHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eveningStarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

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

        // Diurnal Celestial & Atmosphere Paints
        celestialCorePaint.setStyle(Paint.Style.FILL);
        celestialGlowPaint.setStyle(Paint.Style.FILL);
        celestialRaysPaint.setStyle(Paint.Style.STROKE);
        celestialRaysPaint.setStrokeCap(Paint.Cap.ROUND);
        celestialRaysPaint.setStrokeWidth(2f * density);

        cloudPaint.setStyle(Paint.Style.FILL);
        cloudHighlightPaint.setStyle(Paint.Style.FILL);

        eveningStarPaint.setStyle(Paint.Style.FILL);
        eveningStarPaint.setColor(Color.parseColor("#FFFDE7"));

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
     * Draws static natural background: sky gradient, celestial visuals (sun/moon/clouds/stars), and earth.
     */
    public void drawBackground(Canvas canvas, TreeLayoutEngine.LayoutResult layout, boolean isNightMode) {
        DiurnalPhase phase = isNightMode ? DiurnalPhase.NIGHT : DiurnalPhase.fromCurrentTime();
        drawBackground(canvas, layout, phase);
    }

    public void drawBackground(Canvas canvas, TreeLayoutEngine.LayoutResult layout, DiurnalPhase phase) {
        if (layout.bounds.isEmpty()) return;

        RectF bounds = layout.bounds;
        float groundY = layout.groundY;
        float left = bounds.left - (400 * density);
        float top = bounds.top - (400 * density);
        float right = bounds.right + (400 * density);
        float bottom = bounds.bottom + (400 * density);

        // 1. Dynamic Diurnal Sky Gradient
        drawDiurnalSky(canvas, bounds, left, top, right, groundY, phase);

        // 2. Atmospheric Celestial Visuals (Sun, Golden Corona, Sunset, Crescent Moon & Stars)
        drawCelestialVisuals(canvas, bounds, groundY, phase);

        // 3. Ground Earth Band & Grass Line
        drawDiurnalGround(canvas, bounds, left, right, bottom, groundY, phase);
    }

    private void drawDiurnalSky(Canvas canvas, RectF bounds, float left, float top, float right, float groundY, DiurnalPhase phase) {
        LinearGradient skyGradient;
        switch (phase) {
            case DAY:
                // Morning / Day: Crisp daylight blue into soft airy morning horizon
                skyGradient = new LinearGradient(bounds.centerX(), bounds.top, bounds.centerX(), groundY,
                        Color.parseColor("#82B9E9"), Color.parseColor("#EDF6FF"), Shader.TileMode.CLAMP);
                break;
            case AFTERNOON:
                // Afternoon: Azure sky with warm golden sunlit ambient wash
                skyGradient = new LinearGradient(bounds.centerX(), bounds.top, bounds.centerX(), groundY,
                        new int[]{Color.parseColor("#5A93D4"), Color.parseColor("#FCD897"), Color.parseColor("#FFF4DF")},
                        new float[]{0.0f, 0.65f, 1.0f}, Shader.TileMode.CLAMP);
                break;
            case EVENING:
                // Sunset / Godhuli: Deep dusk violet to crimson rose to fiery orange to glowing horizon
                skyGradient = new LinearGradient(bounds.centerX(), bounds.top, bounds.centerX(), groundY,
                        new int[]{Color.parseColor("#261734"), Color.parseColor("#6C2449"), Color.parseColor("#D84315"), Color.parseColor("#FFA726")},
                        new float[]{0.0f, 0.38f, 0.72f, 1.0f}, Shader.TileMode.CLAMP);
                break;
            case NIGHT:
            default:
                // Starry Night: Deep cosmic midnight indigo into dark navy horizon
                skyGradient = new LinearGradient(bounds.centerX(), bounds.top, bounds.centerX(), groundY,
                        Color.parseColor("#080D1A"), Color.parseColor("#142236"), Shader.TileMode.CLAMP);
                break;
        }
        skyPaint.setShader(skyGradient);
        canvas.drawRect(left, top, right, groundY, skyPaint);
    }

    private void drawCelestialVisuals(Canvas canvas, RectF bounds, float groundY, DiurnalPhase phase) {
        switch (phase) {
            case DAY:
                drawDayVisuals(canvas, bounds);
                break;
            case AFTERNOON:
                drawAfternoonVisuals(canvas, bounds);
                break;
            case EVENING:
                drawEveningVisuals(canvas, bounds, groundY);
                break;
            case NIGHT:
            default:
                drawNightVisuals(canvas, bounds, groundY);
                break;
        }
    }

    private void drawDayVisuals(Canvas canvas, RectF bounds) {
        float sunX = bounds.left + bounds.width() * 0.82f;
        float sunY = bounds.top + (35 * density);
        float sunRadius = 22 * density;

        // Radiant Sun Glow
        celestialGlowPaint.setShader(new RadialGradient(sunX, sunY, sunRadius * 3.6f,
                new int[]{Color.parseColor("#70FFF9C4"), Color.parseColor("#22FFE082"), Color.TRANSPARENT},
                new float[]{0.0f, 0.48f, 1.0f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(sunX, sunY, sunRadius * 3.6f, celestialGlowPaint);

        // Sun Rays
        celestialRaysPaint.setColor(Color.parseColor("#80FFE082"));
        celestialRaysPaint.setStrokeWidth(2f * density);
        for (int i = 0; i < 8; i++) {
            double angle = (i * Math.PI / 4.0);
            float x1 = (float) (sunX + Math.cos(angle) * (sunRadius + 4 * density));
            float y1 = (float) (sunY + Math.sin(angle) * (sunRadius + 4 * density));
            float x2 = (float) (sunX + Math.cos(angle) * (sunRadius + 14 * density));
            float y2 = (float) (sunY + Math.sin(angle) * (sunRadius + 14 * density));
            canvas.drawLine(x1, y1, x2, y2, celestialRaysPaint);
        }

        // Sun Core
        celestialCorePaint.setColor(Color.parseColor("#FFF59D"));
        canvas.drawCircle(sunX, sunY, sunRadius, celestialCorePaint);
        celestialCorePaint.setColor(Color.parseColor("#FFFDE7"));
        canvas.drawCircle(sunX, sunY, sunRadius * 0.75f, celestialCorePaint);

        // Fluffy Morning Clouds
        drawStylizedCloud(canvas, bounds.left + bounds.width() * 0.20f, bounds.top + (85 * density), 1.05f, 0xEEFFFFFF, 0xAAFFFFFF);
        drawStylizedCloud(canvas, bounds.left + bounds.width() * 0.58f, bounds.top + (140 * density), 0.78f, 0xDDFFFFFF, 0x99FFFFFF);
    }

    private void drawAfternoonVisuals(Canvas canvas, RectF bounds) {
        float sunX = bounds.left + bounds.width() * 0.74f;
        float sunY = bounds.top + (95 * density);
        float sunRadius = 26 * density;

        // Golden Afternoon Corona Aura
        celestialGlowPaint.setShader(new RadialGradient(sunX, sunY, sunRadius * 3.8f,
                new int[]{Color.parseColor("#80FFD54F"), Color.parseColor("#28FFB300"), Color.TRANSPARENT},
                new float[]{0.0f, 0.50f, 1.0f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(sunX, sunY, sunRadius * 3.8f, celestialGlowPaint);

        // Rich Golden Sun Core
        celestialCorePaint.setColor(Color.parseColor("#FFCA28"));
        canvas.drawCircle(sunX, sunY, sunRadius, celestialCorePaint);
        celestialCorePaint.setColor(Color.parseColor("#FFF8E1"));
        canvas.drawCircle(sunX, sunY, sunRadius * 0.76f, celestialCorePaint);

        // Golden Sunlit Clouds
        drawStylizedCloud(canvas, bounds.left + bounds.width() * 0.18f, bounds.top + (100 * density), 0.92f, 0xE5FFF8E7, 0xB0FFE0B2);
        drawStylizedCloud(canvas, bounds.left + bounds.width() * 0.62f, bounds.top + (165 * density), 1.10f, 0xD8FFF8E7, 0x99FFE0B2);
    }

    private void drawEveningVisuals(Canvas canvas, RectF bounds, float groundY) {
        float sunX = bounds.left + bounds.width() * 0.70f;
        float sunY = groundY - (40 * density);
        float sunRadius = 34 * density;

        // Massive Fiery Sunset Halo
        celestialGlowPaint.setShader(new RadialGradient(sunX, sunY, sunRadius * 4.2f,
                new int[]{Color.parseColor("#95FF7043"), Color.parseColor("#35D84315"), Color.TRANSPARENT},
                new float[]{0.0f, 0.45f, 1.0f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(sunX, sunY, sunRadius * 4.2f, celestialGlowPaint);

        // Setting Sun Disk dipping into horizon
        celestialCorePaint.setColor(Color.parseColor("#FF5722"));
        canvas.drawCircle(sunX, sunY, sunRadius, celestialCorePaint);
        celestialCorePaint.setColor(Color.parseColor("#FFB74D"));
        canvas.drawCircle(sunX, sunY, sunRadius * 0.78f, celestialCorePaint);

        // Evening Twilight Star ("Shukrotara" / Venus) in the upper violet sky
        float starX = bounds.left + bounds.width() * 0.24f;
        float starY = bounds.top + (45 * density);
        drawEveningStar(canvas, starX, starY, 7 * density);

        // Sunset Horizon Dusky Streaks
        drawSunsetCloudStreak(canvas, bounds.left + bounds.width() * 0.35f, groundY - (80 * density), 110 * density, 8 * density, Color.parseColor("#80880E4F"));
        drawSunsetCloudStreak(canvas, bounds.left + bounds.width() * 0.60f, groundY - (60 * density), 150 * density, 10 * density, Color.parseColor("#90E64A19"));
    }

    private void drawNightVisuals(Canvas canvas, RectF bounds, float groundY) {
        // 1. Stars
        float width = bounds.width() + (800 * density);
        float height = groundY - bounds.top + (400 * density);
        for (int i = 0; i < STAR_COUNT; i++) {
            float sx = (bounds.left - 400 * density) + (starX[i] * width);
            float sy = (bounds.top - 200 * density) + (starY[i] * height * 0.75f);
            starPaint.setAlpha(starAlpha[i]);
            canvas.drawCircle(sx, sy, starRadius[i], starPaint);
        }

        // 2. Crescent Moon
        float moonX = bounds.left + bounds.width() * 0.78f;
        float moonY = bounds.top + (50 * density);
        float moonRadius = 22 * density;

        // Lunar silver glow
        celestialGlowPaint.setShader(new RadialGradient(moonX, moonY, moonRadius * 3.2f,
                new int[]{Color.parseColor("#40E0F2FE"), Color.parseColor("#1590CAF9"), Color.TRANSPARENT},
                new float[]{0.0f, 0.45f, 1.0f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(moonX, moonY, moonRadius * 3.2f, celestialGlowPaint);

        // Crescent geometry via Path boolean difference
        tempRootPath.reset();
        tempRootPath.addCircle(moonX, moonY, moonRadius, Path.Direction.CW);
        tempLeafPath.reset();
        tempLeafPath.addCircle(moonX + (7.5f * density), moonY - (4 * density), moonRadius * 0.94f, Path.Direction.CW);
        tempRootPath.op(tempLeafPath, Path.Op.DIFFERENCE);

        celestialCorePaint.setColor(Color.parseColor("#FFFDE7"));
        canvas.drawPath(tempRootPath, celestialCorePaint);
    }

    private void drawEveningStar(Canvas canvas, float cx, float cy, float size) {
        eveningStarPaint.setColor(Color.parseColor("#FFFDE7"));
        tempLeafPath.reset();
        tempLeafPath.moveTo(cx, cy - size);
        tempLeafPath.lineTo(cx + (size * 0.25f), cy - (size * 0.25f));
        tempLeafPath.lineTo(cx + size, cy);
        tempLeafPath.lineTo(cx + (size * 0.25f), cy + (size * 0.25f));
        tempLeafPath.lineTo(cx, cy + size);
        tempLeafPath.lineTo(cx - (size * 0.25f), cy + (size * 0.25f));
        tempLeafPath.lineTo(cx - size, cy);
        tempLeafPath.lineTo(cx - (size * 0.25f), cy - (size * 0.25f));
        tempLeafPath.close();
        canvas.drawPath(tempLeafPath, eveningStarPaint);
    }

    private void drawStylizedCloud(Canvas canvas, float cx, float cy, float scale, int cloudColor, int highlightColor) {
        cloudPaint.setColor(cloudColor);
        cloudHighlightPaint.setColor(highlightColor);

        float r1 = 17 * scale * density;
        float r2 = 13 * scale * density;
        float r3 = 14 * scale * density;
        float r4 = 10 * scale * density;
        float r5 = 11 * scale * density;

        // Base oval
        tempRect.set(cx - (34 * scale * density), cy - (7 * scale * density),
                cx + (36 * scale * density), cy + (12 * scale * density));
        canvas.drawRoundRect(tempRect, 10 * scale * density, 10 * scale * density, cloudHighlightPaint);

        // Cloud puffs
        canvas.drawCircle(cx, cy - (5 * scale * density), r1, cloudPaint);
        canvas.drawCircle(cx - (16 * scale * density), cy - (1 * scale * density), r2, cloudPaint);
        canvas.drawCircle(cx + (17 * scale * density), cy - (2 * scale * density), r3, cloudPaint);
        canvas.drawCircle(cx - (28 * scale * density), cy + (2 * scale * density), r4, cloudPaint);
        canvas.drawCircle(cx + (30 * scale * density), cy + (2 * scale * density), r5, cloudPaint);
    }

    private void drawSunsetCloudStreak(Canvas canvas, float cx, float cy, float w, float h, int color) {
        cloudPaint.setColor(color);
        tempRect.set(cx - (w / 2f), cy - (h / 2f), cx + (w / 2f), cy + (h / 2f));
        canvas.drawRoundRect(tempRect, h / 2f, h / 2f, cloudPaint);
    }

    private void drawDiurnalGround(Canvas canvas, RectF bounds, float left, float right, float bottom, float groundY, DiurnalPhase phase) {
        int groundStart;
        int groundEnd;
        int grassColor;

        switch (phase) {
            case DAY:
                groundStart = Color.parseColor("#E4D8C5");
                groundEnd = Color.parseColor("#CEBEA3");
                grassColor = Color.parseColor("#388E3C");
                break;
            case AFTERNOON:
                groundStart = Color.parseColor("#DAC5A7");
                groundEnd = Color.parseColor("#BA9E79");
                grassColor = Color.parseColor("#689F38");
                break;
            case EVENING:
                groundStart = Color.parseColor("#36201B");
                groundEnd = Color.parseColor("#211310");
                grassColor = Color.parseColor("#4E2C22");
                break;
            case NIGHT:
            default:
                groundStart = Color.parseColor("#151E17");
                groundEnd = Color.parseColor("#0D140F");
                grassColor = Color.parseColor("#1C3722");
                break;
        }

        groundPaint.setShader(new LinearGradient(bounds.centerX(), groundY, bounds.centerX(), bottom,
                groundStart, groundEnd, Shader.TileMode.CLAMP));
        canvas.drawRect(left, groundY, right, bottom, groundPaint);

        grassLinePaint.setColor(grassColor);
        canvas.drawLine(left, groundY, right, groundY, grassLinePaint);
    }

    /**
     * Draws static natural living tree structure: trunk, roots, curved branches, spouse vines, and foliage.
     */
    public void drawNaturalTreeStaticStructure(Canvas canvas, TreeLayoutEngine.LayoutResult layout, boolean isNightMode) {
        DiurnalPhase phase = isNightMode ? DiurnalPhase.NIGHT : DiurnalPhase.fromCurrentTime();
        drawNaturalTreeStaticStructure(canvas, layout, phase);
    }

    public void drawNaturalTreeStaticStructure(Canvas canvas, TreeLayoutEngine.LayoutResult layout, DiurnalPhase phase) {
        if (layout.bounds.isEmpty()) return;

        float groundY = layout.groundY;

        // 1. Roots and Trunk below generation 0
        drawTrunkAndRoots(canvas, layout, groundY, phase);

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
                            unit.level, child.primary.getId(), phase == DiurnalPhase.NIGHT);
                }
            }
        }
    }

    /**
     * Draws complete static natural background and tree structure (for combined rendering).
     */
    public void drawNaturalTreeStaticLayers(Canvas canvas, TreeLayoutEngine.LayoutResult layout, boolean isNightMode) {
        DiurnalPhase phase = isNightMode ? DiurnalPhase.NIGHT : DiurnalPhase.fromCurrentTime();
        drawBackground(canvas, layout, phase);
        drawNaturalTreeStaticStructure(canvas, layout, phase);
    }

    /**
     * Draws a thick trunk and curved roots spreading into the ground with LinearGradient bark texture.
     */
    private void drawTrunkAndRoots(Canvas canvas, TreeLayoutEngine.LayoutResult layout, float groundY, DiurnalPhase phase) {
        float trunkX = layout.trunkBaseX;
        float trunkTopY = layout.trunkTopY;
        float trunkHeight = Math.max(80 * density, groundY - trunkTopY);

        int barkLight;
        int barkDark;
        int barkLineColor;

        if (phase == DiurnalPhase.NIGHT) {
            barkLight = Color.parseColor("#7D5E43");
            barkDark = Color.parseColor("#443021");
            barkLineColor = Color.parseColor("#38271A");
        } else if (phase == DiurnalPhase.EVENING) {
            barkLight = Color.parseColor("#8E5A3C");
            barkDark = Color.parseColor("#4E2718");
            barkLineColor = Color.parseColor("#38190F");
        } else if (phase == DiurnalPhase.AFTERNOON) {
            barkLight = Color.parseColor("#936C4A");
            barkDark = Color.parseColor("#5A3922");
            barkLineColor = Color.parseColor("#422815");
        } else {
            barkLight = Color.parseColor("#8A6A4B");
            barkDark = Color.parseColor("#5C4029");
            barkLineColor = Color.parseColor("#4A3320");
        }

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
        barkLinePaint.setColor(barkLineColor);
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
        String rawName = person.getFullName();
        String fullName = (rawName != null && !rawName.trim().isEmpty()) ? rawName.trim() : "—";
        float maxPillWidth = rect.width();
        float availWidth = Math.max(20f * density, maxPillWidth - (16 * density));
        CharSequence ellipsized = TextUtils.ellipsize(fullName, new android.text.TextPaint(textNamePaint),
                availWidth, TextUtils.TruncateAt.END);
        String safeName = ellipsized != null ? ellipsized.toString() : fullName;

        textNamePaint.getTextBounds(safeName, 0, safeName.length(), tempTextBounds);
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
        canvas.drawText(safeName, centerX, textY, textNamePaint);

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
        String rawName = person.getFullName();
        String fullName = (rawName != null && !rawName.trim().isEmpty()) ? rawName.trim() : "—";
        float availWidth = Math.max(20f * density, rect.right - textX - (8 * density));
        CharSequence ellipsized = TextUtils.ellipsize(fullName, new android.text.TextPaint(textNamePaint),
                availWidth, TextUtils.TruncateAt.END);
        String name = ellipsized != null ? ellipsized.toString() : fullName;
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
