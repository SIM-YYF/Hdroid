package com.hdroid.mvc.controller;

import com.hdroid.mvc.AFragmentActivity;
import com.hdroid.mvc.common.TRequest;
import com.hdroid.mvc.common.TResponse;

public class ActivityStackInfo {
	private Class<? extends AFragmentActivity> activityClass;
	private String Key;//任务对应key值
	private TRequest request;
	private boolean record;//是否记录
	private boolean resetStack;//重置栈
	private TResponse response;

	public ActivityStackInfo() {
	}

	public ActivityStackInfo(String Key, TRequest request,
			boolean record, boolean resetStack) {
		this.Key = Key;
		this.request = request;
		this.record = record;
		this.resetStack = resetStack;
	}

	public ActivityStackInfo(Class<? extends AFragmentActivity> activityClass,
			String Key, TRequest request) {
		this.activityClass = activityClass;
		this.Key = Key;
		this.request = request;
	}

	public ActivityStackInfo(Class<? extends AFragmentActivity> activityClass,
			String Key, TRequest request, boolean record,
			boolean resetStack) {
		this.activityClass = activityClass;
		this.Key = Key;
		this.request = request;
		this.record = record;
		this.resetStack = resetStack;
	}

	public Class<? extends AFragmentActivity> getActivityClass() {
		return activityClass;
	}

	public void setActivityClass(Class<? extends AFragmentActivity> activityClass) {
		this.activityClass = activityClass;
	}

	public String getKey() {
		return Key;
	}

	public void setKey(String Key) {
		this.Key = Key;
	}

	public TRequest getRequest() {
		return request;
	}

	public void setRequest(TRequest request) {
		this.request = request;
	}

	public boolean isRecord() {
		return record;
	}

	public void setRecord(boolean record) {
		this.record = record;
	}

	public TResponse getResponse() {
		return response;
	}

	public void setResponse(TResponse response) {
		this.response = response;
	}

	public boolean isResetStack() {
		return resetStack;
	}

	public void setResetStack(boolean resetStack) {
		this.resetStack = resetStack;
	}
}
