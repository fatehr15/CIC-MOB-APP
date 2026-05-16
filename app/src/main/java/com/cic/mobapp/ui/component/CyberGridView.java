package com.cic.mobapp.ui.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

/**
 * Living cyber-grid background. Draws:
 *   - Subtle infrastructure grid lines (cyan, ~5% opacity)
 *   - Signal pulses traveling along horizontal/vertical grid routes
 *   - Slowly oscillating ambient particles
 *
 * Drop into any layout as the first/lowest child. All siblings should
 * have transparent or semi-transparent backgrounds to let this show through.
 */
public class CyberGridView extends View {

    // ── Grid ─────────────────────────────────────────────────────────────────

    private static final float GRID_DP      = 58f;
    private float              gridStep;
    private final Paint        gridPaint    = new Paint();

    // ── Signal pulses ─────────────────────────────────────────────────────────

    private static final int PULSE_COUNT = 12;
    private final boolean[] pulseH      = new boolean[PULSE_COUNT]; // horizontal?
    private final int[]     pulseLine   = new int[PULSE_COUNT];     // grid-line index
    private final float[]   pulseSpeed  = new float[PULSE_COUNT];   // fraction/sec
    private final float[]   pulseOffset = new float[PULSE_COUNT];   // start phase 0-1
    private final float[]   pulseTrail  = new float[PULSE_COUNT];   // trail length 0-1
    private final int[]     pulseColor  = new int[PULSE_COUNT];     // color
    private final Paint     pulsePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ── Ambient particles ─────────────────────────────────────────────────────

    private static final int PARTICLE_COUNT = 20;
    private final float[] pBaseX      = new float[PARTICLE_COUNT];
    private final float[] pBaseY      = new float[PARTICLE_COUNT];
    private final float[] pSpeedX     = new float[PARTICLE_COUNT];
    private final float[] pSpeedY     = new float[PARTICLE_COUNT];
    private final float[] pPhaseX     = new float[PARTICLE_COUNT];
    private final float[] pPhaseY     = new float[PARTICLE_COUNT];
    private final float[] pAmplitude  = new float[PARTICLE_COUNT];
    private final float[] pBaseAlpha  = new float[PARTICLE_COUNT];
    private final float[] pAlphaFreq  = new float[PARTICLE_COUNT];
    private final int[]   pColor      = new int[PARTICLE_COUNT];
    private final Paint   particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ── State ─────────────────────────────────────────────────────────────────

    private long    startTime = 0;
    private boolean running   = false;

    // ── Construction ─────────────────────────────────────────────────────────

    public CyberGridView(Context ctx)                              { super(ctx); init(ctx); }
    public CyberGridView(Context ctx, AttributeSet attrs)          { super(ctx, attrs); init(ctx); }
    public CyberGridView(Context ctx, AttributeSet attrs, int def) { super(ctx, attrs, def); init(ctx); }

    private void init(Context ctx) {
        float d = ctx.getResources().getDisplayMetrics().density;
        gridStep = GRID_DP * d;

        gridPaint.setColor(0x0C00D1FF);         // ~5% alpha cyan
        gridPaint.setStrokeWidth(d * 0.6f);

        pulsePaint.setStrokeWidth(d * 2f);
        pulsePaint.setStrokeCap(Paint.Cap.ROUND);

        particlePaint.setStyle(Paint.Style.FILL);

        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    // ── Size-dependent init ────────────────────────────────────────────────────

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;

        int cols = (int)(w / gridStep) + 2;
        int rows = (int)(h / gridStep) + 2;

        java.util.Random rng = new java.util.Random(0xC1C_CAFE);

        // Pulses: alternate cyan/purple
        for (int i = 0; i < PULSE_COUNT; i++) {
            pulseH[i]      = i % 2 == 0;
            pulseLine[i]   = pulseH[i] ? rng.nextInt(Math.max(1, rows))
                                       : rng.nextInt(Math.max(1, cols));
            pulseSpeed[i]  = 0.025f + rng.nextFloat() * 0.045f;
            pulseOffset[i] = rng.nextFloat();
            pulseTrail[i]  = 0.04f + rng.nextFloat() * 0.07f;
            pulseColor[i]  = (i % 3 == 0) ? 0xFF8B5CF6 : 0xFF00D1FF;
        }

        // Particles: slow oscillating dots
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            pBaseX[i]     = rng.nextFloat() * w;
            pBaseY[i]     = rng.nextFloat() * h;
            pSpeedX[i]    = 0.12f + rng.nextFloat() * 0.22f;
            pSpeedY[i]    = 0.08f + rng.nextFloat() * 0.18f;
            pPhaseX[i]    = rng.nextFloat() * 6.28f;
            pPhaseY[i]    = rng.nextFloat() * 6.28f;
            pAmplitude[i] = 14f + rng.nextFloat() * 22f;
            pBaseAlpha[i] = 0.08f + rng.nextFloat() * 0.20f;
            pAlphaFreq[i] = 0.25f + rng.nextFloat() * 0.55f;
            pColor[i]     = (i % 4 == 0) ? 0xFF8B5CF6 : 0xFF00D1FF;
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        running   = true;
        startTime = 0;
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        running = false;
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) {
            if (running) postInvalidateOnAnimation();
            return;
        }

        if (startTime == 0) startTime = SystemClock.uptimeMillis();
        float t = (SystemClock.uptimeMillis() - startTime) * 0.001f; // seconds

        drawGrid(canvas, w, h);
        drawPulses(canvas, w, h, t);
        drawParticles(canvas, w, h, t);

        if (running) postInvalidateOnAnimation();
    }

    private void drawGrid(Canvas canvas, int w, int h) {
        for (float x = 0; x <= w + gridStep; x += gridStep)
            canvas.drawLine(x, 0, x, h, gridPaint);
        for (float y = 0; y <= h + gridStep; y += gridStep)
            canvas.drawLine(0, y, w, y, gridPaint);
    }

    private void drawPulses(Canvas canvas, int w, int h, float t) {
        for (int i = 0; i < PULSE_COUNT; i++) {
            float pos   = (t * pulseSpeed[i] + pulseOffset[i]) % 1f;
            float alpha = (float) Math.sin(pos * Math.PI); // smooth fade 0→1→0
            if (alpha <= 0.01f) continue;

            pulsePaint.setColor(pulseColor[i]);
            pulsePaint.setAlpha((int)(alpha * 190));

            if (pulseH[i]) {
                float ly = pulseLine[i] * gridStep;
                if (ly > h) continue;
                float x2 = pos * w;
                float x1 = (pos - pulseTrail[i]) * w;
                canvas.drawLine(Math.max(0, x1), ly, Math.min(w, x2), ly, pulsePaint);
            } else {
                float lx = pulseLine[i] * gridStep;
                if (lx > w) continue;
                float y2 = pos * h;
                float y1 = (pos - pulseTrail[i]) * h;
                canvas.drawLine(lx, Math.max(0, y1), lx, Math.min(h, y2), pulsePaint);
            }
        }
    }

    private void drawParticles(Canvas canvas, int w, int h, float t) {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float ox = (float)(Math.sin(t * pSpeedX[i] + pPhaseX[i]) * pAmplitude[i]);
            float oy = (float)(Math.cos(t * pSpeedY[i] + pPhaseY[i]) * pAmplitude[i]);
            float px = ((pBaseX[i] + ox) % w + w) % w;
            float py = ((pBaseY[i] + oy) % h + h) % h;

            float a = pBaseAlpha[i] + 0.07f * (float) Math.sin(t * pAlphaFreq[i] + i);
            a = Math.max(0.03f, Math.min(0.30f, a));

            particlePaint.setColor(pColor[i]);
            particlePaint.setAlpha((int)(a * 255));
            canvas.drawCircle(px, py, 1.8f, particlePaint);
        }
    }
}
