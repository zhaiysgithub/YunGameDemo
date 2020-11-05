package kptech.game.kit.thread;

import android.os.HandlerThread;
import android.os.Looper;

import kptech.game.kit.utils.Logger;

public class HeartThread extends HandlerThread {
    private static volatile HeartThread thread = null;

    public static HeartThread getInstance() {
        try {
            if (thread!=null && !thread.isAlive()){
                thread.quit();
                thread = null;
            }
            if (thread == null) {
                synchronized(HeartThread.class) {
                    if (thread == null) {
                        thread = new HeartThread("gameheart-thread");
                        thread.start();
                    }
                }
            }

            return thread;
        }catch (Exception e){
//            logger.error(e.getMessage());
            Logger.error("HeartThread", e.getMessage());
        }

        return null;
    }

    public HeartThread(String name) {
        super(name);
    }

}
