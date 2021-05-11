package kptech.game.kit.callback;

/**
 * 用户认证回调接口
 */
public interface OnAuthCallback {

    /**
     * 用户认证成功
     * @param gid 用户id
     */
    void onCerSuccess(String gid);

    /**
     * 用户认证失败
     * @param code 错误码
     * @param errorStr 失败原因
     */
    void onCerError(int code ,String errorStr);
}
