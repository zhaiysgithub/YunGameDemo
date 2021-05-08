package kptech.game.kit.callback;

/**
 * 用户认证回调接口
 */
public interface UserCertificationCallback {

    /**
     * 用户认证成功
     */
    void onCerSuccess();

    /**
     * 用户认证失败
     * @param errorStr 失败原因
     */
    void onCerError(String errorStr);
}
