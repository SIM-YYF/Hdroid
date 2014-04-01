package com.hdroid.mvc.command;

public class ThreadPool {
	/** 核心线程池 */
	private static final int MAX_THREADS_COUNT = 2;
	/** 可用的任务线程数组 */
	private TaskThread threads[] = null;

	private boolean started = false;

	private static ThreadPool instance;

	private ThreadPool() {
	}

	public static ThreadPool getInstance() {
		if (instance == null) {
			instance = new ThreadPool();
		}
		return instance;
	}

	/**
	 * 开启任务
	 */
	public void start() {
		if (!started) {
			int threadCount = MAX_THREADS_COUNT;
			threads = new TaskThread[threadCount];
			for (int threadId = 0; threadId < threadCount; threadId++) {
				threads[threadId] = new TaskThread(threadId);
				threads[threadId].start();
			}
			started = true;
		}
	}

	/**
	 * 取消所有任务
	 */

	public void shutdown() {
		if (started) {
			for (TaskThread thread : threads) {
				thread.stop();
			}
			threads = null;
			started = false;
		}
	}
}
