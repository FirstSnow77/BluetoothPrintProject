package com.suda.bluetoothprintproject.businessManagers.inputA4Note;

import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;

import java.util.ArrayList;

/**
 * 產生地址下拉是選單，所需的資料
 */
public interface IAddressListApiManager
{
	/**
	 * 產生對應的地址下拉式選單資料
	 * @param level 欲查詢層級
	 * @param city 城市
	 * @param district 區域
	 * @param callback callback
	 */
	void getAddressList(int level, String city, String district, AllBusinessCallback.onDAOCallback<ArrayList<String>> callback);
}
