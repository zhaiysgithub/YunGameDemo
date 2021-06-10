package kptech.game.kit.manager;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.kptach.lib.inter.game.IGameBoxManager;
import com.kptach.lib.inter.game.IGameCallback;

import java.util.HashMap;

import kptech.game.kit.APICallback;
import kptech.game.kit.APIConstants;
import kptech.game.kit.BuildConfig;
import kptech.game.kit.DeviceControl;
import kptech.game.kit.GameInfo;
import kptech.game.kit.IDeviceControl;
import kptech.game.kit.model.PassDeviceResponseBean;
import kptech.game.kit.utils.MillisecondsDuration;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;
import kptech.lib.fatory.GameBoxManagerFactory;

public class KpCloudDeviceManager {

    private MillisecondsDuration mTimeDuration;
    private KpCloudDeviceManager() {
    }

    private static class KpCloudDeviceHolder {
        private static final KpCloudDeviceManager INSTANCE = new KpCloudDeviceManager();
    }

    public static KpCloudDeviceManager instance() {
        return KpCloudDeviceHolder.INSTANCE;
    }

    public void initDeviceControl(@NonNull Activity activity, final String mCorpID, PassDeviceResponseBean.PassData data
            , @NonNull final GameInfo inf
            , @NonNull final APICallback<IDeviceControl> callback){

        try {
            //重置打点数据
            Event.resetTrackIdFromBase();

            HashMap<String,Object> ext = new HashMap<>();
            ext.put("gid", inf.gid);
            ext.put("useSDK", inf.useSDK.name());

            //发送打点事件
            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_DEVICE_APPLY_START, inf.pkgName, ext));

            if (mTimeDuration == null) {
                mTimeDuration = new MillisecondsDuration();
            }

            //发送事件耗时统计
            long useTm = mTimeDuration.duration();
            HashMap<String,Object> mapData = new HashMap<>();
            mapData.put("stime", mTimeDuration.getSavedTime());
            mapData.put("etime", mTimeDuration.getCurentTime());
            Event event = Event.getTMEvent(EventCode.DATA_TMDATA_DEVICE_START, useTm, mapData);
            event.setGamePkg(inf.pkgName);
            MobclickAgent.sendTMEvent(event);

            mTimeDuration = new MillisecondsDuration();
        }catch (Exception e){
            e.printStackTrace();
        }

        HashMap<String,Object> sdkParams = new HashMap<>();
        sdkParams.put("resource",data.resource);
        sdkParams.put(IGameBoxManager.PARAMS_KEY_DEBUG, BuildConfig.DEBUG);
        String iaas = data.iaas;
        if (iaas.equals("BD")){
            inf.useSDK = GameInfo.SdkType.BD;
        }else if(iaas.equals("HW")){
            inf.useSDK = GameInfo.SdkType.HW;
        }

        IGameBoxManager gameBoxManager = GameBoxManagerFactory.getGameBoxManager(inf.useSDK, activity.getApplication(),sdkParams);
        gameBoxManager.createDeviceControl(activity, inf.toJsonString(),sdkParams ,new IGameCallback<com.kptach.lib.inter.game.IDeviceControl>() {
            @Override
            public void onGameCallback(com.kptach.lib.inter.game.IDeviceControl innerControl, int code) {

                DeviceControl control = null;
                if (innerControl != null){
                    control = new DeviceControl(innerControl, inf);
                    control.setCorpKey(mCorpID);
                }

                try {
                    //发送打点事件
                    Event event = Event.getEvent(EventCode.getDeviceEventCode(code), inf.pkgName);
                    if (control != null){
                        event.setPadcode(control.getPadcode());
                    }
                    event.setErrMsg(""+code);
                    HashMap ext = new HashMap<>();
                    ext.put("code", code);
                    if(innerControl!=null && innerControl.getSdkType()!=null){
                        ext.put("useSDK", innerControl.getSdkType().toString());
                    }
                    event.setExt(ext);
                    MobclickAgent.sendEvent(event);
                }catch (Exception e){}

                try {
                    //发送事件耗时统计
                    long useTm = mTimeDuration.duration();
                    HashMap data = new HashMap();
                    data.put("stime", mTimeDuration.getSavedTime());
                    data.put("etime", mTimeDuration.getCurentTime());
                    data.put("state", code == APIConstants.APPLY_DEVICE_SUCCESS ? "ok" : "failed");
                    data.put("code", code);
                    Event event = Event.getTMEvent(EventCode.DATA_TMDATA_DEVICE_END, useTm, data);
                    event.setGamePkg(inf.pkgName);
                    if (control != null){
                        event.setPadcode(control.getPadcode());
                    }
                    MobclickAgent.sendTMEvent(event);

                    mTimeDuration = null;
                }catch (Exception e){}

                //回调方法
                if (callback!=null){
                    callback.onAPICallback(control, code);
                }
            }
        });
    }
}
