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

import com.github.YizheYang.tools.MyAppCompatActivity;
import com.github.YizheYang.tools.MyTimer;
import com.github.YizheYang.R;
import com.github.YizheYang.layout.DrawView;
import com.github.YizheYang.layout.Title;

public class DrawActivity extends MyAppCompatActivity {

	private DrawView drawView;
	private int select_color = 0;
	private int select_size = 0;
	private int select_style = 0;

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

	/**
	 * 更改画笔的颜色
	 */
	public void showPaintColorDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("请选择颜色：").setSingleChoiceItems(R.array.paintColor, select_color, (dialog, which) -> {
			select_color = which;
			drawView.setPaintColor(which);
			dialog.dismiss();
		});
		builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
		builder.create().show();
	}

	/**
	 * 更改画笔的大小
	 */
	public void showPaintSizeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("请选择大小：").setSingleChoiceItems(R.array.paintSize, select_size, (dialog, which) -> {
			select_size = which;
			drawView.setPaintSize(which);
			dialog.dismiss();
		});
		builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
		builder.create().show();
	}

	/**
	 * 选择画笔或者是橡皮擦
	 */
	public void showMoreDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("请选择画笔或者橡皮擦：").setSingleChoiceItems(R.array.paintStyle, select_style, (dialog, which) -> {
			select_style = which;
			drawView.selectPaintStyle(which);
			dialog.dismiss();
		});
		builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
		builder.create().show();
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_draw);

		Title title = findViewById(R.id.draw_title);
		title.title.setText("画板");
		title.save.setOnClickListener(v -> {
			String name = "tempDraw_" + MyTimer.getTime();
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

		ImageButton revoke = findViewById(R.id.revoke);
		revoke.setOnClickListener(v -> drawView.revoke());
		ImageButton resume = findViewById(R.id.resume);
		resume.setOnClickListener(v -> drawView.resume());
		ImageButton remove = findViewById(R.id.remove);
		remove.setOnClickListener(v -> drawView.remove());
		ImageButton penSize = findViewById(R.id.penSize);
		penSize.setOnClickListener(v -> showPaintSizeDialog());
		ImageButton board = findViewById(R.id.board);
		board.setOnClickListener(v -> showPaintColorDialog());
		ImageButton eraser = findViewById(R.id.eraser);
		eraser.setOnClickListener(v -> showMoreDialog());
	}

}
