package kptech.game.kit.msg.mqtt;

import android.app.Activity;
import android.app.Application;

import java.lang.ref.WeakReference;
import java.util.Map;

import kptech.game.kit.BuildConfig;
import kptech.game.kit.msg.IMsgReceiver;
import kptech.game.kit.msg.MsgHandler;
import kptech.game.kit.msg.MsgManager2;
import kptech.game.kit.utils.Logger;

/**
 * @author chenggongzhao
 * @version $
 */
public class MsgSuper implements MsgHandler.ICallback{
    protected MsgHandler mHandler;
    protected String mCorpKey;
    protected WeakReference<IMsgReceiver> mReceiverRef;

    private static volatile MsgSuper mInstance;

    public MsgSuper(){

    }

    public static MsgSuper getInstance(){
        if (mInstance == null){
            createRender();
        }
        return mInstance;
    }

    public void initGameMsg(Activity activity, String corpId, String pkgName){
        this.mCorpKey = corpId;
        this.mHandler = new MsgHandler(activity, corpId, pkgName);
        this.mHandler.setCallback(this);
    }

    private static void createRender(){
        if (!BuildConfig.useSDK2){
            mInstance = MsgManager3.instance();
        }else {
            mInstance = MsgManager2.instance();
        }
    }

    public void setMessageReceiver(IMsgReceiver receiver) {
        if (receiver != null) {
            mReceiverRef = new WeakReference<>(receiver);
        }
    }

    public void destory() {
        this.mHandler.destory();
        this.mHandler = null;
        try {
            if (mReceiverRef != null) {
                mReceiverRef.clear();
                mReceiverRef = null;
            }
            mInstance = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPadCode(String padCode){
        if (mHandler!=null){
            mHandler.setPadCode(padCode);
        }
    }

    public void setGameId(String gameId){
        if (mHandler!=null){
            mHandler.setGameId(gameId);
        }
    }

    public void setGameName(String gameName){
        if (mHandler!=null){
            mHandler.setGameName(gameName);
        }
    }

    public void setPkgName(String pkgName){
        if (mHandler!=null){
            mHandler.setPkgName(pkgName);
        }
    }

    public void init(Application application, String corpId){
        Logger.info("MsgSuper","init:hashCode=" + mInstance.hashCode() + ";className = " + mInstance.getClass().getName());
    }

    public void setDebug(boolean debug){
    }

    public void start(Activity activity, String corpId, String padCode, String pkgName, String gameId, String gameName){
    }

    public void sendMessage(String msg){
    }

    public void stop(){
    }

    @Override
    public void onLogin(int code, String err, Map<String, Object> map) {
    }

    @Override
    public void onPay(int code, String err, Map<String, Object> map) {
    }

    @Override
    public void onLogout() {
    }

}
