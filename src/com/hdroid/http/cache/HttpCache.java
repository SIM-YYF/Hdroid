package com.hdroid.http.cache;

import java.util.concurrent.ConcurrentHashMap;

import com.hdroid.http.request.HttpRequest;


import android.text.TextUtils;


/** 
 * @description: http请求缓存机制
 * @time：2014-3-10 上午10:53:37 
 */
public class HttpCache {

    /**
     * key: url
     * value: response result
     */
    private final LruMemoryCache<String, String> mMemoryCache;

    private final static int DEFAULT_CACHE_SIZE = 1024 * 100;// string length
    private final static long DEFAULT_EXPIRY_TIME = 1000 * 60; // 60 seconds

    private int cacheSize = DEFAULT_CACHE_SIZE;

    private static long defaultExpiryTime = DEFAULT_EXPIRY_TIME;

    /**
     * HttpCache(HttpCache.DEFAULT_CACHE_SIZE, HttpCache.DEFAULT_EXPIRY_TIME);
     */
    public HttpCache() {
        this(HttpCache.DEFAULT_CACHE_SIZE, HttpCache.DEFAULT_EXPIRY_TIME);
    }

    public HttpCache(int strLength, long defaultExpiryTime) {
        this.cacheSize = strLength;
        HttpCache.defaultExpiryTime = defaultExpiryTime;

        mMemoryCache = new LruMemoryCache<String, String>(this.cacheSize) {
            @Override
            protected int sizeOf(String key, String value) {
                if (value == null) return 0;
                return value.length();
            }
        };
    }

    public void setCacheSize(int strLength) {
        mMemoryCache.setMaxSize(strLength);
    }

    public static void setDefaultExpiryTime(long defaultExpiryTime) {
        HttpCache.defaultExpiryTime = defaultExpiryTime;
    }

    public static long getDefaultExpiryTime() {
        return HttpCache.defaultExpiryTime;
    }

    public void put(String url, String result) {
        put(url, result, defaultExpiryTime);
    }

    public void put(String url, String result, long expiry) {
        if (url == null || result == null || expiry < 1) return;

        mMemoryCache.put(url, result, System.currentTimeMillis() + expiry);
    }

    public String get(String url) {
        return (url != null) ? mMemoryCache.get(url) : null;
    }

    public void clear() {
        mMemoryCache.evictAll();
    }

    public boolean isEnabled(HttpRequest.HttpMethod method) {
        if (method == null) return false;

        Boolean enabled = httpMethod_enabled_map.get(method.toString());
        return enabled == null ? false : enabled;
    }

    public boolean isEnabled(String method) {
        if (TextUtils.isEmpty(method)) return false;

        Boolean enabled = httpMethod_enabled_map.get(method.toUpperCase());
        return enabled == null ? false : enabled;
    }

    public void setEnabled(HttpRequest.HttpMethod method, boolean enabled) {
        httpMethod_enabled_map.put(method.toString(), enabled);
    }

    private final static ConcurrentHashMap<String, Boolean> httpMethod_enabled_map;

    static {
        httpMethod_enabled_map = new ConcurrentHashMap<String, Boolean>(10);
        httpMethod_enabled_map.put(HttpRequest.HttpMethod.GET.toString(), true);
    }
}

