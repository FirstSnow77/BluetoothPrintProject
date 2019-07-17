package com.suda.bluetoothprintproject.businessManagers.account;

import android.annotation.SuppressLint;
import android.content.Context;

import com.suda.bluetoothprintproject.BuildConfig;
import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;
import com.suda.bluetoothprintproject.businessManagers.BaseManager;
import com.suda.bluetoothprintproject.api.dataManager.ApiDataManager;
import com.suda.bluetoothprintproject.api.model.UserModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 使用者驗證相關
 */
public class AccountManager extends BaseManager
{
	private final static String LOGIN_FILE_NAME = "user_login.txt"; // 使用者登入用的檔案名稱
	
	
	public AccountManager(Context context) {
		super(context);
	}
	
	/**
	 * 帳號驗證
	 *
	 * @param account  帳號
	 * @param pwd      密碼
	 * @param callback 驗證成功與否要執行?
	 */
	public void userLogin(final String account, final String pwd, final AllBusinessCallback.onCallback callback) {
		
		if(userIsLogin(super.mContext)) {
			callback.onSuccess();
			return;
		}
		
		if(BuildConfig.DEBUG) {
			if(account.contentEquals("") && pwd.contentEquals("")) {
				userLogin(super.mContext, "DEBUG_ADMIN"); // 測試將帳號寫入檔案用.
				callback.onSuccess();
			}
			else callback.onFailure();
		}
		else {
			super.mApiDataManager.getUserVerification(account, pwd, new ApiDataManager.onDataCallback<UserModel>()
			{
				@Override public void onGetDataSuccess(UserModel data) {
					if(!data.Result.STATUS.isEmpty() && data.Result.STATUS.equals("true")) {
						userLogin(AccountManager.super.mContext, account);
						callback.onSuccess();
					}
					else
						callback.onFailure();
				}
				
				@Override public void onGetDataFailure() {
					callback.onFailure();
				}
			});
		}
	}
	
	private synchronized static void userLogin(Context context, String account) {
		@SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		Date now = new Date();
		String nowDateStr = sdf.format(now);
		String input = String.format("%s%s%s", nowDateStr, "|", account);
		try {
			FileOutputStream outputStream = context.openFileOutput(LOGIN_FILE_NAME, Context.MODE_PRIVATE); // app 內部空間
			outputStream.write(input.getBytes()); // 若有資料的話則覆蓋原資料
			outputStream.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 判斷是否已登入, 並未超過記憶時間.
	 *
	 * @param context 當前的 context
	 * @return 是否登入?
	 */
	public synchronized static boolean userIsLogin(Context context) {
		try {
			FileInputStream inputStream = context.openFileInput(LOGIN_FILE_NAME);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			if((line = reader.readLine()) == null)  // 都只寫入一行
				return false;
			reader.close();
			inputStream.close();
			
			
			String[] dataArr = line.split("\\|");
			// 因為以前版本並沒有存取 account 必須做這查檢, 並重建資料
			if(dataArr.length == 1) {
				userLogout(context);
				return false;
			}
			else {
				@SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date now = new Date();
				String loginDateStr = dataArr[ 0 ];
				Date loginDate = sdf.parse(loginDateStr);
				long time = (now.getTime() - loginDate.getTime()) / (1000 * 60 * 60 * 24);
				if(time >= 1)
					return false;
			}
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		catch(ParseException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * 使用者登出
	 *
	 * @param context 當前 context
	 */
	public synchronized static void userLogout(Context context) {
		File file = new File(context.getFilesDir(), LOGIN_FILE_NAME);
		if(file.exists())
			file.delete();
	}
	
	/**
	 * 取得登入後的使用者帳號
	 *
	 * @param context 當前 context
	 * @return 帳號
	 */
	public synchronized static String getUserAccount(Context context) {
		try {
			FileInputStream inputStream = context.openFileInput(LOGIN_FILE_NAME);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			if((line = reader.readLine()) == null)  // 都只寫入一行
				return "";
			reader.close();
			inputStream.close();
			String[] dataArr = line.split("\\|");
			return dataArr[ 1 ];
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
			return "";
		}
		catch(IOException e) {
			e.printStackTrace();
			return "";
		}
	}
}
