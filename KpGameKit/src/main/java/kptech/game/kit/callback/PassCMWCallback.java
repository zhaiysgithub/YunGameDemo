package kptech.game.kit.callback;

import kptech.game.kit.model.PassDeviceResponseBean;

public interface PassCMWCallback {

    void onSuccess(PassDeviceResponseBean result);

    void onError(int errorCode,String errorMsg);
}
