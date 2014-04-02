package com.hdroid;

import android.app.Application;

public abstract class HApplication extends Application {
	private static HApplication application;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		this.application = this;
	}

	public static HApplication getApplication() {
		return application;
	}
}
