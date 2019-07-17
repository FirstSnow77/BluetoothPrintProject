package com.suda.bluetoothprintproject.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.suda.bluetoothprintproject.baseActivity.BaseToolbarActivity;
import com.suda.bluetoothprintproject.R;
import com.suda.bluetoothprintproject.utils.DialogUtil;

import static com.suda.bluetoothprintproject.businessManagers.bluetoothPrint.BluetoothPrintManager.MX20Type;
import static com.suda.bluetoothprintproject.businessManagers.bluetoothPrint.BluetoothPrintManager.R220Type;


/**
 * 這是選擇是要 小白單 | 託運單的 入口
 */
public class EntranceOneActivity extends BaseToolbarActivity implements View.OnClickListener
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_entrance_one);
		super.onCreate(savedInstanceState);
		super.checkUpdateWhenAppStartFromLauncher(new IfVersionIsNewest()
		{
			@Override public void doSomethingOnce() {
				EntranceOneActivity.super.showTitle("");
				findViewById(R.id.start_activity_Bluetooth_btn).setOnClickListener(EntranceOneActivity.this);
				findViewById(R.id.start_activity_InputA4Note_btn).setOnClickListener(EntranceOneActivity.this);
			}
		});
	}
	
	@Override public void onClick(View v) {
		super.onClick(v);
		switch(v.getId()) {
			case R.id.start_activity_Bluetooth_btn:
				final CharSequence[] barcodePrintType = new CharSequence[ 2 ];
				barcodePrintType[ 0 ] = MX20Type;
				barcodePrintType[ 1 ] = R220Type;
				DialogUtil.showListAlert(this, "請選擇標籤機型號", barcodePrintType, false,
						new DialogInterface.OnClickListener()
						{
							@Override public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(EntranceOneActivity.this, BluetoothActivity.class);
								intent.putExtra("barcodePrintType", barcodePrintType[ which ]);
								startActivity(intent);
							}
						});
				break;
			case R.id.start_activity_InputA4Note_btn:
				if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
					DialogUtil.showAlert(this, "版本不合", "該功能只支援 安卓版本 > 5.0 的設備使用");
				else
				{
					Intent intent2 = new Intent(EntranceOneActivity.this, EntranceInputA4NoteActivity.class);
					startActivity(intent2);
				}
				break;
		}
	}
}
