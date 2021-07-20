package kptech.game.kit.utils;

import com.kptach.lib.inter.game.APIConstants;

import org.json.JSONObject;

import kptech.lib.analytic.EventCode;

/**
 * code 转换对外输出的 类
 */
public class ApiCodeConvertUtils {

    /**
     * paas 申请设备错误code码的转换
     * @param applyDeviceCode 申请设备服务器返回的错误 code 码
     * @return 转换后的 code
     */
    public static int getPaasApplyDeviceErrorCode(int applyDeviceCode){
        int apiErrorCode;
        switch (applyDeviceCode){
            case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_CORPKEY:  //10000
            case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_PKGNAME:  //10001
            case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_AUTH:     //10002
            case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_APP:      //10004
            case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_DEVICENO:  //10005
                apiErrorCode = com.kptach.lib.inter.game.APIConstants.ERROR_SDK_INIT;
                break;
            case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_DEVICEBUSY:  //10003
                apiErrorCode = com.kptach.lib.inter.game.APIConstants.ERROR_DEVICE_BUSY;
                break;
            case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_DEFAULT:   //10006
                apiErrorCode = com.kptach.lib.inter.game.APIConstants.ERROR_OTHER;
                break;
            case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_SUCCESS:
            case com.kptach.lib.inter.game.APIConstants.APPLY_DEVICE_SUCCESS:
                apiErrorCode = com.kptach.lib.inter.game.APIConstants.APPLY_DEVICE_SUCCESS;
                break;
            default:
                apiErrorCode = APIConstants.ERROR_APPLY_DEVICE;
                break;
        }
        return apiErrorCode;
    }

    public static String[] getTraceEventByApplyDeviceCode(int applyDeviceCode){
        String[] result = new String[3];
        String eventStr = EventCode.DATA_DEVICE_ERROR_UNKNOWN_TRACE;
        int errcode = 0;
        String errmsg = "";
        int eventType = 3;
        try {
            switch (applyDeviceCode){
                case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_SUCCESS:
                case com.kptach.lib.inter.game.APIConstants.APPLY_DEVICE_SUCCESS:
                    eventStr = EventCode.DATA_DEVICE_APPLY_OK_TRACE;
                    eventType = EventCode.TYPE_TRACE_PROCE_NORMAL;
                    break;
                case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_CORPKEY:
                    eventStr = EventCode.DATA_CHECK_FAILED_CORPKEY_TRACE;
                    errcode = applyDeviceCode;
                    errmsg = "corpKey不存在";
                    eventType = EventCode.TYPE_TRACE_PROCE_ERROR;
                    break;
                case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_PKGNAME:
                    eventStr = EventCode.DATA_DEVICE_APP_UNDEFINE_TRACE;
                    errcode = applyDeviceCode;
                    errmsg = "pkgName错误";
                    eventType = EventCode.TYPE_TRACE_PROCE_ERROR;
                    break;
                case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_AUTH:
                    eventStr = EventCode.DATA_DEVICE_APP_NOTMATCH_TRACE;
                    errcode = applyDeviceCode;
                    errmsg = "未授权的APP访问";
                    eventType = EventCode.TYPE_TRACE_PROCE_ERROR;
                    break;
                case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_DEVICEBUSY:
                    eventStr = EventCode.DATA_DEVICE_APPLY_BUSY_TRACE;
                    errcode = applyDeviceCode;
                    errmsg = "无空闲设备";
                    eventType = EventCode.TYPE_TRACE_PROCE_ERROR;
                    break;
                case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_APP:
                    eventStr = EventCode.DATA_DEVICE_APP_NOTINSTALL_TRACE;
                    errcode = applyDeviceCode;
                    errmsg = "APP未安装到相关设备";
                    eventType = EventCode.TYPE_TRACE_PROCE_ERROR;
                    break;
                case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_DEVICENO:
                    eventStr = EventCode.DATA_DEVICE_ERROR_NODEVICE_TRACE;
                    errcode = applyDeviceCode;
                    errmsg = "该企业未分配设备";
                    eventType = EventCode.TYPE_TRACE_PROCE_ERROR;
                    break;
                case kptech.game.kit.pass.manager.PassConstants.PASS_CODE_ERROR_DEFAULT:
                    eventStr = EventCode.DATA_DEVICE_ERROR_IAAS_TRACE;
                    errcode = applyDeviceCode;
                    errmsg = "操作失败,请联系管理员";
                    eventType = EventCode.TYPE_TRACE_PROCE_ERROR;
                    break;
                case APIConstants.ERROR_APPLY_DEVICE:
                    eventStr = EventCode.DATA_DEVICE_APPLY_FAILED_TRACE;
                    errcode = applyDeviceCode;
                    errmsg = "申请设备失败";
                    eventType = EventCode.TYPE_TRACE_PROCE_ERROR;
                    break;
                case APIConstants.ERROR_PAAS_APPLY_IAAS:
                    eventStr = EventCode.DATA_DEVICE_ERROR_UNKNOWN_TRACE;
                    errcode = applyDeviceCode;
                    errmsg = "iaas无匹配值";
                    eventType = EventCode.TYPE_TRACE_PROCE_ERROR;
                    break;
                case APIConstants.ERROR_PAAS_APPLY_TIMEOUT:
                    eventStr = EventCode.DATA_DEVICE_APPLY_TIMEOUT_TRACE;
                    errcode = applyDeviceCode;
                    errmsg = "paas3申请设备超时";
                    eventType = EventCode.TYPE_TRACE_PROCE_FAIL;
                    break;
            }
            result[0] = eventStr;
            result[1] = eventType + "";
            JSONObject jsonMsg = new JSONObject();
            if (errcode > 0){
                jsonMsg.put("errcode",errcode);
                jsonMsg.put("errmsg",errmsg);
            }
            result[2] = jsonMsg.toString();
            return result;
        }catch (Exception e){
            e.printStackTrace();

        }
        return null;
    }


    public static String[] getVideoStatusCode(int code){
        String[] ret = new String[2];
        String eventName;
        int eventType;
        try {
            switch (code){
                case APIConstants.CONNECT_DEVICE_SUCCESS:
                case APIConstants.RECONNECT_DEVICE_SUCCESS:
                    eventName = EventCode.DATA_VIDEO_START_RECVING_TRACE;
                    eventType = EventCode.TYPE_TRACE_PROCE_NORMAL;
                    break;
                case APIConstants.RELEASE_SUCCESS:
                    eventName = EventCode.DATA_VIDEO_CLOSE_TRACE;
                    eventType = EventCode.TYPE_TRACE_PROCE_NORMAL;
                    break;
                case APIConstants.ERROR_SDK_INNER:
                    eventName = EventCode.DATA_DEVICE_APP_FAILED_TRACE;
                    eventType = EventCode.TYPE_TRACE_PROCE_ERROR;
                    break;
                case APIConstants.ERROR_NETWORK:
                    eventName = EventCode.DATA_VIDEO_NET_CONN_ERROR_TRACE;
                    eventType = EventCode.TYPE_TRACE_PROCE_ERROR;
                    break;
                case APIConstants.ERROR_OTHER:
                    eventName = EventCode.DATA_VIDEO_FAILED_UNKNOWN_TRACE;
                    eventType = EventCode.TYPE_TRACE_PROCE_ERROR;
                    break;
                default:
                    eventName = "";
                    eventType = -1;
                    break;
            }
            ret[0] = eventName;
            ret[1] = eventType + "";
            return ret;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

}
