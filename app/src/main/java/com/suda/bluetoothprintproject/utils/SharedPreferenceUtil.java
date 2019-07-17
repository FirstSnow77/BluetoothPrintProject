package com.suda.bluetoothprintproject.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.suda.bluetoothprintproject.R;

public class SharedPreferenceUtil
{
	public static final String TAG_VERSION_NAME = "version_name";
	
	public static void setSharedPreference(Context context, String tag, String data)
	{
		SharedPreferences prefs = context.getSharedPreferences(context.getResources().getString(R.string.app_name), Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(tag, data);
		editor.commit(); // 先暫時使用 commit 方法, 來寫入資料
	}
	
	public static String getSharedPreference(Context context, String tag)
	{
		SharedPreferences prefs = context.getSharedPreferences(context.getResources().getString(R.string.app_name), Activity.MODE_PRIVATE);
		return prefs.getString(tag, "");
	}
	
	public static void removeSharedPreference(Context context, String tag)
	{
		SharedPreferences prefs = context.getSharedPreferences(context.getResources().getString(R.string.app_name), Activity.MODE_PRIVATE);
		prefs.edit().remove(tag).commit();
	}
}
