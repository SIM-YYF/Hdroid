package com.hdroid.http.callback;
/**
 * @Title RequestCallBackHandler.java
 * @Package com.ykdl.common.http.callback
 * @Description
 * @date 2014-3-7 下午2:28:19

 */
public interface RequestCallBackHandler {
	
	boolean updateProgress(long total, long current, boolean forceUpdateUI);
}
