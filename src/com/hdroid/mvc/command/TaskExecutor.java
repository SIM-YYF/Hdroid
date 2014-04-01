package com.hdroid.mvc.command;

import java.util.HashMap;

/**
 * 
 * 执行和处理任务类
 */
public class TaskExecutor {

	private final HashMap<String, Class<? extends ITask>> tasks = new HashMap<String, Class<? extends ITask>>();
	
	
}
