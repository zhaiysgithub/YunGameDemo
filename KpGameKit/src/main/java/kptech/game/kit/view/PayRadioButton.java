package kptech.game.kit.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RadioButton;

import kptech.game.kit.R;

/**
 * 可以设置图片大小的RadioButton
 */


public class PayRadioButton extends RadioButton {

    private int mDrawableHeight;//xml文件中设置的高度
    private int mDrawableWidth;//xml文件中设置的宽度

    public PayRadioButton(Context context) {
        this(context, null, 0);
    }

    public PayRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PayRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        Drawable drawableLeft = null, drawableTop = null, drawableRight = null, drawableBottom = null;
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RadioButton);
        setClickable(true);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.RadioButton_drawableHeight) {
                mDrawableHeight = a.getDimensionPixelSize(R.styleable.RadioButton_drawableHeight, 50);
            } else if (attr == R.styleable.RadioButton_drawableWidth) {
                mDrawableWidth = a.getDimensionPixelSize(R.styleable.RadioButton_drawableWidth, 50);
            } else if (attr == R.styleable.RadioButton_drawableTop) {
                drawableTop = a.getDrawable(attr);
            } else if (attr == R.styleable.RadioButton_drawableBottom) {
                drawableRight = a.getDrawable(attr);
            } else if (attr == R.styleable.RadioButton_drawableRight) {
                drawableBottom = a.getDrawable(attr);
            } else if (attr == R.styleable.RadioButton_drawableLeft) {
                drawableLeft = a.getDrawable(attr);
            }
        }
        a.recycle();

        setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);

    }

    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left,
                                                        Drawable top, Drawable right, Drawable bottom) {

        if (left != null) {
            left.setBounds(0, 0, mDrawableWidth, mDrawableHeight);
        }
        if (right != null) {
            right.setBounds(0, 0, mDrawableWidth, mDrawableHeight);
        }
        if (top != null) {
            top.setBounds(0, 0, mDrawableWidth, mDrawableHeight);
        }
        if (bottom != null) {
            bottom.setBounds(0, 0, mDrawableWidth, mDrawableHeight);
        }
        setCompoundDrawables(left, top, right, bottom);
    }
}
