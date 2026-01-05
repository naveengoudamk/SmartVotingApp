package com.example.smartvotingapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimpleConfettiView extends View {
    private List<Confetto> confetti = new ArrayList<>();
    private Paint paint = new Paint();
    private Random random = new Random();
    private boolean isAnimating = false;
    private int[] colors = { 0xFFEF5350, 0xFFEC407A, 0xFFAB47BC, 0xFF42A5F5, 0xFF26A69A, 0xFFFFEE58, 0xFFFFA726 };

    public SimpleConfettiView(Context context) {
        super(context);
    }

    public SimpleConfettiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void startAnimation() {
        isAnimating = true;
        confetti.clear();
        // Create 50 particles
        for (int i = 0; i < 50; i++) {
            confetti.add(new Confetto(getWidth()));
        }
        invalidate();
    }

    public void stopAnimation() {
        isAnimating = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isAnimating)
            return;

        for (Confetto c : confetti) {
            paint.setColor(c.color);
            canvas.drawCircle(c.x, c.y, c.size, paint);
            c.y += c.speed;
            c.x += Math.sin(c.y / 50f) * 2; // Wiggle behavior

            // Reset if falls off screen
            if (c.y > getHeight()) {
                c.y = -random.nextInt(100);
                c.x = random.nextInt(getWidth() > 0 ? getWidth() : 1080);
            }
        }
        invalidate(); // Trigger next frame
    }

    private class Confetto {
        float x, y, size, speed;
        int color;

        Confetto(int width) {
            x = random.nextInt(width > 0 ? width : 1000);
            y = -random.nextInt(500);
            size = 8 + random.nextInt(12);
            speed = 5 + random.nextInt(10);
            color = colors[random.nextInt(colors.length)];
        }
    }
}
