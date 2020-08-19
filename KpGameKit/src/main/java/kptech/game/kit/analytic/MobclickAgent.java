package kptech.game.kit.analytic;

import com.kptech.netqueue.base.Request;
import com.kptech.netqueue.core.RequestQueue;
import com.kptech.netqueue.core.SimpleNet;
import com.kptech.netqueue.requests.StringRequest;

public class MobclickAgent {

    RequestQueue mQueue = SimpleNet.newRequestQueue();


    private void sendStringRequest() {
        StringRequest request = new StringRequest(Request.HttpMethod.GET, "http://www.baidu.com",
                new Request.RequestListener<String>() {

                    @Override
                    public void onComplete(int stCode, String response, String errMsg) {
//                        mResultTv.setText(Html.fromHtml(response));
                    }
                });

        mQueue.addRequest(request);
    }
}
