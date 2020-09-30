package kptech.game.kit.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

public class PhoneCodeButton extends Button {

    private Handler mHandler;

    public PhoneCodeButton(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public void setOnClickListener(OnClickListener l) {


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean callOnClick() {
        return super.callOnClick();
    }

    public void start(){

    }

    public void stop(){

    }

    public void clean(){

    }

}
