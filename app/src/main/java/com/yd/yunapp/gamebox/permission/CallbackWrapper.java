package com.yd.yunapp.gamebox.permission;

import java.lang.ref.WeakReference;

public class CallbackWrapper<T> {
    private WeakReference<T> mReceiver;

    public CallbackWrapper(T receiver) {
        mReceiver = new WeakReference<T>(receiver);
    }

    protected T fetchReceiver() {
        if (mReceiver != null && mReceiver.get() != null) {
            return mReceiver.get();
        }

        return null;
    }
}
