package kptech.game.kit.model;

public class PassDeviceResponseBean {

    //错误码
    public int code;
    //错误信息描述
    public String msg;
    //时间戳，毫秒级
    public long ts;
    public PassData data;

    public static class PassData {
        //IaaS供应商编码  BD 百度设备， HW 华为设备
        public String iaas;
        //快盘的设备ID
        public String deviceid;
        //设备类型: 百度(845、3399)
        public String devicetype;
        //iaas厂商对设备设定的唯一ID
        public String devicenum;
        //IaaS供应商接口返回的音视频接入访问授权资源描述
//        public DeviceResource resource;
        public String resource;

        @Override
        public String toString() {
            return "PassData{" +
                    "iaas='" + iaas + '\'' +
                    ", deviceid='" + deviceid + '\'' +
                    ", devicetype='" + devicetype + '\'' +
                    ", devicenum='" + devicenum + '\'' +
                    ", resource=" + resource +
                    '}';
        }
    }

    public static class DeviceResource {
        //IaaS供应商接口返回的音视频接入访问授权资源描述
        public String deviceNum;
        public String phoneIp;
        public String domain;
        public String serverIp;
        public String accessPort;
        public String publicIp;
        public String sessionId;
        public String listenPort;

        @Override
        public String toString() {
            return "DeviceResource{" +
                    "deviceNum='" + deviceNum + '\'' +
                    ", phoneIp='" + phoneIp + '\'' +
                    ", domain='" + domain + '\'' +
                    ", serverIp='" + serverIp + '\'' +
                    ", accessPort='" + accessPort + '\'' +
                    ", publicIp='" + publicIp + '\'' +
                    ", sessionId='" + sessionId + '\'' +
                    ", listenPort='" + listenPort + '\'' +
                    '}';
        }
    }


    @Override
    public String toString() {
        return "PassDeviceResponseBean{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", ts=" + ts +
                ", data=" + data +
                '}';
    }
}
