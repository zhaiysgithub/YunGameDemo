package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import kptech.game.kit.R;

public class FloatDownView extends FrameLayout {

    public FloatDownView(Context context) {
        super(context);
        initView();
    }

    public FloatDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.view_float_down, this);
    }

}
