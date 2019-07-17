package com.suda.bluetoothprintproject.widget;

import android.content.Context;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suda.bluetoothprintproject.R;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.model.InputA4TargetPeople;
import com.suda.bluetoothprintproject.utils.EditTextDefUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 是 step 1 會使用到的子 View
 */
public class InputA4SubView extends LinearLayout
{
	private EditText mEditForInputName;
	private EditText mEditForInputTelephone;
	private EditText mEditForInputCellphone;
	private EditText mEditForInputAddress;
	public String _BCustomerId = ""; // 客代資料, 有成功查詢到才有值
	public String W100_IS_CLC = "0"; // 依據客代資料, 判斷是否可有代收單
	
	public InputA4SubView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		// 布局文件實力化, 並加載到 context 中
		View.inflate(context, R.layout.input_a4_sub_view, this);
		this.uiInit();
	}
	
	private void uiInit() {
		this.mEditForInputName = findViewById(R.id.editText_name);
		this.mEditForInputTelephone = findViewById(R.id.editText_telephone);
		this.mEditForInputCellphone = findViewById(R.id.editText_cellphone);
		this.mEditForInputAddress = findViewById(R.id.editText_address);
		
		findViewById(R.id.clear_all_btn).setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v) {
				setUIFromBCustomerProfile("", "", "", "");
			}
		});
		
		// 初始化地址下拉式選單 view
		AddressSelector subViewAddressSelector = findViewById(R.id.ll_address_selector);
		subViewAddressSelector.setOnResultListener(new AddressSelector.OnResultListener()
		{
			@Override public void onChangeTxt(String result)
			{
				mEditForInputAddress.setText(result);
			}
		});
		
		findViewById(R.id.iv_selector_btn).setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				AddressSelector subViewAddressSelector = findViewById(R.id.ll_address_selector);
				if(subViewAddressSelector.getVisibility() == View.GONE)
					subViewAddressSelector.setVisibility(VISIBLE);
				else
					subViewAddressSelector.setVisibility(GONE);
			}
		});
	}
	
	
	/**
	 * 設定標題文字 && 同 XXX 的按鈕
	 *
	 * @param text 標題
	 */
	public void setRightLabelAndSameBtnText(@NotNull String text) {
		if(text.equals("收件人")) // 需依照傳入參數, 調換顯示
			((Button) findViewById(R.id.same_other_people_btn)).setText("同寄件人");
		else
			((Button) findViewById(R.id.same_other_people_btn)).setText("同收件人");
		
		((TextView) findViewById(R.id.input_a4_sub_view_right_label)).setText(text);
	}
	
	/**
	 * 同收件人 || 同寄件人 按鈕的監聽
	 *
	 * @param onClickListener callback
	 */
	public void setSameOtherPeopleClickListener(View.OnClickListener onClickListener) {
		findViewById(R.id.same_other_people_btn).setOnClickListener(onClickListener);
	}
	
	/**
	 * @return 將輸入資料轉換為 InputA4TargetPeople
	 */
	public InputA4TargetPeople getTargetPeople() {
		return new InputA4TargetPeople(this.mEditForInputName.getText().toString(), this.mEditForInputTelephone.getText().toString(),
				this.mEditForInputCellphone.getText().toString(), this.mEditForInputAddress.getText().toString(),
				this._BCustomerId.trim(), this.W100_IS_CLC.trim());
	}
	
	
	/**
	 * @return 輸入框基本防呆
	 */
	public boolean defensiveEditText() {
		boolean check1 = EditTextDefUtil.CheckHaveText(this.mEditForInputName);
		boolean check2 = EditTextDefUtil.ChooseOneInput(this.mEditForInputTelephone, this.mEditForInputCellphone);
		boolean check3 = EditTextDefUtil.CheckHaveText(this.mEditForInputAddress);
		
		return check1 && check2 && check3;
	}
	
	/**
	 * 這主要是 voiceBtn 使用. 因為目前找無其他方法只能用笨方法
	 *
	 * @param onClickListener 語音輸入功能的 onCallback
	 */
	public void setVoiceBtnClickListener(View.OnClickListener onClickListener) {
		findViewById(R.id.address_voice_btn).setOnClickListener(onClickListener);
		findViewById(R.id.telephone_voice_btn).setOnClickListener(onClickListener);
		findViewById(R.id.cellphone_voice_btn).setOnClickListener(onClickListener);
	}
	
	public void setVoiceStrToEditText(String voiceStr, @IdRes int editID) {
		((EditText) findViewById(editID)).setText(voiceStr);
	}
	
	/**
	 * 填寫客代資料
	 *
	 * @param name      姓名
	 * @param telephone 家用電話
	 * @param cellphone 手機
	 * @param address   地址
	 */
	public void setUIFromBCustomerProfile(String name, String telephone, String cellphone, String address) {
		this.mEditForInputName.setText(name);
		this.mEditForInputTelephone.setText(telephone);
		this.mEditForInputCellphone.setText(cellphone);
		this.mEditForInputAddress.setText(address);
	}
	
}
