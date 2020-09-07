package kptech.game.kit.analytic;

import kptech.game.kit.APIConstants;

public class EventCode {
    //启动游戏界面
    public static final String DATA_ACTIVITY_PLAYGAME_ONCREATE = "DATA_ACTIVITY_PLAYGAME_ONCREATE";
    //关闭游戏界面
    public static final String DATA_ACTIVITY_PLAYGAME_DESTORY = "DATA_ACTIVITY_PLAYGAME_DESTORY";
    //游戏中边玩边下按钮
    public static final String DATA_ACTIVITY_PLAYGAME_DOWNLOAD = "DATA_ACTIVITY_PLAYGAME_DOWNLOAD";
    //游戏错误界面下载
    public static final String DATA_ACTIVITY_PLAYERROR_DOWNLOAD = "DATA_ACTIVITY_PLAYERROR_DOWNLOAD";
    //游戏错误界面重新加载
    public static final String DATA_ACTIVITY_PLAYERROR_RELOAD = "DATA_ACTIVITY_PLAYERROR_RELOAD";

    //初始化SDK
    public static final String DATA_SDK_INIT = "DATA_SDK_INIT";
    //SDK初化成功
    public static final String DATA_SDK_INIT_OK = "DATA_SDK_INIT_OK";
    //SDK初化失败
    public static final String DATA_SDK_INIT_FAILED = "DATA_SDK_INIT_FAILED";

    //准备申请设备
    public static final String DATA_DEVICE_APPLY_START = "DATA_DEVICE_APPLY_START";
    //设备申请成功
    public static final String DATA_DEVICE_APPLY_OK = "DATA_DEVICE_APPLY_OK";
    //设备申请失败
    public static final String DATA_DEVICE_REQ_ERROR = "DATA_DEVICE_REQ_ERROR";
    //设备繁忙
    public static final String DATA_DEVICE_APPLY_BUSY = "DATA_DEVICE_APPLY_BUSY";
    //网络异常
    public static final String DATA_DEVICE_NET_ERROR = "DATA_DEVICE_NET_ERROR";
    //设备掉线
    public static final String DATA_DEVICE_APPLY_FAILED = "DATA_DEVICE_APPLY_FAILED";

    //准备开始游戏
    public static final String DATA_VIDEO_READY_RECVING = "DATA_VIDEO_READY_RECVING";
    //成功开始游戏，接收视频
    public static final String DATA_VIDEO_START_RECVING = "DATA_VIDEO_START_RECVING";
    //开始游戏出错
    public static final String DATA_VIDEO_START_RECVINGERR = "DATA_VIDEO_START_RECVINGERR";
    //关闭游戏
    public static final String DATA_VIDEO_CLOSE = "DATA_VIDEO_CLOSE";

    //用户长时间未操作
    public static final String DATA_USER_LEAVE = "DATA_USER_LEAVE";

    //初始化广告成功
    public static final String DATA_AD_INIT_OK = "DATA_AD_INIT_OK";
    //初始化广告失败
    public static final String DATA_AD_INIT_FAILED = "DATA_AD_INIT_FAILED";

    //显示广告窗口
    public static final String DATA_AD_DIALOG_DISPLAY = "DATA_AD_DIALOG_DISPLAY";
    //取消广告，关闭窗口
    public static final String DATA_AD_DIALOG_CANCEL = "DATA_AD_DIALOG_CANCEL";

    //广告行为
    public static final String DATA_AD_REWARD_LOADING = "DATA_AD_REWARD_LOADING";
    public static final String DATA_AD_REWARD_EMPTY = "DATA_AD_REWARD_EMPTY";
    public static final String DATA_AD_REWARD_READY = "DATA_AD_REWARD_READY";
    public static final String DATA_AD_REWARD_DISPLAY = "DATA_AD_REWARD_DISPLAY";
    public static final String DATA_AD_REWARD_VERIFY = "DATA_AD_REWARD_VERIFY";
    public static final String DATA_AD_REWARD_CLOSED = "DATA_AD_REWARD_CLOSED";
    public static final String DATA_AD_REWARD_CLICK = "DATA_AD_REWARD_CLICK";
    public static final String DATA_AD_REWARD_PLAYCOMPLETE = "DATA_AD_REWARD_PLAYCOMPLETE";

    public static final String DATA_AD_EXT_LOADING = "DATA_AD_EXT_LOADING";
    public static final String DATA_AD_EXT_EMPTY = "DATA_AD_EXT_EMPTY";
    public static final String DATA_AD_EXT_READY = "DATA_AD_EXT_READY";
    public static final String DATA_AD_EXT_DISPLAY = "DATA_AD_EXT_DISPLAY";
    public static final String DATA_AD_EXT_VERIFY = "DATA_AD_EXT_VERIFY";
    public static final String DATA_AD_EXT_CLOSED = "DATA_AD_EXT_CLOSED";
    public static final String DATA_AD_EXT_CLICK = "DATA_AD_EXT_CLICK";

    public static final String DATA_GAME_PLAY_TIME = "DATA_GAME_PLAY_TIME";

    public static String getDeviceEventCode(int code){
        if (code == APIConstants.APPLY_DEVICE_SUCCESS){
            return DATA_DEVICE_APPLY_OK;
        }else if(code == APIConstants.ERROR_NO_DEVICE){
            return DATA_DEVICE_APPLY_BUSY;
        }else if(code == APIConstants.ERROR_NETWORK_ERROR){
            return DATA_DEVICE_NET_ERROR;
        }else if(code == APIConstants.ERROR_SDK_INIT_ERROR){
            return DATA_SDK_INIT_FAILED;
        }

        return DATA_DEVICE_REQ_ERROR;
    }

    public static String getGameEventCode(int code){
        if (code == APIConstants.CONNECT_DEVICE_SUCCESS || code == APIConstants.RECONNECT_DEVICE_SUCCESS){
            return DATA_VIDEO_START_RECVING;
        }else if(code == APIConstants.RELEASE_SUCCESS ){
            return DATA_VIDEO_CLOSE;
        }
        return DATA_VIDEO_START_RECVINGERR;
    }
}
