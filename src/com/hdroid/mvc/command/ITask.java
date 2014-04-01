package com.hdroid.mvc.command;

public interface ITask {
//	TARequest getRequest();
//
//	void setRequest(TARequest request);
//
//	TAResponse getResponse();
//
//	void setResponse(TAResponse response);

	void execute();

//	TAIResponseListener getResponseListener();
//
//	void setResponseListener(TAIResponseListener listener);

	void setTerminated(boolean terminated);
	boolean isTerminated();
	
}
