package com.suda.bluetoothprintproject.businessManagers;


import java.io.Serializable;

/**
 * 集中存放所有商業邏輯層的 callback 介面
 */
public class AllBusinessCallback
{
	/**
	 * 在商業邏輯層，沒有資料往返的 callBack
	 */
	public interface onCallback
	{
		void onSuccess();
		
		void onFailure();
	}
	
	/**
	 * 在商業邏輯層，字串資料往返的 callBack
	 */
	public interface onStrginCallback
	{
		void onSuccess(String someData);
		
		void onFailure();
	}
	
	/**
	 * 在商業邏輯層，DAO 資料往返的 callBack
	 */
	public interface onDAOCallback<T extends Serializable>
	{
		void onSuccess(T daoData);
		
		void onFailure();
	}
}
