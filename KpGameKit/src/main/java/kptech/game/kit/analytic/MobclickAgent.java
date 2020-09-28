package kptech.game.kit.analytic;

import com.kptech.netqueue.base.Request;
import com.kptech.netqueue.core.RequestQueue;
import com.kptech.netqueue.core.SimpleNet;
import com.kptech.netqueue.requests.StringRequest;

import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.BuildConfig;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.StringUtil;

public class MobclickAgent {
    private static Logger logger = new Logger("MobclickAgent");

    public static void sendEvent(Event event) {
        if (event == null){
            return;
        }
        //判断corpKey是否为空
        if (event!=null && StringUtil.isEmpty(event.clientId)){
            logger.error("sendEvent error: clientId is null ");
            return;
        }
        try {
            MobclickAgent agent = getInstance();
            if (agent!=null){
                agent.sendStringRequest(event.toRequestJson());
            }
        }catch (Exception e){
            logger.error("sendEvent error:"+e.getMessage());
        }
    }

    public static void sendPlayTimeEvent(Event event){
        if (event == null){
            return;
        }
        //判断corpKey是否为空
        if (event!=null && StringUtil.isEmpty(event.clientId)){
            logger.error("sendPlayTimeEvent error: clientId is null ");
            return;
        }
        try {
            MobclickAgent agent = getInstance();
            if (agent!=null){
                agent.sendStringPlayTimeRequest(event.toTimeRequestMap());
            }
        }catch (Exception e){
            logger.error("sendPlayTimeEvent error:"+e.getMessage());
        }
    }

    public static void sendTMEvent(Event event){
        if (event == null){
            return;
        }
        //判断corpKey是否为空
        if (StringUtil.isEmpty(event.clientId)){
            logger.error("sendTMEvent error: clientId is null ");
            return;
        }
        try {
            MobclickAgent agent = getInstance();
            if (agent!=null){
                agent.sendStringTMRequest(event.toTMRequestMap());
            }
        }catch (Exception e){
            logger.error("sendTMEvent error:"+e.getMessage());
        }
    }


    private String URL_ACTION = "https://interface.open.kuaipantech.com/useraction.php";
    private String URL_TIME = "https://interface.open.kuaipantech.com/useraction_playtimes.php";
    private String URL_TM_ACTION = "https://interface.open.kuaipantech.com/useraction_special.php";
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
            logger.error(e.getMessage());
        }
    }

    /**
     * 发送GET请求,返回的是String类型的数据, 同理还有{@see JsonRequest}、{@see MultipartRequest}
     */
    private void sendStringRequest(String data) {
        try {
            if (mQueue!=null){
                logger.info("actSend:" + data);

                StringRequest request = new StringRequest(Request.HttpMethod.POST, URL_ACTION,
                    new Request.RequestListener<String>() {
                        @Override
                        public void onComplete(int stCode, String response, String errMsg) {
                            if (stCode != 200){
                                logger.error("sendLog response code:" + stCode + ",response:" + response + ",errMsg:" + errMsg);
                            }
                        }
                    });
                request.setShouldCache(false);
                Map<String,String > params =  request.getParams();
                params.put("kphtmldata", data);
                mQueue.addRequest(request);
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    /**
     * 发送GET请求,返回的是String类型的数据, 同理还有{@see JsonRequest}、{@see MultipartRequest}
     */
    private void sendStringPlayTimeRequest(Map data) {
        try {
            if (mQueue!=null){
                logger.info("timeSend:" + data.toString());
                StringRequest request = new StringRequest(Request.HttpMethod.POST, URL_TIME,
                        new Request.RequestListener<String>() {
                            @Override
                            public void onComplete(int stCode, String response, String errMsg) {
                                if (stCode != 200){
                                    logger.error("sendPlayTimeLog response code:" + stCode + ",response:" + response + ",errMsg:" + errMsg);
                                }
                            }
                        });
                request.setShouldCache(false);
                Map<String,String> params = request.getParams();
                params.putAll(data);
                mQueue.addRequest(request);
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    /**
     * 发送GET请求,返回的是String类型的数据, 同理还有{@see JsonRequest}、{@see MultipartRequest}
     */
    private void sendStringTMRequest(Map map) {
        try {
            if (mQueue!=null){
                logger.info("tmSend:" + map.toString());
                StringRequest request = new StringRequest(Request.HttpMethod.POST, URL_TM_ACTION,
                        new Request.RequestListener<String>() {
                            @Override
                            public void onComplete(int stCode, String response, String errMsg) {
                                if (stCode != 200){
                                    logger.error("sendTMLog response code:" + stCode + ",response:" + response + ",errMsg:" + errMsg);
                                }
                            }
                        });
                request.setShouldCache(false);
                Map<String,String> params = request.getParams();
                params.putAll(map);
                mQueue.addRequest(request);
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }
}
