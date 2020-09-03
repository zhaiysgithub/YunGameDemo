package kptech.game.kit;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.lang.ref.WeakReference;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;

import kptech.game.kit.utils.Logger;

public abstract class GameDownloader {
    /**
     * 下载状态
     */
    public static final int STATUS_STARTED = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_STOPED = 3;
    public static final int STATUS_FINISHED = 4;
    public static final int STATUS_CANCEL = 5;
    public static final int STATUS_WAITTING = 6;
    public static final int STATUS_ERROR = 7;

    private Logger logger = new Logger("GameDownloader");

    private ArrayList<WeakReference<GameDownloader.ICallback>> mCallbackList = new ArrayList<>();
    private CallbackHandler mHandler = null;

    public GameDownloader(){
        mHandler = new CallbackHandler();
    }

    /**
     * 开始下载
     * @param game
     * @return true 正常下载
     */
    public abstract boolean start(GameInfo game);

    /**
     * 停止下载
     * @param game
     */
    public void stop(GameInfo game){

    }

    /**
     * 进度回调方法
     * @param current
     * @param total
     */
    public synchronized void onProgresss(long current, long total, GameInfo game){
        if (mHandler!=null){
            Object[] data = new Object[]{current, total, game};
            mHandler.sendMessage(Message.obtain(mHandler,2, data));
        }
    }

    /**
     * 状态回调
     * @param status
     * @param msg
     */
    public synchronized void onStatusChanged(int status, String msg, GameInfo game){
        if (mHandler!=null){
            if (msg == null){
                msg = "";
            }
            Object[] obj = new Object[]{status, msg, game};
            mHandler.sendMessage(Message.obtain(mHandler, 1, obj));
        }
    }


    /**
     * 暂停
     */
    public void pause(){

    }

    /**
     * 继续
     */
    public void resume(){

    }


    public synchronized void addCallback(ICallback callback){
        //判断是否已经存在
        for (int i = 0; i < mCallbackList.size(); i++) {
            if(mCallbackList.get(i).get() == callback){
                return;
            }
        }
        mCallbackList.add(new WeakReference(callback));
        logger.info("addCallback" );
    }

    public synchronized void removeCallback(ICallback callback){
        for (int i = 0; i < mCallbackList.size(); i++) {
            if(mCallbackList.get(i).get() == callback){
                mCallbackList.remove(i);
                logger.info("removeCallback" );
                return;
            }
        }
    }

    public interface ICallback {

        void onDownloadStatusChanged(int status, String msg, GameInfo game);

        void onDownloadProgress(long total, long current,  GameInfo game);
    }

    private class CallbackHandler extends Handler {
        public CallbackHandler(){
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            Object[] obj = (Object[])msg.obj;
            switch (msg.what){
                //下载状态
                case 1:
                    try {
                        int status = (int) obj[0];
                        String info = (String) obj[1];
                        GameInfo game = null;
                        if (obj[2]!=null){
                            game = (GameInfo) obj[2];
                        }
                        for (WeakReference<GameDownloader.ICallback> ref : mCallbackList){
                            if ( ref.get() != null ){
                                ref.get().onDownloadStatusChanged(status, info, game);
                            }
                        }
                    }catch (Exception e){
                        logger.error(""+e.getMessage());
                    }
                    break;
                //下载进度
                case 2:
                    try {
                        long current = (long)obj[0];
                        long total = (long)obj[1];
                        GameInfo game = null;
                        if (obj[2]!=null){
                            game = (GameInfo) obj[2];
                        }
                        for (WeakReference<GameDownloader.ICallback> ref : mCallbackList){
                            if ( ref.get() != null ){
                                ref.get().onDownloadProgress(current, total, game);
                            }
                        }
                    }catch (Exception e){
                        logger.error(""+e.getMessage());
                    }
                    break;
            }

        }
    };
}
