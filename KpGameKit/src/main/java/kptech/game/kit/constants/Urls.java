package kptech.game.kit.constants;

import kptech.game.kit.BuildConfig;

public class Urls {

    public static final String URL = BuildConfig.DEBUG ? "https://test-operation.kuaipantech.com" : "https://wxapp.kuaipantech.com";

    public static final String GET_AD_CONFIG = URL + "/kp/api/cp/resource/channel/adData";
    public static final String GET_CONFIG = URL + "/kp/api/cp/resource/channel/data";
    public static final String GET_GAME_LIST = URL + "/kp/api/cp/resource/games";
    public static final String GET_GAME_INFO = URL + "/kp/api/cp/resource/game/detail";
    public static final String GET_GAME_EXIT_LIST = URL + "/kp/api/cp/resource/game/detention";

    public static final String EVENT_RUL = "https://interface.open.kuaipantech.com";

    public static final String HTTP_URL = "https://wxapp.kuaipantech.com/h5demo/Toc/kpuser.php";
    public static final String LOGIN_ARGMENT_URL = "https://rs.cdn.kuaipantech.com/kuaipandoc.php";
    public static final String DEFAULT_REFERER = "https://wxapp.kuaipantech.com";
    public static final String PAY_URL = "https://wxapp.kuaipantech.com/h5demo/Toc/androidpay/androidpay.php";


}
