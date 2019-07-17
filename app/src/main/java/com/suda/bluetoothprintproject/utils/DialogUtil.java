package com.suda.bluetoothprintproject.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.suda.bluetoothprintproject.R;
import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;

import java.util.ArrayList;

public class DialogUtil
{
	
	/**
	 * 若再無網路時, 使用者使用需要 http 請求時, 跳出一個提示
	 *
	 * @param context 當前 context
	 */
	public static void showNetworkErrorAlert(Context context) {
		if(context != null && !((Activity) context).isFinishing()) {
			new AlertDialog.Builder(context).setTitle("目前無網路連線，請檢查網路連線狀態")
					.setPositiveButton("確定", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialogInterface, int i) { }
					}).create().show();
		}
	}
	
	/**
	 * 顯示清單選項的對話框
	 *
	 * @param context         當前 context
	 * @param title           title
	 * @param list            清單內容
	 * @param canCancel       是否可取消該對話?
	 * @param onClickListener callback
	 */
	public static void showListAlert(Context context, String title, CharSequence[] list, boolean canCancel
			, DialogInterface.OnClickListener onClickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setCancelable(canCancel).setItems(list, onClickListener).create().show();
	}
	
	/**
	 * 顯示一般提示訊息
	 *
	 * @param context 當前 context
	 * @param title   title
	 * @param message 訊息
	 */
	public static void showAlert(Context context, String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setIcon(R.mipmap.ic_launcher).setMessage(message).show();
	}
	
	/**
	 * 設定 Spinner 清單顯示的內容 && 樣板配置
	 *
	 * @param context  當前 context
	 * @param dataList Spinner 中顯示的清單內容
	 */
	public static void setSpinnerDialog(Context context, Spinner spinner, ArrayList<String> dataList) {
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, dataList);
		dataAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
		spinner.setAdapter(dataAdapter);
	}
}
