package kptach.game.kit.lib.baidu;

import android.app.Activity;
import android.app.Application;

import java.util.HashMap;

import kptach.game.kit.inter.game.GameInfo;
import kptach.game.kit.inter.game.IDeviceControl;
import kptach.game.kit.inter.game.IGameBoxManager;
import kptach.game.kit.inter.game.IGameCallback;

public class BdGameBoxManager implements IGameBoxManager {

    @Override
    public void initLib(Application application, HashMap params, IGameCallback<String> callback) {

    }

    @Override
    public void applyCloudDevice(Activity activity, GameInfo inf, IGameCallback<IDeviceControl> callback) {

    }

}
