package com.suda.bluetoothprintproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.suda.bluetoothprintproject.baseActivity.BaseToolbarActivity;
import com.suda.bluetoothprintproject.R;

/**
 * 託運單選擇 B客 | C客 的入口
 */
public class EntranceInputA4NoteActivity extends BaseToolbarActivity implements View.OnClickListener
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_entrance_input_a4_note);
		super.onCreate(savedInstanceState);
		super.checkUpdateWhenAppStartFromLauncher(new IfVersionIsNewest() {
			
			@Override public void doSomethingOnce() {
				EntranceInputA4NoteActivity.this.uiInit();
			}
		});
	}
	
	private void uiInit()
	{
		super.showTitle("");
		super.showBack();
		super.showGoHome();
		findViewById(R.id.start_c_inputA4Note_step).setOnClickListener(this);
		findViewById(R.id.start_b_inputA4Note_step).setOnClickListener(this);
	}
	
	@Override public void onClick(View v) {
		super.onClick(v);
		
		Intent intent = null;
		switch(v.getId())
		{
			case R.id.start_c_inputA4Note_step:
				intent = new Intent(this, InputA4NoteStep1Activity.class);
				intent.putExtra("WhoSelected", "C_Customer");
				break;
			case R.id.start_b_inputA4Note_step:
				intent = new Intent(this, InputA4NoteStep1Activity.class);
				intent.putExtra("WhoSelected", "B_Customer");
				break;
		}
		if(intent != null)
			startActivity(intent);
	}
}
