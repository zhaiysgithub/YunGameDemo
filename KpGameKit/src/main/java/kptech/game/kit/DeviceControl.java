package kptech.game.kit;

public abstract class DeviceControl implements IDeviceControl {

//    protected String gameInfo;

    public interface PlayListener {
        boolean onNoOpsTimeout(int i, long j);

        void onPingUpdate(int i);

        void onScreenCapture(byte[] bArr);

        void onScreenChange(int i);
    }

    public interface SensorSamplerListener {
        void onSensorSamper(@SensorConstants.CloudPhoneSensorId int i, @SensorConstants.SensorState int i2);
    }

    public interface TimeCountDownListener {
        void countDown(int i);
    }
//
//    public DeviceControl(GameInfo str) {
//        this.deviceInfo = str;
//    }


}
