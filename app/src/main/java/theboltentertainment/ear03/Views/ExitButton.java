package theboltentertainment.ear03.Views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class ExitButton extends View {

    private Paint paint;

    public ExitButton(Context context) {
        super(context);
    }

    public ExitButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ExitButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ExitButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init (int color) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5f);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(canvas.getWidth() / 4,canvas.getHeight() / 4,
                canvas.getWidth() * 3/4, canvas.getHeight() * 3/4, paint);
        canvas.drawLine(canvas.getWidth() * 3/4,canvas.getHeight() / 4,
                canvas.getWidth() / 4, canvas.getHeight() * 3/4, paint);
    }
}
