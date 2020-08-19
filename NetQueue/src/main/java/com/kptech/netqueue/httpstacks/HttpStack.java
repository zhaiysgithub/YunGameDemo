package com.kptech.netqueue.httpstacks;

import com.kptech.netqueue.base.Request;
import com.kptech.netqueue.base.Response;

/**
 * 执行网络请求的接口
 * 
 * @author mrsimple
 */
public interface HttpStack {
    /**
     * 执行Http请求
     * 
     * @param request 待执行的请求
     * @return
     */
    public Response performRequest(Request<?> request);
}
