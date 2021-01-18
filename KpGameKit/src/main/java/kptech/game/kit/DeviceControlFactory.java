package kptech.game.kit;

import kptech.game.kit.redfinger.RedDeviceControl;

public class DeviceControlFactory {

    public static IDeviceControl getDeviceControl(String devInfo, GameInfo gameInfo){
        DeviceInfo info = DeviceInfo.getInstance(devInfo);
        if (info.pass == DeviceInfo.Pass.BAIDU){
            return null;
        }else {
            return new RedDeviceControl(info, gameInfo);
        }
    }
}
