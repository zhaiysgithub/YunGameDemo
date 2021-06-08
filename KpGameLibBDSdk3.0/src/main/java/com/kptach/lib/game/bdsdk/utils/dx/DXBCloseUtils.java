package com.kptach.lib.game.bdsdk.utils.dx;


import java.io.Closeable;

/* compiled from: DXBCloseUtils */
public class DXBCloseUtils {
    public static void closeable(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable th) {
            }
        }
    }
}