package com.suda.bluetoothprintproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.suda.bluetoothprintproject.baseActivity.BaseToolbarActivity;
import com.suda.bluetoothprintproject.BuildConfig;
import com.suda.bluetoothprintproject.businessManagers.account.AccountManager;
import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;
import com.suda.bluetoothprintproject.R;
import com.suda.bluetoothprintproject.utils.ToastUtil;

public class LoginActivity extends BaseToolbarActivity
{
	private AccountManager mAccountManager = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(AccountManager.userIsLogin(LoginActivity.this)) {
			startActivity(new Intent(LoginActivity.this, EntranceOneActivity.class));
			finish();
		}
		
		setContentView(R.layout.activity_login);
		super.onCreate(savedInstanceState);
		
		super.checkUpdateWhenAppStartFromLauncher(new IfVersionIsNewest()
		{
			@Override public void doSomethingOnce() {
				
				LoginActivity.super.showTitle("");
				
				if(LoginActivity.this.mAccountManager == null)
					LoginActivity.this.mAccountManager = new AccountManager(LoginActivity.this);
				
				LoginActivity.this.uiInit();
			}
		});
	}
	
	private void uiInit() {
		((TextView) findViewById(R.id.tv_version_name)).setText(String.format("%s%s", "版本: ", BuildConfig.VERSION_NAME));
		
		findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v) {
				LoginActivity.super.showProgressDialog();
				String account = ((EditText) findViewById(R.id.editText_login_id)).getText().toString();
				String pwd = ((EditText) findViewById(R.id.editText_login_pwd)).getText().toString();
				LoginActivity.this.mAccountManager.userLogin(account, pwd, new AllBusinessCallback.onCallback()
				{
					@Override public void onSuccess() {
						LoginActivity.super.dismissProgressDialog();
						startActivity(new Intent(LoginActivity.this, EntranceOneActivity.class));
						finish();
					}
					
					@Override public void onFailure() {
						LoginActivity.super.dismissProgressDialog();
						ToastUtil.showToast(LoginActivity.this, "帳號密碼錯誤, 請重新輸入!!");
					}
				});
			}
		});
	}
}
