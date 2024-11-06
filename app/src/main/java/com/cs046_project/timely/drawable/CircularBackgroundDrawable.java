package com.cs046_project.timely.drawable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class CircularBackgroundDrawable extends Drawable {
    private final Paint paint;
    private final int color;

    public CircularBackgroundDrawable(int color) {
        this.color = color;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        int width = getBounds().width();
        int height = getBounds().height();
        int size = Math.min(width, height);

        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = size / 2f;

        canvas.drawCircle(centerX, centerY, radius, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }

}
