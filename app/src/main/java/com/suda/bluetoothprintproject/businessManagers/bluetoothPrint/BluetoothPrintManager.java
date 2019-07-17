package com.suda.bluetoothprintproject.businessManagers.bluetoothPrint;

import android.annotation.SuppressLint;
import android.content.Context;

import com.suda.bluetoothprintproject.api.dataManager.ApiDataManager;
import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;
import com.suda.bluetoothprintproject.businessManagers.BaseManager;
import com.suda.bluetoothprintproject.businessManagers.account.AccountManager;
import com.suda.bluetoothprintproject.businessManagers.businessService.IBlueToothService;
import com.suda.bluetoothprintproject.businessManagers.businessService.printerControl.MX20BluetoothService;
import com.suda.bluetoothprintproject.businessManagers.businessService.printerControl.PrinterLogCallback;
import com.suda.bluetoothprintproject.businessManagers.businessService.printerControl.R220BluetoothService;
import com.suda.bluetoothprintproject.api.model.PostalNumber;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 藍芽印表機列印相關
 */
public class BluetoothPrintManager extends BaseManager implements IBlePrintApiManager, IBlePrintManager
{
	public static final String MX20Type = "MX20";
	public static final String R220Type = "R220";
	
	private IBlueToothService mBlueService;
	private final PrinterControlLog mPrinterControlLog;
	
	public BluetoothPrintManager(Context context) {
		super(context);
		this.mPrinterControlLog = null;
	}
	
	public BluetoothPrintManager(Context context, String printType, PrinterControlLog callback) throws Exception {
		super(context);
		this.mPrinterControlLog = callback;
		
		switch(printType) {
			case MX20Type:
				this.mBlueService = new MX20BluetoothService(context); // MX20
				break;
			case R220Type:
				this.mBlueService = new R220BluetoothService(context, new PrinterLogCallback()
				{
					@Override public void printerLogCallback(String log) {
						if(BluetoothPrintManager.this.mPrinterControlLog != null)
							BluetoothPrintManager.this.mPrinterControlLog.callbackPrinterLog(log);
					}
					
					@Override public Context getCurrentContext() {
						return null;
					}
				}); // R220
				break;
			default:
				throw new Exception(String.format("%s%s", "BluetoothPrintManager 不支援此列印型別:", printType));
		}
	}
	
	/**
	 * 取得七碼郵號 (含 dash)
	 *
	 * @param customerId 客代
	 * @param address    查詢地址
	 * @param callback   callback
	 */
	@Override
	public void get7AddressResult(String customerId, String address, final AllBusinessCallback.onDAOCallback<PostalNumber> callback) {
		BluetoothPrintManager.super.mApiDataManager.getAddressResult("000000000000", address, new ApiDataManager.onDataCallback<PostalNumber>()
		{
			@Override public void onGetDataSuccess(PostalNumber data) {
				if(data != null && data.Code.trim().equals("0") && data.Data != null && !data.Data.trim().isEmpty())
					callback.onSuccess(data);
				
				else
					callback.onFailure();
			}
			
			@Override public void onGetDataFailure() {
				callback.onFailure();
			}
		});
	}
	
	/**
	 * 取得完整理貨區碼(含五碼郵號, no dash)
	 *
	 * @param customerId 客代
	 * @param address    查詢地址
	 * @param callback   callback
	 */
	@Override
	public void getAreaCode(String customerId, String address, final AllBusinessCallback.onDAOCallback<PostalNumber> callback) {
		super.mApiDataManager.getAreaCodeResult("000000000000", address, new ApiDataManager.onDataCallback<PostalNumber>()
		{
			@Override public void onGetDataSuccess(PostalNumber data) {
				if(data != null && data.Code.trim().equals("0") && data.Data != null && !data.Data.trim().isEmpty())
					callback.onSuccess(data);
				else
					callback.onFailure();
			}
			
			@Override public void onGetDataFailure() {
				callback.onFailure();
			}
		});
	}
	
	@Override public void sendUserInfo() {
		String account = AccountManager.getUserAccount(this.mContext);
		this.mApiDataManager.postUserInfo(account, String.valueOf(android.os.Build.VERSION.SDK_INT),
				"Android", "selectAddress");
	}
	
	/**
	 * @return 取得設備名稱
	 */
	@Override public String getDeviceName() {
		return this.mBlueService.GetDeviceName();
	}
	
	/**
	 * 連線至印表機
	 *
	 * @param callback callback
	 */
	@Override public void connectBluePrinter(final AllBusinessCallback.onCallback callback) {
		super.runUiThreadSetNewLooper(new runUiThreadAndDoSomething()
		{
			@Override public void doSomething() {
				if(BluetoothPrintManager.this.mBlueService.ConnectBluePrinter())
					callback.onSuccess();
				else
					callback.onFailure();
			}
		});
	}
	
	/**
	 * 傳送欲列印資料到印表機
	 *
	 * @param title          標題
	 * @param contentBarcode 條碼內容
	 * @param underAddress   底部地址
	 * @param printQuantity  列印數量
	 * @param callback       callback
	 */
	@Override
	public void sandStrToBluePrinter(@NotNull final String title, @NotNull final String contentBarcode, @NotNull final String underAddress
			, final int printQuantity, final AllBusinessCallback.onCallback callback) {
		super.runUiThreadSetNewLooper(new runUiThreadAndDoSomething()
		{
			@Override public void doSomething() {
				@SuppressLint("SimpleDateFormat") String underText = String.format("%s%s%s",
						new SimpleDateFormat("MM/dd").format(new Date()), " ", mControllerStrLength(underAddress));
				if(BluetoothPrintManager.this.mBlueService.SandStrToBluePrinter(title, contentBarcode, underText, printQuantity))
					callback.onSuccess();
				else
					callback.onFailure();
			}
		});
	}
	
	
	/**
	 * @param str 原始字串
	 * @return 擷取地址的長度, 由後往前擷取 10 碼.
	 */
	private String mControllerStrLength(@NotNull String str) {
		String input = str.trim();
		int more = input.length() - 10;
		if(more <= 0) return input;
		else return input.substring(more);
	}
	
	@Override public void destroy() {
		this.mBlueService.Destroy();
		this.mBlueService = null;
	}
	
	@Override public boolean switchBluetoothDevice(Boolean turnOrOff) {
		return this.mBlueService.SwitchBluetoothDevice(turnOrOff);
	}
	
	/**
	 * 針對印表機狀態返回的 log
	 */
	public interface PrinterControlLog
	{
		void callbackPrinterLog(String printerLog);
	}
}
