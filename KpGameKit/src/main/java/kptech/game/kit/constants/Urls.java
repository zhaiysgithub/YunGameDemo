package kptech.game.kit.constants;

import kptech.game.kit.BuildConfig;

public class Urls {
    private static final String RELEASE_URL = "https://wxapp.kuaipantech.com";
    private static final String DEBUG_URL = "https://test-operation.kuaipantech.com";

    public static final String URL = DEBUG_URL;//BuildConfig.DEBUG ? DEBUG_URL : RELEASE_URL;

    public static final String GET_CONFIG = URL + "/kp/api/cp/resource/channel/data";
    public static final String GET_GAME_LIST = URL + "/kp/api/cp/resource/games";
    public static final String GET_GAME_INFO = URL + "/kp/api/cp/resource/game/detail";
}
