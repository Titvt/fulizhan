package com.titvt.fulizhan;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

public class ProgressButton extends AppCompatButton {
    private float cornerRadius = 0;
    private int max = 100, progress = 0;
    private GradientDrawable progressDrawable = new GradientDrawable(),
            progressBackgroundDrawable = new GradientDrawable();
    private float[] cornerRadii = new float[8];

    public ProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton);
        try {
            cornerRadius = attr.getDimension(R.styleable.ProgressButton_cornerRadius, cornerRadius);
            max = attr.getInteger(R.styleable.ProgressButton_max, max);
            progress = attr.getInteger(R.styleable.ProgressButton_progress, progress);
            progressDrawable.setColor(attr.getColor(R.styleable.ProgressButton_progressColor, getResources().getColor(R.color.colorPrimary)));
            progressBackgroundDrawable.setColor(attr.getColor(R.styleable.ProgressButton_progressBackgroundColor, getResources().getColor(R.color.colorPrimary)));
        } finally {
            attr.recycle();
        }
        cornerRadii[0] = cornerRadii[1] = cornerRadii[6] = cornerRadii[7] = cornerRadius;
        progressDrawable.setCornerRadii(cornerRadii);
        progressBackgroundDrawable.setCornerRadius(cornerRadius);
        setBackgroundDrawable(progressBackgroundDrawable);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        progress = progress < 0 ? 0 : progress;
        progress = progress > max ? max : progress;
        float progressWidth = (float) getMeasuredWidth() * progress / max;
        if (progressWidth >= getMeasuredWidth() - cornerRadius)
            cornerRadii[2] = cornerRadii[3] = cornerRadii[4] = cornerRadii[5] = progressWidth + cornerRadius - getMeasuredWidth();
        else
            cornerRadii[2] = cornerRadii[3] = cornerRadii[4] = cornerRadii[5] = 0;
        progressDrawable.setCornerRadii(cornerRadii);
        progressDrawable.setBounds(0, 0, (int) progressWidth, getMeasuredHeight());
        progressDrawable.draw(canvas);
        super.onDraw(canvas);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }
}