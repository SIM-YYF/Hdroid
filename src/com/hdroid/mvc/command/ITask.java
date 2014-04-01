package com.hdroid.mvc.command;

import com.hdroid.mvc.common.TIResponseListener;
import com.hdroid.mvc.common.TRequest;
import com.hdroid.mvc.common.TResponse;

public interface ITask {
	TRequest getRequest();

	void setRequest(TRequest request);

	TResponse getResponse();

	void setResponse(TResponse response);

	TIResponseListener getResponseListener();

	void setResponseListener(TIResponseListener listener);

	void setTerminated(boolean terminated);

	boolean isTerminated();

	void execute();

}
