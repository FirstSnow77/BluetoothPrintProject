package com.suda.bluetoothprintproject.utils;

public class Utils
{
	public static boolean IsFromLauncher = true; // 是否為第一次啟動的 flag
	
	// 两次点击按钮之间的点击间隔不能少于1000毫秒
	private static final int MIN_CLICK_DELAY_TIME = 1000;
	private static long lastClickTime;
	
	public synchronized static boolean isFastClick() {
		boolean flag = false;
		long curClickTime = System.currentTimeMillis();
		if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
			flag = true;
		}
		lastClickTime = curClickTime;
		return flag;
	}

}
