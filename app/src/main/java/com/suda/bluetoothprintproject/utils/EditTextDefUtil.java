package com.suda.bluetoothprintproject.utils;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.widget.EditText;

import org.jetbrains.annotations.NotNull;

/**
 * 輸入框的防呆
 */
public class EditTextDefUtil
{
	private static final @ColorInt int _ErrorTextColor = Color.RED;
	
	/**
	 * 兩個擇一輸入
	 *
	 * @param edit1 輸入框1
	 * @param edit2 輸入框2
	 * @return 驗證通過與否?
	 */
	public static boolean ChooseOneInput(@NotNull EditText edit1, @NotNull EditText edit2) {
		String str1 = mGetEditTrimText(edit1);
		String str2 = mGetEditTrimText(edit2);
		if(str1.isEmpty() && str2.isEmpty()) {
			edit1.setHintTextColor(_ErrorTextColor);
			edit2.setHintTextColor(_ErrorTextColor);
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * @param editText 輸入框
	 * @return 確定是否有文字?
	 */
	public static boolean CheckHaveText(@NotNull EditText editText) {
		if(mGetEditTrimText(editText).isEmpty()) {
			editText.setHintTextColor(_ErrorTextColor);
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * 確認輸入框的數字是否在某個區間
	 * @param min 最小值
	 * @param max 最大值
	 * @param editText 輸入框
	 * @return 是否再區間?
	 */
	public static boolean CheckNumberRange(int min, int max, EditText editText) {
		String numberStr = mGetEditTrimText(editText);
		if(numberStr.isEmpty()) numberStr = "0";
		int number = Integer.parseInt(numberStr);
		if(!((number >= min) && (number <= max))) {
			editText.setTextColor(_ErrorTextColor);
			editText.setHintTextColor(_ErrorTextColor);
			return false;
		}
		
		return true;
	}
	
	private static String mGetEditTrimText(@NotNull EditText editText) {
		return editText.getText().toString().trim();
	}
}
