package com.suda.bluetoothprintproject.businessManagers.bluetoothPrint;

import android.support.annotation.IntRange;

import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;

import org.jetbrains.annotations.NotNull;

/**
 * 這是為了讓 activity 使用時，更清楚知道要使用哪些方法所建立的 interface(其實就是 DIP )
 */
public interface IBlePrintManager
{
	/**
	 * @return 當前連線的藍芽印表機名稱
	 */
	String getDeviceName();
	
	/**
	 * 連線到印表機
	 *
	 * @param callback callback
	 */
	void connectBluePrinter(AllBusinessCallback.onCallback callback);
	
	/**
	 * 傳送欲列印資料到印表機
	 *
	 * @param title          標題
	 * @param contentBarcode 條碼內容
	 * @param underText      底部文字
	 * @param printQuantity  列印數量
	 * @param callback       callback
	 */
	void sandStrToBluePrinter(@NotNull String title, @NotNull String contentBarcode, @NotNull String underText,
			@IntRange int printQuantity, AllBusinessCallback.onCallback callback);
	
	/**
	 * @param turnOrOff 設置藍芽開啟/關閉
	 * @return 是否成功?
	 */
	boolean switchBluetoothDevice(Boolean turnOrOff);
	
	/**
	 * 記得管控 此物件生命週期(同 Activity 即可)
	 */
	void destroy();
}
