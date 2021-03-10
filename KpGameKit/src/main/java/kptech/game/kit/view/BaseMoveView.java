package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

public class BaseMoveView extends FrameLayout {

    private float mSlop;
    boolean isMove = false;
    private float[] mTemp = new float[]{
            0f, 0f
    };

    public BaseMoveView(@NonNull Context context) {
        super(context);
    }
    public BaseMoveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();
        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                mTemp[0] = event.getX();
                mTemp[1] = event.getY();
                isMove = false;
                break;
            case MotionEvent.ACTION_UP:
                if (isMove) {
                    int viewX = (int) (getLeft() + getTranslationX() + event.getX());
                    int viewY = (int) (getTop() + getTranslationY() + event.getY());
                    int viewWidth = getWidth();
                    int viewHeight = getHeight();
                    int parentWidth = ((View) getParent()).getWidth();
                    int parentHeight = ((View) getParent()).getHeight();

                    int targetX = viewX - (int) event.getX();
                    int targetY = viewY - (int) event.getY();

                    int xDistance = Math.min(Math.abs(viewX - 0), Math.abs(parentWidth - viewX));
                    int yDistance = Math.min(Math.abs(viewY - 0), Math.abs(parentHeight - viewY));
                    if (xDistance < yDistance) {
                        if (viewX < parentWidth / 2) {
                            targetX = 0;
                        } else {
                            targetX = parentWidth - viewWidth;
                        }

                        if (targetY > parentHeight - viewHeight) {
                            targetY = parentHeight - viewHeight;
                        } else if (targetY < 0) {
                            targetY = 0;
                        }
                    } else {
                        if (viewY < parentHeight / 2) {
                            targetY = 0;
                        } else {
                            targetY = parentHeight - viewHeight;
                        }

                        if (targetX > parentWidth - viewWidth) {
                            targetX = parentWidth - viewWidth;
                        } else if (targetX < 0) {
                            targetX = 0;
                        }
                    }

                    setX(targetX);
                    setY(targetY);
                } else {
                    performClick();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isMove || !pointInView(this, event.getX(), event.getY())) {
                    isMove = true;
                    int diffX = (int) (event.getX() - mTemp[0]);
                    int diffY = (int) (event.getY() - mTemp[1]);
                    setTranslationX(diffX + getTranslationX());
                    setTranslationY(diffY + getTranslationY());
                }
                break;
            default:
                break;
        }
        return true;
    }

    private boolean pointInView(View view, float localX, float localY) {
        return localX >= -mSlop && localY >= -mSlop
                && localX < ((view.getRight() - view.getLeft()) + mSlop)
                && localY < ((view.getBottom() - view.getTop()) + mSlop);
    }

}
