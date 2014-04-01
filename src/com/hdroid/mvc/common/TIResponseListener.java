package com.hdroid.mvc.common;

public interface TIResponseListener {

	void onStart();

	void onSuccess(TResponse response);

	void onRuning(TResponse response);

	void onFailure(TResponse response);

}
