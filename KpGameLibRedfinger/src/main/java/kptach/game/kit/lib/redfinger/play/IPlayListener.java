package kptach.game.kit.lib.redfinger.play;

public interface IPlayListener {
    void onConnectSuccess(String str, int i);
    void onConnectError(String str, int i);
    void onReceiverBuffer();
    void onRelease();

    void onScreenCapture(byte[] bArr);
    void onScreenChange(int i);
    void onSensorInput(int i, int i2);
    void onTransparentMsg(int i, String str, String str2);
    void onMsgSendFailed(int i, int i2, String str);

    void onDelayTime(int i);
    void onNoOpsTimeout(int i, long j);

}
