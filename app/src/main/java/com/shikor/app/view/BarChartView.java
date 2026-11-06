package com.shikor.app.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
 * BarChartView - Custom View drawing animated bar charts with touch selection and tooltips.
 * CONCEPT: Custom Views, Canvas, Paint optimization, ValueAnimator, and touch interaction.
 */
public class BarChartView extends View {

    private List<String> labels = Collections.emptyList();
    private List<Float> values = Collections.emptyList();

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float animationProgress = 1.0f;
    private ValueAnimator animator;
    private int selectedIndex = -1;

    private int barColor;
    private int barSelectedColor;

    public BarChartView(Context context) {
        super(context);
        init(context, null);
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        barColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, ContextCompat.getColor(context, R.color.primary));
        barSelectedColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorTertiary, ContextCompat.getColor(context, R.color.tertiary));

        if (attrs != null) {
            try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BarChartView)) {
                barColor = a.getColor(R.styleable.BarChartView_barColor, barColor);
                barSelectedColor = a.getColor(R.styleable.BarChartView_barSelectedColor, barSelectedColor);
            }
        }

        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(barColor);

        barSelectedPaint.setStyle(Paint.Style.FILL);
        barSelectedPaint.setColor(barSelectedColor);

        textPaint.setTextSize(32f);
        textPaint.setColor(MaterialColors.getColor(context, android.R.attr.textColorSecondary, ContextCompat.getColor(context, android.R.color.darker_gray)));
        textPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2f);
        gridPaint.setColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOutlineVariant, Color.LTGRAY));

        tooltipPaint.setStyle(Paint.Style.FILL);
        tooltipPaint.setColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceContainerHigh, Color.DKGRAY));

        setupAnimator();
    }

    private void setupAnimator() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(800);
        animator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();
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

    public void setData(List<String> labels, List<Float> values) {
        this.labels = labels != null ? labels : Collections.emptyList();
        this.values = values != null ? values : Collections.emptyList();
        selectedIndex = -1;
        if (animator != null) animator.start();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minW = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
        int minH = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom() + 300;
        int w = resolveSize(Math.max(minW, 600), widthMeasureSpec);
        int h = resolveSize(Math.max(minH, 400), heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (values.isEmpty()) return;

        float paddingLeft = getPaddingLeft() + 40f;
        float paddingRight = getPaddingRight() + 40f;
        float paddingTop = getPaddingTop() + 40f;
        float paddingBottom = getPaddingBottom() + 60f;

        float width = getWidth() - paddingLeft - paddingRight;
        float height = getHeight() - paddingTop - paddingBottom;

        float maximumVal = 1f;
        for (float v : values) {
            if (v > maximumVal) maximumVal = v;
        }

        // Draw horizontal grid lines
        canvas.drawLine(paddingLeft, paddingTop + height, paddingLeft + width, paddingTop + height, gridPaint);
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft + width, paddingTop, gridPaint);

        int count = values.size();
        float slotWidth = width / count;
        float barWidth = slotWidth * 0.6f;

        for (int i = 0; i < count; i++) {
            float val = values.get(i);
            float barH = (val / maximumVal) * height * animationProgress;

            float left = paddingLeft + (i * slotWidth) + (slotWidth - barWidth) / 2f;
            float top = paddingTop + height - barH;
            float right = left + barWidth;
            float bottom = paddingTop + height;

            Paint p = (i == selectedIndex) ? barSelectedPaint : barPaint;
            canvas.drawRect(left, top, right, bottom, p);

            // Draw label
            if (i < labels.size()) {
                String label = labels.get(i);
                canvas.drawText(label, left + barWidth / 2f, bottom + 40f, textPaint);
            }
        }

        // Draw tooltip if selected
        if (selectedIndex >= 0 && selectedIndex < values.size()) {
            float val = values.get(selectedIndex);
            String label = selectedIndex < labels.size() ? labels.get(selectedIndex) : "";
            String tooltip = label + ": " + (int) val;
            float textW = textPaint.measureText(tooltip);
            float slotW = width / count;
            float cx = paddingLeft + (selectedIndex * slotW) + slotW / 2f;
            float cy = paddingTop + height * 0.3f;

            canvas.drawRoundRect(cx - textW / 2f - 20f, cy - 50f, cx + textW / 2f + 20f, cy + 10f, 12f, 12f, tooltipPaint);
            canvas.drawText(tooltip, cx, cy - 10f, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float paddingLeft = getPaddingLeft() + 40f;
            float width = getWidth() - paddingLeft - (getPaddingRight() + 40f);
            if (values.isEmpty() || width <= 0) return false;

            float slotWidth = width / values.size();
            int index = (int) ((x - paddingLeft) / slotWidth);
            if (index >= 0 && index < values.size()) {
                selectedIndex = index;
                invalidate();
                performClick();
                return true;
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
