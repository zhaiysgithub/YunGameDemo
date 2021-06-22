package kptech.game.kit.msg.mqtt;

import android.app.Activity;
import android.app.Application;

import java.lang.ref.WeakReference;
import java.util.Map;

import kptech.game.kit.BuildConfig;
import kptech.game.kit.msg.IMsgReceiver;
import kptech.game.kit.msg.MsgHandler;
import kptech.game.kit.msg.MsgManager2;

/**
 * @author chenggongzhao
 * @version $
 * @des
 * @updateAuthor $
 * @updateDes
 */
public class MsgSuper implements MsgHandler.ICallback{
    protected MsgHandler mHandler;
    protected String mCorpKey;
    protected WeakReference<IMsgReceiver> mReceiverRef;

    protected static MsgSuper mInstance;

    public MsgSuper(){
        createRender();
    }

    public static MsgSuper getInstance(){
        return mInstance;
    }


    protected void initGameMsg(Activity activity, String corpId, String pkgName){
        this.mCorpKey = corpId;
        this.mHandler = new MsgHandler(activity, corpId, pkgName);
        this.mHandler.setCallback(this);
    }

    public void createRender(){
        if (!BuildConfig.useSDK2){
            mInstance = MsgManager3.instance();
        }else {
            mInstance = null;
//            mInstance = MsgManager2.instance();
        }
    }




    public void setMessageReceiver(IMsgReceiver receiver) {
        if (receiver != null) {
            mReceiverRef = new WeakReference<>(receiver);
        }
    }

    protected void destory() {
        this.mHandler.destory();
        this.mHandler = null;
        try {
            if (mReceiverRef != null) {
                mReceiverRef.clear();
                mReceiverRef = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setPadCode(String padCode){
        if (mHandler!=null){
            mHandler.setPadCode(padCode);
        }
    }

    protected void setGameId(String gameId){
        if (mHandler!=null){
            mHandler.setGameId(gameId);
        }
    }

    protected void setGameName(String gameName){
        if (mHandler!=null){
            mHandler.setGameName(gameName);
        }
    }

    protected void setPkgName(String pkgName){
        if (mHandler!=null){
            mHandler.setPkgName(pkgName);
        }
    }

    public void init(Application application, String corpId){
//        if (mInstance != null){
//            mInstance.init(application, corpId);
//        }

    }

    public void setDebug(boolean debug){
//        if (mInstance != null){
//            mInstance.setDebug(debug);
//        }

    }

    public void start(Activity activity, String corpId, String padCode, String pkgName, String gameId, String gameName){
//        mInstance.subStart(activity, corpId, padCode, pkgName, gameId, gameName);
    }

    public void sendMessage(String msg){
//        mInstance.sendMessage(msg);
    }

    public void stop(){
//        mInstance.stop();
    }


    @Override
    public void onLogin(int code, String err, Map<String, Object> map) {
//        mInstance.onLogin(code, err, map);
    }

    @Override
    public void onPay(int code, String err, Map<String, Object> map) {
//        mInstance.onPay(code, err, map);
    }

    @Override
    public void onLogout() {
//        mInstance.onLogout();
    }
}
