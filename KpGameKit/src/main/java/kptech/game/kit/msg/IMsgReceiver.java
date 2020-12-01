package kptech.game.kit.msg;

import java.util.Map;

public interface IMsgReceiver {

    void onMessageReceived(String msg);
    void onMessageReceived(String event, Map<String,Object> params);
}
