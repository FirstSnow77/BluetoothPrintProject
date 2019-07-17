package com.suda.bluetoothprintproject.businessManagers.businessService;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

/**
 * 所有藍芽印表機的基底類，抽出基礎功能
 */
public abstract class BaseBluetoothPrintService implements IBlueToothService
{
	protected enum PairBluetoothTarget
	{
		R220, MX20
	}
	private final static String PAIRED_R220_BLUETOOTH_FILE_NAME = "user_paired_r220_bluetooth.txt"; // 使用者配對且最後連線 R220 的檔案名稱
	private final static String PAIRED_MX20_BLUETOOTH_FILE_NAME = "user_paired_mx20_bluetooth.txt"; // 使用者配對且最後連線 MX20 的檔案名稱
	
	private final String TAG;
	private final String mDeviceStartName;
	private final Context mContext;
	private String deviceName = "";
	private String deviceAddress = "";
	protected final PairBluetoothTarget pairBluetoothTarget; // 配對產出文檔的選擇.
	
	/**
	 * 建構式
	 * @param pairBluetoothTarget 藍芽印表機的配對目標
	 * @param deviceStartName 對應的藍芽印表機，開頭字串
	 * @param context 當前 context
	 */
	protected BaseBluetoothPrintService(PairBluetoothTarget pairBluetoothTarget, String deviceStartName, Context context)
	{
		this.TAG = "BaseBluetoothPrintService" + deviceStartName;
		this.mContext = context;

		this.pairBluetoothTarget = pairBluetoothTarget;
		this.mDeviceStartName = deviceStartName;
	}
	
	
	/**
	 * 依據裝置目前配對的設備, 第一台符合的配置儲存 "設備名稱 & 設備 IP".
	 */
	protected void setPairedDevices()
	{
		if(this.SwitchBluetoothDevice(true))
		{
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			Set<BluetoothDevice> bondedDeviceSet = bluetoothAdapter.getBondedDevices();
			for(BluetoothDevice device : bondedDeviceSet)
			{
				if(device.getName().startsWith(mDeviceStartName))
				{
					this.deviceName = device.getName();
					this.deviceAddress = device.getAddress();
					return;
				}
			}
			this.deviceName = "";
			this.deviceAddress = "";
		}
	}
	
	/**
	 *  檢查是否有寫入過配對資料, 若有則將資訊寫入到 deviceName && deviceAddress
	 * @return 是否寫入過配對資料?
	 */
	protected boolean checkFileAndSetDeviceData()
	{
		boolean result = isPairedBluetoothDeviceFile(pairBluetoothTarget, this.mContext);
		if(result) // 若有寫入過檔案, 只連線該台設備.
		{
			String[] data = GetUserPairedBluetooth(pairBluetoothTarget, this.mContext);
			if(data != null && data.length == 2)
			{
				this.deviceName = data[ 0 ];
				this.deviceAddress = data[ 1 ];
			}
		}
		else
		{
			this.deviceName = "";
			this.deviceAddress = "";
		}
		return result;
	}
	
	protected String GetDeviceAddress()
	{
		return this.deviceAddress;
	}
	
	public boolean SwitchBluetoothDevice(Boolean turnOrOff)
	{
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(bluetoothAdapter == null)
		{
			Log.e(TAG, "switchBluetoothDevice: 此設備無藍芽");
			return false;
		}
		else
		{
			if(turnOrOff)
				bluetoothAdapter.enable();
			else
				bluetoothAdapter.disable();
			return true;
		}
	}
	
	public String GetDeviceName()
	{
		return this.deviceName;
	}
	
	// 寫入使用者配對的藍芽印表機資訊
	protected synchronized static void userWritePairedBluetoothPrint(String printName, String printAddress, PairBluetoothTarget deviceTarget, Context context)
	{
		try
		{
			String targetFileName = "";
			switch(deviceTarget)
			{
				case R220:
					targetFileName = PAIRED_R220_BLUETOOTH_FILE_NAME;
					break;
				case MX20:
					targetFileName = PAIRED_MX20_BLUETOOTH_FILE_NAME;
					break;
			}
			FileOutputStream outputStream = context.openFileOutput(targetFileName, Context.MODE_PRIVATE); // app 內部空間
			outputStream.write(String.format("%s%s%s", printName, ",", printAddress).getBytes()); // 若有資料的話則覆蓋原資料
			outputStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// 使用者是否已經寫入過藍芽配對的檔案?
	protected synchronized static boolean isPairedBluetoothDeviceFile(PairBluetoothTarget deviceTarget, Context context)
	{
		String targetFileName = "";
		switch(deviceTarget)
		{
			case R220:
				targetFileName = PAIRED_R220_BLUETOOTH_FILE_NAME;
				break;
			case MX20:
				targetFileName = PAIRED_MX20_BLUETOOTH_FILE_NAME;
				break;
		}
		File file = new File(context.getFilesDir(), targetFileName);
		return file.exists();
	}
	
	protected synchronized static void deletePairedBluetoothDeviceFile(PairBluetoothTarget deviceTarget, Context context)
	{
		String targetFileName = "";
		switch(deviceTarget)
		{
			case R220:
				targetFileName = PAIRED_R220_BLUETOOTH_FILE_NAME;
				break;
			case MX20:
				targetFileName = PAIRED_MX20_BLUETOOTH_FILE_NAME;
				break;
		}
		File file = new File(context.getFilesDir(), targetFileName);
		if(file.exists())
			file.delete();
	}
	
	// 取得使用者上次寫入的資料, [2] => 設備名稱, 設備IP
	protected synchronized static String[] GetUserPairedBluetooth(PairBluetoothTarget deviceTarget, Context context)
	{
		String targetFileName = "";
		switch(deviceTarget)
		{
			case R220:
				targetFileName = PAIRED_R220_BLUETOOTH_FILE_NAME;
				break;
			case MX20:
				targetFileName = PAIRED_MX20_BLUETOOTH_FILE_NAME;
				break;
		}
		try
		{
			FileInputStream inputStream = context.openFileInput(targetFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			
			if(( line = reader.readLine() ) == null) // 都只寫入一行
				return null;
			
			reader.close();
			inputStream.close();
			
			return line.split(",");
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
