package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import kptech.game.kit.R;

public class RecordView extends BaseMoveView {

    public RecordView(Context context) {
        super(context);
        initView();
    }

    public RecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        View view = inflate(getContext(), R.layout.kp_view_record, null);
        addView(view);
    }

}
