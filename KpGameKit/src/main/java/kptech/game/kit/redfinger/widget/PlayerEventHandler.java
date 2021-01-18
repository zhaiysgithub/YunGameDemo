package kptech.game.kit.redfinger.widget;

import android.view.KeyEvent;
import android.view.MotionEvent;

public interface PlayerEventHandler {
    void setOnKeyDown(int i, KeyEvent keyEvent);

    void setOnKeyUp(int i, KeyEvent keyEvent);

    void setOnTouchEvent(MotionEvent motionEvent);
}
