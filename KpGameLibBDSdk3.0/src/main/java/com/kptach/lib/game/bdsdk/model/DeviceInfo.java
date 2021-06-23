package com.kptach.lib.game.bdsdk.model;

import org.json.JSONObject;

public class DeviceInfo {
    public static class ResolutionRatio {
        public int width;
        public int height;
        public ResolutionRatio(int w, int h){
            this.width = w;
            this.height = h;
        }

        @Override
        public String toString() {
            return "ResolutionRatio{" +
                    "width=" + width +
                    ", height=" + height +
                    '}';
        }

        public String toJsonString(){
            JSONObject obj = new JSONObject();
            try {
                obj.put("width",width);
                obj.put("height",height);
            }catch (Exception e){}
            return obj.toString();
        }
    }
//    public enum Pass {
//        REDFIRGER,
//        BAIDU,
//    }
//    public Pass pass = Pass.REDFIRGER;

    public String videoQuality;
    public ResolutionRatio resolutionRatio;
    public boolean isAudio = true;

    public int gop;
    public int bitrate;
    public int maxFPS;
    public int minFPS;
    public int perUpFPS;
    public int perDownFPS;
    public int encodeType;

    public String padCode;
    public String deviceParams;
    public int apiLevel = 2;
    public int useSSL = 0;

    public static DeviceInfo getInstance(String str,String deviceId){
        if (str == null){
            return null;
        }
        try {

            JSONObject obj = new JSONObject(str);
            DeviceInfo deviceInfo = new DeviceInfo();

            try {
                if (obj.has("extInfo")) {
                    JSONObject extObj = obj.getJSONObject("extInfo");
                    deviceInfo.apiLevel = extObj.has("apiLevel") ? extObj.optInt("apiLevel") : 2;
                    deviceInfo.useSSL = extObj.has("useSSL") ? extObj.optInt("useSSL") : 0;
                }
//                deviceInfo.padCode= obj.optString("padCode");
                deviceInfo.padCode= deviceId;
            } catch (Exception e) {
                e.printStackTrace();
            }

            deviceInfo.deviceParams = str;

            return deviceInfo;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public enum VideoQuality {
        GRADE_LEVEL_HD,
        GRADE_LEVEL_ORDINARY,
        GRADE_LEVEL_HS,
        GRADE_LEVEL_LS,
        GRADE_LEVEL_AUTO
    }

    /* compiled from: PlaySDKManager */
    public enum ResolutionLevel {
        LEVEL_DEFAULT,
        LEVEL_720_1280,
        LEVEL_480_856,
        LEVEL_368_652,
        LEVEL_288_512
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "videoQuality='" + videoQuality + '\'' +
                ", resolutionRatio=" + resolutionRatio +
                ", isAudio=" + isAudio +
                ", gop=" + gop +
                ", bitrate=" + bitrate +
                ", maxFPS=" + maxFPS +
                ", minFPS=" + minFPS +
                ", perUpFPS=" + perUpFPS +
                ", perDownFPS=" + perDownFPS +
                ", encodeType=" + encodeType +
                ", padCode='" + padCode + '\'' +
                ", deviceParams='" + deviceParams + '\'' +
                ", apiLevel=" + apiLevel +
                ", useSSL=" + useSSL +
                '}';
    }

    public String toJsonString(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("videoQuality",videoQuality);
            obj.put("resolutionRatio",resolutionRatio.toJsonString());
            obj.put("isAudio",isAudio);
            obj.put("gop",gop);
            obj.put("bitrate",bitrate);
            obj.put("maxFPS",maxFPS);
            obj.put("minFPS",minFPS);
            obj.put("perUpFPS",perUpFPS);
            obj.put("perDownFPS",perDownFPS);
            obj.put("encodeType",encodeType);
            obj.put("padCode",encodeType);
            obj.put("apiLevel",apiLevel);
            obj.put("useSSL",useSSL);
        }catch (Exception e){
        }
        return obj.toString();
    }
}
