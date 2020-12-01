package kptech.game.kit.msg;

import java.util.Map;

public abstract class BaseMsgReceiver implements IMsgReceiver{
    public static final String EVENT_EXIT = "exit";

    @Override
    public void onMessageReceived(String msg) {

    }

    @Override
    public void onMessageReceived(String event, Map<String,Object> params){

    }

}
