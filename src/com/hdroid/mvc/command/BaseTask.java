package com.hdroid.mvc.command;

import com.hdroid.mvc.common.TIResponseListener;
import com.hdroid.mvc.common.TRequest;
import com.hdroid.mvc.common.TResponse;

public abstract class BaseTask implements ITask {
	private TRequest request;
	private TResponse response;
	private TIResponseListener responseListener;
	private boolean terminated;// 是否终止任务

	@Override
	public TRequest getRequest() {
		return request;
	}

	@Override
	public void setRequest(TRequest request) {
		this.request = request;
	}

	@Override
	public TResponse getResponse() {
		return response;
	}

	@Override
	public void setResponse(TResponse response) {
		this.response = response;
	}

	@Override
	public TIResponseListener getResponseListener() {
		return responseListener;
	}

	@Override
	public void setResponseListener(TIResponseListener responseListener) {
		this.responseListener = responseListener;
	}

	@Override
	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}

	@Override
	public boolean isTerminated() {
		return terminated;
	}

}
