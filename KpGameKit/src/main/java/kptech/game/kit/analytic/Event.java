package kptech.game.kit.analytic;

import android.app.Application;
import android.content.Context;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

import kptech.game.kit.BuildConfig;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.game.kit.utils.StringUtil;

public class Event {
    /**
     * 固定前缀，其他分段可作为行为标识，全部大写
     */
    String event;
    /**
     * corpKey
     */
    String clientId;
    /**
     * 随机数：h51597817231222，一次的用户行为
     */
    String traceId;
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

    int hearttimes = 0;


    private static Context mContext;
    private static String mCorpKey;

    public static void init(Application application, String appKey) {
        if (mContext==null && application!=null ){
            mContext = application;

        }
        if (mCorpKey==null && !StringUtil.isEmpty(appKey)){
            mCorpKey = appKey;
        }
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
            JSONObject extObj = null;
            if (this.ext != null){
                try { extObj = new JSONObject(this.ext); }catch (Exception e){}
            }
            obj.put("ext", extObj != null ? extObj : "");
        }catch (Exception e){

        }
        return obj.toString();
    }

    public String toTimeRequestJson(){
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("clientid=" + (this.clientId != null ? this.clientId : ""));
            sb.append("&package=" + (this.gamePkg != null ? this.gamePkg : ""));
            sb.append("&traceid=" + (this.traceId != null ? this.traceId : ""));
            sb.append("&hearttimes=" +  this.hearttimes);
            sb.append("&datafrom=" + this.datafrom);
            sb.append("&userid=" + (this.userId != null ? this.userId : ""));
            sb.append("&padcode=" + (this.padcode != null ? this.padcode : ""));
            sb.append("&usertype=" + this.userType);
            sb.append("&h5sdkversion=" + this.ver);
        }catch (Exception e){

        }

        return sb.toString();
    }


    private static String createTraceId(){
        return "ar"+new Date().getTime();
    }

    private static String getUserId(Context context){
        try {
            String userId = ProferencesUtils.getString(context, SharedKeys.KEY_EVENT_USERID, null);
            if (userId == null || "".equals(userId)){
                int random = (int)(Math.random()*900)+100;
                userId = new Date().getTime() + "" + random;
                ProferencesUtils.setString(context, SharedKeys.KEY_EVENT_USERID, userId);
            }
            return userId;
        }catch (Exception e){
        }
        return "";
    }

    private static Event base = null;

    public static void createBaseEvent(Context context, String corpId){
        if (context!=null){
            base = new Event();
            base.clientId = corpId != null ? corpId : mCorpKey;
            base.userId = getUserId(context);
            base.traceId = createTraceId();
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
}
