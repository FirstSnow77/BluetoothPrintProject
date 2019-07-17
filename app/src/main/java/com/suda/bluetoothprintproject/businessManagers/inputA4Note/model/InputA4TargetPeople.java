package com.suda.bluetoothprintproject.businessManagers.inputA4Note.model;

import java.io.Serializable;

/**
 * 存放A4 託運單，寄件人 || 收件人 的資料
 */
public class InputA4TargetPeople implements Serializable
{
	public String name;
	public String telephone;
	public String cellphone;
	public String address;
	public String B_CustomerID; // 要 B 客, 且寄件人才有.
	
	/**
	 * 地址取郵號
	 */
	public String poNumber = "";
	
	/**
	 * 地址取郵號(no dash的)
	 */
	public String poNotDashNumber = "";
	
	/**
	 * base 號碼
	 */
	public String baseNumber = "";
	
	/**
	 * 理貨區碼
	 */
	public String areaCode = "";
	
	/**
	 * 依據客代查詢資料, 判斷是否可有代收單
	 */
	public String W100_IS_CLC = "0";
	
	public InputA4TargetPeople() {
	}
	
	public InputA4TargetPeople(String name, String telephone, String cellphone, String address, String b_CustomerID,
			String W100_IS_CLC) {
		this.name = name;
		this.telephone = telephone;
		this.cellphone = cellphone;
		this.address = address;
		this.B_CustomerID = b_CustomerID;
		this.W100_IS_CLC = W100_IS_CLC;
	}
}