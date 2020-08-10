package kptech.game.kit;

import android.app.Activity;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import java.util.Map;

import kptech.cloud.kit.msg.Messager;
import kptech.game.kit.msg.MsgManager;
import kptech.game.kit.view.AdRemindDialog;

public class DeviceControl {
    private static final String TAG = "GameControl";

    private com.yd.yunapp.gameboxlib.DeviceControl mDeviceControl;
    private GameInfo mGameInfo;

    protected DeviceControl(com.yd.yunapp.gameboxlib.DeviceControl control){
        this(control,null);
    }

    protected DeviceControl(com.yd.yunapp.gameboxlib.DeviceControl control, GameInfo game){
        this.mDeviceControl = control;
        this.mGameInfo = game;
    }

    /**
     * 启动游戏
     * @param activity
     * @param res
     * @param callback
     */
    public void startGame(@NonNull final Activity activity, @IdRes final int res, @NonNull final APICallback<String> callback){
        //请求网络获取广告显示

        //显示提醒弹窗
        new AdRemindDialog(activity)
                .setAdCode("ZM_SDKAD_1_00066")
                .setCallback(new AdRemindDialog.IRemindDialogCallback() {
                    @Override
                    public void onSubmit() {
                        //启动游戏
                        execStartGame(activity, res, callback);
                    }

                    @Override
                    public void onCancel() {
                        //关闭
                        if (callback!=null){
                            callback.onAPICallback("game cancel", APIConstants.ERROR_GAME_CANCEL);
                        }
                    }
                })
                .show();
    }

    private void execStartGame(@NonNull Activity activity, @IdRes int res, @NonNull final APICallback<String> callback){
        //连接设备
        MsgManager.start(activity, mDeviceControl.getDeviceToken());

        mDeviceControl.startGame(activity, res, new com.yd.yunapp.gameboxlib.APICallback<String>() {
            @Override
            public void onAPICallback(String s, int i) {
                if (callback!=null){
                    callback.onAPICallback(s,i);
                }
            }
        });
    }

    /**
     * 停止试玩，在退出试玩的时候必须回调，否则无法进行下一次试玩
     */
    public void stopGame(){
        try {
            MsgManager.stop();
        }catch (Exception e){
            e.printStackTrace();
        }

        mDeviceControl.stopGame();
    }

    /**
     * 设置无操作超时
     * @param font 前台超时，单位s
     * @param back 后台超时，单位s
     */
    public void setNoOpsTimeout(long font, long back) {
        mDeviceControl.setNoOpsTimeout(font, back);
    }

    /**
     * 调整试玩的码率
     * @param leve 等级，目前支持5档
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_AUTO} 自动
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_HD} 高清
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_ORDINARY} 普通
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_HS} 一般
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_LS} 流畅
     */
    public void switchQuality(@APIConstants.VideoQuality String leve){
        mDeviceControl.switchQuality(leve);
    }

    /**
     * 试玩声音开关
     * @param audioSwitch
     */
    public void setAudioSwitch(boolean audioSwitch){
        mDeviceControl.setAudioSwitch(audioSwitch);
    }

    public boolean isReleased(){
        return mDeviceControl.isReleased();
    }

    /**
     * 注册排队监听回调
     * @param callback 回调
     */
    public void registerQueueCallback(final APICallback<QueueRankInfo> callback){
        mDeviceControl.registerQueueCallback(new com.yd.yunapp.gameboxlib.APICallback<com.yd.yunapp.gameboxlib.QueueRankInfo>() {
            @Override
            public void onAPICallback(com.yd.yunapp.gameboxlib.QueueRankInfo queueRankInfo, int i) {
                if (callback!=null){
                    callback.onAPICallback(new QueueRankInfo(queueRankInfo), i);
                }
            }
        });
    }

    /**
     * 设置试玩监听
     * @param listener  PlayListener 回调
     */
    public void setPlayListener(final PlayListener listener){
        mDeviceControl.setPlayListener(new com.yd.yunapp.gameboxlib.DeviceControl.PlayListener(){
            @Override
            public void onPingUpdate(int ping) {
                if (listener!=null){
                    listener.onPingUpdate(ping);
                }
            }

            @Override
            public boolean onNoOpsTimeout(int type, long timeout) {
                if (listener!=null){
                    return listener.onNoOpsTimeout(type,timeout);
                }
                return false;
            }

            @Override
            public void onScreenChange(int i) {
                if (listener!=null){
                    listener.onScreenChange(i);
                }
            }
        });
    }

    /**
     * 设置试玩倒计时监听
     * @param threshold 阈值，到达开始倒计时回调，如果小于等于0，则从设置监听起开始回调
     * @param listener 监听listener
     */
    public void setTimeDownListener(int threshold, final TimeDownListener listener){

        mDeviceControl.setTimeCountDownListener(threshold, new com.yd.yunapp.gameboxlib.DeviceControl.TimeCountDownListener() {
            @Override
            public void countDown(int remainingTime) {
                if (listener != null) {
                    listener.countDown(remainingTime);
                }
            }
        });
    }

    /**
     * 注册监听，接收云手机App返回的消息
     *
     * @param listener
     */
    public void registerDataTransferListener(final DataTransferListener listener){
        mDeviceControl.registerDataTransferListener(new com.yd.yunapp.gameboxlib.DeviceControl.DataTransferListener(){

            @Override
            public void onReceiveData(Map<String, String> map) {
                if (listener != null) {
                    listener.onReceiveData(map);
                }
            }

            @Override
            public void onSendFailed(int i) {
                if (listener != null) {
                    listener.onSendFailed(i);
                }
            }
        });
    }

    /**
     * 注册监听，接收硬件采集信息
     *
     * @param listener
     */
    public void registerSensorSamplerListener(final SensorSamplerListener listener){
        mDeviceControl.registerSensorSamplerListener(new com.yd.yunapp.gameboxlib.DeviceControl.SensorSamplerListener() {
            @Override
            public void onSensorSamper(int sensor, int state) {
                if (listener != null) {
                    listener.onSensorSamper(sensor, state);
                }
            }
        });
    }

    /**
     * 发送硬件信息，此接口针对摄像头和麦克风数据
     *
     * @param type 硬件类型 @{@SensorConstants.CloudPhoneSensorId}
     * @param type 数据@{@SensorConstants.CameraVideoType}
     * @return
     */
    public void sendSensorInputData(@SensorConstants.CloudPhoneSensorId int sendor,
                                             @SensorConstants.AudioType @SensorConstants.CameraVideoType int type,
                                             byte[] data){
        mDeviceControl.sendSensorInputData(sendor, type, data);
    }

    /**
     * 发送硬件传感器信息，此接口针对陀螺仪、加速器、重力感应等传感器
     * @param sendor 传感器id @{@SensorConstants.CloudPhoneSensorId}
     * @param sensorType @{传感器类型 SensorConstants.SensorType}
     * @param data 传感器数据参数
     */
    public void sendSensorInputData(@SensorConstants.CloudPhoneSensorId int sendor,
                                             @SensorConstants.SensorType int sensorType,
                                             float... data){
        mDeviceControl.sendSensorInputData(sendor,sensorType,data);
    }


    /**
     * 设置视频流显示模式，默认自动切换，需要在startGame之前调用
     *
     * @param orientation ActivityInfo#SCREEN_ORIENTATION_PORTRAIT，ActivityInfo#SCREEN_ORIENTATION_LANDSCAPE
     */
    public void setVideoOrientation(int orientation){
        mDeviceControl.setVideoOrientation(orientation);
    }


    /**
     * 试玩监听
     */
    public interface PlayListener {
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
     * 试玩结束倒计时监听
     */
    public interface TimeDownListener {
        /**
         *  倒计时触发
         * @param remainingTime remainingTime 剩余时间，单位s秒
         */
        void countDown(int remainingTime);
    }


    /**
     * 云手机消息接收监听
     */
    public interface DataTransferListener {

        /**
         * 接收到云手机消息回掉
         *
         * @param data 消息未自定义格式
         */
        void onReceiveData(Map<String, String> data);

        /**
         * 消息发送失败
         *
         * @param code
         */
        void onSendFailed(int code);
    }

    /**
     * 采集硬件信息回掉
     */
    public interface SensorSamplerListener {

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
