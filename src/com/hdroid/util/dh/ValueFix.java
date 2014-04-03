package com.hdroid.util.dh;


import android.view.Display;

/**
 * 
 * 全局的网络数据修复
 * 应用中基本需要实现的
 * 
 */
public interface ValueFix {
	
	public Object fix(Object o,String type);
//	public DisplayImageOptions imageOptions(String type);
	
}
