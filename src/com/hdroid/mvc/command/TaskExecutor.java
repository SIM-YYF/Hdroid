package com.hdroid.mvc.command;

import java.lang.reflect.Modifier;
import java.util.HashMap;

import com.hdroid.mvc.common.TIResponseListener;
import com.hdroid.mvc.common.TRequest;

/**
 * 
 * 执行和处理任务类
 */
public class TaskExecutor {

	private final HashMap<String, Class<? extends ITask>> tasks = new HashMap<String, Class<? extends ITask>>();

	private static TaskExecutor instance = null;
	private boolean initialized = false;

	private TaskExecutor() {
		if (!initialized) {
			initialized = true;
			// 初始化线程池和任务队列
			TaskQueueManager.getInstance().initialize();
		}
	}

	public synchronized static TaskExecutor getInstance() {
		if (instance == null) {
			instance = new TaskExecutor();
		}
		return instance;
	}

	/**
	 * 注册任务(存储在HashMap)
	 * 
	 * @param key
	 * @param task
	 */
	public void registerTask(String key, Class<? extends ITask> task) {
		if (task != null) {
			tasks.put(key, task);
		}
	}

	/**
	 * 取消注册任务
	 * 
	 * @param key
	 */
	public void unregisterTask(String key) {
		tasks.remove(key);
	}

	/**
	 * 根据key获得对应的任务
	 * 
	 * @param key
	 */
	public ITask getTask(String key) throws Exception {
		ITask task = null;
		if (tasks.containsKey(key)) {
			Class<? extends ITask> t = tasks.get(key);
			if (t != null) {
				int modifiers = t.getModifiers();
				if ((modifiers & Modifier.ABSTRACT) == 0
						&& (modifiers & Modifier.INTERFACE) == 0) {
					try {
						task = t.newInstance();
					} catch (Exception e) {
						throw new Exception("没发现" + key + "命令");
					}
				} else {
					throw new Exception("没发现" + key + "命令");
				}
			}
		}

		return task;
	}

	public void addTask(String key, TRequest request,
			TIResponseListener listener) throws Exception {
		final ITask task = getTask(key);
		addTask(task, request, listener);
	}

	/**
	 * 将任务添加对任务队列中
	 * 
	 * @param task
	 * @param request
	 * @param listener
	 * @throws Exception
	 */
	public void addTask(ITask task, TRequest request,
			TIResponseListener listener) throws Exception {
		if (task != null) {
			task.setRequest(request);
			task.setResponseListener(listener);
			TaskQueueManager.getInstance().addTask(task);
		}
	}

	public void addTask(ITask task) throws Exception {
		addTask(task, null);
	}

	public void addTask(ITask task, TRequest request) throws Exception {
		addTask(task, null, null);
	}

}
