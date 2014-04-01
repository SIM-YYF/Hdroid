package com.hdroid.mvc.command;

public final class TaskQueueManager {

	private static TaskQueueManager instance;
	private ThreadPool pool;
	private TaskQueue queue;
	private boolean initialized = false;
	
	private TaskQueueManager(){}

	public static synchronized TaskQueueManager getInstance()
	{
		if (instance == null){
			instance = new TaskQueueManager();
		}
		return instance;
	}
	
	/**
	 * 初始化线程池
	 */
	public void initialize()
	{
		if (!initialized)
		{
			queue = new TaskQueue();//实例化任务队列
			pool = ThreadPool.getInstance();//实例化线程池
			pool.start();
			initialized = true;
		}
	}
	/**
	 * 获得任务
	 * 
	 * @return TAICommand
	 */
	public ITask getNextTask()
	{
		ITask task = queue.getNextTask();
		return task;
	}

	/**
	 * 添加任务
	 */
	public void addTask(ITask task)
	{
		queue.addTask(task);
	}
	/**
	 * 清空任务
	 */
	
	public void clear(){
		queue.clear();
	}
	
	/**
	 * 关闭所有的线程任务
	 */
	public void shutdown()
	{
		if (initialized)
		{
			queue.clear();
			pool.shutdown();
			initialized = false;
		}
	}
	
}
