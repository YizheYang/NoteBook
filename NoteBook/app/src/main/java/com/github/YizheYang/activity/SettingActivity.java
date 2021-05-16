package com.github.YizheYang.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.YizheYang.MySQLiteOpenHelper;
import com.github.YizheYang.R;
import com.google.android.material.button.MaterialButton;

public class SettingActivity extends AppCompatActivity {
	private static final String TAG = "SettingActivity";
	private int color = R.color.white;

	private String path;
	private String password;
	private MySQLiteOpenHelper helper;
	private SQLiteDatabase db;

	private boolean colorChoose = false;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		helper = new MySQLiteOpenHelper(this, "NoteBook.db", null);
		db = helper.getWritableDatabase();

		Intent intent = getIntent();
		password = intent.getStringExtra("password");
		color = intent.getIntExtra("color", R.color.white);
		getWindow().getDecorView().setBackgroundColor(getResources().getColor(color));

		MaterialButton button1 = findViewById(R.id.setting_button1);
		button1.setOnClickListener(v -> {
			if (!colorChoose) {
				color = R.color.white;
				Toast.makeText(this, "恢复初始颜色", Toast.LENGTH_SHORT).show();
			}
			Intent it = new Intent();
			it.putExtra("color", color);
			setResult(RESULT_OK, it);
			finish();
		});

		MaterialButton button2 = findViewById(R.id.setting_button2);
		button2.setOnClickListener(v -> {
//			Intent intent = new Intent();
//			intent.setType("image/*");
//			intent.setAction(Intent.ACTION_GET_CONTENT);
//			startActivityForResult(intent, 1);
		});


		RadioGroup group1 = findViewById(R.id.radioGroup1);
		group1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId){
					case R.id.beijinghong:
						color = R.color.背景枣红;
						break;
					case R.id.beijinglv:
						color = R.color.背景绿;
						break;
					case R.id.beijinghuang:
						color = R.color.背景黄;
						break;
					case R.id.beijingzong:
						color = R.color.背景棕;
						break;
					case R.id.beijinglan:
						color = R.color.背景蓝;
						break;
					case R.id.zhongmuse:
						color = R.color.中木色;
						break;
					case R.id.beijingmibai:
						color = R.color.背景米白;
						break;
					default:
				}
				colorChoose = true;
				getWindow().getDecorView().setBackgroundColor(getResources().getColor(color));
			}
		});

		EditText oldPassword = findViewById(R.id.setting_oldPassword);
		EditText newPassword1 = findViewById(R.id.setting_newPassword1);
		EditText newPassword2 = findViewById(R.id.setting_newPassword2);
		MaterialButton button3 = findViewById(R.id.setting_button3);
		button3.setOnClickListener(v1 -> {
			if (oldPassword.getText().toString().isEmpty() || newPassword1.getText().toString().isEmpty() || newPassword2.getText().toString().isEmpty()) {
				Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
			}else {
				if (!oldPassword.getText().toString().equals(password)) {
					Toast.makeText(this, "旧密码错误", Toast.LENGTH_SHORT).show();
				}else {
					if (!newPassword1.getText().toString().equals(newPassword2.getText().toString())) {
						Toast.makeText(this, "两个新密码不相同", Toast.LENGTH_SHORT).show();
					}else {
						Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
						password = newPassword1.getText().toString();
						ContentValues values = new ContentValues();
						values.put("PASSWORD", password);
						db.update("Password", values, "PASSWORD=?", new String[]{oldPassword.getText().toString()});
						oldPassword.setText("");
						newPassword1.setText("");
						newPassword2.setText("");
					}
				}
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				path = uri.toString();
			}
		}
	}

}
