package com.hdroid.mvc.command;

public class TaskThread implements Runnable {
	
	private int threadId;
	private Thread thread = null;
	private boolean running = false;
	private boolean stop = false;
	
	public TaskThread(int threadId)
	{
		this.threadId = threadId;
		thread = new Thread(this);
	}
	
	@Override
	public void run() {
		while (!stop)
		{
			ITask task = TaskQueueManager.getInstance().getNextTask();
			task.execute();
		}
	}
	
	/**
	 * 开始任务
	 */
	public void start()
	{
		thread.start();
		running = true;
	}
	/**
	 * 取消任务
	 */
	
	public void stop()
	{
		stop = true;
		running = false;
	}

	public boolean isRunning()
	{
		return running;
	}

	public int getThreadId()
	{
		return threadId;
	}
}
