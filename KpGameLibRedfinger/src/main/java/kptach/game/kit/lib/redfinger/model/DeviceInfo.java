package kptach.game.kit.lib.redfinger.model;

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

    public String padCode;
    public String videoQuality;
    public ResolutionRatio resolutionRatio;
    public boolean isAudio;

    public int gop;
    public int bitrate;
    public int maxFPS;
    public int minFPS;
    public int perUpFPS;
    public int perDownFPS;
    public int encodeType;

    public String deviceParams;
    public int apiLevel = 2;
    public int useSSL = 0;

    public static DeviceInfo getInstance(String str){
        if (str == null){
            return null;
        }
        try {
            JSONObject obj = new JSONObject(str);

            DeviceInfo deviceInfo = new DeviceInfo();

            try {
                if (obj.has("extInfo")){
                    JSONObject extObj = obj.getJSONObject("extInfo");
                    deviceInfo.apiLevel = extObj.has("apiLevel") ? extObj.optInt("apiLevel") : 2;
                    deviceInfo.useSSL = extObj.has("useSSL") ? extObj.optInt("useSSL") : 0;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            JSONObject devObj = obj.getJSONObject("resultInfo");
            deviceInfo.deviceParams = devObj.toString();

            deviceInfo.padCode = devObj.optString("padCode");

            try {
                String resolutionRatio = devObj.optString("resolutionRatio");
                if (resolutionRatio!=null){
                    String[] arr = resolutionRatio.split(" X ");
                    if (arr.length == 2){
                        int w = Integer.parseInt(arr[0]);
                        int h = Integer.parseInt(arr[1]);
                        deviceInfo.resolutionRatio = new ResolutionRatio(w, h);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            deviceInfo.gop = devObj.optInt("GOP");
            deviceInfo.bitrate = devObj.optInt("bitrate");
            deviceInfo.maxFPS = devObj.optInt("maxFPS");
            deviceInfo.minFPS = devObj.optInt("minFPS");
            deviceInfo.perUpFPS = devObj.optInt("perUpFPS");
            deviceInfo.perDownFPS = devObj.optInt("perDownFPS");
            deviceInfo.encodeType = devObj.optInt("encodeType");
            deviceInfo.isAudio = devObj.optInt("isAudio") == 1 ? true : false;

            try {
                int quality = devObj.optInt("gameVideoQuality");
                if (quality >= 0 && quality < VideoQuality.values().length){
                    VideoQuality[] arr = VideoQuality.values();
                    deviceInfo.videoQuality = arr[quality].name();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
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
                "padCode='" + padCode + '\'' +
                ", videoQuality='" + videoQuality + '\'' +
                ", resolutionRatio=" + resolutionRatio +
                ", isAudio=" + isAudio +
                ", gop=" + gop +
                ", bitrate=" + bitrate +
                ", maxFPS=" + maxFPS +
                ", minFPS=" + minFPS +
                ", perUpFPS=" + perUpFPS +
                ", perDownFPS=" + perDownFPS +
                ", encodeType=" + encodeType +
                ", deviceParams='" + deviceParams + '\'' +
                ", apiLevel=" + apiLevel +
                ", useSSL=" + useSSL +
                '}';
    }

    public String toJsonString(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("padCode",padCode);
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
            obj.put("apiLevel",apiLevel);
            obj.put("useSSL",useSSL);
        }catch (Exception e){
        }
        return obj.toString();
    }
}
