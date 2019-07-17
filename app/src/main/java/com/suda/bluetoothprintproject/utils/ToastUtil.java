package com.suda.bluetoothprintproject.utils;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Response;

public class ToastUtil
{
	// 顯示打 api 遇到 httpCode != 200 的時候
	public static <model> void showApiResponseErrorToast(Context context, Response<model> response) {
		try {
			String message = new String(response.errorBody().string());
			JSONObject obj = null;
			try {
				obj = new JSONObject(message);
			}
			catch(JSONException e) {
				e.printStackTrace();
			}
			if(obj != null) {
				showToast(context, obj.optString("message"));
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void showToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}
