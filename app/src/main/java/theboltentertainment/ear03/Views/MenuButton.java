package theboltentertainment.ear03.Views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Admin on 20/02/2018.
 */

public class MenuButton extends View {

    private Paint paint;

    public MenuButton(Context context) {
        super(context);
    }

    public MenuButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MenuButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init (int color) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1f);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 4, canvas.getHeight() / 12, paint);
        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, canvas.getHeight() / 12, paint);
        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() * 3 / 4, canvas.getHeight() / 12, paint);
    }
}
