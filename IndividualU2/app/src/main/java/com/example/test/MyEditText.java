package com.example.test;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

public class MyEditText extends androidx.appcompat.widget.AppCompatEditText {

    private Rect rect;
    private Paint paint;

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        rect = new Rect();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int baseline = getBaseline();
        for (int i = 0; i < getLineCount(); i++) {
            canvas.drawText("" + (i+1), rect.left, baseline, paint);
            baseline += getLineHeight();
        }
        super.onDraw(canvas);
    }
}