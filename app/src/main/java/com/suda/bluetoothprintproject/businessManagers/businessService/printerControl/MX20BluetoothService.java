package com.suda.bluetoothprintproject.businessManagers.businessService.printerControl;

import android.content.Context;
import android.util.Log;

import com.godex.Godex;
import com.suda.bluetoothprintproject.businessManagers.businessService.BaseBluetoothPrintService;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * 管理 MX20 的列印服務
 */
public class MX20BluetoothService extends BaseBluetoothPrintService
{
	private static final String TAG = "MX20BluetoothService";
	private static final String MX20Type = "MX20";
	
	private final Context mContext;
	private boolean mConnectedFlag = false; // 目前印表機是否連線
	
	public MX20BluetoothService(Context context)
	{
		super(PairBluetoothTarget.MX20, MX20Type, context);
		this.mContext = context;
		// Godex.Insb = true; // 開啟加密連線. sdk 版本. > sdk2.3
		Godex.debug(3); // 選擇 log 輸出模式.
		super.setPairedDevices();
	}
	
	@Override public boolean ConnectBluePrinter() {
		if(this.mConnectedFlag)
		{
			this._DisconnectBluePrinter();
			deletePairedBluetoothDeviceFile(super.pairBluetoothTarget, this.mContext);
			return false;
		}
		
		// 若沒有寫入過資料的話先行配對
		if(!super.checkFileAndSetDeviceData())
			super.setPairedDevices();
		
		if(super.GetDeviceName().isEmpty() || super.GetDeviceAddress().isEmpty())
			return false;
		
		this.mConnectedFlag = Godex.open(super.GetDeviceAddress(), 2); // 連接設備, 使用 Bluetooth.
		if(this.mConnectedFlag)
		{
			// MX20: 203 dpi (8 dots/mm)
			//參數依序為: 標籤高度mm, 列印黑度0~19, 列印速度 2 ~ 7, 紙張類型 0(標籤 | 1(連續, 標籤間距mm, 黑線標記距離 mm.
			Godex.setup("50", "10", "3", "0", "0", "Not");
			userWritePairedBluetoothPrint(super.GetDeviceName(), super.GetDeviceAddress(), super.pairBluetoothTarget, this.mContext);
		}
		else
		{
			deletePairedBluetoothDeviceFile(super.pairBluetoothTarget, this.mContext);
		}
		
		return this.mConnectedFlag;
	}
	
	/**
	 * 傳送欲列印資料到印表機
	 * @param title 標題
	 * @param contentBarcode 條碼內容
	 * @param underText 底部文字
	 * @param printQuantity 列印數量
	 * @return 是否列印成功?
	 */
	@Override
	public boolean SandStrToBluePrinter(@NotNull String title, @NotNull String contentBarcode, @NotNull String underText, int printQuantity) {
		if(mConnectedFlag) this._DisconnectBluePrinter();
		final int maxTimes = 3; // 重新連線最大次數
		if(super.checkFileAndSetDeviceData())
		{
			mConnectedFlag = Godex.open(super.GetDeviceAddress(), 2);
			int times = 1;
			while(times < maxTimes && !mConnectedFlag)
			{
				mConnectedFlag = Godex.open(super.GetDeviceAddress(), 2);
				times++;
			}
			if(mConnectedFlag)
				Godex.setup("50", "10", "3", "0", "0", "Not");
		}
		else
		{
			ConnectBluePrinter();
			int times = 1;
			while(times < maxTimes && !mConnectedFlag)
			{
				ConnectBluePrinter();
				times ++;
			}
		}
		
		if(mConnectedFlag)
		{
			// 列印張數設置
			String printQty;
			if(printQuantity < 1 || printQuantity > 10) printQty = "^P1";
			else printQty = "^P" + String.valueOf(printQuantity).trim();
			String data = printQty + "\r\n" + "^L" + "\r\n" + "AT,0,0,72,72,8,0B,0,0," + title + "\r\n" +
					              "BA,16,96,2,6,168,0,0," + contentBarcode.trim() + "\r\n" +
					              "ATA,0,292,23,23,0,0BE,A,0," + underText.trim() + "\r\n" + "E";
			// 若想看送至印表機的指令維和的話使用 ~S,DUMP 可以印出命令. 要退出該模式按 Feed 鍵
			boolean result = Godex.sendCommand(data);
			try // 等待印表機完成
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			this._DisconnectBluePrinter();
			return result;
		}
		
		return false;
		
	}
	
	@Override public void Destroy() {
		this._DisconnectBluePrinter();
	}
	
	private void _DisconnectBluePrinter()
	{
		try
		{
			if(this.mConnectedFlag)
			{
				Godex.close();
			}
		}
		catch (IOException e)
		{
			Log.e(TAG, "_DisconnectBluePrinter: 關閉藍芽連線時發生錯誤", e);
			e.printStackTrace();
		}
		finally
		{
			this.mConnectedFlag = false;
		}
	}
}
