package com.suda.bluetoothprintproject.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.suda.bluetoothprintproject.R;
import com.suda.bluetoothprintproject.baseActivity.BaseToolbarActivity;
import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.IInputA4NoteApiManager;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.IPdfPrintManager;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.InputA4NoteManager;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.model.InputA4AllData;
import com.suda.bluetoothprintproject.utils.DialogUtil;
import com.suda.bluetoothprintproject.utils.EditTextDefUtil;
import com.suda.bluetoothprintproject.utils.ToastUtil;
import com.suda.bluetoothprintproject.utils.Utils;
import com.suda.bluetoothprintproject.widget.AddSubView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * 完成託運單的步驟二
 */
public class InputA4NoteStep2Activity extends BaseToolbarActivity implements View.OnClickListener
{
	private String TAG = InputA4NoteStep2Activity.class.getSimpleName();
	private EditText mEditTextShipDate;
	private EditText mEditTextHopeArriveDate;
	private AddSubView mAddSubView; // 加減號物件
	
	private IInputA4NoteApiManager mInputA4NoteApiManager = null;
	private IPdfPrintManager mPdfPrintManager = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_input_a4_note_step2);
		super.onCreate(savedInstanceState);
		super.checkUpdateWhenAppStartFromLauncher(new IfVersionIsNewest()
		{
			@Override public void doSomethingOnce() {
				if(InputA4NoteStep2Activity.this.mInputA4NoteApiManager == null || InputA4NoteStep2Activity.this.mPdfPrintManager == null) {
					final InputA4NoteManager manager = new InputA4NoteManager(InputA4NoteStep2Activity.this);
					InputA4NoteStep2Activity.this.mInputA4NoteApiManager = manager;
					InputA4NoteStep2Activity.this.mPdfPrintManager = manager;
				}
				InputA4NoteStep2Activity.this.uiInit();
			}
		});
	}
	
	private void uiInit() {
		super.showTitle(getResources().getString(R.string.input_app_name));
		super.showBack();
		super.showGoHome();
		
		if(!this.mIfScreenSmaller(768)) {
			RadioGroup rg = findViewById(R.id.radioGroup_arrive_time);
			rg.setOrientation(LinearLayout.VERTICAL);
		}
		
		// 讓 editText 取得焦點時不彈出對話框, 而是到點擊時
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		this.mEditTextShipDate = findViewById(R.id.editText_input_date);
		this.mEditTextHopeArriveDate = findViewById(R.id.editText_input_hope_date);
		// 不顯示系統鍵盤
		this.mEditTextShipDate.setInputType(InputType.TYPE_NULL);
		this.mEditTextHopeArriveDate.setInputType(InputType.TYPE_NULL);
		
		mSetCalenderAndClickListener();
		
		findViewById(R.id.browser_printer_btn).setOnClickListener(this);
		findViewById(R.id.previous_step_btn).setOnClickListener(this);
		// 託運單類型的切換, 會切換報值金額是否顯示
		((RadioGroup) findViewById(R.id.radioGroup_waybill_type)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			@Override public void onCheckedChanged(RadioGroup group, int checkedId) {
				TextView textView = findViewById(R.id.textView_product_price);
				EditText editText = findViewById(R.id.editText_product_price);
				switch(checkedId) {
					case R.id.radio_waybill_set1:
						textView.setText("報值金額");
						editText.setHint("請輸入報值金額");
						textView.setVisibility(View.VISIBLE);
						editText.setVisibility(View.VISIBLE);
						editText.setText("");
						break;
					case R.id.radio_waybill_set2:
						textView.setText("代收金額");
						editText.setHint("請輸入代收金額(必填)");
						textView.setVisibility(View.VISIBLE);
						editText.setVisibility(View.VISIBLE);
						editText.setText("");
						break;
				}
			}
		});
		
		this.mSetSpinnerArrayList("");
		// 原本是設計使用者點選了其它後開啟輸入, 但目前沒有這個需求.
		((Spinner) findViewById(R.id.s_product_name)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String choose = ((Spinner) findViewById(R.id.s_product_name)).getSelectedItem().toString();
				if(choose.equals("其他")) {
					//					AlertDialog.Builder builder = new AlertDialog.Builder(InputA4NoteStep2Activity.this);
					//					builder.setTitle("請輸入品項名稱");
					//					final EditText input = new EditText(InputA4NoteStep2Activity.this);
					//					input.setInputType(InputType.TYPE_CLASS_TEXT);
					//					builder.setView(input);
					//
					//					builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					//						@Override
					//						public void onClick(DialogInterface dialog, int which) {
					//							mSetSpinnerArrayList(input.getText().toString());
					//						}
					//					});
					//					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					//						@Override
					//						public void onClick(DialogInterface dialog, int which) {
					//							mSetSpinnerArrayList("");
					//							dialog.cancel();
					//						}
					//					});
					//
					//					builder.show();
				}
			}
			
			@Override public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		this.mAddSubView = findViewById(R.id.pdf_add_sub_view);
		this.mAddSubView.setViewText("託運單列印");
		
		// 核對寄件人 W100_IS_CLC 的資料, 判斷是否開開啟代收單的 UI.
		final InputA4AllData inputA4AllData = (InputA4AllData) getIntent().getSerializableExtra("inputA4AllData");
		switch(inputA4AllData.sendPeople.W100_IS_CLC) {
			case "1":
				findViewById(R.id.radio_waybill_set2).setVisibility(View.VISIBLE);
				break;
			case "0":
				break;
		}
	}
	
	@Override public void onClick(View v) {
		super.onClick(v);
		switch(v.getId()) {
			case R.id.browser_printer_btn:
				// 金額查檢
				int productPrice = 0;
				int carryPrice = 0;
				EditText priceEditText = findViewById(R.id.editText_product_price);
				String chanceType = this.mGetRadioGroupCheckedText(R.id.radioGroup_waybill_type);
				switch(chanceType) {
					case "一般單":
						if(!EditTextDefUtil.CheckNumberRange(0, 0, priceEditText)) // 檢查是否有填值
							if(!EditTextDefUtil.CheckNumberRange(20001, 100000, priceEditText)) {
								ToastUtil.showToast(this, "金額需大於 20000 且小於 100000 以內的金額");
								return;
							}
							else {
								String numberStr = priceEditText.getText().toString();
								productPrice = Integer.parseInt(numberStr.isEmpty() ? "0" : numberStr);
							}
						break;
					case "代收單":
						if(!EditTextDefUtil.CheckNumberRange(1, 100000, priceEditText)) {
							ToastUtil.showToast(this, "金金額需大於 1 且小於 100000 以內的金額");
							return;
						}
						else {
							String numberStr = priceEditText.getText().toString();
							carryPrice = Integer.parseInt(numberStr.isEmpty() ? "0" : numberStr);
						}
						break;
				}
				if(Utils.isFastClick())
					try {
						this.mInitDataForNextStep(productPrice, carryPrice);
					}
					catch(ParseException ex) {
						ex.printStackTrace();
						ToastUtil.showToast(this, "日期轉型時發生錯誤");
					}
				break;
			case R.id.previous_step_btn:
				super.onBackPressed();
				break;
		}
	}
	
	/**
	 * @param productPrice 報值金額
	 * @param carryPrice   代收金額
	 * @throws ParseException 日期轉型時發生錯誤
	 */
	@SuppressLint("SimpleDateFormat")
	private void mInitDataForNextStep(final int productPrice, final int carryPrice) throws ParseException {
		super.showProgressDialog();
		final String goodsName = ((Spinner) findViewById(R.id.s_product_name)).getSelectedItem().toString();
		final String note = ((EditText) findViewById(R.id.editText_note)).getText().toString().trim();
		
		final Date sendDate = new SimpleDateFormat("yyyy/MM/dd").parse(this.mEditTextShipDate.getText().toString());
		final Date hopeReceiveDate = new SimpleDateFormat("yyyy/MM/dd").parse(this.mEditTextHopeArriveDate.getText().toString());
		
		final String hopeArriveTime = this.mGetRadioGroupCheckedText(R.id.radioGroup_arrive_time);
		final String temperate = this.mGetRadioGroupCheckedText(R.id.radioGroup_temperate);
		
		final boolean easyBrokeItem = ((CheckBox) findViewById(R.id.checkbox_easy_broke)).isChecked();
		final boolean precisionItem = ((CheckBox) findViewById(R.id.checkbox_precision_item)).isChecked();
		
		final InputA4AllData inputA4AllData = (InputA4AllData) getIntent().getSerializableExtra("inputA4AllData");
		inputA4AllData.SetData(goodsName, "", note, sendDate, hopeReceiveDate, hopeArriveTime,
				temperate, productPrice, carryPrice, "", easyBrokeItem, precisionItem);
		
		// 發票相關
		//		String taxID = ( (EditText) findViewById(R.id.editText_TaxID) ).getText().toString().trim();
		//		String isPrint = "";
		//		switch(this._GetRadioGroupCheckedText(R.id.radioGroup_IsPrint))
		//		{
		//			case "列印發票":
		//				isPrint = "Y";
		//				break;
		//			case "不列印發票":
		//				isPrint = "N";
		//				break;
		//		}
		//		String isGive = "";
		//		switch(this._GetRadioGroupCheckedText(R.id.radioGroup_IsGive))
		//		{
		//			case "捐贈":
		//				isGive = "Y";
		//				break;
		//			case "不捐贈":
		//				isGive = "N";
		//				break;
		//		}
		//		String invCarrier = ( (EditText) findViewById(R.id.editText_InvoiceCarrier) ).getText().toString().trim();
		//		inputA4AllData._GetQrCodeBitmap(isPrint, isGive, taxID, invCarrier);
		
		// 判斷是一般單還是到付單
		String waybillType = this.mGetRadioGroupCheckedText(R.id.radioGroup_waybill_type).contentEquals("一般單") ? "A" : "B";
		String customerId = waybillType.contentEquals("A") ? "0000000000" : "0000000000";
		
		// 取得託運單號
		this.mInputA4NoteApiManager.getWaybillNumber(customerId, waybillType, this.mAddSubView.getCurrentValue(),
				new AllBusinessCallback.onDAOCallback<ArrayList<String>>()
				{
					@Override public void onSuccess(ArrayList<String> daoData) {
						inputA4AllData.setWaybillNumber(daoData);
						getBitmaps();
					}
					
					@Override public void onFailure() {
						getBitmaps();
					}
					
					private void getBitmaps() { // 取得對應的 bitMap
						InputA4NoteStep2Activity.this.mInputA4NoteApiManager.getQRCodeBitmap(inputA4AllData, "",
								"", "", "", new AllBusinessCallback.onDAOCallback<HashMap<String, Bitmap>>()
								{
									@Override public void onSuccess(HashMap<String, Bitmap> daoData2) {
										inputA4AllData.setBitmap_QR_CODE(daoData2);
										goNextStep();
									}
									
									@Override public void onFailure() {
										goNextStep();
									}
									
									private void goNextStep() {
										InputA4NoteStep2Activity.this.mNextStep();
									}
								});
					}
				});
	}
	
	private void mNextStep() {
		final InputA4AllData inputA4AllData = (InputA4AllData) getIntent().getSerializableExtra("inputA4AllData");
		this.mInputA4NoteApiManager.sendOBTData(inputA4AllData);
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) { // 使用 列印功能 版本必須大於 5.0
			super.runUiThreadOnMainThread(new activityRunUiOnMainThread()
			{
				@Override public void runOnMainThread() {
					InputA4NoteStep2Activity.this.mAddSubView.setCurrentValue(1);
					InputA4NoteStep2Activity.this.mPdfPrintManager.drawPdfDocument(InputA4NoteStep2Activity.this, inputA4AllData,
							new AllBusinessCallback.onDAOCallback<File>()
							{
								@Override public void onSuccess(File file) {
									InputA4NoteStep2Activity.super.showProgressDialog();
									InputA4NoteStep2Activity.this.mPdfPrintManager.doPdfViewToPrint(InputA4NoteStep2Activity.this, file);
									InputA4NoteStep2Activity.super.dismissProgressDialog();
								}
								
								@Override public void onFailure() {
								}
							});
				}
			});
		}
		else {
			super.dismissProgressDialog();
			DialogUtil.showAlert(this, "版本提示", "瀏覽列印功能需要安卓版本 > 5.0");
		}
	}
	
	/**
	 * 設置所有用到小日曆的 view 物件
	 */
	private void mSetCalenderAndClickListener() {
		Calendar ca = Calendar.getInstance();
		ca.setTime(new Date());
		
		// 先修改 input 輸入框的日期為今天
		@SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		if(ca.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			this.mEditTextShipDate.setText(sdf.format(ca.getTime()));
			// 判斷隔日是否為禮拜天, 是的話往後一天
			ca.add(Calendar.DATE, 1);
			if(ca.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
				ca.add(Calendar.DATE, 1);
			this.mEditTextHopeArriveDate.setText(sdf.format(ca.getTime()));
		}
		else {
			ca.add(Calendar.DATE, 1);
			this.mEditTextShipDate.setText(sdf.format(ca.getTime()));
			ca.add(Calendar.DATE, 1);
			this.mEditTextHopeArriveDate.setText(sdf.format(ca.getTime()));
		}
		
		View.OnClickListener dateOnClickListener = new View.OnClickListener()
		{
			@Override public void onClick(final View v) {
				Calendar c = Calendar.getInstance();
				DatePickerDialog picker = new DatePickerDialog(InputA4NoteStep2Activity.this, new DatePickerDialog.OnDateSetListener()
				{
					@SuppressLint("SetTextI18n") @Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						Calendar ca = Calendar.getInstance();
						ca.set(year, monthOfYear, dayOfMonth);
						if(ca.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) { // 判斷是否為 星期日
							if(v.getId() == R.id.calender_btn1 || v.getId() == R.id.editText_input_date)
								((EditText) findViewById(R.id.editText_input_date)).setText(year + "/" + (monthOfYear + 1) + "/" + dayOfMonth);
							else
								((EditText) findViewById(R.id.editText_input_hope_date)).setText(year + "/" + (monthOfYear + 1) + "/" + dayOfMonth);
							
						}
						else
							DialogUtil.showAlert(InputA4NoteStep2Activity.this, "禮拜日不進行配送", "禮拜日不配送, 日期選擇未更改");
					}
				}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
				picker.getDatePicker().setMinDate(c.getTimeInMillis());
				picker.show();
			}
		};
		
		this.mEditTextShipDate.setOnClickListener(dateOnClickListener);
		this.mEditTextHopeArriveDate.setOnClickListener(dateOnClickListener);
		findViewById(R.id.calender_btn1).setOnClickListener(dateOnClickListener);
		findViewById(R.id.calender_btn2).setOnClickListener(dateOnClickListener);
	}
	
	/**
	 * 初始化 spinner
	 *
	 * @param input 第一個選項
	 */
	private void mSetSpinnerArrayList(@NotNull String input) {
		ArrayList<String> noteList = new ArrayList<>();
		if(!input.trim().equals(""))
			noteList.add(input);
		
		noteList.add("");
		noteList.add("3C/精密儀器");
		noteList.add("水果類");
		noteList.add("節慶禮品");
		noteList.add("冷凍/冷藏食品");
		noteList.add("易碎品");
		noteList.add("一般食品類");
		noteList.add("其它");
		
		DialogUtil.setSpinnerDialog(this, (Spinner) findViewById(R.id.s_product_name), noteList);
	}
	
	/**
	 * @param radioGroupId radioGroupId
	 * @return 取得 radioGroup 中所選取的文字
	 */
	private String mGetRadioGroupCheckedText(@IdRes int radioGroupId) {
		RadioGroup group = findViewById(radioGroupId);
		return ((RadioButton) findViewById(group.getCheckedRadioButtonId())).getText().toString();
	}
	
	/**
	 * @param smallestWidth 小於多少的寬度/dpi
	 * @return 螢幕小於參數設定
	 */
	private boolean mIfScreenSmaller(int smallestWidth) {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		float scale = metrics.scaledDensity;
		float screenWidth = width / scale;
		return (screenWidth >= smallestWidth);
	}
}
