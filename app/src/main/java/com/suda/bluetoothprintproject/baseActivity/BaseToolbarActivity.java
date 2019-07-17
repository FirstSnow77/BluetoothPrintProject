package com.suda.bluetoothprintproject.baseActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.suda.bluetoothprintproject.businessManagers.account.AccountManager;
import com.suda.bluetoothprintproject.R;
import com.suda.bluetoothprintproject.activity.EntranceOneActivity;
import com.suda.bluetoothprintproject.activity.LoginActivity;

@SuppressLint("Registered")
public class BaseToolbarActivity extends BaseUpdateCheckActivity implements View.OnClickListener
{
	private ImageButton mImageBtnBack;
	private TextView mTvTitle;
	private TextView mTvLogout;
	private TextView mTvGoHome;
	
	@Override protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.uiInit();
	}
	
	private void uiInit() {
		// find
		this.mImageBtnBack = findViewById(R.id.image_btn_back);
		this.mTvTitle = findViewById(R.id.tv_title);
		this.mTvLogout = findViewById(R.id.tv_toolbar_logout);
		this.mTvGoHome = findViewById(R.id.tv_toolbar_go_home);
		// setVisibility
		this.mImageBtnBack.setVisibility(View.GONE);
		this.mTvTitle.setVisibility(View.GONE);
		this.mTvLogout.setVisibility(View.GONE);
		this.mTvGoHome.setVisibility(View.GONE);
		// set clickListener
		this.mImageBtnBack.setOnClickListener(this);
		this.mTvLogout.setOnClickListener(this);
		this.mTvGoHome.setOnClickListener(this);
	}
	
	/**
	 * 開啟標題顯示
	 *
	 * @param title 若為 null 或空字串，則帶預設標題文字
	 */
	protected void showTitle(String title) {
		if(title != null && !TextUtils.isEmpty(title))
			this.mTvTitle.setText(title);
		
		this.mTvTitle.setVisibility(View.VISIBLE);
	}
	
	
	/**
	 * 開啟登出
	 */
	protected void showLogout() {
		this.mTvLogout.setVisibility(View.VISIBLE);
	}
	
	
	/**
	 * 開啟回首頁
	 */
	protected void showGoHome() {
		this.mTvGoHome.setVisibility(View.VISIBLE);
	}
	
	
	/**
	 * 開啟返回鍵
	 */
	protected void showBack() {
		this.mImageBtnBack.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 統一管理 MainThread 更新 Ui
	 * @param runUiOnMainThread callback
	 */
	protected void runUiThreadOnMainThread(final activityRunUiOnMainThread runUiOnMainThread) {
		runOnUiThread(new Runnable() {
			@Override public void run() {
				runUiOnMainThread.runOnMainThread();
			}
		});
	}
	
	protected interface activityRunUiOnMainThread {
		void runOnMainThread();
	}
	
	@Override public void onClick(View v) {
		if(v.equals(this.mImageBtnBack)) {
			onBackPressed(); // 退回等同於手機的 back 鍵
		}
		else if(v.equals(this.mTvLogout)) {
			AccountManager.userLogout(this);
			Intent intent = new Intent(this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
		else if(v.equals(this.mTvGoHome)) {
			Intent intent = new Intent(this, EntranceOneActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
	}
	
	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("是否返回?")
				.setCancelable(false)
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						onSuperBackPressed();
					}
				})
				.setNegativeButton("否", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void onSuperBackPressed(){
		super.onBackPressed();
	}
}
