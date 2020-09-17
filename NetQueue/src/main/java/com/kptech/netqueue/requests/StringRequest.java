package com.kptech.netqueue.requests;

import com.kptech.netqueue.base.Request;
import com.kptech.netqueue.base.Response;

public class StringRequest extends Request<String> {

    public StringRequest(HttpMethod method, String url, RequestListener<String> listener) {
        super(method, url, listener);
    }

    @Override
    public String parseResponse(Response response) {
        try {
            return new String(response.getRawData());
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

}
