package com.suda.bluetoothprintproject.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suda.bluetoothprintproject.R;

/**
 * 一個畫面插件，給使用者調整數量。
 * 主要是與其他布局做搭配使用。
 */
public class AddSubView extends LinearLayout implements View.OnClickListener
{
	private TextView tv_number;
	private int currentValue = 1; // 當前值
	private int minValue = 1; // 最小許可值
	private int maxValue = 10; // 最大許可值
	
	public AddSubView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		
		// 布局文件實力化, 並加載到 context 中
		View.inflate(context, R.layout.add_sub_view, this);
		this.tv_number = findViewById(R.id.tv_number);
		
		this.setCurrentValue(this.getCurrentValue());
		
		findViewById(R.id.iv_sub).setOnClickListener(this);
		findViewById(R.id.iv_add).setOnClickListener(this);
	}
	
	/**
	 * 取得當前數值
	 * @return 當前數值
	 */
	public int getCurrentValue()
	{
		
		String strValue = this.tv_number.getText().toString();
		if(!TextUtils.isEmpty(strValue))
		{
			this.currentValue = Integer.parseInt(strValue);
		}
		return this.currentValue;
	}
	
	/**
	 * 依參數改變當前數值
	 * @param currentValue 數值
	 */
	@SuppressLint("SetTextI18n") public void setCurrentValue(int currentValue)
	{
		this.currentValue = currentValue;
		this.tv_number.setText(currentValue + ""); // 不知為何要加 "" 才不會抱錯
	}
	
	public int getMinValue()
	{
		return this.minValue;
	}
	
	public void setMinValue(int minValue)
	{
		this.minValue = minValue;
	}
	
	public int getMaxValue()
	{
		return this.maxValue;
	}
	
	public void setMaxValue(int maxValue)
	{
		this.maxValue = maxValue;
	}
	
	/**
	 * 設定標題名稱
	 * @param text 標題名稱
	 */
	public void setViewText(String text)
	{
		((TextView)findViewById(R.id.add_sub_view_Text1)).setText(text);
	}
	
	@Override public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.iv_sub:
				this._subNumber();
				break;
			case R.id.iv_add:
				this._addNumber();
				break;
		}
	}
	
	private void _subNumber()
	{
		if(currentValue > minValue)
		{
			currentValue--;
		}
		setCurrentValue(currentValue);
		if(onAddSubClickListener != null)
		{
			onAddSubClickListener.onNumberChange(currentValue);
		}
	}
	
	private void _addNumber()
	{
		if(currentValue < maxValue)
		{
			currentValue++;
		}
		setCurrentValue(currentValue);
		if(onAddSubClickListener != null)
		{
			onAddSubClickListener.onNumberChange(currentValue);
		}
	}
	
	// 給外部注入使用的 callback
	private OnAddSubClickListener onAddSubClickListener;
	
	public void setOnAddSubClickListener(OnAddSubClickListener onAddSubClickListener)
	{
		this.onAddSubClickListener = onAddSubClickListener;
	}
	
	/**
	 * 給外部注入 callback 使用
	 */
	public interface OnAddSubClickListener
	{
		void onNumberChange(int value);
	}
}
