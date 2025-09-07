package com.rebound.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.rebound.R;


public class TimelineDecoration extends RecyclerView.ItemDecoration {

    private final int lineWidthPx;
    private final int centerOffsetPx;
    private final Paint paint;

    public TimelineDecoration(Context context) {
        lineWidthPx = dpToPx(context, 2);
        centerOffsetPx = dpToPx(context, 11); // align với icon tick

        paint = new Paint();
        paint.setColor(ContextCompat.getColor(context, R.color.lineColor));
        paint.setStrokeWidth(lineWidthPx);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int childCount = parent.getChildCount();
        if (childCount < 2) return;

        int centerX = parent.getPaddingLeft() + centerOffsetPx;

        View firstItem = parent.getChildAt(0).findViewById(R.id.imgTick);
        View lastItem = parent.getChildAt(childCount - 1).findViewById(R.id.imgTick);

        if (firstItem == null || lastItem == null) return;

        int[] rvLocation = new int[2];
        int[] viewLocation = new int[2];
        parent.getLocationOnScreen(rvLocation);

        firstItem.getLocationOnScreen(viewLocation);
        int top = viewLocation[1] - rvLocation[1] + firstItem.getHeight() / 2;

        lastItem.getLocationOnScreen(viewLocation);
        int bottom = viewLocation[1] - rvLocation[1] + lastItem.getHeight() / 2;

        canvas.drawLine(centerX, top, centerX, bottom, paint);
    }

    private int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }
}
