//package com.suda.bluetoothprintproject.receiver;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//
//// 網路狀態的監聽, 最主要是為了讓使用 http 請求的類別, 可以監聽網路連線的狀態
//// 目前可能會不使用
//public class NetworkChangeReceiver extends BroadcastReceiver
//{
//	INetworkChangeReceiver iNetworkChangeReceiver;
//	private final static int TYPE_WIFI = 1;
//	private final static int TYPE_MOBILE = 2;
//	private final static int TYPE_NOT_CONNECTED = 0;
//
//	public NetworkChangeReceiver(INetworkChangeReceiver iNetworkChangeReceiver) {
//		super();
//		this.iNetworkChangeReceiver = iNetworkChangeReceiver;
//	}
//
//	// 取得網路連線狀態代碼
//	private int getConnectivityStatus(Context context) {
//		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//		if(null != activeNetwork) {
//			switch(activeNetwork.getType()) {
//				case ConnectivityManager.TYPE_WIFI:
//					return TYPE_WIFI;
//				case ConnectivityManager.TYPE_MOBILE:
//					return TYPE_MOBILE;
//			}
//		}
//		return TYPE_NOT_CONNECTED;
//	}
//
//	// 可否連線?
//	private boolean isConnected(Context context) {
//		int conn = this.getConnectivityStatus(context);
//		return conn != TYPE_NOT_CONNECTED;
//	}
//
//	@Override public void onReceive(Context context, Intent intent) {
//		boolean isConnected = this.isConnected(context);
//		if(this.iNetworkChangeReceiver != null)
//			if(isConnected)
//				this.iNetworkChangeReceiver.isConnected();
//			else
//				this.iNetworkChangeReceiver.isNotConnected();
//	}
//}
