package com.suda.bluetoothprintproject.businessManagers.businessService;

import android.support.annotation.IntRange;

import org.jetbrains.annotations.NotNull;

/**
 * 任何印表機服務的類，都需要實作此介面
 */
public interface IBlueToothService
{
	/**
	 * @return 當前連線的藍芽印表機名稱
	 */
	String GetDeviceName();
	
	/**
	 * 連線藍芽印表機
	 * @return 是否連線成功
	 */
	boolean ConnectBluePrinter();
	
	/**
	 * 傳送欲列印資料到印表機
	 * @param title 標題
	 * @param contentBarcode 條碼內容
	 * @param underText 底部文字
	 * @param printQuantity 列印數量
	 * @return 是否列印成功?
	 */
	boolean SandStrToBluePrinter(@NotNull String title, @NotNull String contentBarcode, @NotNull String underText,
			@IntRange int printQuantity);
	
	/**
	 * 記得管控 此物件生命週期(同 Activity 即可)
	 */
	void Destroy();
	
	/**
	 * @param turnOrOff 設置藍芽開啟/關閉
	 * @return 是否成功?
	 */
	boolean SwitchBluetoothDevice(Boolean turnOrOff);
}
