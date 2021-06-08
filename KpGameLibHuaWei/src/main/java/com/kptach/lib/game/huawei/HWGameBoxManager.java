package com.kptach.lib.game.huawei;

import android.app.Activity;
import android.app.Application;

import com.kptach.lib.inter.game.IDeviceControl;
import com.kptach.lib.inter.game.IGameBoxManager;
import com.kptach.lib.inter.game.IGameCallback;

import java.util.HashMap;

public class HWGameBoxManager implements IGameBoxManager {

    @Override
    public void initLib(Application application, HashMap hashMap, IGameCallback<String> iGameCallback) {

    }

    @Override
    public void applyCloudDevice(Activity activity, String s, IGameCallback<IDeviceControl> iGameCallback) {

    }
}
