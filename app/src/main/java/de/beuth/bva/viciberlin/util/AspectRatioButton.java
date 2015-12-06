package de.beuth.bva.viciberlin.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by TwoBulls on 10/27/14.
 */
public class AspectRatioButton extends ImageButton {

    double aspectRatio;
    int width;
    int height;

    public AspectRatioButton(Context context){
        super(context);
        Drawable bg = this.getBackground();

        width = bg.getIntrinsicWidth();
        height = bg.getIntrinsicHeight();

        aspectRatio = width *1.0/height ;

    }

    public AspectRatioButton(Context context, AttributeSet attrs){
        super(context, attrs);
        Drawable bg = this.getBackground();

        width = bg.getIntrinsicWidth();
        height = bg.getIntrinsicHeight();

        aspectRatio = width *1.0/height ;

    }

    // This is used to make buttons with aspect ratio protected.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int currentHeight = MeasureSpec.getSize(heightMeasureSpec);
        int currentWidth = MeasureSpec.getSize(widthMeasureSpec);

        if(currentHeight <= currentWidth){
            int newWidth = (int)(currentHeight*aspectRatio);
            setMeasuredDimension(newWidth, currentHeight);
        } else {
            int newHeight = (int)(currentWidth*aspectRatio);
            setMeasuredDimension(newHeight, currentWidth);
        }

    }

}
