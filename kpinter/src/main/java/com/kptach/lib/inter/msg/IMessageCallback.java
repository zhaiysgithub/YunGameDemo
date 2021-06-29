package com.kptach.lib.inter.msg;

public interface IMessageCallback {

    void onMessageReceived(MessageAction event, String msg);

    void onEvent(MessageEvent event, int code, String msg);

}
