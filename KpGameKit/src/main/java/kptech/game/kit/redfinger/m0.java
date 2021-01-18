package kptech.game.kit.redfinger;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/* compiled from: DeviceInfo */
public class m0 implements Serializable {
    private int id;
    private int status;
    private String deviceId;
    private String deviceToken;
//    private int e;
    private int usedTime;
    private int totalTime;
    private int gop;
    private int bitRate;
    private int maxDescentFrame;
    private int maxFrameRate;
    private int minDescentFrame;
    private int minFrameRate;
    private boolean sound;

    private CompressionType compressionType;
    private PicQuality picQuality;
    private Resolution resolution;
    private QueueRankInfoInner queueInfo;


    /* compiled from: PlaySDKManager */
    public enum CompressionType {
        DEFAULT,
        X264,
        VPU
    }

    /* compiled from: PlaySDKManager */
    public enum Resolution {
        LEVEL_DEFAULT,
        LEVEL_720_1280,
        LEVEL_480_856,
        LEVEL_368_652,
        LEVEL_288_512
    }

    /* compiled from: PlaySDKManager */
    public enum PicQuality {
        VIDEO_LEVEL_AUTO,
        VIDEO_LEVEL_HD,
        VIDEO_LEVEL_STANDARD,
        VIDEO_LEVEL_FLUENCY,
    }

    public static final int VIDEO_LEVEL_AUTO = 0;
    public static final int VIDEO_LEVEL_HD = 1;
    public static final int VIDEO_LEVEL_STANDARD = 2;
    public static final int VIDEO_LEVEL_FLUENCY = 3;

    /* compiled from: DeviceInfo */
    public enum a {
        PHONE(1),
        GAME(0);

        int a;

        private a(int i) {
            this.a = i;
        }

        public int a() {
            return this.a;
        }
    }

    public void setBitRate(int i2) {
        this.bitRate = i2;
    }

    public void setDeviceId(String str) {
        this.deviceId = str;
    }

    public void setDeviceToken(String str) {
        this.deviceToken = str;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public int getGop() {
        return this.gop;
    }

    public int getMaxFrameRate() {
        return this.maxFrameRate;
    }

    public void setMinFrameRate(int i2) {
        this.minFrameRate = i2;
    }

    public QueueRankInfoInner setPicQuality() {
        return this.queueInfo;
    }

    public Resolution setResolution() {
        return this.resolution;
    }

    public boolean isSound() {
        return this.sound;
    }

    public int setStatus() {
        return this.status;
    }

    public String setTotalTime() {
        return this.deviceToken;
    }

//    public void m(int i2) {
//        this.CompressionType = i2;
//    }

    public void setUsedTime(int i2) {
        this.usedTime = i2;
    }

    public void setQueueInfo(QueueRankInfoInner q0Var) {
        this.queueInfo = q0Var;
    }

    public int setCompressionType() {
        return this.bitRate;
    }

    public void setGop(int i2) {
        this.gop = i2;
    }

    public void setMaxDescentFrame(int i2) {
        this.maxDescentFrame = i2;
    }

    public void setMaxFrameRate(int i2) {
        this.maxFrameRate = i2;
    }

    public void setMinDescentFrame(int i2) {
        this.minDescentFrame = i2;
    }

    public PicQuality getPicQuality() {
        return this.picQuality;
    }

    public void setPicQuality(int picQuality) {
        switch (picQuality) {
            case 0:
                this.picQuality = PicQuality.VIDEO_LEVEL_AUTO;
                return;
            case 1:
                this.picQuality = PicQuality.VIDEO_LEVEL_HD;
                return;
            case 2:
                this.picQuality = PicQuality.VIDEO_LEVEL_STANDARD;
                return;
            case 3:
                this.picQuality = PicQuality.VIDEO_LEVEL_FLUENCY;
                return;
//            case 4:
//                this.picQuality = PicQuality.e;
//                return;
            default:
                this.picQuality = PicQuality.VIDEO_LEVEL_AUTO;
                return;
        }
    }

    public void setResolution(int resolution) {
        switch (resolution) {
            case 0:
                this.resolution = Resolution.LEVEL_288_512;
                return;
            case 1:
                this.resolution = Resolution.LEVEL_368_652;
                return;
            case 2:
                this.resolution = Resolution.LEVEL_480_856;
                return;
            case 3:
                this.resolution = Resolution.LEVEL_720_1280;
                return;
            default:
                this.resolution = Resolution.LEVEL_DEFAULT;
                return;
        }
    }

    public void setSound(int sound) {
        if (sound == 0) {
            this.sound = false;
        } else if (sound == 1) {
            this.sound = true;
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public void setCompressionType(int i2) {
        if (i2 == 0) {
            this.compressionType = CompressionType.X264;
        } else if (i2 == 1) {
            this.compressionType = CompressionType.VPU;
        } else {
            this.compressionType = CompressionType.DEFAULT;
        }
    }

    public CompressionType getCompressionType() {
        return this.compressionType;
    }

    public String a() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("deviceId", this.deviceId);
            jSONObject.put("status", this.status);
            jSONObject.put("usedTime", this.usedTime);
            jSONObject.put("totalTime", this.totalTime);
            jSONObject.put("deviceToken", this.deviceToken);
            jSONObject.put("gop", this.gop);
            jSONObject.put("bitRate", this.bitRate);
            if (this.compressionType != null) {
                jSONObject.put("compressionType", this.compressionType.name());
            }
            jSONObject.put("maxDescentFrame", this.maxDescentFrame);
            jSONObject.put("minDescentFrame", this.minDescentFrame);
            jSONObject.put("maxFrameRate", this.maxFrameRate);
            jSONObject.put("minFrameRate", this.minFrameRate);
            if (this.picQuality != null) {
                jSONObject.put("picQuality", this.picQuality.name());
            }
            if (this.resolution != null) {
                jSONObject.put("resolution", this.resolution.name());
            }
            jSONObject.put("sound", Boolean.valueOf(this.sound));
            if (this.queueInfo != null) {
                jSONObject.put("playQueueCount", this.queueInfo.playQueueCount);
                jSONObject.put("queueRanking", this.queueInfo.queueRanking);
                jSONObject.put("supportPlayQueue", this.queueInfo.supportPlayQueue);
                jSONObject.put("queueWaitTime", this.queueInfo.queueWaitTime);
            }
        } catch (JSONException e2) {
        }
        return jSONObject.toString();
    }

    public void setJson(String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            this.deviceId = jSONObject.optString("deviceId");
            this.status = jSONObject.optInt("status");
            this.usedTime = jSONObject.optInt("usedTime");
            this.totalTime = jSONObject.optInt("totalTime");
            this.deviceToken = jSONObject.optString("deviceToken");
            this.gop = jSONObject.optInt("gop");
            this.bitRate = jSONObject.optInt("bitRate");
            String compressionType = jSONObject.optString("compressionType");
            if (!TextUtils.isEmpty(compressionType)) {
                this.compressionType = (CompressionType) Enum.valueOf(CompressionType.class, compressionType);
            }
            this.maxDescentFrame = jSONObject.optInt("maxDescentFrame");
            this.minDescentFrame = jSONObject.getInt("minDescentFrame");
            this.maxFrameRate = jSONObject.optInt("maxFrameRate");
            this.minFrameRate = jSONObject.optInt("minFrameRate");
            String picQuality = jSONObject.optString("picQuality");
            if (!TextUtils.isEmpty(picQuality)) {
                this.picQuality = (PicQuality) Enum.valueOf(PicQuality.class, picQuality);
            }
            String resolution = jSONObject.optString("resolution");
            if (!TextUtils.isEmpty(compressionType)) {
                this.resolution = (Resolution) Enum.valueOf(Resolution.class, resolution);
            }
            this.sound = jSONObject.optBoolean("sound");
            QueueRankInfoInner queueInfo = new QueueRankInfoInner();
            this.queueInfo = queueInfo;
            queueInfo.playQueueCount = jSONObject.optInt("playQueueCount");
            this.queueInfo.queueRanking = jSONObject.optInt("queueRanking");
            this.queueInfo.queueWaitTime = jSONObject.optInt("queueWaitTime");
            this.queueInfo.supportPlayQueue = jSONObject.optBoolean("supportPlayQueue");
        } catch (JSONException e2) {
        }
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer("DeviceInfo{");
        stringBuffer.append("id=").append(this.id);
        stringBuffer.append(", status=").append(this.status);
        stringBuffer.append(", deviceId='").append(this.deviceId).append('\'');
        stringBuffer.append(", deviceToken='").append(this.deviceToken).append('\'');
//        stringBuffer.append(", type=").append(this.CompressionType);
        stringBuffer.append(", usedTime=").append(this.usedTime);
        stringBuffer.append(", totalTime=").append(this.totalTime);
        stringBuffer.append(", gop=").append(this.gop);
        stringBuffer.append(", bitRate=").append(this.bitRate);
        stringBuffer.append(", compressionType=").append(this.compressionType);
        stringBuffer.append(", maxDescentFrame=").append(this.maxDescentFrame);
        stringBuffer.append(", maxFrameRate=").append(this.maxFrameRate);
        stringBuffer.append(", minDescentFrame=").append(this.minDescentFrame);
        stringBuffer.append(", minFrameRate=").append(this.minFrameRate);
        stringBuffer.append(", picQuality=").append(this.picQuality);
        stringBuffer.append(", resolution=").append(this.resolution);
        stringBuffer.append(", sound=").append(this.sound);
        stringBuffer.append(", queueInfo=").append(this.queueInfo);
        stringBuffer.append('}');
        return stringBuffer.toString();
    }

    public String toRedfingerParams(){
        try {
            JSONObject params = new JSONObject();
            params.put("padCode", this.deviceId);
            params.put("resolutionRatio", getResolutionRatio(this.resolution));
            params.put("resultCode", this.status);
            params.put("GOP", this.gop);
            params.put("bitrate", this.bitRate);
            params.put("maxFPS", this.maxFrameRate);
            params.put("minFPS", this.minFrameRate);
            params.put("perUpFPS", this.maxDescentFrame);
            params.put("perDownFPS", this.minDescentFrame);
            params.put("encodeType", this.compressionType.ordinal());
            params.put("gameTrialTime", (this.totalTime - this.usedTime) + "");
            params.put("isAudio", this.sound ? 1 : 0);
            params.put("gameVideoQuality", this.picQuality.ordinal());
            params.put("gameDownloadUrl", "");

            JSONObject resultInfoObj = new JSONObject(this.deviceToken);
            params.put("resultInfo", resultInfoObj);

            return params.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private String getResolutionRatio(Resolution resolution){
        if (resolution == Resolution.LEVEL_288_512) {
            return "288 X 512";
        } else if (resolution == Resolution.LEVEL_368_652) {
            return "368 X 652";
        } else if (resolution == Resolution.LEVEL_480_856) {
            return "480 X 856";
        } else if (resolution == Resolution.LEVEL_720_1280) {
            return "720 X 1280";
        }
        return "default";
    }
}
