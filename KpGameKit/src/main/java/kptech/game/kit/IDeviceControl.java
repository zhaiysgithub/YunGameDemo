package kptech.game.kit;

import android.app.Activity;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import kptech.game.kit.msg.IMsgReceiver;

public interface IDeviceControl {

    /**
     * 启动游戏
     * @param activity
     * @param res
     * @param callback
     */
    void startGame(@NonNull final Activity activity, @IdRes final int res, @NonNull final APICallback<String> callback);

    /**
     * 停止试玩，在退出试玩的时候必须回调，否则无法进行下一次试玩
     */
    void stopGame();

    /**
     * 获取padcode
     * @return
     */
    String getPadcode();

    /**
     * 当前声音是否打开
     * @return
     */
    boolean isSoundEnable();

    /**
     * 画面质量
     * @return
     */
    String getVideoQuality();

    /**
     * 获取视频分辨率
     * @return
     */
    int[] getVideoSize();

    /**
     * 是否已释放
     * @return
     */
    boolean isReleased();

    /**
     * 设置无操作超时
     * @param font 前台超时，单位s
     * @param back 后台超时，单位s
     */
    void setNoOpsTimeout(long font, long back);

    /**
     * 调整试玩的码率
     * @param level 等级，目前支持5档
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_AUTO} 自动
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_HD} 高清
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_ORDINARY} 普通
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_HS} 一般
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_LS} 流畅
     */
    void switchQuality(@APIConstants.VideoQuality String level);

    /**
     * 试玩声音开关
     * @param audioSwitch
     */
    void setAudioSwitch(boolean audioSwitch);

    /**
     * 发送按键事件
     * @param padKey
     */
    void sendPadKey(int padKey);

    /**
     * 发送硬件信息，此接口针对摄像头和麦克风数据
     *
     * @param type 硬件类型 @{@SensorConstants.CloudPhoneSensorId}
     * @param type 数据@{@SensorConstants.CameraVideoType}
     * @return
     */
    void sendSensorInputData(@SensorConstants.CloudPhoneSensorId int sendor,
                                    @SensorConstants.AudioType @SensorConstants.CameraVideoType int type,
                                    byte[] data);

    /**
     * 发送硬件传感器信息，此接口针对陀螺仪、加速器、重力感应等传感器
     * @param sendor 传感器id @{@SensorConstants.CloudPhoneSensorId}
     * @param sensorType @{传感器类型 SensorConstants.SensorType}
     * @param data 传感器数据参数
     */
    void sendSensorInputData(@SensorConstants.CloudPhoneSensorId int sendor,
                                    @SensorConstants.SensorType int sensorType,
                                    float... data);

    /**
     * 注册监听，接收硬件采集信息
     *
     * @param listener
     */
    void registerSensorSamplerListener(SensorSamplerListener listener);

    /**
     * 游戏信息回调
     * @param listener
     */
    void setPlayListener(PlayListener listener);

    /**
     * 消息接收
     * @param receiver
     */
    void setMessageReceiver(IMsgReceiver receiver);

    /**
     * 发送消息
     * @param msg
     */
    void sendMessage(String msg);

    String getDeviceInfo();

    /**
     * 试玩监听
     */
    interface PlayListener {
        /**
         * 网络延时反馈
         * @param ping 网络延时，单位ms
         */
        void onPingUpdate(int ping);

        /**
         * 无操作超时回调
         * @param type 类型。1为后台，2为前台
         * @param timeout 超时时长，单位s
         * @return 返回true表示消耗了事件，sdk不处理超时逻辑；返回false表示未消耗事件，在倒计时结束后，sdk会停止试玩
         */
        boolean onNoOpsTimeout(int type, long timeout);

        void onScreenChange(int value);
    }

    /**
     * 采集硬件信息回掉
     */
    interface SensorSamplerListener {

        /**
         * 采集硬件信息状态发生变化
         *
         * @param sensor 硬件类型  @SensorConstants.CloudPhoneSensorId
         * @param state  硬件状态  @SensorConstants.SensorState
         */
        void onSensorSamper(@SensorConstants.CloudPhoneSensorId int sensor,
                            @SensorConstants.SensorState int state);
    }
}
