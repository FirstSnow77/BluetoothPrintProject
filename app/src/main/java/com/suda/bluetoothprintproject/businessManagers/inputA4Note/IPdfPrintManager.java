package com.suda.bluetoothprintproject.businessManagers.inputA4Note;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.suda.bluetoothprintproject.businessManagers.AllBusinessCallback;
import com.suda.bluetoothprintproject.businessManagers.inputA4Note.model.InputA4AllData;

import java.io.File;

/**
 * 使用 inputA4AllData 資料產出 pdf 並顯示於瀏覽畫面
 */
public interface IPdfPrintManager
{
	/**
	 * 依據資料創造出 Pdf 文檔
	 *
	 * @param context        當前的 context
	 * @param inputA4AllData 資料
	 * @param onDAOCallback  callback
	 */
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	void drawPdfDocument(Context context, InputA4AllData inputA4AllData, AllBusinessCallback.onDAOCallback<File> onDAOCallback);
	
	/**
	 * 將 pdf 文檔, 顯示於安卓原生的瀏覽畫面
	 *
	 * @param context 當前的 context
	 * @param file    pdf 文檔
	 */
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	void doPdfViewToPrint(Context context, File file);
}
