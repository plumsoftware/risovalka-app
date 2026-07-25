package com.plumsoftware.risovalka.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Random;

/**
 * Простой холст для детского рисования с несколькими стилями кисти.
 * Заменяет com.github.gcacace.signaturepad.views.SignaturePad, чтобы
 * дать полный контроль над Paint (маркер, неон, радуга, распылитель, пунктир).
 *
 * Совместим по названию ключевых методов с SignaturePad
 * (getSignatureBitmap / isEmpty / clear), чтобы минимально менять DrawActivity.
 */
public class DrawingView extends View {

    public enum BrushType {
        PENCIL,   // обычный карандаш
        MARKER,   // полупрозрачный маркер
        NEON,     // неоновое свечение
        RAINBOW,  // цвет меняется по ходу линии
        SPRAY,    // аэрозоль/распылитель
        DOTTED    // пунктирная линия
    }

    private Bitmap bitmap;
    private Canvas bitmapCanvas;

    private int canvasColor = Color.WHITE;
    private int penColor = Color.BLACK;
    private int strokeWidthPx = 8;
    private BrushType brushType = BrushType.PENCIL;

    private float lastX, lastY;
    private boolean isEmpty = true;
    private float rainbowHue = 0f;
    private final Random random = new Random();

    private final Paint workingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DrawingView(Context context) {
        super(context);
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        if (w > 0 && h > 0) {
            Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas(newBitmap);
            newCanvas.drawColor(canvasColor);
            // переносим то, что уже было нарисовано, при смене размера экрана
            if (bitmap != null) {
                newCanvas.drawBitmap(bitmap, 0, 0, null);
            }
            bitmap = newBitmap;
            bitmapCanvas = newCanvas;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isEmpty = false;
                lastX = x;
                lastY = y;
                if (brushType == BrushType.SPRAY) {
                    spray(x, y);
                } else {
                    drawSegment(lastX, lastY, x, y);
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (brushType == BrushType.SPRAY) {
                    spray(x, y);
                } else {
                    drawSegment(lastX, lastY, x, y);
                }
                lastX = x;
                lastY = y;
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void drawSegment(float startX, float startY, float endX, float endY) {
        if (bitmapCanvas == null) return;

        configurePaintForBrush();

        switch (brushType) {
            case NEON: {
                // широкий полупрозрачный слой снизу + яркая тонкая линия сверху = эффект свечения
                Paint glow = new Paint(workingPaint);
                glow.setStrokeWidth(strokeWidthPx * 3f);
                glow.setAlpha(70);
                glow.setMaskFilter(new BlurMaskFilter(strokeWidthPx, BlurMaskFilter.Blur.NORMAL));
                bitmapCanvas.drawLine(startX, startY, endX, endY, glow);

                Paint core = new Paint(workingPaint);
                core.setStrokeWidth(Math.max(2f, strokeWidthPx / 2f));
                core.setColor(Color.WHITE);
                bitmapCanvas.drawLine(startX, startY, endX, endY, core);
                break;
            }
            case RAINBOW: {
                rainbowHue = (rainbowHue + 6f) % 360f;
                workingPaint.setColor(Color.HSVToColor(new float[]{rainbowHue, 1f, 1f}));
                bitmapCanvas.drawLine(startX, startY, endX, endY, workingPaint);
                break;
            }
            default:
                bitmapCanvas.drawLine(startX, startY, endX, endY, workingPaint);
        }
    }

    private void spray(float cx, float cy) {
        if (bitmapCanvas == null) return;
        Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
        dot.setColor(penColor);
        dot.setStyle(Paint.Style.FILL);

        int density = 22;
        float radius = strokeWidthPx * 2.2f;
        for (int i = 0; i < density; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = random.nextDouble() * radius;
            float px = (float) (cx + distance * Math.cos(angle));
            float py = (float) (cy + distance * Math.sin(angle));
            bitmapCanvas.drawCircle(px, py, 1.2f + random.nextFloat() * 1.3f, dot);
        }
    }

    private void configurePaintForBrush() {
        workingPaint.reset();
        workingPaint.setAntiAlias(true);
        workingPaint.setStyle(Paint.Style.STROKE);
        workingPaint.setStrokeJoin(Paint.Join.ROUND);
        workingPaint.setStrokeCap(Paint.Cap.ROUND);
        workingPaint.setColor(penColor);
        workingPaint.setStrokeWidth(strokeWidthPx);

        switch (brushType) {
            case MARKER:
                workingPaint.setStrokeWidth(strokeWidthPx * 1.6f);
                workingPaint.setAlpha(120);
                break;
            case DOTTED:
                workingPaint.setStrokeCap(Paint.Cap.ROUND);
                workingPaint.setPathEffect(
                        new DashPathEffect(new float[]{strokeWidthPx * 0.2f, strokeWidthPx * 1.6f}, 0));
                break;
            case PENCIL:
            default:
                // настройки по умолчанию, заданные выше
                break;
        }
    }

    // ---- Публичный API, используемый DrawActivity ----

    public void setPenColor(int color) {
        this.penColor = color;
    }

    public void setStrokeWidth(int widthPx) {
        this.strokeWidthPx = Math.max(1, widthPx);
    }

    public void setBrushType(BrushType type) {
        this.brushType = type;
    }

    public BrushType getBrushType() {
        return brushType;
    }

    /** Меняет цвет холста. По умолчанию также очищает рисунок (как и раньше делал SignaturePad). */
    public void setCanvasColor(int color) {
        this.canvasColor = color;
        clear();
    }

    public int getCanvasColor() {
        return canvasColor;
    }

    public void clear() {
        if (bitmapCanvas != null) {
            bitmapCanvas.drawColor(canvasColor, PorterDuff.Mode.SRC);
        }
        isEmpty = true;
        invalidate();
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    /** Название сохранено таким же, как у SignaturePad, чтобы не менять код сохранения в DrawActivity. */
    public Bitmap getSignatureBitmap() {
        return bitmap;
    }
}
