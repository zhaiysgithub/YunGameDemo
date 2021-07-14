package kptech.game.kit;

import android.app.Activity;

import com.kptach.lib.inter.game.APIConstants;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import kptech.game.kit.msg.IMsgReceiver;

public interface IDeviceControl {

    /**
     * 游戏状态，游戏启动成功、数据加载、预加载
     * @param listener
     */
    void registerPlayStateListener(PlayStateListener listener);

    /**
     * 游戏相关数据事件，延时、超时、帧率、码率
     * @param listener
     */
    void registerPlayDataListener (PlayDataListener listener);

    /**
     * 屏幕事件, 屏幕方向、分辨率
     * @param listener
     */
    void registerPlayScreenListener(PlayScreenListener listener);

    /**
     * 消息接收回调
     * @param listener
     */
    void registerCloudMessageListener(CloudMessageListener listener);

    /**
     * 传感器数据，接收硬件采集信息
     *
     * @param listener
     */
    void registerSensorSamplerListener(SensorSamplerListener listener);


    /**
     * 启动游戏
     * @param activity
     * @param res
     * @param callback
     */
    void startGame(@NonNull final Activity activity, @IdRes final int res, @NonNull final APICallback<String> callback);
    /**
     * 启动游戏
     * @param activity
     * @param container
     */
    void startGame(Activity activity, int container);
    /**
     * 停止试玩，在退出试玩的时候必须回调，否则无法进行下一次试玩
     */
    void stopGame();

    /**
     * 当前声音是否打开
     * @return
     */
    boolean isSoundEnable();

    /**
     * 试玩声音开关
     * @param audioSwitch
     */
    void setAudioSwitch(boolean audioSwitch);

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
     * 调整试玩的码率
     * @param level 等级，目前支持5档
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_AUTO} 自动
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_HD} 高清
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_ORDINARY} 普通
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_LS} 流畅
     */
    void switchQuality(@APIConstants.VideoQuality String level);

    /**
     * 获取padcode
     * @return
     */
    String getPadcode();

    /**
     * 获取当前设备信息
     * @return
     */
    String getDeviceInfo();

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
     * 发送按键事件
     * @param padKey
     */
    void sendPadKey(int padKey);

    /**
     * 发送消息
     * @param event
     * @param data
     */
    void sendCloudMessage(String event, String data);

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
     * 云消监听器
     */
    interface CloudMessageListener {
        void onMessage(String event, String data);
    }

    /**
     * 云游戏状态监听器
     */
    interface PlayStateListener {
        void onNotify(int code, String msg);
    }

    /**
     * 游戏信息回调
     * @param listener
     */
    void setPlayListener(PlayListener listener);

    /**
     * 发送消息
     * @param msg
     */
    void sendMessage(String msg);

    /**
     * 消息接收
     * @param receiver
     */
    void setMessageReceiver(IMsgReceiver receiver);

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
     * 设置图像显示模式
     * @param isFill 是否是全屏
     */
    void setVideoDisplayMode(boolean isFill);
    /**
     * 云游戏数据监听器
     */
    interface PlayDataListener {
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

        /**
         * 流信息
         * @param fps
         * @param stream
         */
        void onSteamInfo(int fps, long bitrate);
    }

    /**
     * 屏幕变化监听器
     */
    interface PlayScreenListener {
        /**
         * 分辨率
         * @param width
         * @param height
         */
        void onVideoSizeChange(int width, int height);

        /**
         * 屏幕方向
         * @param orientation
         */
        void onOrientationChange (int orientation);

    }

    /**
     * 采集硬件信息回掉
     */
    interface SensorSamplerListener {

        /**
         * 采集硬件信息状态发生变化
         * @param sensor 硬件类型  @SensorConstants.CloudPhoneSensorId
         * @param state  硬件状态  @SensorConstants.SensorState
         */
        void onSensorSamper(@SensorConstants.CloudPhoneSensorId int sensor,
                            @SensorConstants.SensorState int state);
    }
}
