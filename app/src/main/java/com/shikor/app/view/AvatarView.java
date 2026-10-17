package com.shikor.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.shikor.app.R;

import java.io.File;

/**
 * AvatarView - Custom View that renders a circular user avatar with gender ring and deceased dot.
 *
 * CONCEPT: Custom View extending View, custom attributes via obtainStyledAttributes,
 * Paint and Matrix pre-allocation, Canvas drawing, and custom onMeasure.
 */
public class AvatarView extends View {

    private int ringColor;
    private float ringWidth;
    private String initials = "";
    private boolean isDeceased = false;
    private Bitmap photoBitmap;
    private BitmapShader bitmapShader;

    // Paints and Matrix allocated once to avoid GC pauses during onDraw
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint deceasedDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint deceasedBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Matrix shaderMatrix = new Matrix();

    private final Rect textBounds = new Rect();

    public AvatarView(Context context) {
        super(context);
        init(context, null);
    }

    public AvatarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AvatarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        int defaultRingColor = ContextCompat.getColor(context, R.color.primary);
        float defaultRingWidth = context.getResources().getDimension(R.dimen.avatar_ring_width_default);

        if (attrs != null) {
            // CONCEPT: Custom attributes read in constructor via obtainStyledAttributes
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AvatarView);
            ringColor = a.getColor(R.styleable.AvatarView_ringColor, defaultRingColor);
            ringWidth = a.getDimension(R.styleable.AvatarView_ringWidth, defaultRingWidth);
            initials = a.getString(R.styleable.AvatarView_initials);
            if (initials == null) initials = "";
            isDeceased = a.getBoolean(R.styleable.AvatarView_isDeceased, false);
            a.recycle();
        } else {
            ringColor = defaultRingColor;
            ringWidth = defaultRingWidth;
        }

        // Initialize paints
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.primary_container));

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(ringWidth);
        ringPaint.setColor(ringColor);

        textPaint.setColor(ContextCompat.getColor(context, R.color.on_primary_container));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        deceasedDotPaint.setStyle(Paint.Style.FILL);
        deceasedDotPaint.setColor(ContextCompat.getColor(context, R.color.deceased_indicator));

        deceasedBorderPaint.setStyle(Paint.Style.STROKE);
        deceasedBorderPaint.setStrokeWidth(3f);
        deceasedBorderPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredSize = (int) getResources().getDimension(R.dimen.avatar_size_medium);
        int width = resolveSize(desiredSize, widthMeasureSpec);
        int height = resolveSize(desiredSize, heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        float centerX = width / 2.0f;
        float centerY = height / 2.0f;
        float radius = (Math.min(width, height) / 2.0f) - (ringWidth / 2.0f);
        if (radius <= 0) return;

        // 1. Draw circular photo or initials background
        if (photoBitmap != null && bitmapShader != null) {
            float diameter = radius * 2.0f;
            float scale = diameter / (float) photoBitmap.getWidth();
            shaderMatrix.reset();
            shaderMatrix.setScale(scale, scale);
            shaderMatrix.postTranslate(centerX - radius, centerY - radius);
            bitmapShader.setLocalMatrix(shaderMatrix);

            canvas.drawCircle(centerX, centerY, radius, bitmapPaint);
        } else {
            canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

            // 2. Draw Initials text
            if (!initials.isEmpty()) {
                textPaint.setTextSize(radius * 0.85f);
                textPaint.getTextBounds(initials, 0, initials.length(), textBounds);
                float textY = centerY + (textBounds.height() / 2.0f) - textBounds.bottom;
                canvas.drawText(initials, centerX, textY, textPaint);
            }
        }

        // 3. Draw outer colored ring
        ringPaint.setColor(ringColor);
        ringPaint.setStrokeWidth(ringWidth);
        canvas.drawCircle(centerX, centerY, radius, ringPaint);

        // 4. Draw Deceased indicator dot if deceased
        if (isDeceased) {
            float dotRadius = radius * 0.22f;
            float dotCenterX = centerX + (float) (radius * Math.cos(Math.PI / 4));
            float dotCenterY = centerY - (float) (radius * Math.sin(Math.PI / 4));

            canvas.drawCircle(dotCenterX, dotCenterY, dotRadius + 1.5f, deceasedBorderPaint);
            canvas.drawCircle(dotCenterX, dotCenterY, dotRadius, deceasedDotPaint);
        }
    }

    public void setInitials(String initials) {
        this.initials = initials != null ? initials.toUpperCase() : "";
        invalidate();
    }

    public void setRingColor(int color) {
        this.ringColor = color;
        invalidate();
    }

    public void setRingWidth(float width) {
        this.ringWidth = width;
        requestLayout();
        invalidate();
    }

    public void setDeceased(boolean deceased) {
        this.isDeceased = deceased;
        invalidate();
    }

    public void setGender(String gender) {
        if ("female".equalsIgnoreCase(gender)) {
            ringColor = ContextCompat.getColor(getContext(), R.color.gender_female);
            backgroundPaint.setColor(Color.parseColor("#F7D6E6"));
            textPaint.setColor(Color.parseColor("#5A133A"));
        } else if ("male".equalsIgnoreCase(gender)) {
            ringColor = ContextCompat.getColor(getContext(), R.color.gender_male);
            backgroundPaint.setColor(Color.parseColor("#D4E7F5"));
            textPaint.setColor(Color.parseColor("#0F3C59"));
        } else {
            ringColor = ContextCompat.getColor(getContext(), R.color.gender_other);
            backgroundPaint.setColor(Color.parseColor("#E4DCEB"));
            textPaint.setColor(Color.parseColor("#3D2949"));
        }
        invalidate();
    }

    public void setPhotoPath(String path) {
        if (path != null && !path.trim().isEmpty()) {
            File file = new File(path);
            if (file.exists()) {
                // Decode bounds first to prevent OutOfMemory on high-res gallery photos
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                int targetSize = 512;
                int maxDim = Math.max(options.outWidth, options.outHeight);
                int inSampleSize = 1;
                while (maxDim / inSampleSize > targetSize) {
                    inSampleSize *= 2;
                }

                options.inJustDecodeBounds = false;
                options.inSampleSize = inSampleSize;
                Bitmap raw = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                if (raw != null) {
                    int size = Math.min(raw.getWidth(), raw.getHeight());
                    int x = Math.max(0, (raw.getWidth() - size) / 2);
                    int y = Math.max(0, (raw.getHeight() - size) / 2);
                    Bitmap cropped = Bitmap.createBitmap(raw, x, y, size, size);
                    this.bitmapShader = new BitmapShader(cropped, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    this.bitmapPaint.setShader(bitmapShader);
                    this.photoBitmap = cropped;
                    invalidate();
                    return;
                }
            }
        }
        this.photoBitmap = null;
        this.bitmapShader = null;
        this.bitmapPaint.setShader(null);
        invalidate();
    }
}
