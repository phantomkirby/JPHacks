package im.ene.androooid.jphacks.widgets;

import android.content.Context;
import android.util.AttributeSet;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by eneim on 12/13/14.
 */
public class SquareGifImageByWidth extends GifImageView {

    public SquareGifImageByWidth(Context context) {
        super(context);
    }

    public SquareGifImageByWidth(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareGifImageByWidth(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SquareGifImageByWidth(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }

    @Override
    public void requestLayout() {
        forceLayout();
    }
}
