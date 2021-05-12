package com.github.YizheYang.layout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.github.YizheYang.R;

public class Title extends LinearLayout {

	public ImageButton back;
	public ImageButton save;

	public Title(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.title, this);
		back = findViewById(R.id.back);
		save = findViewById(R.id.save);
		back.setOnClickListener(v -> {
			AlertDialog.Builder builder = new AlertDialog.Builder(context)
					.setTitle("警告:")
					.setMessage("是否退出本页？");
				builder.setPositiveButton("是", (dialog, which) -> {
					((Activity)getContext()).finish();
					dialog.dismiss();
				});
				builder.setNegativeButton("否", (dialog, which) -> dialog.dismiss());
				builder.create();
				builder.show();
		});
	}

}
