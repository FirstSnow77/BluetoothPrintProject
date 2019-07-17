package com.suda.bluetoothprintproject.businessManagers.businessService.printerControl;

import android.content.Context;

import com.bxl.config.editor.BXLConfigLoader;
import com.suda.bluetoothprintproject.businessManagers.businessService.BaseBluetoothPrintService;

import org.jetbrains.annotations.NotNull;

public class R220BluetoothService extends BaseBluetoothPrintService implements PrinterLogCallback
{
	private static final String R220Type = "R220";
	private static BixolonPrinter bxlPrinter = null;
	private final int portType = BXLConfigLoader.DEVICE_BUS_BLUETOOTH;
	private final String logicalName = "SPP-R220";
	private final Context mContext;
	private final PrinterLogCallback mPrinterLogCallback;
	
	public R220BluetoothService(Context context, PrinterLogCallback printerLogCallback) {
		// 註冊廣播服務
		super(PairBluetoothTarget.R220, R220Type, context);
		this.mContext = context;
		bxlPrinter = new BixolonPrinter(this);
		this.mPrinterLogCallback = printerLogCallback;
		super.setPairedDevices(); // 先行配對設備的連線
	}
	
	/**
	 * 連線到藍芽印表機
	 *
	 * @return 連線是否成功?
	 */
	@Override public boolean ConnectBluePrinter() {
		
		if(!super.checkFileAndSetDeviceData())
			super.setPairedDevices();
		
		if(super.GetDeviceName().isEmpty() || super.GetDeviceAddress().isEmpty())
			return false;
		
		// 第四個參數決定列印是否非同步
		boolean result = bxlPrinter.printerOpen(this.portType, this.logicalName, super.GetDeviceAddress(), true);
		
		if(result)
			userWritePairedBluetoothPrint(super.GetDeviceName(), super.GetDeviceAddress(), super.pairBluetoothTarget, this.mContext);
		else
			deletePairedBluetoothDeviceFile(super.pairBluetoothTarget, this.mContext);
		
		return result;
		
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
		if(bxlPrinter.CheckIsConnPrint())
			bxlPrinter.printerClose();
		
		final int maxTimes = 3; // 重新連線最大次數
		
		if(super.checkFileAndSetDeviceData()) // 若已有檔案, 則只能透過檔案內指定的設備來列印. 除非重新按下 "藍芽印表機連線按鈕"
		{
			boolean conn1Result = bxlPrinter.printerOpen(this.portType, this.logicalName, super.GetDeviceAddress(), true);
			int times = 1;
			while(times < maxTimes && !conn1Result) {
				conn1Result = bxlPrinter.printerOpen(this.portType, this.logicalName, super.GetDeviceAddress(), true);
				times++;
			}
		}
		else {
			boolean conn2Result = ConnectBluePrinter();
			int times = 1;
			while(times < maxTimes && !conn2Result) {
				conn2Result = ConnectBluePrinter();
				times++;
			}
		}
		
		if(bxlPrinter.CheckIsConnPrint()) {
			int times = printQuantity;
			if(printQuantity < 1 || printQuantity > 10)
				times = 1;
			bxlPrinter.beginTransactionPrint();
			for(int i = 0; i < times; i++) {
				bxlPrinter.printText(title + "\n", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.ATTRIBUTE_FONT_C, 3);
				bxlPrinter.printText("\n", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.ATTRIBUTE_NORMAL, 1);
				bxlPrinter.printBarcode(contentBarcode, BixolonPrinter.BARCODE_TYPE_Code39, 2, 140, BixolonPrinter.ALIGNMENT_CENTER,
						BixolonPrinter.BARCODE_HRI_NONE);
				bxlPrinter.printText("\n", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.ATTRIBUTE_NORMAL, 1);
				bxlPrinter.printText(underText + "\n", BixolonPrinter.ALIGNMENT_LEFT, BixolonPrinter.ATTRIBUTE_BOLD, 1);
				bxlPrinter.formFeed();
			}
			boolean result = bxlPrinter.endTransactionPrint();
			try // 等待印表機完成
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
			bxlPrinter.printerClose();
			return result;
		}
		
		return false;
	}
	
	@Override public void Destroy() {
		bxlPrinter.printerClose();
	}
	
	@Override public void printerLogCallback(String log) {
		if(this.mPrinterLogCallback != null)
			this.mPrinterLogCallback.printerLogCallback(log);
	}
	
	@Override public Context getCurrentContext() {
		return this.mContext;
	}
}
