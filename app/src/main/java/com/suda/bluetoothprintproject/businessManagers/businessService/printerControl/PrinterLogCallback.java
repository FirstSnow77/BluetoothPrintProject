package com.suda.bluetoothprintproject.businessManagers.businessService.printerControl;

import android.content.Context;

/**
 * 第三方套件所需要的
 */
public interface PrinterLogCallback
{
	void printerLogCallback(String log);
	Context getCurrentContext();
}
