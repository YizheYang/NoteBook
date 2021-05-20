package com.github.YizheYang.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.github.YizheYang.R;
import com.github.YizheYang.tools.MyAppCompatActivity;

public class SecretActivity extends MyAppCompatActivity {

	private EditText editText;
	private String password;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_secret);
		Intent intent = getIntent();
		password = intent.getStringExtra("password");
		editText = findViewById(R.id.matchPassword);
		Button commit = findViewById(R.id.matchCommit);
		Button cancel = findViewById(R.id.matchCancel);

		commit.setOnClickListener(v -> {
			if (editText.getText().toString().isEmpty()) {
				Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
			}else {
				if (!editText.getText().toString().equals(password)) {
					Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
				}else {
					Intent i = new Intent();
					i.putExtra("isCorrect", true);
					setResult(RESULT_OK, i);
					SecretActivity.this.finish();
				}
			}
		});
		cancel.setOnClickListener(v -> {
			Intent i = new Intent();
			i.putExtra("isCorrect", false);
			setResult(RESULT_OK, i);
			finish();
		});
	}
}
