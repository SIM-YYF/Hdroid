package com.hdroid.http.response;


import org.apache.http.HttpResponse;

import com.hdroid.http.callback.RequestCallBackHandler;
import com.hdroid.http.response.result.ResponseInfo;


import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * @Title BaseHttpResponseHandler.java
 * @Description
 * @date 2014-3-7 下午2:44:53

 */
public abstract class BaseHttpResponseHandler implements RequestCallBackHandler{
	
	protected static final int SUCCESS_MESSAGE = 4;
	protected static final int FAILURE_MESSAGE = 1;
	protected static final int START_MESSAGE = 2;
	protected static final int FINISH_MESSAGE = 3;
	protected static final int PROGRESS_MESSAGE = 0;
	protected boolean isUploading = true;
	
	protected String responseCharset = null;
	
	public void setResponseCharset(String responseCharset){
		this.responseCharset = responseCharset;
	}
	
	
	private Handler handler = null;
	
	@SuppressLint("HandlerLeak")
	public BaseHttpResponseHandler(){
		if(Looper.myLooper() != null){
			handler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					BaseHttpResponseHandler.this.handleMessage(msg);
				}
			};
		}
	}
	
	/**
	 * 处理消息，并操作ui线程
	 * @param msg
	 */
	protected  void handleMessage(Message msg){
		Object[] response;

		switch (msg.what)
		{
		case START_MESSAGE:
			response = (Object[])msg.obj;
			onStart((Long)response[0], (String)response[1], (Boolean)response[2], (Boolean)response[3]);
			break;
		case PROGRESS_MESSAGE:
			response = (Object[]) msg.obj;
			onProgress(((Long)response[0]).longValue(), ((Long)response[1]).longValue(), ((Boolean)response[2]).booleanValue());
			break;
		case SUCCESS_MESSAGE:
			response = (Object[]) msg.obj;
			onSuccess((ResponseInfo)response[0]);
			break;
		case FAILURE_MESSAGE:
			response = (Object[]) msg.obj;
			onFailure((Throwable)response[0], (StringBuilder)response[1]);
			break;
		}
	}

	/**
	 * 生成消息
	 * @param responseMessage
	 * @param response
	 * @return
	 */
	protected Message obtainMessage(int responseMessage, Object response)
	{
		Message msg = null;
		if (handler != null)
		{
			msg = this.handler.obtainMessage(responseMessage, response);
		} else {
			msg = Message.obtain();
			msg.what = responseMessage;
			msg.obj = response;
		}
		
		return msg;
	}
	
	/**
	 * 发送消息
	 * @param msg
	 */
	protected void sendMessage(Message msg){
		if (handler != null)
		{
			handler.sendMessage(msg);
		} else
		{
			handleMessage(msg);
		}
	}

	public  void onStart(long threadId, String threadName, boolean isAlive, boolean isInterrupted){}
	public  void onProgress(long totalSize, long currentSize,  boolean isUploading){}
	public  void onSuccess(ResponseInfo responseInfo){}
	public  void onFailure(Throwable error, StringBuilder msg){}
	
	@Override
	public boolean updateProgress(long total, long current, boolean forceUpdateUI) {
		sendMessage(obtainMessage(PROGRESS_MESSAGE, new Object[]{new Long(total), new Long(current), new Boolean(isUploading)}));
		return true;
	}
	
	
	/**
	 * 构建开始请求消息并发送
	 * @param threadId
	 * @param threadName
	 * @param isAlive
	 * @param isInterrupted
	 */
	@SuppressLint("UseValueOf")
	public void sendStartMessage(long threadId, String threadName, boolean isAlive, boolean isInterrupted){
		//TODO 
		sendMessage(obtainMessage(START_MESSAGE, new Object[]{new Long(threadId), new String(threadName), new Boolean(isAlive), new Boolean(isInterrupted)}));
	}
	/**
	 * 构建请求进度消息并发送
	 * @param progressMessage
	 * @param objects
	 */
	public void sendProgressMessage(int progressMessage, Object[] objects) {
		updateProgress((Long)objects[0], (Long)objects[1], (Boolean)objects[2]);
	}
	
	/**
	 * 构建请求成功消息并发送
	 * @param response
	 * @param responseBody
	 */
	public void sendSuccessMessage(HttpResponse response, StringBuilder responseBody){
		ResponseInfo responseInfo = new ResponseInfo(response, responseBody, false);
		sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[]{responseInfo}));
	}
	/**
	 * 构建请求失败消息并发送
	 * @param e
	 * @param responseBody
	 */
	public void sendFailureMessage(Throwable e, StringBuilder responseBody){
		sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{e, responseBody}));
	}
	
}
