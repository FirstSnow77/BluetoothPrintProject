package com.suda.bluetoothprintproject.businessManagers.bluetoothPrint;

import com.suda.bluetoothprintproject.api.model.PostalNumber;
import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;

/**
 * 郵號查詢相關的 API
 */
public interface IBlePrintApiManager
{
	/**
	 * 取得七碼郵號 (含 dash)
	 *
	 * @param customerId 客代
	 * @param address    查詢地址
	 * @param callback   callback
	 */
	void get7AddressResult(String customerId, String address, AllBusinessCallback.onDAOCallback<PostalNumber> callback);
	
	/**
	 * 取得完整理貨區碼(含五碼郵號, no dash)
	 *
	 * @param customerId 客代
	 * @param address 查詢地址
	 * @param callback callback
	 */
	void getAreaCode(String customerId, String address, AllBusinessCallback.onDAOCallback<PostalNumber> callback);
	
	
	/**
	 * 傳送使用者資訊
	 */
	void sendUserInfo();
}
