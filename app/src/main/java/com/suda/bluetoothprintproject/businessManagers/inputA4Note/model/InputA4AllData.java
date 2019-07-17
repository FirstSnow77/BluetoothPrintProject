package com.suda.bluetoothprintproject.businessManagers.inputA4Note.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 *
 */
public class InputA4AllData implements Serializable
{
	/**
	 * 寄件人
	 */
	public InputA4TargetPeople sendPeople;
	
	/**
	 * 收件人
	 */
	public InputA4TargetPeople receivePeople;
	
	/**
	 * 商品名稱
	 */
	public String goodsName;
	
	/**
	 * 訂單編號
	 */
	public String orderId;
	
	/**
	 * 備註
	 */
	public String note;
	
	/**
	 * 收貨日
	 */
	public Date sendDate;
	
	/**
	 * 希望收貨日
	 */
	public Date hopeReceiveDate;
	
	/**
	 * 希望到達時間
	 */
	public String hopeArriveTime;
	
	/**
	 * 溫層
	 */
	public String temperate;
	
	/**
	 * 報值金額
	 */
	public int productPrice;
	
	/**
	 * 代收金額
	 */
	public int carryPrice;
	
	/**
	 * 易碎物品
	 */
	public boolean easyBrokeItem;
	
	/**
	 * 精密儀器
	 */
	public boolean precisionItem;
	
	/**
	 * 前在流程上都無作用, 程式留著而已
	 */
	public String packageSize;
	
	/**
	 * 託運單號
	 */
	private ArrayList<String> mWaybillNumber = new ArrayList<>();
	
	/**
	 * 託運單對應的 QR - Code (每組 QR-CODE 都有對應的 託運單號)
	 */
	private HashMap<String, Bitmap> mBitmap_QR_CODE = new HashMap<>();
	
	
	public void SetData(String goodsName, String orderId, String note, Date sendDate, Date receiveDate,
			String hopeArriveTime, String temperate, int productPrice, int carryPrice, String packageSize, boolean easyBrokeItem, boolean precisionItem) {
		this.goodsName = goodsName;
		this.orderId = orderId;
		this.note = note;
		this.sendDate = sendDate;
		this.hopeReceiveDate = receiveDate;
		this.hopeArriveTime = hopeArriveTime;
		this.temperate = temperate;
		this.productPrice = productPrice;
		this.carryPrice = carryPrice;
		this.packageSize = packageSize;
		this.easyBrokeItem = easyBrokeItem;
		this.precisionItem = precisionItem;
	}
	
	/**
	 * @return 取得託運單號
	 */
	public ArrayList<String> getWaybillNumber() {
		return mWaybillNumber;
	}
	
	/**
	 * @param mWaybillNumber 託運單號的資料
	 */
	public void setWaybillNumber(ArrayList<String> mWaybillNumber) {
		this.mWaybillNumber.clear();
		this.mWaybillNumber = mWaybillNumber;
	}
	
	/**
	 * @return 取得 QrCode
	 */
	public HashMap<String, Bitmap> getBitmap_QR_CODE() {
		return mBitmap_QR_CODE;
	}
	
	/**
	 * @param bitmap_QR_CODE QR CODE 的資料
	 */
	public void setBitmap_QR_CODE(HashMap<String, Bitmap> bitmap_QR_CODE) {
		this.mBitmap_QR_CODE.clear();
		this.mBitmap_QR_CODE = bitmap_QR_CODE;
	}
}
