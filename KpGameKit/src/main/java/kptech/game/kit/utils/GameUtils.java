package kptech.game.kit.utils;

import com.kptach.lib.inter.game.APIConstants;

public class GameUtils {

    public static String getMsgByCode(int code) {

        String msg;
        switch (code) {
            case APIConstants.ERROR_GAME_INIT:
                msg = "游戏初始化错误";
                break;
            case APIConstants.ERROR_CALL_API:
                msg = "接口错误,请稍后再试";
                break;
            case APIConstants.APPLY_DEVICE_SUCCESS:
                msg = "申请设备成功";
                break;
            case APIConstants.ERROR_APPLY_DEVICE:
                msg = "申请设备失败，请稍后再试";
                break;
            case APIConstants.CONNECT_DEVICE_SUCCESS:
                msg = "连接设备成功";
                break;
            case APIConstants.ERROR_CONNECT_DEVICE:
                msg = "连接设备失败";
                break;
            case APIConstants.RELEASE_SUCCESS:
                msg = "释放设备";
                break;
            case APIConstants.ERROR_DEVICE_BUSY:
                msg = "试玩人数过多，请稍后再试";
                break;
            case APIConstants.ERROR_NETWORK:
                msg = "网络错误，请检查网络后再试";
                break;
            case APIConstants.ERROR_DEVICE_EXPIRED:
                msg = "设备过期";
                break;
            case APIConstants.ERROR_SDK_INIT:
                msg = "初始化游戏失败";
                break;
            case APIConstants.ERROR_GAME_INFO:
                msg = "未获取到游戏信息";
                break;
            case APIConstants.ERROR_GAME_CANCLED:
                msg = "游戏被取消启动";
                break;
            case APIConstants.GAME_SDK_INIT_SUCCESS:
                msg = "启动游戏成功";
                break;
            case APIConstants.AD_LOADING:
                msg = "广告加载中";
                break;
            case APIConstants.GAME_LOADING:
                msg = "游戏加载中";
                break;
            case APIConstants.RECOVER_DATA_LOADING:
                msg = "云手机数据恢复中";
                break;
            case APIConstants.TIMEOUT_AVAILABLE_TIME:
                msg = "可用时间到达";
                break;
            case APIConstants.TIMEOUT_NO_OPS:
                msg = "长时间未操作";
                break;
            case APIConstants.GAME_EXIT_SUCCESS:
                msg = "游戏退出";
                break;
            case APIConstants.DATA_FPS_GAME:
                msg = "游戏帧率数据";
                break;
            case APIConstants.DATA_NETWORK_LATENCY:
                msg = "网络延迟数据";
                break;
            case APIConstants.SWITCH_GAME_RESOLUTION_SUCCESS:
                msg = "切换游戏分辨率成功";
                break;
            case APIConstants.SWITCH_GAME_RESOLUTION_ERROR:
                msg = "切换游戏分辨率失败";
                break;
            case APIConstants.ERROR_GAME_INNER:
                msg = "游戏数据内部错误";
                break;
            case APIConstants.ERROR_OTHER:
                msg = "其他错误联系管理员";
            default:
                msg = "服务异常，请稍后再试";
                break;
        }

        return msg;
    }
}
