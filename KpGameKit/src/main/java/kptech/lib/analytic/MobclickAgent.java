package kptech.lib.analytic;

import com.kptech.netqueue.base.Request;
import com.kptech.netqueue.core.RequestQueue;
import com.kptech.netqueue.core.SimpleNet;
import com.kptech.netqueue.requests.StringRequest;

import org.json.JSONObject;

import java.util.Map;

import kptech.lib.constants.Urls;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.StringUtil;

public class MobclickAgent {

    public static void sendEvent(Event event) {
        if (event == null){
            return;
        }
        //判断corpKey是否为空
        if (event!=null && StringUtil.isEmpty(event.clientId)){
            Logger.error("MobclickAgent","sendEvent error: clientId is null ");
            return;
        }
        try {
            MobclickAgent agent = getInstance();
            if (agent!=null){
                agent.sendStringRequest(event.toRequestJson());
            }
        }catch (Exception e){
            Logger.error("MobclickAgent","sendEvent error:"+e.getMessage());
        }
    }

    public static void sendPlayTimeEvent(Event event){
        if (event == null){
            return;
        }
        //判断corpKey是否为空
        if (event!=null && StringUtil.isEmpty(event.clientId)){
            Logger.error("MobclickAgent","sendPlayTimeEvent error: clientId is null ");
            return;
        }
        try {
            MobclickAgent agent = getInstance();
            if (agent!=null){
                agent.sendStringPlayTimeRequest(event.toTimeRequestMap());
            }
        }catch (Exception e){
            Logger.error("MobclickAgent","sendPlayTimeEvent error:"+e.getMessage());
        }
    }

    public static void sendPlayTimeEventPAAS(Event event,int wttm){
        Logger.info("MobclickAgent_PAAS", "sendPlayTimeEventPAAS");
        if (event == null){
            return;
        }
        //判断corpKey是否为空
        if (StringUtil.isEmpty(event.clientId)){
            Logger.error("MobclickAgent_PAAS","sendPlayTimeEvent error: clientId is null ");
            return;
        }
        try {
            MobclickAgent agent = getInstance();
            if (agent!=null){
                agent.sendStringPlayTimeRequestPAAS(event.toTimeRequestPaasMap(wttm));
            }
        }catch (Exception e){
            e.printStackTrace();
            Logger.error("MobclickAgent_PAAS",e.getMessage());
        }
    }

    public static void sendTMEvent(Event event){
        if (event == null){
            return;
        }
        //判断corpKey是否为空
        if (StringUtil.isEmpty(event.clientId)){
            Logger.error("MobclickAgent","sendTMEvent error: clientId is null ");
            return;
        }
        try {
            MobclickAgent agent = getInstance();
            if (agent!=null){
                agent.sendStringTMRequest(event.toTMRequestMap());
            }
        }catch (Exception e){
            Logger.error("MobclickAgent","sendTMEvent error:"+e.getMessage());
        }
    }

    private RequestQueue mQueue;

    private static volatile MobclickAgent agent = null;
    private static MobclickAgent getInstance() {
        if (agent == null) {
            synchronized(MobclickAgent.class) {
                if (agent == null) {
                    agent = new MobclickAgent();
                }
            }
        }
        return agent;
    }

    private MobclickAgent(){
        mQueue = SimpleNet.newRequestQueue();
    }

    private void destory(){
        try {
            if (mQueue!=null){
                mQueue.stop();
            }
        }catch (Exception e){
            Logger.error("MobclickAgent", e.getMessage());
        }
    }

    /**
     * 发送GET请求,返回的是String类型的数据, 同理还有{@see JsonRequest}、{@see MultipartRequest}
     */
    private void sendStringRequest(String data) {
        try {
            if (mQueue!=null){
                Logger.info("MobclickAgent","actSend:" + data);

                StringRequest request = new StringRequest(Request.HttpMethod.POST, Urls.URL_ACTION,
                    new Request.RequestListener<String>() {
                        @Override
                        public void onComplete(int stCode, String response, String errMsg) {
                            if (stCode != 200){
                                Logger.error("MobclickAgent","sendLog response code:" + stCode + ",response:" + response + ",errMsg:" + errMsg);
                            }
                        }
                    });
                request.setShouldCache(false);
                Map<String,String > params =  request.getParams();
                params.put("kphtmldata", data);
                mQueue.addRequest(request);
            }
        }catch (Exception e){
            Logger.error("MobclickAgent",e.getMessage());
        }
    }

    /**
     * 发送GET请求,返回的是String类型的数据, 同理还有{@see JsonRequest}、{@see MultipartRequest}
     */
    private void sendStringPlayTimeRequest(Map data) {
        try {
            if (mQueue!=null){
                Logger.info("MobclickAgent","timeSend:" + data.toString());
                StringRequest request = new StringRequest(Request.HttpMethod.POST, Urls.URL_TIME,
                        new Request.RequestListener<String>() {
                            @Override
                            public void onComplete(int stCode, String response, String errMsg) {
                                if (stCode != 200){
                                    Logger.error("MobclickAgent","sendPlayTimeLog response code:" + stCode + ",response:" + response + ",errMsg:" + errMsg);
                                }
                            }
                        });
                request.setShouldCache(false);
                Map<String,String> params = request.getParams();
                params.putAll(data);
                mQueue.addRequest(request);
            }
        }catch (Exception e){
            Logger.error("MobclickAgent",e.getMessage());
        }
    }

    /**
     * PAAS3心跳数据
     */
    private void sendStringPlayTimeRequestPAAS(String param) {
        try {
            if (mQueue!=null){
                String timeUrlPaas = Urls.getTraceUrlPAAS();
                String url = timeUrlPaas + "?f=BEET&p=" + param;
                Logger.info("MobclickAgent_PAAS","url:" + url);
                StringRequest request = new StringRequest(Request.HttpMethod.GET, url,
                        (stCode, response, errMsg) -> {
                            if (stCode != 200){
                                Logger.error("MobclickAgent_PAAS","sendPlayTimeLog response code:" + stCode + ",response:" + response + ",errMsg:" + errMsg);
                            }
                        });
                request.setShouldCache(false);
                mQueue.addRequest(request);
            }
        }catch (Exception e){
            Logger.error("MobclickAgent_PAAS",e.getMessage());
        }
    }

    /**
     * 发送GET请求,返回的是String类型的数据, 同理还有{@see JsonRequest}、{@see MultipartRequest}
     */
    private void sendStringTMRequest(Map map) {
        try {
            if (mQueue!=null){
                Logger.info("MobclickAgent","tmSend:" + map.toString());
                StringRequest request = new StringRequest(Request.HttpMethod.POST, Urls.URL_TM_ACTION,
                        new Request.RequestListener<String>() {
                            @Override
                            public void onComplete(int stCode, String response, String errMsg) {
                                if (stCode != 200){
                                    Logger.error("MobclickAgent","sendTMLog response code:" + stCode + ",response:" + response + ",errMsg:" + errMsg);
                                }
                            }
                        });
                request.setShouldCache(false);
                Map<String,String> params = request.getParams();
                params.putAll(map);
                mQueue.addRequest(request);
            }
        }catch (Exception e){
            Logger.error("MobclickAgent", e.getMessage());
        }
    }

    /**
     * PAAS3发送 TRACE 数据
     */
    public  static void sendPaas3TraceEvent(Event event,int eventType, String extData){
        if (event == null){
            return;
        }
        //判断corpKey是否为空
        if (StringUtil.isEmpty(event.clientId)){
            Logger.error("MobclickAgent_PAAS","sendPlayTimeEvent error: clientId is null ");
            return;
        }

        try {
            MobclickAgent agent = getInstance();
            if (agent != null){
                String params = event.getPaas3TraceP(eventType,extData);
                agent.sendPAAS3TraceData(params);
            }
        }catch (Exception e){
            e.printStackTrace();
            Logger.error("MobclickAgent_TRACE",e.getMessage());
        }
    }


    /**
     * PAAS3发送 TRACE 数据
     */
    private void sendPAAS3TraceData(String param){
        try {
            if (mQueue != null){
                String traceBaseUrl = Urls.getTraceUrlPAAS();
                String url = traceBaseUrl + "?f=TRACE&p=" + param;
//                Logger.info("MobclickAgent_TRACE","url:" + url);
                StringRequest request = new StringRequest(Request.HttpMethod.GET, url,
                        (stCode, response, errMsg) -> {
                    if (stCode != 200){
                        Logger.error("MobclickAgent_TRACE","code:" + stCode + ",response:" + response + ",errMsg:" + errMsg);
                    }
                });
                request.setShouldCache(false);
                mQueue.addRequest(request);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
