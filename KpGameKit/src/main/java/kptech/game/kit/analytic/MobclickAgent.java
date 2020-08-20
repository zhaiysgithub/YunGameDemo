package kptech.game.kit.analytic;

import android.util.Log;

import com.kptech.netqueue.base.Request;
import com.kptech.netqueue.core.RequestQueue;
import com.kptech.netqueue.core.SimpleNet;
import com.kptech.netqueue.requests.StringRequest;

import kptech.game.kit.utils.Logger;

public class MobclickAgent {
    private static Logger logger = new Logger("MobclickAgent", false);

    public static void sendEvent(Event event) {
        if (event == null){
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

    private String URL = "https://interface.open.kuaipantech.com/useraction.php";
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
    private void sendStringRequest(String params) {
        try {
            if (mQueue!=null){
                String url = URL + "?kphtmldata=" + params;
                logger.info("sendLog:" + url);
                StringRequest request = new StringRequest(Request.HttpMethod.GET, url,
                        new Request.RequestListener<String>() {
                            @Override
                            public void onComplete(int stCode, String response, String errMsg) {
                                if (stCode != 200){
                                    logger.info("sendLog response code:" + stCode + ",response:" + response + ",errMsg:" + errMsg);
                                }
                            }
                        });

                mQueue.addRequest(request);
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }

    }
}
