package com.hdroid.mvc.command;

import android.os.Handler;
import android.os.Message;

import com.hdroid.mvc.common.TIResponseListener;
import com.hdroid.mvc.common.TResponse;

public abstract class Task extends BaseTask {

	protected final static int task_start = 1;
	protected final static int task_runting = 2;
	protected final static int task_failure = 3;
	protected final static int task_success = 4;
	private TIResponseListener listener;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case task_start:
				listener.onStart();
				break;
			case task_runting:
				listener.onRuning(getResponse());
				break;
			case task_success:
				listener.onSuccess(getResponse());
				break;
			case task_failure:
				listener.onFailure(getResponse());
				break;
			default:
				break;
			}
		};

	};

	public final void execute() {
		onPreExecuteTask();
		executeTask();
		onPostExecuteTask();
	}

	protected abstract void executeTask();

	protected abstract void onPreExecuteTask();

	protected abstract void onPostExecuteTask();

	protected void sendMessage(int state) {
		listener = getResponseListener();
		if (listener != null) {
			handler.sendEmptyMessage(state);
		}
	}

	/**
	 * 发送开始执行消息
	 */
	public void sendStartMessage() {
		sendMessage(task_start);
	}

	/**
	 * 发送成功消息
	 * 
	 * @param object
	 */
	public void sendSuccessMessage(Object object) {
		TResponse response = new TResponse();
		response.setData(object);
		setResponse(response);
		sendMessage(task_success);
	}

	/**
	 * 发送失败消息
	 * 
	 * @param object
	 */
	public void sendFailureMessage(Object object) {
		TResponse response = new TResponse();
		response.setData(object);
		setResponse(response);
		sendMessage(task_failure);
	}

	/**
	 * 发送正在执行消息
	 * 
	 * @param object
	 */
	public void sendRuntingMessage(Object object) {
		TResponse response = new TResponse();
		response.setData(object);
		setResponse(response);
		sendMessage(task_runting);
	}

}
