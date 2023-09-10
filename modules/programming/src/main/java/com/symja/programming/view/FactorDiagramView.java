package com.symja.programming.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;


import com.symja.programming.R;

import java.util.ArrayList;

public class FactorDiagramView extends View {

    public static final int MAX = 1024;
    private static final boolean DEBUG = false;
    private static final String TAG = "FactorDiagramView";
    private int n = 0;
    private ArrayList<Integer> list;
    private Paint paint;
    private Paint debugPaint;

    public FactorDiagramView(Context context) {
        super(context);
        init(context, null);
    }

    public FactorDiagramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FactorDiagramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public static ArrayList<Integer> specialPrimeFactor(int n) {
        ArrayList<Integer> primes = new ArrayList<>();
        if (n <= 0) {
            return primes;
        }
        if (n == 1) {
            primes.add(1);
            return primes;
        }

        // https://mathlesstraveled.com/2012/10/05/factorization-diagrams/
        // There is a special case for 2: if the picture is wider than tall,
        // then we put the two copies one above the other; otherwise,
        // we put them next to each other. In both cases we also add
        // some space in between the copies (equal to half the height or width, respectively).
        while (n % 4 == 0) {
            primes.add(4);
            n /= 4;
        }

        double upperBound = Math.sqrt(n);
        for (int i = 2; i <= upperBound; i++) {
            while (n % i == 0) {
                primes.add(i);
                n /= i;
            }
        }
        // n is prime number
        if (n > 1) {
            primes.add(n);
        }
        return primes;
    }

    private void init(Context context, AttributeSet attrs) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLUE);
        paint.setAlpha(100);

        debugPaint = new Paint();
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setStrokeWidth(1);
        debugPaint.setColor(Color.RED);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FactorDiagramView);
            n = typedArray.getInteger(R.styleable.FactorDiagramView_number, 27);
            int color = typedArray.getColor(R.styleable.FactorDiagramView_color, Color.BLACK);
            paint.setColor(color);
            typedArray.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (n <= 0 || n > MAX) {
            return;
        }
        // view is not visible
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        if (list == null) {
            list = specialPrimeFactor(n);
        }

        float size = Math.min(getWidth(), getHeight());
        canvas.save();
        // center view
        canvas.translate((getWidth() - size) / 2f, (getHeight() - size) / 2f);
        drawDiagram(canvas, size / 2, size / 2, size / 2, list.size() - 1);
        canvas.restore();
    }

    private void drawDiagram(Canvas canvas, float x, float y, float size, int depth) {
        if (DEBUG) {
            canvas.drawRect(x - size, y - size, x + size, y + size, debugPaint);
        }
        if (depth < 0) {
            drawPattern(canvas, x, y, size);
        } else {
            drawPolygon(canvas, list.get(depth), depth, size, x, y);
        }
    }

    /**
     * @param n a prime number in list
     */
    private void drawPolygon(Canvas canvas, int n, int depth, float size, float x, float y) {
        // draw around the circles
        double step = Math.PI * 2 / n;
        // the first position of the pattern
        double init;
        if (n == 2) {
            // Draw on horizontal line
            init = 0;
        } else if (n == 4) {
            // Draw around a square
            init = Math.PI / 4;
        } else {
            init = 3 * Math.PI / 2;
        }

        float dotRadius = (2 * size) / (n + 2);
        float radius = (n * size) / (n + 2);
        double deltaY;
        if (n % 2 == 0) {
            deltaY = 0;
        } else {
            deltaY = (radius / 2) * (1 - Math.cos(Math.PI / n));
        }
        if (DEBUG) {
            canvas.drawCircle(x, y, radius, debugPaint);
        }
        for (int i = 0; i < n; i++) {
            double angle = init + step * i;
            float localX = (float) (x + Math.cos(angle) * radius);
            float localY = (float) (y + Math.sin(angle) * radius + deltaY);

            if (DEBUG) {
                canvas.drawCircle(localX, localY, dotRadius, debugPaint);
            }

            drawDiagram(canvas, localX, localY, dotRadius, depth - 1);
        }
    }

    private void drawPattern(Canvas canvas, float centerX, float centerY, float radius) {
        canvas.drawCircle(centerX, centerY, radius * 0.85f, paint);
    }


    @UiThread
    public void setNumber(int number) {
        this.n = number;
        this.list = specialPrimeFactor(number);
        postInvalidate();
    }
}
