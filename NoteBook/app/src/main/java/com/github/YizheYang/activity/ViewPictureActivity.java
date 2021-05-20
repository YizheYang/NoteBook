package com.github.YizheYang.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.github.YizheYang.R;
import com.github.YizheYang.tools.MyAppCompatActivity;

public class ViewPictureActivity extends MyAppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view);
		Intent intent = getIntent();
		String path = intent.getStringExtra("path");
		ImageView imageView = findViewById(R.id.view);
		Uri uri = Uri.parse(path);
		imageView.setImageURI(uri);
	}

}
