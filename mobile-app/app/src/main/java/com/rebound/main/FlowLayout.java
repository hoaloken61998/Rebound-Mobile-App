package com.rebound.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class FlowLayout extends ViewGroup {

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int height = 0;
        int lineWidth = 0;
        int lineHeight = 0;

        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            LayoutParams lp = child.getLayoutParams();

            if (lineWidth + child.getMeasuredWidth() > widthSize) {
                height += lineHeight;
                lineWidth = 0;
                lineHeight = 0;
            }

            lineWidth += child.getMeasuredWidth();
            lineHeight = Math.max(lineHeight, child.getMeasuredHeight());
        }

        height += lineHeight;

        setMeasuredDimension(widthSize, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int x = 0;
        int y = 0;
        int lineHeight = 0;

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childW = child.getMeasuredWidth();
            int childH = child.getMeasuredHeight();

            if (x + childW > width) {
                x = 0;
                y += lineHeight;
                lineHeight = 0;
            }

            child.layout(x, y, x + childW, y + childH);
            x += childW;
            lineHeight = Math.max(lineHeight, childH);
        }
    }
}

