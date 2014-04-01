package com.hdroid.mvc.common;

import java.io.Serializable;

public class TResponse implements Serializable {
	private static final long serialVersionUID = 2715141726276497343L;

	private Object tag;
	private Object data;
	private String activityKey;
	private int activityKeyResID;

	public TResponse() {

	}

	public TResponse(Object tag, Object data) {
		this.tag = tag;
		this.data = data;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public int getActivityKeyResID() {
		return activityKeyResID;
	}

	public void setActivityKeyResID(int activityKeyResID) {
		this.activityKeyResID = activityKeyResID;
	}

	public String getActivityKey() {
		return activityKey;
	}

	public void setActivityKey(String activityKey) {
		this.activityKey = activityKey;
	}
}
