package com.hdroid.http.request;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import com.hdroid.http.cache.HttpCache;
import com.hdroid.http.callback.HttpRedirectHandler;
import com.hdroid.http.response.FileHttpResponseHandler;
import com.hdroid.http.response.HttpResponseHandler;



/**
 * @description:
 * @time：2014-3-10 下午7:11:34
 */
public class RequestTask implements Runnable  {

	private final AbstractHttpClient client;
	private final HttpContext context;
	private HttpRequestBase request;
	private final HttpResponseHandler responseHandler;
	private int retriedTimes = 0;

	private String responseCharset = null;
//	private String requestMethod;
	

	private HttpRedirectHandler httpRedirectHandler;

	public void setHttpRedirectHandler(HttpRedirectHandler httpRedirectHandler) {
		if (httpRedirectHandler != null) {
			this.httpRedirectHandler = httpRedirectHandler;
		}
	}

//	private long expiry = HttpCache.getDefaultExpiryTime();
//
//	public void setExpiry(long expiry) {
//		this.expiry = expiry;
//	}

	public void setHttpRequest(HttpRequestBase request) {
		this.request = request;
	}

	public RequestTask(AbstractHttpClient client, HttpContext context,
			String charset, HttpResponseHandler responseHandler) {
		this.client = client;
		this.context = context;
		this.responseCharset = charset;
		this.responseHandler = responseHandler;
		
	}

	@Override
	public void run() {
		try{
			if (responseHandler != null) {
				
				if(responseHandler instanceof FileHttpResponseHandler){
					FileHttpResponseHandler fileHttpResponseHandler = (FileHttpResponseHandler) responseHandler;
					File tempFile = fileHttpResponseHandler.getTempFile();
					if(tempFile.exists()){
						//设置上次下载的数量,并设计range头
					    long previousFileSize = tempFile.length();
					    fileHttpResponseHandler.setPreviousFileSize(previousFileSize);
					    this.request.setHeader("RANGE", "bytes=" + previousFileSize + "-");
					}
				}
				
				responseHandler.setResponseCharset(responseCharset);
				responseHandler.sendStartMessage(Thread.currentThread().getId(),Thread.currentThread().getName(), Thread.currentThread().isAlive(), Thread.currentThread().isInterrupted());
			}
		makeRequestWithRetries();
		}catch(IOException e){
			if (responseHandler != null)
			{
					responseHandler.sendFailureMessage(e, null);
			}
		}

	}

	private void makeRequestWithRetries() throws ConnectException {
		boolean retry = true;
		IOException cause = null;
		HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();
		while (retry) {
			if (!Thread.currentThread().isInterrupted()) {
				try {
//					requestMethod = request.getMethod();
//					if (AsynHttpClient.sHttpCache.isEnabled(requestMethod)) {
//						String result = AsynHttpClient.sHttpCache.get(request.getURI().toString());
//						if (result != null) {
//							// 封装httpResponse对象
//							return;
////							responseHandler.sendResponseMessage(result);
//						}
//					}
					makeRequest();
					return;
				} catch (UnknownHostException e)
				{
					if (responseHandler != null)
					{
						responseHandler.sendFailureMessage(e, new StringBuilder("can't resolve host"));
					}
					return;
				} catch (SocketException e)
				{
					// Added to detect host unreachable
					if (responseHandler != null)
					{
						responseHandler.sendFailureMessage(e, new StringBuilder("can't resolve host"));
					}
					return;
				} catch (SocketTimeoutException e)
				{
					if (responseHandler != null)
					{
						responseHandler.sendFailureMessage(e, new StringBuilder("socket time out"));
					}
					return;
				} catch (IOException e)
				{
					cause = e;
					retry = retryHandler.retryRequest(cause, ++retriedTimes,context);
				} catch (NullPointerException e)
				{
					cause = new IOException("NPE in HttpClient" + e.getMessage());
					retry = retryHandler.retryRequest(cause, ++retriedTimes, context);
				}
			}
		}
		
		ConnectException ex = new ConnectException();
		ex.initCause(cause);
		throw ex;
	}
	
	
	private void makeRequest() throws IOException
	{
		if (!Thread.currentThread().isInterrupted())
		{
			try
			{
				HttpResponse response = client.execute(request, context);
				if (!Thread.currentThread().isInterrupted())
				{
					if (responseHandler != null)
					{
						responseHandler.sendResponseMessage(response);
					}
				}
			} catch (IOException e)
			{
				if (!Thread.currentThread().isInterrupted())
				{
					throw e;
				}
			}
		}
	}

}
