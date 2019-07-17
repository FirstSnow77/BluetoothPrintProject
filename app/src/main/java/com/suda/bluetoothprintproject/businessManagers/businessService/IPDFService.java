package com.suda.bluetoothprintproject.businessManagers.businessService;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.Serializable;

/**
 * 使用 PDF 文檔工具
 */
public interface IPDFService<T extends Serializable>
{
	
	/**
	 * 依據資料創造出 Pdf 文檔
	 * @param context 當前的 context
	 * @param modelData 資料
	 * @return 產出的 pdf 文檔
	 */
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	File drawPdfDocument(Context context, T modelData);
	
	/**
	 * 將 pdf 文檔, 顯示於安卓原生的瀏覽畫面
	 * @param context 當前的 context
	 * @param file pdf 文檔
	 */
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	void doPdfViewToPrint(Context context, File file);
}
