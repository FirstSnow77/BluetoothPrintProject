package com.suda.bluetoothprintproject.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.suda.bluetoothprintproject.R;
import com.suda.bluetoothprintproject.api.model.PostalNumber;
import com.suda.bluetoothprintproject.baseActivity.BaseToolbarActivity;
import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;
import com.suda.bluetoothprintproject.businessManagers.bluetoothPrint.BluetoothPrintManager;
import com.suda.bluetoothprintproject.businessManagers.bluetoothPrint.IBlePrintApiManager;
import com.suda.bluetoothprintproject.businessManagers.bluetoothPrint.IBlePrintManager;
import com.suda.bluetoothprintproject.receiver.BluetoothStateReceiver;
import com.suda.bluetoothprintproject.utils.ToastUtil;
import com.suda.bluetoothprintproject.widget.AddSubView;

import java.util.ArrayList;

public class BluetoothActivity extends BaseToolbarActivity implements View.OnClickListener
{
	private String TAG = BluetoothActivity.class.getSimpleName();
	
	private ImageView mBlueStatus;
	private Button mConnBluePrinterBtn;
	private Button mPrintToBluePrinterBtn;
	
	private AddSubView mAddSubView; // 加減號物件 部分檢視
	
	private IBlePrintManager mBlePrintManager = null;
	private IBlePrintApiManager mBlePrintApiManager = null;
	private static BluetoothStateReceiver mBluetoothStateReceiver;
	
	// 存儲資料用
	private String mSuccessSearchAddress = ""; // 如果地址搜尋到的 address 是正確的, 則儲存中文地址於此.
	
	// 第三方列印套件使用
	private TextView deviceMessagesTextView;
	
	private final Handler mHandler = new Handler(new Handler.Callback()
	{
		@Override
		public boolean handleMessage(Message msg) {
			switch(msg.what) {
				case 0: // 接收到印表機服務回傳的 log
					String log = (String) msg.obj;
					if(log != null)
						deviceMessagesTextView.append(log + "\n");
					else
						deviceMessagesTextView.append("Print log is null" + "\n");
					
					Layout layout = deviceMessagesTextView.getLayout();
					if(layout != null) {
						int y = layout.getLineTop(deviceMessagesTextView.getLineCount()) - deviceMessagesTextView.getHeight();
						
						if(y > 0) {
							deviceMessagesTextView.scrollTo(0, y);
							deviceMessagesTextView.invalidate();
						}
					}
					break;
				case 1:
					if((boolean) msg.obj) {
						BluetoothActivity.this.mBlueStatus.setImageResource(R.drawable.bluetooth_open);
						ToastUtil.showToast(BluetoothActivity.this, "藍芽開啟");
					}
					else {
						BluetoothActivity.this.mBlueStatus.setImageResource(R.drawable.bluetooth_close);
						ToastUtil.showToast(BluetoothActivity.this, "藍芽關閉");
					}
					break;
			}
			return false;
		}
	});
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_bluetooth);
		super.onCreate(savedInstanceState);
		
		super.checkUpdateWhenAppStartFromLauncher(new IfVersionIsNewest()
		{
			@Override public void doSomethingOnce() {
				BluetoothActivity.this.uiInit();
				
				mBluetoothStateReceiver = new BluetoothStateReceiver(new BluetoothStateReceiver.BluetoothStateCallback()
				{
					@Override public void bluetoothStateCallback(boolean blueState) { // 監聽手機藍芽的變換4
						BluetoothActivity.this.mHandler.obtainMessage(1, 0, 0, blueState).sendToTarget();
					}
				});
				
				IntentFilter blueState = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
				registerReceiver(mBluetoothStateReceiver, blueState);
				
				// 初始化藍芽列印管理器
				if(BluetoothActivity.this.mBlePrintManager == null || BluetoothActivity.this.mBlePrintApiManager == null) {
					try {
						final BluetoothPrintManager printManager  = new BluetoothPrintManager(
								BluetoothActivity.this,
								getIntent().getStringExtra("barcodePrintType"),
								new BluetoothPrintManager.PrinterControlLog() // 接收印表機服務的相關事件
								{
									@Override public void callbackPrinterLog(String printerLog) {
										BluetoothActivity.this.mHandler.obtainMessage(0, 0, 0, printerLog).sendToTarget();
									}
								});
						BluetoothActivity.this.mBlePrintManager = printManager;
						BluetoothActivity.this.mBlePrintApiManager = printManager;
					}
					catch(Exception ex) {
						Log.e(TAG, "印表機服務設定錯誤: ", ex);
						finish();
					}
				}
			}
		});
	}
	
	
	private void uiInit() {
		super.showTitle(getResources().getString(R.string.search_app_name));
		super.showBack();
		super.showLogout();
		super.showGoHome();
		
		this.mAddSubView = findViewById(R.id.add_sub_view); // 增加列印張數的 部分檢視
		
		// 將所有按鈕轉到屬性上
		this.mBlueStatus = findViewById(R.id.status_bluetooth);
		this.mConnBluePrinterBtn = findViewById(R.id.connect_bluetooth);
		this.mPrintToBluePrinterBtn = findViewById(R.id.print_blue_Printer);
		
		// 監聽
		this.mBlueStatus.setOnClickListener(this);
		findViewById(R.id.search_address).setOnClickListener(this);
		this.mConnBluePrinterBtn.setOnClickListener(this);
		this.mPrintToBluePrinterBtn.setOnClickListener(this);
		findViewById(R.id.clear_all).setOnClickListener(this);
		findViewById(R.id.address_voice_btn).setOnClickListener(this);
		
		// 藍芽印表機服務顯示 log
		deviceMessagesTextView = findViewById(R.id.textViewDeviceMessages);
		deviceMessagesTextView.setMovementMethod(new ScrollingMovementMethod());
		deviceMessagesTextView.setVerticalScrollBarEnabled(true);
		
		// 讓 editText 取得焦點時不彈出對話框, 而是到點擊時
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
	
	@Override public void onClick(View v) {
		super.onClick(v);
		switch(v.getId()) {
			case R.id.search_address:
				String input = ((EditText) findViewById(R.id.address_input)).getText().toString();
				this.searchAddressCode(input);
				this.mBlePrintApiManager.sendUserInfo();
				break;
			case R.id.status_bluetooth:
				if(BluetoothAdapter.getDefaultAdapter().isEnabled())
					this.mBlePrintManager.switchBluetoothDevice(false);
				else
					this.mBlePrintManager.switchBluetoothDevice(true);
				break;
			case R.id.connect_bluetooth:
				this.connectToPrinter();
				break;
			case R.id.print_blue_Printer:
				this.printUsePrint();
				break;
			case R.id.clear_all:
				((EditText) findViewById(R.id.address_input)).setText("");
				((EditText) findViewById(R.id.address_code)).setText("");
				((EditText) findViewById(R.id.address_no_dash_code)).setText("");
				this.mPrintToBluePrinterBtn.setEnabled(false);
				this.mAddSubView.setCurrentValue(1);
				break;
			case R.id.address_voice_btn:
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
				intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請說～");
				try {
					startActivityForResult(intent, 200);
				}
				catch(ActivityNotFoundException a) {
					ToastUtil.showToast(this, "請開啟語音輸入");
				}
				break;
		}
	}
	
	/**
	 * 1. 地址為空時不動作.
	 * 2. 查詢理貨區碼
	 * 3. 查詢七碼郵號
	 *
	 * @param address 查詢地址
	 */
	private void searchAddressCode(final String address) {
		if(address.trim().isEmpty()) return; // 為空時不跑
		super.showProgressDialog();
		
		// step 1 查詢理貨區碼
		this.mBlePrintApiManager.getAreaCode("000000000000", address, new AllBusinessCallback.onDAOCallback<PostalNumber>()
		{
			@Override public void onSuccess(PostalNumber daoData1) {
				final String areaCode = daoData1.GetAreaCode();
				// step2 查詢七碼郵號
				BluetoothActivity.this.mBlePrintApiManager.get7AddressResult("000000000000", address
						, new AllBusinessCallback.onDAOCallback<PostalNumber>()
						{
							@Override public void onSuccess(PostalNumber daoData2) {
								BluetoothActivity.super.dismissProgressDialog();
								((EditText) findViewById(R.id.address_code)).setText(String.format("%s%s", daoData2.Data, areaCode));
								((EditText) findViewById(R.id.address_no_dash_code)).setText(daoData2.GetNoAddressDashData());
								BluetoothActivity.this.mPrintToBluePrinterBtn.setEnabled(true);
								BluetoothActivity.this.mSuccessSearchAddress = address;
							}
							
							@Override public void onFailure() {
								BluetoothActivity.super.dismissProgressDialog();
								((EditText) findViewById(R.id.address_code)).setText("查無此郵號");
								((EditText) findViewById(R.id.address_no_dash_code)).setText("查無此郵號");
								BluetoothActivity.this.mPrintToBluePrinterBtn.setEnabled(false);
								BluetoothActivity.this.mSuccessSearchAddress = "";
							}
						});
			}
			
			@Override public void onFailure() {
				BluetoothActivity.super.dismissProgressDialog();
				((EditText) findViewById(R.id.address_code)).setText("查無此郵號");
				((EditText) findViewById(R.id.address_no_dash_code)).setText("查無此郵號");
				BluetoothActivity.this.mPrintToBluePrinterBtn.setEnabled(false);
				BluetoothActivity.this.mSuccessSearchAddress = "";
			}
		});
	}
	
	/**
	 * 連限制藍芽印表機
	 */
	private void connectToPrinter() {
		super.showProgressDialog();
		this.mBlePrintManager.connectBluePrinter(new AllBusinessCallback.onCallback()
		{
			@Override public void onSuccess() {
				BluetoothActivity.super.runUiThreadOnMainThread(new activityRunUiOnMainThread()
				{
					@Override public void runOnMainThread() {
						BluetoothActivity.super.dismissProgressDialog();
						String name = BluetoothActivity.this.mBlePrintManager.getDeviceName();
						((TextView) findViewById(R.id.connect_state_txt)).setText(name);
						ToastUtil.showToast(BluetoothActivity.this, "藍芽印表機連接成功");
						BluetoothActivity.super.dismissProgressDialog();
						BluetoothActivity.this.mConnBluePrinterBtn.setBackgroundColor(
								ContextCompat.getColor(BluetoothActivity.this, R.color.colorPrimaryDark));
					}
				});
			}
			
			@Override public void onFailure() {
				BluetoothActivity.super.runUiThreadOnMainThread(new activityRunUiOnMainThread()
				{
					@Override public void runOnMainThread() {
						BluetoothActivity.super.dismissProgressDialog();
						((TextView) findViewById(R.id.connect_state_txt)).setText("");
						ToastUtil.showToast(BluetoothActivity.this, "藍芽印表機連接失敗");
						BluetoothActivity.this.mConnBluePrinterBtn.setBackgroundColor(Color.RED);
					}
				});
			}
		});
	}
	
	private void printUsePrint() {
		String title = ((EditText) findViewById(R.id.address_code)).getText().toString();
		String barcodeContent = ((EditText) findViewById(R.id.address_no_dash_code)).getText().toString();
		if(!(title.trim().isEmpty()) && !(barcodeContent.trim().isEmpty()) && !(this.mSuccessSearchAddress.trim().isEmpty())) {
			super.showProgressDialog();
			this.mBlePrintManager.sandStrToBluePrinter(title, barcodeContent, this.mSuccessSearchAddress,
					this.mAddSubView.getCurrentValue(), new AllBusinessCallback.onCallback()
					{
						@Override public void onSuccess() {
							BluetoothActivity.super.dismissProgressDialog();
						}
						
						@Override public void onFailure() {
							BluetoothActivity.super.dismissProgressDialog();
							ToastUtil.showToast(BluetoothActivity.this, "請確認以連接上藍芽印表機");
						}
					});
		}
		else
			ToastUtil.showToast(this, "請先查詢到正確的郵號");
	}
	
	@Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == 200) {
			if(resultCode == RESULT_OK && data != null) {
				// 取得語音輸入的值. 裏頭包含各個語言的資料
				ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				((EditText) findViewById(R.id.address_input)).setText(result.get(0));
				this.mAddSubView.setCurrentValue(1);
				View view = new View(this);
				view.setId(R.id.search_address);
				onClick(view);
			}
		}
	}
	
	@Override protected void onDestroy() {
		this.mBlePrintManager.destroy();
		this.mBlePrintManager = null;
		unregisterReceiver(mBluetoothStateReceiver); // 解除註冊
		mBluetoothStateReceiver = null;
		super.onDestroy();
	}
}
