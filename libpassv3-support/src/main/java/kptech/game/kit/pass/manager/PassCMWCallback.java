package kptech.game.kit.pass.manager;

public interface PassCMWCallback {

    void onSuccess(PassDeviceResponseBean result);

    void onError(int errorCode,String errorMsg);
}
