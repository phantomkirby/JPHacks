package im.ene.androooid.jphacks.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by eneim on 12/13/14.
 */
public class SquareLinearLayoutByHeight extends LinearLayout {
    public SquareLinearLayoutByHeight(Context context) {
        super(context);
    }

    public SquareLinearLayoutByHeight(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareLinearLayoutByHeight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquareLinearLayoutByHeight(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        setMeasuredDimension(height, height);
    }

    @Override
    public void requestLayout() {
        forceLayout();
    }
}
