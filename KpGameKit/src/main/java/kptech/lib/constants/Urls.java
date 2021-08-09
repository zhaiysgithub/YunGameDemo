package kptech.lib.constants;

import kptech.game.kit.env.Env;

public class Urls {



//    public static final String BaseUrl = BuildConfig.DEBUG ? "https://test-operation.kuaipantech.com" : "https://wxapp.kuaipantech.com";

    public static final String GET_AD_CONFIG = "/kp/api/cp/resource/channel/adData";
    public static final String GET_CONFIG = "/kp/api/cp/resource/channel/data";
    public static final String GET_GAME_LIST = "/kp/api/cp/resource/games";
    public static final String GET_GAME_INFO = "/kp/api/cp/resource/game/detail";
    public static final String GET_GAME_EXIT_LIST = "/kp/api/cp/resource/game/detention";
    public static final String GET_DEVICE_CONNECT = "/kp/api/paas/connect/And";
    public static final String URL_RECORD_SCREEN = "/kp/api/wss/type/screen";

    public static final String HTTP_URL = "https://wxapp.kuaipantech.com/h5demo/Toc/kpuser.php";
    public static final String HTTP_URL_CLIENTUSER = "https://wxapp.kuaipantech.com/h5demo/Toc/clientuser.php";
    public static final String LOGIN_ARGMENT_URL = "https://rs.cdn.kuaipantech.com/kuaipandoc.php";
    public static final String DEFAULT_REFERER = "https://wxapp.kuaipantech.com";
    public static final String PAY_URL = "https://wxapp.kuaipantech.com/h5demo/Toc/androidpay/androidpay.php";
    public static final String NOTICE_URL = "http://kpsdkapi.kuaipantech.com/KpWebSDKApi/KpCloudCtrlsys/index.php";

    //打点接口
    public static final String EVENT_RUL = "https://interface.open.kuaipantech.com";
    public static String URL_ACTION = Urls.EVENT_RUL + "/useraction.php";
    public static String URL_TIME = Urls.EVENT_RUL + "/useraction_playtimes.php";
    public static String URL_TM_ACTION = Urls.EVENT_RUL + "/useraction_special.php";

    //检测 guid,token有效性
    public static final String HTTP_PLAT_KPUSER = "http://wxapp.kuaipantech.com/h5demo/Toc/kpuser.php";

    public static String getEnvUrl(String url){
        String BaseUrl = Env.isTestEnv() ? "http://test-operation.kuaipantech.com" : "https://wxapp.kuaipantech.com";
        return BaseUrl + url;
    }

}
