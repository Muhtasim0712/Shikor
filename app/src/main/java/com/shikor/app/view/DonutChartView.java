package com.shikor.app.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.shikor.app.R;

import java.util.Collections;
import java.util.List;

/**
 * DonutChartView - Custom View drawing animated donut/pie charts with slice selection.
 * CONCEPT: Custom Views, Canvas arc math, ValueAnimator, and touch interaction.
 */
public class DonutChartView extends View {

    private List<String> labels = Collections.emptyList();
    private List<Float> values = Collections.emptyList();
    private List<Integer> colors = Collections.emptyList();

    private final Paint slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float sweepAnimationProgress = 1.0f;
    private ValueAnimator animator;
    private int selectedIndex = -1;
    private final RectF arcRect = new RectF();

    public DonutChartView(Context context) {
        super(context);
        init(context);
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) return;
        slicePaint.setStyle(Paint.Style.STROKE);
        slicePaint.setStrokeWidth(80f);

        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, Color.WHITE));

        textPaint.setTextSize(36f);
        textPaint.setColor(MaterialColors.getColor(context, android.R.attr.textColorPrimary, Color.BLACK));
        textPaint.setTextAlign(Paint.Align.CENTER);

        setupAnimator();
    }

    private void setupAnimator() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(900);
        animator.addUpdateListener(animation -> {
            sweepAnimationProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        try {
            if (android.provider.Settings.Global.getFloat(getContext().getContentResolver(),
                    android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f) != 0f) {
                animator.start();
            }
        } catch (Exception ignored) {
            animator.start();
        }
    }

    public void setData(List<String> labels, List<Float> values, List<Integer> colors) {
        this.labels = labels != null ? labels : Collections.emptyList();
        this.values = values != null ? values : Collections.emptyList();
        this.colors = colors != null ? colors : Collections.emptyList();
        selectedIndex = -1;
        if (animator != null) animator.start();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = resolveSize(500, widthMeasureSpec);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (values.isEmpty()) return;

        float w = getWidth();
        float h = getHeight();
        float radius = Math.min(w, h) / 2f - 60f;
        arcRect.set(w / 2f - radius, h / 2f - radius, w / 2f + radius, h / 2f + radius);

        float total = 0f;
        for (float v : values) total += v;
        if (total <= 0f) return;

        float currentAngle = -90f;
        for (int i = 0; i < values.size(); i++) {
            float val = values.get(i);
            float sweepAngle = (val / total) * 360f * sweepAnimationProgress;
            int color = i < colors.size() ? colors.get(i) : Color.GRAY;

            slicePaint.setColor(color);
            if (i == selectedIndex) {
                slicePaint.setStrokeWidth(95f);
            } else {
                slicePaint.setStrokeWidth(80f);
            }

            canvas.drawArc(arcRect, currentAngle, sweepAngle, false, slicePaint);
            currentAngle += sweepAngle;
        }

        // Draw center hole
        canvas.drawCircle(w / 2f, h / 2f, radius - 45f, centerPaint);

        // Draw center text / selected info
        if (selectedIndex >= 0 && selectedIndex < values.size()) {
            String label = selectedIndex < labels.size() ? labels.get(selectedIndex) : "";
            float val = values.get(selectedIndex);
            int pct = (int) ((val / total) * 100f);
            canvas.drawText(label, w / 2f, h / 2f - 10f, textPaint);
            canvas.drawText(pct + "% (" + (int) val + ")", w / 2f, h / 2f + 35f, textPaint);
        } else {
            canvas.drawText("Family", w / 2f, h / 2f + 10f, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX() - getWidth() / 2f;
            float y = event.getY() - getHeight() / 2f;
            double angle = Math.toDegrees(Math.atan2(y, x)) + 90;
            if (angle < 0) angle += 360;

            float total = 0f;
            for (float v : values) total += v;
            if (total <= 0f) return false;

            float currentAngle = 0f;
            for (int i = 0; i < values.size(); i++) {
                float sweep = (values.get(i) / total) * 360f;
                if (angle >= currentAngle && angle <= currentAngle + sweep) {
                    selectedIndex = i;
                    invalidate();
                    performClick();
                    return true;
                }
                currentAngle += sweep;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            selectedIndex = -1;
            invalidate();
            performClick();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Nullable
    @Override
    protected android.os.Parcelable onSaveInstanceState() {
        android.os.Parcelable superState = super.onSaveInstanceState();
        android.os.Bundle bundle = new android.os.Bundle();
        bundle.putParcelable("superState", superState);
        bundle.putInt("selectedIndex", selectedIndex);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(android.os.Parcelable state) {
        android.os.Parcelable currentSt = state;
        if (currentSt instanceof android.os.Bundle) {
            android.os.Bundle bundle = (android.os.Bundle) currentSt;
            selectedIndex = bundle.getInt("selectedIndex", -1);
            currentSt = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(currentSt);
    }
}
