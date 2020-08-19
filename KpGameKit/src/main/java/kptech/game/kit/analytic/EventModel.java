package kptech.game.kit.analytic;

public class EventModel {
    /**
     * 固定前缀，其他分段可作为行为标识，全部大写
     */
    String event;
    /**
     * 随机数：h51597817231222，一次的用户行为
     */
    String traceId;
    /**
     * corpKey
     */
    String clientId;
    /**
     *
     */
    String userType = "androidKpUser";
    /**
     * 用户唯一id, 当前设备生成，缓存到本地
     */
    String userId;
    /**
     * 云设备padcode
     */
    String padcode;
    /**
     * 游戏包名
     */
    String gamePkg;
    /**
     *
     */
    String actionResult = "SUCC";
    /**
     * 错误消息
     */
    String errMsg;
    /**
     * 调试模式，1：调试模式，数据写入测试表
     */
    int debug;
    /**
     * 扩展
     */
    String ext;

}
