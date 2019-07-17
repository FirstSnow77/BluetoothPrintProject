package com.suda.bluetoothprintproject;

import android.content.Intent;
import android.os.Bundle;

import com.suda.bluetoothprintproject.activity.LoginActivity;
import com.suda.bluetoothprintproject.baseActivity.BaseUpdateCheckActivity;

public class MainActivity extends BaseUpdateCheckActivity
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		super.checkUpdateWhenAppStartFromLauncher(new IfVersionIsNewest() {
			@Override public void doSomethingOnce()
			{
				Intent intent = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}
	
	
}
