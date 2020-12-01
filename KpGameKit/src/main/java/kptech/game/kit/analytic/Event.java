package kptech.game.kit.analytic;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kptech.game.kit.BuildConfig;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.utils.DeviceUtils;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.game.kit.utils.StringUtil;

public class Event implements Cloneable {
    /**
     * 固定前缀，其他分段可作为行为标识，全部大写
     */
    String event;
    /**
     * corpKey
     */
    String clientId;
    /**
     * groupId
     */
    String groupId;
    /**
     * 随机数：h51597817231222，一次的用户行为
     */
    public String traceId;
    /**
     * 用户唯一id, 当前设备生成，缓存到本地
     */
    String userId;
    /**
     * 固定值，android用户
     */
    String userType = "AR_KPUSER";
    /**
     * 云设备padcode
     */
    String padcode;
    /**
     * 游戏包名
     */
    String gamePkg;
    /**
     * 固定值
     */
    String actionResult = "SUCC";
    /**
     * 错误消息
     */
    String errMsg;
    /**
     * 调试模式，1：调试模式，数据写入测试表
     */
    int debug = BuildConfig.DEBUG ? 1 : 0;
    /**
     * 扩展
     */
    Map ext;
    /**
     * 版本号
     */
    String ver = BuildConfig.VERSION_NAME;
    /**
     *
     */
    String datafrom = "androidapp";

    /**
     * 视频时间
     */
    int hearttimes = 0;

    /**
     * 时间段统计数据
     */
    Map tmData;
    /**
     * 时间段时间
     */
    long tmLocalTime = 0;
    /**
     * 时间段统计时长
     */
    long tmTimeLen = 0;


    private static Context mContext;
    private static String mCorpKey;
    private static String mGuid;

    private static boolean inited = false;

    private static String mBaseTraceId = null;

    public static void init(Application application, String appKey) {
        if (inited) {
            return;
        }
        if (application == null || StringUtil.isEmpty(appKey)){
            return;
        }

        if (application!=null && !StringUtil.isEmpty(appKey)){
            mContext = application;
            mCorpKey = appKey;
            mBaseTraceId = createTraceId();
            createBaseEvent(application, appKey);
            inited = true;
        }
    }

    public static void setGuid(String guid) {
        mGuid = guid;
    }

    /**
     * 请求json
     * @return
     */
    public String toRequestJson(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("event", this.event);
            obj.put("clientid", this.clientId != null ? this.clientId : "");
            obj.put("usertype", this.userType);
            obj.put("userid", this.userId != null ? this.userId : "");
            obj.put("padcode", this.padcode != null ? this.padcode : "");
            obj.put("package", this.gamePkg != null ? this.gamePkg : "");
            obj.put("actionresult", this.actionResult);
            obj.put("errmsg", this.errMsg != null ? this.errMsg : "");
            obj.put("traceid", this.traceId != null ? this.traceId : "");
            obj.put("h5sdkversion", this.ver);
            obj.put("datafrom", this.datafrom);
            obj.put("debug", this.debug);
            obj.put("groupid", this.groupId);
            obj.put("guid", mGuid!=null?mGuid:"");
            JSONObject extObj = null;
            if (this.ext != null){
                try { extObj = new JSONObject(this.ext); }catch (Exception e){}
            }
            obj.put("ext", extObj != null ? extObj : "");
        }catch (Exception e){

        }
        return obj.toString();
    }

    public Map toTimeRequestMap(){
        Map<String, String> map = new HashMap<>();
        try {
            map.put("clientid", (this.clientId != null ? this.clientId : ""));
            map.put("package", (this.gamePkg != null ? this.gamePkg : ""));
            map.put("traceid", (this.traceId != null ? this.traceId : ""));
            map.put("hearttimes",  ""+this.hearttimes);
            map.put("datafrom", this.datafrom);
            map.put("userid", (this.userId != null ? this.userId : ""));
            map.put("padcode", (this.padcode != null ? this.padcode : ""));
            map.put("usertype", this.userType);
            map.put("h5sdkversion", this.ver);
        }catch (Exception e){
        }
        return map;
    }

//    public String toTimeRequestJson(){
//        StringBuilder sb = new StringBuilder();
//
//        try {
//            sb.append("clientid=" + (this.clientId != null ? this.clientId : ""));
//            sb.append("&package=" + (this.gamePkg != null ? this.gamePkg : ""));
//            sb.append("&traceid=" + (this.traceId != null ? this.traceId : ""));
//            sb.append("&hearttimes=" +  this.hearttimes);
//            sb.append("&datafrom=" + this.datafrom);
//            sb.append("&userid=" + (this.userId != null ? this.userId : ""));
//            sb.append("&padcode=" + (this.padcode != null ? this.padcode : ""));
//            sb.append("&usertype=" + this.userType);
//            sb.append("&h5sdkversion=" + this.ver);
//        }catch (Exception e){
//
//        }
//
//        return sb.toString();
//    }


    public Map toTMRequestMap(){
        Map<String, String> map = new HashMap<>();

        try {
            map.put("corpkey", (this.clientId != null ? this.clientId : ""));
            map.put("uid", (this.userId != null ? this.userId : ""));
            map.put("traceid", (this.traceId != null ? this.traceId : ""));

            map.put("action", this.event);
            map.put("type","SDK");

            map.put("pkgname",(this.gamePkg != null ? this.gamePkg : ""));
            map.put("padcode",(this.padcode != null ? this.padcode : ""));

            map.put("localtm", "" + this.tmLocalTime);
            map.put("useractiontime", "" + this.tmTimeLen);

            map.put("sdkversion", this.ver);
            map.put("debug", "" + this.debug);

            if (this.tmData != null){
                try {
                    JSONObject tmDataObj = new JSONObject(this.tmData);
                    map.put("data", tmDataObj.toString());
                }catch (Exception e){}
            }

            if (this.ext != null){
                try {
                    JSONObject extObj = new JSONObject(this.ext);
                    map.put("ext", extObj.toString());
                }catch (Exception e){}
            }

        }catch (Exception e){

        }

        return map;
    }

//    public String toTMRequestJson(){
//        StringBuilder sb = new StringBuilder();
//
//        try {
//            sb.append("corpkey=" + (this.clientId != null ? this.clientId : ""));
//            sb.append("&uid=" + (this.userId != null ? this.userId : ""));
//            sb.append("&traceid=" + (this.traceId != null ? this.traceId : ""));
//
//            sb.append("&action=" + this.event);
//            sb.append("&type=SDK");
//
//            sb.append("&pkgname=" + (this.gamePkg != null ? this.gamePkg : ""));
//            sb.append("&padcode=" + (this.padcode != null ? this.padcode : ""));
//
//            sb.append("&localtm=" + this.tmLocalTime);
//            sb.append("&useractiontime=" + this.tmTimeLen);
//
//            sb.append("&sdkversion=" + this.ver);
//            sb.append("&debug=" + this.debug);
//
//            if (this.tmData != null){
//                try {
//                    JSONObject tmDataObj = new JSONObject(this.tmData);
//                    sb.append("&data=" + tmDataObj.toString());
//                }catch (Exception e){}
//            }
//
//            if (this.ext != null){
//                try {
//                    JSONObject extObj = new JSONObject(this.ext);
//                    sb.append("&ext=" + extObj.toString());
//                }catch (Exception e){}
//            }
//
//        }catch (Exception e){
//
//        }
//
//        return sb.toString();
//    }

    private static String createTraceId(){
        int random = (int)(Math.random()*900)+100;
        String  traceId = new Date().getTime() + "" + random;
        return "ar" + traceId;
    }

    private static String createGroupId(){
        int random = (int)(Math.random()*90)+10;
        String  traceId = new Date().getTime() + "" + random;
        return "ar" + traceId;
    }

//    private static String getUserId(Context context){
//        try {
//            String userId = ProferencesUtils.getString(context, SharedKeys.KEY_EVENT_USERID, null);
//            if (userId == null || "".equals(userId)){
////                String str = DeviceUtils.getDeviceId(context);
//
//                String uniqueID = UUID.randomUUID().toString();
////                if (StringUtil.isEmpty(str)){
////                    int random = (int)(Math.random()*900)+100;
////                    str = new Date().getTime() + "" + random;
////                }
//                userId = StringUtil.getMD5(uniqueID);
//                ProferencesUtils.setString(context, SharedKeys.KEY_EVENT_USERID, userId);
//            }
//            return userId;
//        }catch (Exception e){
//        }
//        return "";
//    }

    private static Event base = null;

    private static void createBaseEvent(Context context, String corpId){
        if (context!=null){
            base = new Event();
            base.clientId = corpId != null ? corpId : mCorpKey;
            base.userId = DeviceInfo.getUserId(context);
            base.groupId = createGroupId();
            base.traceId = mBaseTraceId;
        }
    }

    public static void resetBaseTraceId(){
        mBaseTraceId = createTraceId();
        if (base!= null){
            base.traceId = mBaseTraceId;
        }
    }

    public static String getBaseTraceId(){
        return mBaseTraceId;
    }

    public static void resetTrackIdFromBase(){
        if (base != null){
            int random = (int)(Math.random()*900)+100;
            base.traceId = mBaseTraceId + "-" + random;
        }
    }


    public static Event getEvent(String event, String gamePkg, String padcode, String errMsg, Map ext){
        if (base == null){
            createBaseEvent(mContext, mCorpKey);
        }

        if (base != null){
            base.event = event;
            base.gamePkg = gamePkg;
            base.padcode = padcode;
            base.errMsg = errMsg!=null ? errMsg : null;
            base.ext = ext!=null ? ext : null;
            return base;
        }

        return new Event();
    }

    private static byte[] lock = new byte[0];
    public static Event getTMEvent(String event, long tmLen, Map data){
        synchronized (lock){
            Event base = getEvent(event, null, null, null, null);
            HashMap map =  new HashMap(data);
            map.put("value",tmLen);
            map.put("unit", "mm");
            base.tmData = map;
            base.tmLocalTime = new Date().getTime();
            base.tmTimeLen = tmLen;
            return base;
        }
    }

    public static Event getEvent(String event, String gamePkg, String padcode){
        return getEvent(event, gamePkg, padcode, null, null);
    }

    public static Event getEvent(String event, String gamePkg){
        return getEvent(event, gamePkg, null, null, null);
    }

    public static Event getEvent(String event){
        return getEvent(event, null, null, null, null);
    }

    public static Event getEvent(String event, String gamePkg, Map ext){
        return getEvent(event, gamePkg, null, null, ext);
    }

    public static Event getEvent(String event, Map ext){
        return getEvent(event, null, null, null, ext);
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public void setExt(Map ext) {
        this.ext = ext;
    }

    public void setPadcode(String padcode) {
        this.padcode = padcode;
    }

    public void setGamePkg(String gamePkg) {
        this.gamePkg = gamePkg;
    }

    public void setHearttimes(int hearttimes) {
        this.hearttimes = hearttimes;
    }


    public void setTmData(Map data){
        this.tmData = data;
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
