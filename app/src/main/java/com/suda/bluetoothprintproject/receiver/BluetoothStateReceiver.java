package com.suda.bluetoothprintproject.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 接收藍芽改變的狀態
 */
public class BluetoothStateReceiver extends BroadcastReceiver
{
	private static final String TAG = "BluetoothStateReceiver";
	private boolean mBluetoothState = false;
	private final BluetoothStateCallback mBluetoothStateCallback;
	
	public BluetoothStateReceiver(BluetoothStateCallback bluetoothStateCallback) {
		this.mBluetoothStateCallback = bluetoothStateCallback;
	}
	
	public boolean GetBluetoothState() {
		return this.mBluetoothState;
	}
	
	@Override public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) // 接收藍芽連接狀態變換
		{
			int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			switch(blueState) {
				case BluetoothAdapter.STATE_ON:
					this.mBluetoothState = true;
//					Log.e(TAG, "藍芽 STATE_ON");
					break;
				case BluetoothAdapter.STATE_OFF:
					this.mBluetoothState = false;
//					Log.e(TAG, "藍芽 STATE_OFF");
					break;
				case BluetoothAdapter.ERROR:
					this.mBluetoothState = false;
//					Log.e(TAG, "監聽器設定錯誤");
					break;
			}
			
			this.mBluetoothStateCallback.bluetoothStateCallback(this.mBluetoothState);
		}
	}
	
	public interface BluetoothStateCallback {
		void bluetoothStateCallback(boolean blueState);
	}
}
