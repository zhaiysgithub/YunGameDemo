package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class LoadingSeekBar extends SeekBar {

    public LoadingSeekBar(Context context) {
        super(context);
    }

    public LoadingSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LoadingSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
