package com.hdroid.http.callback;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

/** 
 * @description: 
 * @time：2014-3-10 上午10:48:23 
 */
public interface HttpRedirectHandler {
    HttpRequestBase getDirectRequest(HttpResponse response);
}

