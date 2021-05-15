package com.github.YizheYang.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.YizheYang.R;

public class ViewPictureActivity extends AppCompatActivity {

	private ImageView imageView;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view);
		Intent intent = getIntent();
		String path = intent.getStringExtra("path");
		imageView = findViewById(R.id.view);
		Uri uri = Uri.parse(path);
		Message message = new Message();
		message.obj = uri;
		handler.sendMessage(message);
	}

	Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			Uri uri = (Uri) msg.obj;
			imageView.setImageURI(uri);
		}
	};

}
