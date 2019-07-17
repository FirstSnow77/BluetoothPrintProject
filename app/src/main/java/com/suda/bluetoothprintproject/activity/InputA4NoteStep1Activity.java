package com.suda.bluetoothprintproject.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.suda.bluetoothprintproject.baseActivity.BaseToolbarActivity;
import com.suda.bluetoothprintproject.R;
import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.IInputA4NoteApiManager;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.InputA4NoteManager;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.model.BCustomerProfile;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.model.InputA4AllData;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.model.InputA4TargetPeople;
import com.suda.bluetoothprintproject.utils.DialogUtil;
import com.suda.bluetoothprintproject.utils.Utils;
import com.suda.bluetoothprintproject.widget.InputA4SubView;

import java.util.ArrayList;

/**
 * 完成託運單的步驟一
 */
public class InputA4NoteStep1Activity extends BaseToolbarActivity implements View.OnClickListener
{
	private InputA4SubView mSubViewReceiveMan;
	private InputA4SubView mSubViewSendMan;
	private IInputA4NoteApiManager mInputA4NoteApiManager = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_input_a4_note_step1);
		super.onCreate(savedInstanceState);
		super.checkUpdateWhenAppStartFromLauncher(new IfVersionIsNewest()
		{
			@Override public void doSomethingOnce() {
				if(InputA4NoteStep1Activity.this.mInputA4NoteApiManager == null)
					InputA4NoteStep1Activity.this.mInputA4NoteApiManager = new InputA4NoteManager(InputA4NoteStep1Activity.this);
				InputA4NoteStep1Activity.this.uiInit();
			}
		});
	}
	
	private void uiInit() {
		// 讓 editText 取得焦點時不彈出對話框, 而是到點擊時
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		super.showTitle(getResources().getString(R.string.input_app_name));
		super.showBack();
		super.showGoHome();
		
		this.mSubViewReceiveMan = findViewById(R.id.input_a4_sub_view_receive_man);
		this.mSubViewSendMan = findViewById(R.id.input_a4_sub_view_sand_man);
		this.mSubViewReceiveMan.setVoiceBtnClickListener(this.ReceiveManVoiceBtn);
		this.mSubViewSendMan.setVoiceBtnClickListener(this.SendManVoiceBtn);
		this.mSubViewReceiveMan.setSameOtherPeopleClickListener(this.ReceiveManSameBtn);
		this.mSubViewSendMan.setSameOtherPeopleClickListener(this.SendManSameBtn);
		
		// 設定 widget 中的 Text
		this.mSubViewReceiveMan.setRightLabelAndSameBtnText("收件人");
		this.mSubViewSendMan.setRightLabelAndSameBtnText("寄件人");
		
		// 核對 W100_IS_CLC 的資料, 判斷是否開開啟代收單的 UI.
		String who = getIntent().getStringExtra("WhoSelected");
		switch(who) {
			case "C_Customer":
				break;
			case "B_Customer":
				findViewById(R.id.set_code_visible).setVisibility(View.VISIBLE);
				break;
		}
		
		// 設定按鈕的監聽
		findViewById(R.id.next_step_btn).setOnClickListener(this);
		findViewById(R.id.previous_step_btn).setOnClickListener(this);
		findViewById(R.id.search_customer_profile_btn).setOnClickListener(this);
	}
	
	@Override public void onClick(View v) {
		super.onClick(v);
		switch(v.getId()) {
			case R.id.next_step_btn:
				boolean check1 = this.mSubViewReceiveMan.defensiveEditText();
				boolean check2 = this.mSubViewSendMan.defensiveEditText();
				if(check1 & check2)
					if(Utils.isFastClick())
						this.mNextStep();
				break;
			case R.id.previous_step_btn:
				super.onBackPressed();
				break;
			case R.id.search_customer_profile_btn:
				super.showProgressDialog();
				String bId = ((EditText) findViewById(R.id.editText_BCustomerId)).getText().toString();
				this.mInputA4NoteApiManager.getBCustomerProfile(bId, new AllBusinessCallback.onDAOCallback<BCustomerProfile>()
				{
					@Override public void onSuccess(BCustomerProfile daoData) {
						InputA4NoteStep1Activity.super.dismissProgressDialog();
						InputA4NoteStep1Activity.this.mSubViewSendMan.W100_IS_CLC = daoData.W100_IS_CLC;
						InputA4NoteStep1Activity.this.mSubViewSendMan._BCustomerId = daoData.newBCustomerId;
						InputA4NoteStep1Activity.this.mSubViewSendMan.setUIFromBCustomerProfile(daoData.customerFullName
								, daoData.telephone, daoData.telephone, daoData.address);
						hiddenSoftKeyboard();
					}
					
					@Override public void onFailure() {
						InputA4NoteStep1Activity.super.dismissProgressDialog();
						InputA4NoteStep1Activity.this.mSubViewSendMan.W100_IS_CLC = "0";
						InputA4NoteStep1Activity.this.mSubViewSendMan._BCustomerId = "";
						InputA4NoteStep1Activity.this.mSubViewSendMan.setUIFromBCustomerProfile("", "", "", "");
						hiddenSoftKeyboard();
					}
					
					private void hiddenSoftKeyboard() {
						// 隱藏鍵盤
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						// imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
						imm.hideSoftInputFromWindow(findViewById(R.id.search_customer_profile_btn).getWindowToken(), 0);
					}
				});
				break;
		}
	}
	
	/**
	 * 運行到下一個頁面
	 */
	private void mNextStep() {
		String who = getIntent().getStringExtra("WhoSelected"); // B客 一定得填寫客代
		if((who.equals("B_Customer") && this.mSubViewSendMan._BCustomerId.trim().isEmpty()) ||
				   (who.equals("B_Customer") && this.mSubViewSendMan._BCustomerId.trim().contentEquals("000000000000"))) {
			DialogUtil.showAlert(this, "請填寫客代", "必須填寫客代");
			return;
		}
		super.showProgressDialog();
		final InputA4AllData inputA4AllData = new InputA4AllData();
		inputA4AllData.sendPeople = this.mSubViewSendMan.getTargetPeople();
		inputA4AllData.receivePeople = this.mSubViewReceiveMan.getTargetPeople();
		this.mInputA4NoteApiManager.setStep1NextData(inputA4AllData, new AllBusinessCallback.onCallback()
		{
			@Override public void onSuccess() {
				InputA4NoteStep1Activity.super.dismissProgressDialog();
				Bundle bundle = new Bundle();
				bundle.putSerializable("inputA4AllData", inputA4AllData);
				Intent intent = new Intent(InputA4NoteStep1Activity.this, InputA4NoteStep2Activity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			
			@Override public void onFailure() {
				onSuccess();
			}
		});
	}
	
	@Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 200 || requestCode == 201) {
			if(resultCode == RESULT_OK && data != null) {
				// 取得語音輸入的值. 裏頭包含各個語言的資料
				ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				if(requestCode == 200)
					this.mSubViewReceiveMan.setVoiceStrToEditText(result.get(0), this._voice_Use);
				else
					this.mSubViewSendMan.setVoiceStrToEditText(result.get(0), this._voice_Use);
			}
		}
	}
	
	// 依據 voice 的 btn ID 來辨識, 以做後續修改相對應的 editText 使用
	private View.OnClickListener ReceiveManVoiceBtn = new View.OnClickListener()
	{
		@Override public void onClick(View v) {
			@IdRes int edit;
			switch(v.getId()) {
				case R.id.address_voice_btn:
					edit = R.id.editText_address;
					break;
				case R.id.telephone_voice_btn:
					edit = R.id.editText_telephone;
					break;
				case R.id.cellphone_voice_btn:
					edit = R.id.editText_cellphone;
					break;
				default:
					edit = 0;
			}
			startVoiceIntent(200, edit);
		}
	};
	private View.OnClickListener SendManVoiceBtn = new View.OnClickListener()
	{
		@Override public void onClick(View v) {
			@IdRes int edit;
			switch(v.getId()) {
				case R.id.address_voice_btn:
					edit = R.id.editText_address;
					break;
				case R.id.telephone_voice_btn:
					edit = R.id.editText_telephone;
					break;
				case R.id.cellphone_voice_btn:
					edit = R.id.editText_cellphone;
					break;
				default:
					edit = 0;
			}
			startVoiceIntent(201, edit);
		}
	};
	
	@IdRes int _voice_Use = 0; // 專門提供給聲音使用(辨識哪個按鈕的 ID).
	
	private void startVoiceIntent(int requestCode, @IdRes int editID) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請說～");
		this._voice_Use = editID;
		try {
			startActivityForResult(intent, requestCode);
		}
		catch(ActivityNotFoundException a) {
			Toast.makeText(getApplicationContext(), "請開啟語音輸入", Toast.LENGTH_SHORT).show();
		}
	}
	
	// 收件人的 "同寄件人"按鈕監聽
	private View.OnClickListener ReceiveManSameBtn = new View.OnClickListener()
	{
		@Override public void onClick(View v) {
			InputA4TargetPeople sandManData = mSubViewSendMan.getTargetPeople();
			mSubViewReceiveMan.setUIFromBCustomerProfile(sandManData.name, sandManData.telephone, sandManData.cellphone, sandManData.address);
		}
	};
	
	// 寄件人的 "同收件人"按鈕監聽
	private View.OnClickListener SendManSameBtn = new View.OnClickListener()
	{
		@Override public void onClick(View v) {
			InputA4TargetPeople receiveData = mSubViewReceiveMan.getTargetPeople();
			mSubViewSendMan.setUIFromBCustomerProfile(receiveData.name, receiveData.telephone, receiveData.cellphone, receiveData.address);
		}
	};
}
