package com.hdroid.mvc.command;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 任务队列
 * 
 */
public class TaskQueue {

	private LinkedBlockingQueue<ITask> theQueue = new LinkedBlockingQueue<ITask>();

	public TaskQueue() {
	}

	/**
	 * 添加任务
	 */
	public synchronized void addTask(ITask task) {
		theQueue.add(task);
	}

	/**
	 * 取得任务
	 */
	public synchronized ITask getNextTask() {
		ITask task = null;
		try {
			task = theQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return task;
	}

	/**
	 * 情况队列中的所有任务
	 */
	public synchronized void clear() {
		theQueue.clear();
	}

}
