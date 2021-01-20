package kptach.game.kit.lib.baidu;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.yd.yunapp.gameboxlib.APIConstants;
import com.yd.yunapp.gameboxlib.GamePadKey;

import org.json.JSONObject;

import kptach.game.kit.inter.game.GameInfo;
import kptach.game.kit.inter.game.IDeviceControl;
import kptach.game.kit.inter.game.IGameCallback;
import kptach.game.kit.lib.baidu.utils.Logger;

public class BdDeviceControl implements IDeviceControl {
    private static final String TAG = BdDeviceControl.class.getSimpleName();

    private com.yd.yunapp.gameboxlib.DeviceControl mDeviceControl;

    private JSONObject mDeviceToken;
    private String mPadcode = "";
    private boolean mIsSoundEnable = true;
    private String mPicQuality = "";

    private Handler mGameHandler;
    private Activity mActivity;
    private int mGameRes;
//    private APICallback<String> mGameStartCallback;

    protected BdDeviceControl(com.yd.yunapp.gameboxlib.DeviceControl control, GameInfo game){
        this.mDeviceControl = control;
//        this.mGameInfo = game;
        //解析deviceToken
        parseDeviceToken();
    }


    private void parseDeviceToken(){
        try {
            String deviceStr = mDeviceControl.getDeviceToken();
            JSONObject deviceTokenObj = new JSONObject(deviceStr);
            if (deviceTokenObj!=null && deviceTokenObj.has("token")){
                String tokenStr = deviceTokenObj.getString("token");
                JSONObject tokenObject = new JSONObject(tokenStr);

                mDeviceToken = tokenObject;

                //解析padcode
                if (mDeviceToken != null && mDeviceToken.has("deviceId")) {
                    String str = mDeviceToken.getString("deviceId");
                    mPadcode = str;
                }

                //解析当前声音开关
                if (mDeviceToken != null && mDeviceToken.has("sound")) {
                    String str = mDeviceToken.getString("sound");
                    mIsSoundEnable = "true".equals(str);
                }

                //解析当前画面质量
                if (mDeviceToken != null && mDeviceToken.has("picQuality")) {
                    String str = mDeviceToken.getString("picQuality");
                    mPicQuality = str;
                }
            }

        }catch (Exception e){
            Logger.error(TAG, "parseDeviceToken, error:"+e.getMessage());
        }
    }

    @Override
    public void startGame(Activity activity, int res, IGameCallback<String> callback) {
        mDeviceControl.startGame(activity, res, new com.yd.yunapp.gameboxlib.APICallback<String>(){
            @Override
            public void onAPICallback(String s, int i) {

            }
        });
    }

    @Override
    public void stopGame() {
        mDeviceControl.stopGame();
    }

    @Override
    public String getPadcode() {
        return mPadcode;
    }

    @Override
    public boolean isSoundEnable() {
        return mIsSoundEnable;
    }

    @Override
    public String getVideoQuality() {
        return mPicQuality;
    }

    @Override
    public int[] getVideoSize() {
        try {
            if (mDeviceToken!=null && mDeviceToken.has("resolution")){
                String str = mDeviceToken.getString("resolution");
                String[] arr = str.split("_");
                if (arr.length == 3){
                    int w = Integer.parseInt(arr[1]);
                    int h = Integer.parseInt(arr[2]);
                    return new int[]{w, h};
                }
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }

        return new int[]{720, 1280};
    }

    @Override
    public boolean isReleased() {
        return mDeviceControl.isReleased();
    }

    @Override
    public void setNoOpsTimeout(long font, long back) {
        mDeviceControl.setNoOpsTimeout(font, back);
    }

    @Override
    public void switchQuality(String level) {
        mPicQuality = level;
        mDeviceControl.switchQuality(level);
    }

    @Override
    public void setAudioSwitch(boolean audioSwitch) {
        mIsSoundEnable = audioSwitch;
        mDeviceControl.setAudioSwitch(audioSwitch);
    }

    @Override
    public void sendPadKey(int key) {
        try {
            if (mDeviceControl != null){
//                if (key == APIConstants.PAD_KEY_BACK){
//                    mDeviceControl.setGamePadKey(GamePadKey.GAMEPAD_BACK);
//                }else if (key == APIConstants.PAD_KEY_HOME){
//                    mDeviceControl.setGamePadKey(GamePadKey.GAMEPAD_HOME);
//                }
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
    }

    @Override
    public void sendSensorInputData(int sendor, int type, byte[] data) {
        mDeviceControl.sendSensorInputData(sendor, type, data);
    }

    @Override
    public void sendSensorInputData(int sendor, int sensorType, float... data) {
        mDeviceControl.sendSensorInputData(sendor,sensorType,data);
    }

    @Override
    public void registerSensorSamplerListener(SensorSamplerListener listener) {

    }

    @Override
    public void setPlayListener(PlayListener listener) {

    }

}
