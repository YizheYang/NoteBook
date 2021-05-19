package com.github.YizheYang.tools;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MyAppCompatActivity extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyUncaughtExceptionHandler handler = new MyUncaughtExceptionHandler(this);
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}
}
