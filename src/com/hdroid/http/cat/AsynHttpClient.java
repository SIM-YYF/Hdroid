package com.hdroid.http.cat;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

import com.hdroid.http.cat.cache.HttpCache;
import com.hdroid.http.cat.callback.HttpRedirectHandler;
import com.hdroid.http.cat.entity.GZipDecompressingEntity;
import com.hdroid.http.cat.request.HttpRequest;
import com.hdroid.http.cat.request.RequestParams;
import com.hdroid.http.cat.request.RequestTask;
import com.hdroid.http.cat.request.RetryHandler;
import com.hdroid.http.cat.response.FileHttpResponseHandler;
import com.hdroid.http.cat.response.HttpResponseHandler;



import android.content.Context;
import android.text.TextUtils;

/**
 * @Title AsynHttpClient.java
 * @Package com.ykdl.common.http
 * @Description 异步网络请求
 * @date 2014-3-6 下午3:38:03
 */
public class AsynHttpClient {

	private static final String VERSION = "1.1";
	/** 线程池维护线程的最少数量 */
	private static final int DEFAULT_CORE_POOL_SIZE = 5;
	/** 线程池最大线程数 **/
	private static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
	/** 线程池维护线程所允许的空闲时间 */
	private static final int DEFAULT_KEEP_ALIVETIME = 0;
	/** http请求最大并发连接数 */
	private static final int DEFAULT_MAX_CONNECTIONS = 3;
	/** 超时时间，默认10秒 */
	private static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;
	/** 默认错误尝试次数 */
	private static final int DEFAULT_MAX_RETRIES = 3;
	/** 默认的套接字缓冲区大小 */
	private static final int DEFAULT_SOCKET_BUFFER_SIZE = 1024 * 2;
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_GZIP = "gzip";

	private static int maxConnections = DEFAULT_MAX_CONNECTIONS;
	private static int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

	private String responseTextCharset = HTTP.UTF_8;
	private HttpRedirectHandler httpRedirectHandler;
//	public final static HttpCache sHttpCache = new HttpCache(); //目前不开放缓存机制
	private long currentRequestExpiry = HttpCache.getDefaultExpiryTime();

	private final DefaultHttpClient httpClient;
	private final HttpContext httpContext;
	private ThreadPoolExecutor threadPool;
	private Map<Context, List<WeakReference<Future<?>>>> requestMap = null;
	private Map<String, String> clientHeaderMap = null;

	public AsynHttpClient() {
		HttpParams params = new BasicHttpParams();

		ConnManagerParams.setTimeout(params, socketTimeout);
		HttpConnectionParams.setSoTimeout(params, socketTimeout);
		HttpConnectionParams.setConnectionTimeout(params, socketTimeout);

		ConnManagerParams.setMaxConnectionsPerRoute(params,
				new ConnPerRouteBean(maxConnections));
		ConnManagerParams.setMaxTotalConnections(params,
				DEFAULT_MAX_CONNECTIONS);

		HttpConnectionParams.setTcpNoDelay(params, true);
		HttpConnectionParams.setSocketBufferSize(params,
				DEFAULT_SOCKET_BUFFER_SIZE);

		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUserAgent(params,
				String.format("ykdl/%s (http://www.wxxr.com.cn)", VERSION));

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
				params, schemeRegistry);

		httpContext = new SyncBasicHttpContext(new BasicHttpContext());
		httpClient = new DefaultHttpClient(cm, params);
		httpClient.addRequestInterceptor(new HttpRequestInterceptor() {

			@Override
			public void process(org.apache.http.HttpRequest request, HttpContext context)
					throws HttpException, IOException {
				if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
					request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
				}

			}
		});

		httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
			@Override
			public void process(HttpResponse response, HttpContext httpContext)
					throws org.apache.http.HttpException, IOException {
				final HttpEntity entity = response.getEntity();
				if (entity == null) {
					return;
				}
				final Header encoding = entity.getContentEncoding();
				if (encoding != null) {
					for (HeaderElement element : encoding.getElements()) {
						if (element.getName().equalsIgnoreCase("gzip")) {
							response.setEntity(new GZipDecompressingEntity(
									response.getEntity()));
							return;
						}
					}
				}
			}
		});

		httpClient.setHttpRequestRetryHandler(new RetryHandler(
				DEFAULT_MAX_RETRIES));

		threadPool = new ThreadPoolExecutor(DEFAULT_CORE_POOL_SIZE,
				DEFAULT_MAXIMUM_POOL_SIZE, DEFAULT_KEEP_ALIVETIME,
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),
				new ThreadPoolExecutor.CallerRunsPolicy());

		requestMap = new WeakHashMap<Context, List<WeakReference<Future<?>>>>();
		clientHeaderMap = new HashMap<String, String>();

	}

	/**
	 * 获得 HttpClient 实例。 用户访问设置客户端的连接管理器请求,httpparams 和 schemeregistry。 Get the
	 * underlying HttpClient instance. This is useful for setting additional
	 * fine-grained settings for requests by accessing the client's
	 * ConnectionManager, HttpParams and SchemeRegistry.
	 */
	public HttpClient getHttpClient() {
		return this.httpClient;
	}

	/**
	 * 获得HttpContext实例。 通过访问上下文的属性，来设置CookieStore Get the underlying HttpContext
	 * instance. This is useful for getting and setting fine-grained settings
	 * for requests by accessing the context's attributes such as the
	 * CookieStore.
	 */
	public HttpContext getHttpContext() {
		return this.httpContext;
	}

	/**
	 * 配置响应编码方式
	 * 
	 * @param charSet
	 *            默认：UTF-8
	 */
	public void setResponseTextCharset(String charSet) {
		if (!TextUtils.isEmpty(charSet)) {
			this.responseTextCharset = charSet;
		}
	}

	/**
	 * 设置重定向
	 * 
	 * @param httpRedirectHandler
	 */
	public void setHttpRedirectHandler(HttpRedirectHandler httpRedirectHandler) {
		this.httpRedirectHandler = httpRedirectHandler;
	}

//	public void setHttpCacheSize(int httpCacheSize) {
//		sHttpCache.setCacheSize(httpCacheSize);
//	}

	public void setDefaultHttpCacheExpiry(long defaultExpiry) {
		HttpCache.setDefaultExpiryTime(defaultExpiry);
		currentRequestExpiry = HttpCache.getDefaultExpiryTime();
	}

	public void setCurrentHttpCacheExpiry(long currRequestExpiry) {
		this.currentRequestExpiry = currRequestExpiry;
	}

	public void setCookieStore(CookieStore cookieStore) {
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	public void setUserAgent(String userAgent) {
		HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
	}

	public void setTimeout(int timeout) {
		final HttpParams httpParams = this.httpClient.getParams();
		ConnManagerParams.setTimeout(httpParams, timeout);
		HttpConnectionParams.setSoTimeout(httpParams, timeout);
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
	}

	public void setRegisterScheme(Scheme scheme) {
		this.httpClient.getConnectionManager().getSchemeRegistry()
				.register(scheme);
	}

	public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
		Scheme scheme = new Scheme("https", sslSocketFactory, 443);
		this.httpClient.getConnectionManager().getSchemeRegistry()
				.register(scheme);
	}

	public void setRequestRetryCount(int count) {
		this.httpClient.setHttpRequestRetryHandler(new RetryHandler(count));
	}

	public void setRequestThreadPoolSize(int threadPoolSize) {
		threadPool = new ThreadPoolExecutor(threadPoolSize,
				DEFAULT_MAXIMUM_POOL_SIZE, DEFAULT_KEEP_ALIVETIME,
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}
	
	
	public void download(HttpRequest.HttpMethod method, String url, HttpResponseHandler responseHandler)
	{
		download(null, method, url, null, responseHandler);
	}
	public void download(HttpRequest.HttpMethod method, String url, RequestParams params,HttpResponseHandler responseHandler)
	{
		download(null, method, url, params, responseHandler);
	}
	public void download(Context context, HttpRequest.HttpMethod method, String url,HttpResponseHandler responseHandler)
	{
		download(context, method, url, null, responseHandler);
	}
	public void download(Context context, HttpRequest.HttpMethod method, String url, RequestParams params, HttpResponseHandler responseHandler){
		HttpRequest request = new HttpRequest(method, url);
		sendAsyncRequest(context, request, params, responseHandler);
	}
	
	public  void sendAsync(HttpRequest.HttpMethod method, String url,HttpResponseHandler responseHandler) {
		 sendAsync(null, method, url, null, responseHandler);
	}
	public  void sendAsync(Context context, HttpRequest.HttpMethod method, String url,HttpResponseHandler responseHandler) {
		 sendAsync(context, method, url, null, responseHandler);
	}
	public  void sendAsync(HttpRequest.HttpMethod method, String url,RequestParams params, HttpResponseHandler responseHandler) {
		 sendAsync(null, method, url, params, responseHandler);
	}
	
	/**
	 * 异步发送请求
	 * @param method
	 * @param url
	 * @param callBack
	 * @return
	 */
	public  void sendAsync(Context context, HttpRequest.HttpMethod method, String url,RequestParams params, HttpResponseHandler responseHandler) {
		if (url == null)
			throw new IllegalArgumentException("url may not be null");

		HttpRequest request = new HttpRequest(method, url);
		sendAsyncRequest(context, request, params, responseHandler);
	}
	/**
	 * 异步发送请求
	 * @param method
	 * @param url
	 * @param callBack
	 * @return
	 */
    private  void sendAsyncRequest(Context context, HttpRequest request, RequestParams params, HttpResponseHandler responseHandler) {

    	RequestTask task = new RequestTask(httpClient, httpContext, responseTextCharset, responseHandler);

//    	task.setExpiry(currentRequestExpiry);
    	task.setHttpRedirectHandler(httpRedirectHandler);
        request.setRequestParams(params, responseHandler);
        task.setHttpRequest(request);
        
		Future<?> work = threadPool.submit(task);
		
		if (context != null)
		{
			// Add request to request map
			List<WeakReference<Future<?>>> requestList = requestMap.get(context);
			if (requestList == null)
			{
				requestList = new LinkedList<WeakReference<Future<?>>>();
				requestMap.put(context, requestList);
			}
			requestList.add(new WeakReference<Future<?>>(work));
		}

    }
    
	/**
	 * 同步发送请求
	 * @param method
	 * @param url
	 * @param callBack
	 * @return
	 */
	public  void sendSync(HttpRequest.HttpMethod method, String url, HttpResponseHandler responseHandler){
		 sendSync(method, url, null, responseHandler);
	}
	/**
	 * 同步发送请求
	 * @param method
	 * @param url
	 * @param callBack
	 * @return
	 */
	public  void sendSync(HttpRequest.HttpMethod method, String url,RequestParams params, HttpResponseHandler responseHandler){
		if (url == null)
			throw new IllegalArgumentException("url may not be null");

		HttpRequest request = new HttpRequest(method, url);
		sendSyncRequest(request, params, responseHandler);
	}
	
	/**
	 * 同步发送请求
	 * @param method
	 * @param url
	 * @param callBack
	 * @return
	 */
    private  void sendSyncRequest(HttpRequest request, RequestParams params, HttpResponseHandler responseHandler) {

    	RequestTask  task = new RequestTask(httpClient, httpContext, responseTextCharset, responseHandler);
//    	task.setExpiry(currentRequestExpiry);
    	task.setHttpRedirectHandler(httpRedirectHandler);
        request.setRequestParams(params, responseHandler);
        task.setHttpRequest(request);
        new Thread(task).start();
    }
    
    /**
     * 根据当前context取消请求
     * @param context
     * @param mayInterruptIfRunning
     */
	public void cancelRequests(Context context, boolean mayInterruptIfRunning)
	{
		List<WeakReference<Future<?>>> requestList = requestMap.get(context);
		if (requestList != null)
		{
			for (WeakReference<Future<?>> requestRef : requestList)
			{
				Future<?> request = requestRef.get();
				if (request != null)
				{
					request.cancel(mayInterruptIfRunning);
				}
			}
		}
		requestMap.remove(context);
	}
   
}
