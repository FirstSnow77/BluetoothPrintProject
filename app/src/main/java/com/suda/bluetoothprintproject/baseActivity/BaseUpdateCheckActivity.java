package com.suda.bluetoothprintproject.baseActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;
import com.suda.bluetoothprintproject.BuildConfig;
import com.suda.bluetoothprintproject.R;
import com.suda.bluetoothprintproject.utils.DialogUtil;
import com.suda.bluetoothprintproject.utils.SharedPreferenceUtil;
import com.suda.bluetoothprintproject.utils.Utils;

/**
 * 在第一次啟動 app 時, 一定會查檢(包含移除 flag), 若查檢通過則注入一個永久存在手機中的 flag
 */
@SuppressLint("Registered")
public class BaseUpdateCheckActivity extends AppCompatActivity
{
	private static String TAG = BaseUpdateCheckActivity.class.getSimpleName();
	
	private AlertDialog mAlertDialog = null;
	
	private AppUpdaterUtils mAppUpdaterUtils;
	// 若版本檢核通過後, 子 activity 想做的事情
	private IfVersionIsNewest mIfVersionIsNewest = new IfVersionIsNewest()
	{
		@Override public void doSomethingOnce() { }
	};
	private boolean versionIsNewest = false;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Utils.IsFromLauncher)
			SharedPreferenceUtil.removeSharedPreference(BaseUpdateCheckActivity.this, SharedPreferenceUtil.TAG_VERSION_NAME);
		
		this.setAppUpdater();
	}
	
	
	/**
	 * 每個 activity 啟動的時候都做一次, 並且在查檢成功後想做什麼事??
	 *
	 * @param ifVersionIsNewest callback
	 */
	protected void checkUpdateWhenAppStartFromLauncher(IfVersionIsNewest ifVersionIsNewest) {
		this.mIfVersionIsNewest = ifVersionIsNewest;
		if(Utils.IsFromLauncher) {
			Utils.IsFromLauncher = false;
		}
		this.startAppUpdater();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		this.startAppUpdater();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		this.mAppUpdaterUtils.stop();
	}
	
	@Override protected void onDestroy() {
		if(this.mAlertDialog != null) {
			this.mAlertDialog.dismiss();
		}
		super.onDestroy();
	}
	
	/**
	 * 顯示等待轉圈畫面
	 */
	protected synchronized void showProgressDialog() {
		//		if(mFrameLayout == null) // 取得當前 layout 內容
		//			mFrameLayout = getWindow().getDecorView().findViewById(android.R.id.content);
		//
		//		if(mProgressBar == null) { // 創建 ProgressBar
		//			mProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
		//
		//			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
		//					ViewGroup.LayoutParams.WRAP_CONTENT);
		//			params.addRule(RelativeLayout.CENTER_IN_PARENT);
		//			mFrameLayout.addView(mProgressBar, params);
		//		}
		
		runOnUiThread(new Runnable()
		{
			@Override public void run() {
				if(mAlertDialog == null)
					mAlertDialog = new AlertDialog.Builder(BaseUpdateCheckActivity.this, R.style.AlertDialogTheme)
							               .setTitle("Progress Dialog").setView(R.layout.base_progress_bar).setCancelable(false).create();
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
				if(mAlertDialog != null)
					mAlertDialog.show();
			}
		});
	}
	
	/**
	 * 關閉等待轉圈畫面
	 */
	protected synchronized void dismissProgressDialog() {
		runOnUiThread(new Runnable()
		{
			@Override public void run() {
				if(mAlertDialog != null) {
					mAlertDialog.hide();
					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
				}
			}
		});
	}
	
	private synchronized void setAppUpdater() {
		this.mAppUpdaterUtils = new AppUpdaterUtils(this).setUpdateFrom(UpdateFrom.GOOGLE_PLAY).withListener(new AppUpdaterUtils.UpdateListener()
		{
			@Override
			public void onSuccess(Update update, Boolean aBoolean) {
				String currentVersion = BuildConfig.VERSION_NAME;
				Semver currentSemver = new Semver(currentVersion);
				
				final String storeVersion = update.getLatestVersion();
				Semver storeSemver = new Semver(storeVersion);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(BaseUpdateCheckActivity.this)
						                              .setTitle("軟體更新").setCancelable(false)
						                              .setMessage("軟體已有更新，請先更新再進行後續操作")
						                              .setPositiveButton("更新", new DialogInterface.OnClickListener()
						                              {
							                              public void onClick(DialogInterface dialog, int whichButton) {
								                              dialog.dismiss();
								                              go2GooglePlay();
							                              }
						                              });
				
				if(!BuildConfig.DEBUG) {
					dismissProgressDialog();
					if(storeSemver.MAJOR > currentSemver.MAJOR)
						builder.show();
					else if(storeSemver.MINOR > currentSemver.MINOR)
						builder.show();
					else if(storeSemver.PATCH > currentSemver.PATCH) {
						builder.setNegativeButton("略過", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								SharedPreferenceUtil.setSharedPreference(BaseUpdateCheckActivity.this, SharedPreferenceUtil.TAG_VERSION_NAME, storeVersion);
								doSomething();
							}
						});
						builder.show();
					}
					else {
						SharedPreferenceUtil.setSharedPreference(BaseUpdateCheckActivity.this, SharedPreferenceUtil.TAG_VERSION_NAME, storeVersion);
						doSomething();
					}
				}
				else { // debug
					dismissProgressDialog();
					SharedPreferenceUtil.setSharedPreference(BaseUpdateCheckActivity.this, SharedPreferenceUtil.TAG_VERSION_NAME, storeVersion);
					doSomething();
				}
			}
			
			@Override
			public void onFailed(AppUpdaterError appUpdaterError) {
				dismissProgressDialog();
				Log.d("debug", appUpdaterError.toString());
				DialogUtil.showAlert(BaseUpdateCheckActivity.this, "請檢察網路狀態", "請確認網路狀態後重新啟動 app!!");
			}
		});
	}
	
	private synchronized void startAppUpdater() {
		if(TextUtils.isEmpty(SharedPreferenceUtil.getSharedPreference(BaseUpdateCheckActivity.this, SharedPreferenceUtil.TAG_VERSION_NAME))) {
			showProgressDialog();
			mAppUpdaterUtils.start();
		}
		else
			this.doSomething();
	}
	
	// 若版本是最新版的話, 則執行子類要做的事情 (不重複做, 只做一次)
	private void doSomething() {
		if(!this.versionIsNewest)
			this.mIfVersionIsNewest.doSomethingOnce();
		
		this.versionIsNewest = true;
	}
	
	/**
	 * 前往Google play
	 */
	private void go2GooglePlay() {
		final String appPackageName = this.getPackageName();
		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
		}
		catch(android.content.ActivityNotFoundException e) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
		}
	}
	
	protected interface IfVersionIsNewest
	{
		void doSomethingOnce();
	}
	
	
	/**
	 * 存放 & 拆分 app versionName 的類
	 */
	private class Semver
	{
		int MAJOR = 0;
		int MINOR = 0;
		int PATCH = 0;
		
		Semver(String version) {
			String[] semver = version.split("\\.");
			if(semver.length < 3)
				return;
			
			MAJOR = Integer.valueOf(semver[ 0 ]);
			MINOR = Integer.valueOf(semver[ 1 ]);
			PATCH = Integer.valueOf(semver[ 2 ]);
		}
	}
}
