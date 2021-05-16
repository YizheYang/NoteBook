package com.github.YizheYang.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.YizheYang.MyTimer;
import com.github.YizheYang.R;
import com.github.YizheYang.layout.DrawView;
import com.github.YizheYang.layout.Title;

public class DrawActivity extends AppCompatActivity {

	private Title title;
	private DrawView drawView;
	private ImageButton revoke;
	private ImageButton resume;
	private ImageButton remove;
	private ImageButton penSize;
	private ImageButton board;
	private ImageButton eraser;

	private int select_color = 0;
	private int select_size = 0;
	private int select_style = 0;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_draw);

		title = findViewById(R.id.draw_title);
		title.save.setOnClickListener(v -> {
			MyTimer mt = new MyTimer();
			String name = "tempDraw_" + mt.getTime();
			String result = MediaStore.Images.Media.insertImage(getContentResolver(), drawView.getBitmap(), name, null);
			Intent it = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(result));
			sendBroadcast(it);
			Intent intent = getIntent();
			Bundle bundle = new Bundle();
			bundle.putString("draw", Environment.getExternalStorageDirectory() + "/Pictures/" + name + ".jpg");
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			DrawActivity.this.finish();
		});

		drawView = findViewById(R.id.drawView);

		revoke = findViewById(R.id.revoke);
		revoke.setOnClickListener(v -> {
			drawView.revoke();
		});
		resume = findViewById(R.id.resume);
		resume.setOnClickListener(v -> {
			drawView.resume();
		});
		remove = findViewById(R.id.remove);
		remove.setOnClickListener(v -> {
			drawView.remove();
		});
		penSize = findViewById(R.id.penSize);
		penSize.setOnClickListener(v -> {
			showPaintSizeDialog(drawView);
		});
		board = findViewById(R.id.board);
		board.setOnClickListener(v -> {
			showPaintColorDialog(drawView);
		});
		eraser = findViewById(R.id.eraser);
		eraser.setOnClickListener(v -> {
			showMoreDialog(drawView);
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setTitle("警告:")
					.setMessage("是否退出本页？");
			builder.setPositiveButton("是", (dialog, which) -> {
				this.finish();
				dialog.dismiss();
			});
			builder.setNegativeButton("否", (dialog, which) -> dialog.dismiss());
			builder.create();
			builder.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void showPaintColorDialog(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("请选择颜色：").setSingleChoiceItems(R.array.paintColor, select_color, (dialog, which) -> {
			select_color = which;
			drawView.setPaintColor(which);
			dialog.dismiss();
		});
		builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
		builder.create().show();
	}

	public void showPaintSizeDialog(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("请选择大小：").setSingleChoiceItems(R.array.paintSize, select_size, (dialog, which) -> {
			select_size = which;
			drawView.setPaintSize(which);
			dialog.dismiss();
		});
		builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
		builder.create().show();
	}

	public void showMoreDialog(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("请选择画笔或者橡皮擦：").setSingleChoiceItems(R.array.paintStyle, select_style, (dialog, which) -> {
			select_style = which;
			drawView.selectPaintStyle(which);
			dialog.dismiss();
		});
		builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
		builder.create().show();
	}

}
