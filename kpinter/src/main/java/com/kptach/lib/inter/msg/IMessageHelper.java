package com.kptach.lib.inter.msg;

import android.app.Application;

import java.util.Map;

public interface IMessageHelper {

    void init(Application app, IMessageCallback callback, boolean debug);
    void start(String deviceId);
    void stop();
    void destory();

    void setParams(Map<String, Object> params);
    void sendMessage(MessageAction event, int code, String err,  String data);

}
