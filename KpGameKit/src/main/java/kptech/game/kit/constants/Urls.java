package kptech.game.kit.constants;

import kptech.game.kit.BuildConfig;

public class Urls {

    public static final String URL = !BuildConfig.DEBUG ? "https://test-operation.kuaipantech.com" : "https://wxapp.kuaipantech.com";

    public static final String GET_CONFIG = URL + "/kp/api/cp/resource/channel/data";
    public static final String GET_GAME_LIST = URL + "/kp/api/cp/resource/games";
    public static final String GET_GAME_INFO = URL + "/kp/api/cp/resource/game/detail";


}
