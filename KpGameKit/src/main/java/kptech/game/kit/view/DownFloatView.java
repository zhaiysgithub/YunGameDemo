package kptech.game.kit.view;

import android.content.Context;
import android.widget.LinearLayout;

import kptech.game.kit.R;


public class DownFloatView extends LinearLayout {

    public DownFloatView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.down_float_view, this);
    }

    public void setOnCloseListener(OnClickListener onCloseListener) {
        if (onCloseListener == null){
            return;
        }
        findViewById(R.id.close).setOnClickListener(onCloseListener);
    }

    public void setOnDownListener(OnClickListener onDownListener) {
        if (onDownListener == null){
            return;
        }
        findViewById(R.id.download).setOnClickListener(onDownListener);
    }


}
