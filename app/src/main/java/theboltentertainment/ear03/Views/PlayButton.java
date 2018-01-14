package theboltentertainment.ear03.Views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;

/**
 * Created by Admin on 13/01/2018.
 */

public class PlayButton extends android.support.v7.widget.AppCompatButton {
    public PlayButton(Context context) {
        super(context);
    }

    public PlayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static class ScaleWidthAnimator extends Animation {
        private View view;
        private float start;
        private float end;

        // Constructor
        public ScaleWidthAnimator(View view, float start, float end, long duration) {
            this.view = view;
            this.start = start;
            this.end = end;
            this.setDuration(duration);
        }

        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            // Used to apply the animation to the view
            final int curPos = (int) (start + (int) ((end - start) * interpolatedTime));
            view.getLayoutParams().width = curPos;
            // Ensure the view is measured appropriately
            view.requestLayout();
        }
    }
}
