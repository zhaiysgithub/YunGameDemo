package kptech.game.kit.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kptech.game.kit.APIConstants;
import kptech.game.kit.DeviceControl;
import kptech.game.kit.R;

public class FloatDownView extends FrameLayout {

    public FloatDownView(@NonNull Context context) {
        super(context);
        initView();
    }

    public FloatDownView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.view_float_down, this);
    }

}
