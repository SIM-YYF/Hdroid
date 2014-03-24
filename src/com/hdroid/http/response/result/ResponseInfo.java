package com.hdroid.http.response.result;

import java.lang.reflect.Method;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

import com.hdroid.http.resolve.IParser;


/**
 * @Title ResponseInfo.java
 * @Package com.ykdl.common.http.response.result
 * @Description
 * @date 2014-3-7 下午5:12:27

 */
public final class ResponseInfo {

    private final HttpResponse response;
    public Object result;
    public Object parserData = null;
    private IParser  parser;
    public final boolean resultFormCache;

    public final Locale locale;

    // status line
    public final int statusCode;
    public final ProtocolVersion protocolVersion;
    public final String reasonPhrase;

    // entity
    public final long contentLength;
    public final Header contentType;
    public final Header contentEncoding;

    public Header[] getAllHeaders() {
        if (response == null) return null;
        return response.getAllHeaders();
    }

    public Header[] getHeaders(String name) {
        if (response == null) return null;
        return response.getHeaders(name);
    }

    public Header getFirstHeader(String name) {
        if (response == null) return null;
        return response.getFirstHeader(name);
    }

    public Header getLastHeader(String name) {
        if (response == null) return null;
        return response.getLastHeader(name);
    }

    public ResponseInfo(final HttpResponse response, Object result, boolean resultFormCache) {
        this.response = response;
        this.result = result;
        this.resultFormCache = resultFormCache;

        if (response != null) {
            locale = response.getLocale();

            // status line
            StatusLine statusLine = response.getStatusLine();
            if (statusLine != null) {
                statusCode = statusLine.getStatusCode();
                protocolVersion = statusLine.getProtocolVersion();
                reasonPhrase = statusLine.getReasonPhrase();
            } else {
                statusCode = 0;
                protocolVersion = null;
                reasonPhrase = null;
            }

            // entity
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                contentLength = entity.getContentLength();
                contentType = entity.getContentType();
                contentEncoding = entity.getContentEncoding();
            } else {
                contentLength = 0;
                contentType = null;
                contentEncoding = null;
            }
        } else {
            locale = null;

            // status line
            statusCode = 0;
            protocolVersion = null;
            reasonPhrase = null;

            // entity
            contentLength = 0;
            contentType = null;
            contentEncoding = null;
        }
    }
    
    
    public void parser(Class<? extends Object> clazz) throws Exception{
    	Object obj = clazz.newInstance();
		Method m = clazz.getDeclaredMethod("execute", String.class);
		parserData = m.invoke(obj, result);
    }
}
