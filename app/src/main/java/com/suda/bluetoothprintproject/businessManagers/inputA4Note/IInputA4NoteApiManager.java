package com.suda.bluetoothprintproject.businessManagers.inputA4Note;

import android.graphics.Bitmap;

import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.model.BCustomerProfile;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.model.InputA4AllData;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 託運單 api 相關的
 */
public interface IInputA4NoteApiManager
{
	/**
	 * 依客戶代號取得客代相關資料
	 *
	 * @param bCustomerId 契客代號
	 * @param callback    callback
	 */
	void getBCustomerProfile(String bCustomerId, AllBusinessCallback.onDAOCallback<BCustomerProfile> callback);
	
	/**
	 * 目前 step1 取得所需的資料 (未來會將此方法拆分得更細, 以滿足未來 step 異動時的需求)
	 *
	 * @param inputA4AllData 以驗證過的資料(並且直接使用 reference 去修改物件中的資料)
	 * @param callback       callback
	 */
	void setStep1NextData(InputA4AllData inputA4AllData, AllBusinessCallback.onCallback callback);
	
	/**
	 * 取得託運單號
	 *
	 * @param customerId  客戶代號 (目前都用 all 0 )
	 * @param waybillType 託運單類型 A | B (一般 | 代收)
	 * @param count       託運單號數量
	 * @param callback    callback
	 */
	void getWaybillNumber(String customerId, String waybillType, int count,
			AllBusinessCallback.onDAOCallback<ArrayList<String>> callback);
	
	
	/**
	 * 依據相關資料組出對應的 QR CODE 集合
	 *
	 * @param inputA4AllData 以驗證過的資料(包含託運單號) (並且直接使用 reference 去修改物件中的資料)
	 * @param printInvoice   是否需要列印?
	 * @param printDonation  是否捐贈?
	 * @param invoiceUni     統編
	 * @param common         載具
	 * @param callback       callback
	 */
	void getQRCodeBitmap(InputA4AllData inputA4AllData, String printInvoice, String printDonation, String invoiceUni, String common,
			AllBusinessCallback.onDAOCallback<HashMap<String, Bitmap>> callback);
	
	/**
	 * 透過 EGS 傳 OBT 資料
	 *
	 * @param inputA4AllData 以驗證過的資料(包含託運單號)
	 */
	void sendOBTData(InputA4AllData inputA4AllData);
}
