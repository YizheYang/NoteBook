package com.github.YizheYang.activity;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.github.YizheYang.R;
import com.github.YizheYang.tools.MyAppCompatActivity;
import com.github.YizheYang.tools.MyLog;

public class TestActivity extends MyAppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		Button button1 = findViewById(R.id.test_button1);
		button1.setOnClickListener(v -> {
			int i = 3;
			int j = 0;
			MyLog.d(this, String.valueOf(i/j));
		});
	}
}
