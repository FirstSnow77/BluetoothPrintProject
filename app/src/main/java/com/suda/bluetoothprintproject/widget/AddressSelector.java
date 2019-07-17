package com.suda.bluetoothprintproject.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.suda.bluetoothprintproject.R;
import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.IAddressListApiManager;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.InputA4NoteManager;
import com.suda.bluetoothprintproject.utils.DialogUtil;
import com.suda.bluetoothprintproject.utils.ToastUtil;

import java.util.ArrayList;

/**
 * 提供下拉式地址選單
 */
public class AddressSelector extends LinearLayout
{
	private Context mContext;
	private Spinner mSpnCounty;
	private Spinner mSpnArea;
	private Spinner mSpnStreet;
	private ArrayList<String> mSpnStreetData = null; // 儲存 "街 & 路" 的資料 (提供使用者過濾資料用)
	private OnResultListener mOnResultListener = null;
	private AddressClass addressResult = new AddressClass(); // 存放使用者當前的選擇
	private IAddressListApiManager mAddressListApiManager = null;
	
	public AddressSelector(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		View.inflate(context, R.layout.address_selector_sub_view, this);
		this.mContext = context;
		
		this.uiInit();
	}
	
	private void uiInit() {
		this.mSpnCounty = findViewById(R.id.s_address_menu1);
		this.mSpnArea = findViewById(R.id.s_address_menu2);
		this.mSpnStreet = findViewById(R.id.s_address_menu3);
		
		// 初始化所有下拉式
		this.setSpnCounty();
		setSpnToEmpty(mSpnArea);
		setSpnToEmpty(mSpnStreet);
		
		// 將每個 spinner 設定對應的 tag & listener
		this.mSpnCounty.setTag("County");
		this.mSpnCounty.setOnItemSelectedListener(mSpnOnItemSelected);
		this.mSpnArea.setTag("Area");
		this.mSpnArea.setOnItemSelectedListener(mSpnOnItemSelected);
		this.mSpnStreet.setTag("Street");
		this.mSpnStreet.setOnItemSelectedListener(mSpnOnItemSelected);
		
		// 監聽每個 editText 的變化
		EditText editText = findViewById(R.id.laneText);
		editText.addTextChangedListener(new MyTextWatcher(editText));
		editText = findViewById(R.id.alleyText);
		editText.addTextChangedListener(new MyTextWatcher(editText));
		editText = findViewById(R.id.noText);
		editText.addTextChangedListener(new MyTextWatcher(editText));
		editText = findViewById(R.id.floorText);
		editText.addTextChangedListener(new MyTextWatcher(editText));
		editText = findViewById(R.id.roomText);
		editText.addTextChangedListener(new MyTextWatcher(editText));
		editText = findViewById(R.id.dashText);
		editText.addTextChangedListener(new MyTextWatcher(editText));
		
		// user 的 filter (Street)
		editText = findViewById(R.id.streetFilterText);
		editText.addTextChangedListener(new TextWatcher()
		{
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
			
			@Override public void afterTextChanged(Editable s) {  // 這裡只針對原有資料做 filter 不使用 http 取資料.
				String filter = s.toString().trim();
				if(!filter.isEmpty() && mSpnStreetData.size() > 1) {
					ArrayList<String> filterArray = new ArrayList<>();
					filterArray.add("");
					for(String item : mSpnStreetData) {
						if(item.trim().contains(filter))
							filterArray.add(item);
					}
					DialogUtil.setSpinnerDialog(AddressSelector.this.mContext, mSpnStreet, filterArray);
				}
				else
					DialogUtil.setSpinnerDialog(AddressSelector.this.mContext, mSpnStreet, mSpnStreetData);
			}
		});
	}
	
	/**
	 * 註冊 callback
	 *
	 * @param onResultListener 外部可使用此 callback, 來取得使用者當前的選擇的內容
	 */
	public void setOnResultListener(OnResultListener onResultListener) {
		this.mOnResultListener = onResultListener;
	}
	
	/**
	 * 使用 callback 傳送使用者選擇的內容
	 */
	private void sendAddressMessage() {
		if(this.mOnResultListener != null) {
			String result = String.format("%s%s%s%s%s%s%s%s%s", addressResult.County, addressResult.Area, addressResult.Street,
					addressResult.Lane, addressResult.Alley, addressResult.No, addressResult.Floor, addressResult.Room, addressResult.Dash);
			this.mOnResultListener.onChangeTxt(result);
		}
	}
	
	/**
	 * 監看 EditText, 偵測到使用者輸入後, 並做出修正後傳入 addressResult 儲存
	 */
	private class MyTextWatcher implements TextWatcher
	{
		private EditText mEditText;
		
		MyTextWatcher(EditText editText) {
			this.mEditText = editText;
		}
		
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
		@Override public void afterTextChanged(Editable s) {
			String str = mEditText.getText().toString().trim();
			if(str.isEmpty()) {
				switch(mEditText.getId()) {
					case R.id.laneText:
						addressResult.Lane = str;
						break;
					case R.id.alleyText:
						addressResult.Alley = str;
						break;
					case R.id.noText:
						addressResult.No = str;
						break;
					case R.id.floorText:
						addressResult.Floor = str;
						break;
					case R.id.roomText:
						addressResult.Room = str;
						break;
					case R.id.dashText:
						addressResult.Dash = str;
						break;
				}
			}
			else {
				switch(mEditText.getId()) {
					case R.id.laneText:
						addressResult.Lane = str + "巷";
						break;
					case R.id.alleyText:
						addressResult.Alley = str + "弄";
						break;
					case R.id.noText:
						addressResult.No = str + "號";
						break;
					case R.id.floorText:
						addressResult.Floor = str + "樓";
						break;
					case R.id.roomText:
						addressResult.Room = str + "室";
						break;
					case R.id.dashText:
						addressResult.Dash = "之" + str;
						break;
				}
			}
			sendAddressMessage();
		}
	}
	
	/**
	 * Spinner 選擇的監聽
	 */
	private AdapterView.OnItemSelectedListener mSpnOnItemSelected = new AdapterView.OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
			final String selectedStr = parent.getItemAtPosition(position).toString().trim();
			final boolean isEmpty = selectedStr.isEmpty();
			switch(parent.getTag().toString()) {
				case "County":
					setSpnToEmpty(mSpnStreet);
					if(isEmpty)
						setSpnToEmpty(mSpnArea);
					else
						setSpnArea(selectedStr);
					
					addressResult.County = selectedStr;
					break;
				case "Area":
					if(isEmpty)
						setSpnToEmpty(mSpnStreet);
					else
						setSpnStreet(mSpnCounty.getSelectedItem().toString().trim(), selectedStr);
					
					addressResult.Area = selectedStr;
					break;
				case "Street":
					addressResult.Street = selectedStr;
					break;
			}
			sendAddressMessage();
		}
		
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};
	
	/**
	 * 清空 Spinner
	 *
	 * @param spn Spinner
	 */
	private void setSpnToEmpty(Spinner spn) {
		ArrayList<String> data = new ArrayList<>();
		data.add("");
		DialogUtil.setSpinnerDialog(this.mContext, spn, data);
		
		// 儲存街路的資料
		this.mSpnStreetData = data;
	}
	
	/**
	 * level:0, 縣市通常只會有一次.
	 */
	private void setSpnCounty() {
		this.getAddressData(0, "", "", new onGetAddressData()
		{
			@Override public void onSuccess(ArrayList<String> data) {
				DialogUtil.setSpinnerDialog(AddressSelector.this.mContext, AddressSelector.this.mSpnCounty, data);
			}
		});
		
	}
	
	/**
	 * 以選擇的城市去撈取對應的資料, 並將資料塞進對應的 Spinner
	 *
	 * @param city 縣市
	 */
	private void setSpnArea(String city) {
		this.getAddressData(1, city, "", new onGetAddressData()
		{
			@Override public void onSuccess(ArrayList<String> data) {
				DialogUtil.setSpinnerDialog(AddressSelector.this.mContext, AddressSelector.this.mSpnArea, data);
			}
		});
	}
	
	/**
	 * 以選擇的 城市 & 區域 去撈取對應的資料, 並將資料塞進對應的 Spinner
	 *
	 * @param city     縣市
	 * @param district 區域
	 */
	private void setSpnStreet(String city, String district) {
		this.getAddressData(5, city, district, new onGetAddressData()
		{
			@Override public void onSuccess(ArrayList<String> data) {
				DialogUtil.setSpinnerDialog(AddressSelector.this.mContext, AddressSelector.this.mSpnStreet, data);
				AddressSelector.this.mSpnStreetData = data; // 儲存街路的資料
			}
		});
	}
	
	
	private void getAddressData(int level, String city, String district, final onGetAddressData callback) {
		if(this.mAddressListApiManager == null)
			this.mAddressListApiManager = new InputA4NoteManager(this.mContext);
		this.mAddressListApiManager.getAddressList(level, city, district, new AllBusinessCallback.onDAOCallback<ArrayList<String>>()
		{
			@Override public void onSuccess(ArrayList<String> daoData) {
				callback.onSuccess(daoData);
			}
			
			@Override public void onFailure() {
				ToastUtil.showToast(AddressSelector.this.mContext, "查詢地址清單失敗");
				ArrayList<String> result = new ArrayList<>();
				result.add("");
				callback.onSuccess(result);
			}
		});
	}
	
	/**
	 * 儲存使用者當前選擇的內容
	 */
	private class AddressClass
	{
		String County = "";
		String Area = "";
		String Street = "";
		String Lane = "";
		String Alley = "";
		String No = "";
		String Floor = "";
		String Room = "";
		String Dash = "";
	}
	
	/**
	 * 外部可使用此 callback, 來取得使用者當前的選擇
	 */
	public interface OnResultListener
	{
		void onChangeTxt(String result);
	}
	
	interface onGetAddressData
	{
		void onSuccess(ArrayList<String> data);
	}
}
