package com.suda.bluetoothprintproject.businessManagers;

import android.content.Context;
import android.os.Looper;

import com.suda.bluetoothprintproject.api.dataManager.ApiDataManager;

public abstract class BaseManager
{
	protected final ApiDataManager mApiDataManager;
	protected final Context mContext;
	
	protected BaseManager(Context context) {
		this.mContext = context;
		this.mApiDataManager = new ApiDataManager(context);
	}
	
	/**
	 * 在子 thread 中，在使用 thread 操作 UI。需要額外對 Thread looper 進行配置
	 * @param runCallback callback
	 */
	protected void runUiThreadSetNewLooper(final runUiThreadAndDoSomething runCallback) {
		new Thread(new Runnable() {
			@Override public void run() {
				Looper.prepare();
				runCallback.doSomething();
				Looper.loop();
			}
		}).start();
	}
	
	protected interface runUiThreadAndDoSomething
	{
		void doSomething();
	}
}
