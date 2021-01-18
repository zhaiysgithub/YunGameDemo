package kptech.game.kit.redfinger.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class PlayerGLSurfaceView extends GLSurfaceView implements SurfaceHolder.Callback {
    private PlayerEventHandler playerEventHandler;
    private boolean setRenderered = false;
    private SurfaceHolder surfaceHolder;

    public PlayerGLSurfaceView(Context context) {
        super(context);
        setFocusable(true);
        setKeepScreenOn(true);
        SurfaceHolder holder = getHolder();
        this.surfaceHolder = holder;
        holder.addCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.setRenderered) {
            setRenderMode(0);
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        PlayerEventHandler playerEventHandler2 = this.playerEventHandler;
        if (playerEventHandler2 == null) {
            return true;
        }
        playerEventHandler2.setOnKeyDown(keyEvent.getScanCode(), keyEvent);
        return true;
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        PlayerEventHandler playerEventHandler2 = this.playerEventHandler;
        if (playerEventHandler2 == null) {
            return true;
        }
        playerEventHandler2.setOnKeyUp(keyEvent.getScanCode(), keyEvent);
        return true;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        PlayerEventHandler playerEventHandler2 = this.playerEventHandler;
        if (playerEventHandler2 == null) {
            return true;
        }
        playerEventHandler2.setOnTouchEvent(motionEvent);
        return true;
    }

    public void setPlayerEventHandler(PlayerEventHandler playerEventHandler2) {
        this.playerEventHandler = playerEventHandler2;
    }

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        super.setRenderer(renderer);
        this.setRenderered = true;
    }

    public PlayerGLSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setFocusable(true);
        setKeepScreenOn(true);
        getHolder().addCallback(this);
    }
}