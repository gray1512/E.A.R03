package theboltentertainment.ear03.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;


/**
 * A simple class that draws waveform data received from a
 * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
 */
public class VisualizerView extends View {
    private byte[] fftBytes;
    private int samplingRate;
    private float range;

    private Paint paint;
    //private Path curvePath;

    private float[][] displayFft;
    private float[] drawValue;

    private int numberOfZone = 3; // 2^n
    private float rotation = (float) ((2 * Math.PI) / (numberOfZone*2));

    private float itemHeight;
    private float centerX, centerY;
    private float radius;
    private float width = 0;

    float n1X, n1Y, n2X, n2Y;
    float mX, mY;
    float[] point1 = new float[2], point2 = new float[2], coorStart, coorEnd, coorPeak;

    Path curvePath = new Path();

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }
    public VisualizerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStrokeWidth(3f);
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(255, 255, 255));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        drawValue = new float[numberOfZone];
    }

    public void updateVisualizerFft (byte[] fftBytes, int samplingRate) {
        // fft data from 0 or DC to haft of samplingRate
        this.fftBytes = fftBytes;
        this.samplingRate = samplingRate;
        this.range = samplingRate;
        this.displayFft = getDisplayValueFft();
        //this.curvePath = getCubicValue();
        invalidate();
        //startThread();
    }

    private float[][] getDisplayValueFft() {
        float max = 0;
        for (int i = 0; i < numberOfZone; i ++) {
            int n;
            float magnitude, freq;
            float area = 0, size = 0;
            switch (i) {
                case 0: { area = 0; size = (fftBytes.length / 4) * 0.125f; break;}
                case 1: { area = 0.125f; size = (fftBytes.length / 4) * 0.125f; break; }
                case 2: { area = 0.25f; size = (fftBytes.length / 4) * 3.75f; break; }
            }
            for (int j = 0; j < size; j += 2) {
                n = (int) (area * (fftBytes.length / 4) + j);
                magnitude = (float) Math.sqrt(fftBytes[n]*fftBytes[n] + fftBytes[n+1]*fftBytes[n+1]);
                freq = magnitude * samplingRate/(fftBytes.length / 4);
                if (j == 0) max = freq;
                if (freq > max) {
                    max = freq;
                }
            }
            drawValue[i] = max;
        }
        return getDrawPoint();
    }

    private synchronized float[][] getDrawPoint() {
        float[][] drawPoint = new float[numberOfZone * 2 * 2][2];
        float a = 0, height;
        float value;
        for (int n = 0; n < drawPoint.length; n += 2) {
            float[] coor = new float[2], coorRoot = new float[2];
            coorRoot[0] =  (float) (centerX + (Math.sin(a) * (radius))); // +2 so the line will not lie behind the img
            coorRoot[1] =  (float) (centerY + (Math.cos(a) * (radius))); // +2 so the line will not lie behind the img
            a += rotation / 2;

            if (n < drawPoint.length / 2) {
                value = (drawValue[n/2] + 1) / range;
            } else {
                value = (drawValue[drawPoint.length/2 - n/2 - 1]  + 1) / range;
            }

            height = itemHeight * value;
            float length = radius + height;
            coor[0] = (float) (centerX + (Math.sin(a) * length));
            coor[1] = (float) (centerY + (Math.cos(a) * length));

            drawPoint[n] = coorRoot;
            drawPoint[n + 1] = coor;

            a += rotation / 2;
        }
        return drawPoint;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (displayFft == null) {
            return;
        }
        if (width == 0) {
            this.width = canvas.getWidth();
            this.itemHeight = (float) (width * 0.2);
            this.centerX = (float) (width * 0.5);
            this.centerY = (float) (width * 0.5);
            this.radius = (float) (width * 0.24);
        }


        curvePath.reset();
        curvePath.moveTo(displayFft[0][0], displayFft[0][1]);
        for (int a = 0 ; a < displayFft.length; a ++) {
            coorStart = displayFft[a];
            coorEnd = (a == displayFft.length - 1) ? displayFft[0] : displayFft[a + 1]; // last one is 15 + 1 = 16

            // cubicTo (point1, point2, endPoint)
            // Cubic Bezier: B(t) = (1 - t)^3 + 3*(1 - t)^2 * t * p1 + 3*(1-t)* t^2 * p2 + t^3 * p3 (0 <= t <= 1)
            n1X = centerY - coorEnd[1];
            n1Y = coorEnd[0] - centerX + 0.1f;
            n2X = coorEnd[0] - centerX;
            n2Y = coorEnd[1] - centerY + 0.1f;

            mX = 0.5f * (coorStart[0] + coorEnd[0]);
            mY = 0.5f * (coorStart[1] + coorEnd[1]);

            point1[0] = (n1Y * (n2Y * mY - n2X * coorStart[0] - n2Y * coorStart[1]) + n1X * n2Y * mX + 1) / (n1X * n2Y - n1Y * n2X);
            point1[1] = (n2X * coorStart[0] - n2X * point1[0] + n2Y * coorStart[1]) / (n2Y);

            point2[0] = coorStart[0] + coorEnd[0] - point1[0];
            point2[1] = coorStart[1] + coorEnd[1] - point1[1];

            curvePath.cubicTo(point1[0], point1[1],
                    point2[0], point2[1],
                    coorEnd[0], coorEnd[1]);
        }

        /*for (int a = 0 ; a < displayFft.length; a += 2) {
            coorPeak = displayFft[a+1];
            coorEnd = (a == displayFft.length - 2) ? displayFft[0] : displayFft[a + 2];

            curvePath.quadTo(coorPeak[0], coorPeak[1], coorEnd[0], coorEnd[1]);
        }*/

        canvas.drawCircle(centerX, centerY, radius+1, paint);
        canvas.drawPath(curvePath, paint);
    }
}
