package com.github.YizheYang.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.github.YizheYang.tools.MyAppCompatActivity;
import com.github.YizheYang.tools.MyLog;
import com.github.YizheYang.tools.MyTimer;
import com.github.YizheYang.R;
import com.github.YizheYang.layout.Title;

import java.io.File;
import java.io.IOException;

public class RecordActivity extends MyAppCompatActivity {

	private static final int RECORD_MODE = 31;
	private static final int PLAY_MODE = 32;
	private int mode;
	private String playPath;
	private MediaPlayer player;

	private TextView time;
	private ImageButton start;
	private ImageButton finish;
	private Title titleLayout;

	private String path;// = Environment.getExternalStorageDirectory().getAbsolutePath();
	private String name = null;
	private MediaRecorder mediaRecorder;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mediaRecorder != null) {
				Toast.makeText(this, "请结束录音再退出本页", Toast.LENGTH_SHORT).show();
				return false;
			}
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

	@RequiresApi(api = Build.VERSION_CODES.Q)
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);

		time = findViewById(R.id.record_time);
		start = findViewById(R.id.record_start);
		finish = findViewById(R.id.record_finish);
		titleLayout = findViewById(R.id.record_title);
		ImageView background = findViewById(R.id.second_background);

		Intent it = getIntent();
		int color = it.getIntExtra("color", R.color.white);
		getWindow().getDecorView().setBackgroundColor(getResources().getColor(color));
		String bgPath = it.getStringExtra("path");
		if (bgPath != null && !bgPath.equals("")) {
			Bitmap bitmap = BitmapFactory.decodeFile(bgPath);
			background.setImageBitmap(bitmap);
		}
		mode = it.getIntExtra("mode", RECORD_MODE);
		if (mode == PLAY_MODE) {
			Bundle bundle = it.getBundleExtra("data");
			playPath = bundle.getString("path");
			titleLayout.title.setText(playPath.substring(60));
		}

		path = ContextCompat.getExternalFilesDirs(getApplicationContext(), null)[0].getAbsolutePath();
		Log.d("TAG", "onCreate: " + path);
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		if (mode == RECORD_MODE) {
			start.setOnClickListener(v -> {
				if (name != null) {//可以重复多次开始，只保存最后一次
					File f = new File(path, name);
					f.delete();
				}
				name = "record_" + MyTimer.getTime() + ".amr";
				MyLog.d(this, "name:" + name);
				File file1 = new File(path, name);
				if (!file1.exists()) {
					try {
						file1.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				mediaRecorder = new MediaRecorder();
				mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				mediaRecorder.setOutputFile(path + "/" + name);
				try {
					mediaRecorder.prepare();
					mediaRecorder.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
				whenStart();
				//开始计时
				handler.post(task);
			});

			finish.setOnClickListener(v -> {
				handler.removeCallbacks(task);
				try {
					mediaRecorder.stop();
				} catch (Exception e) {
					mediaRecorder = null;
					mediaRecorder = new MediaRecorder();
				}
				mediaRecorder.release();
				mediaRecorder = null;
				whenFinish();
				Toast.makeText(RecordActivity.this, "录制成功", Toast.LENGTH_SHORT).show();
			});

			titleLayout.save.setOnClickListener(v -> {
				if (mediaRecorder != null) {
					Toast.makeText(RecordActivity.this, "正在录音，请结束后再保存", Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("audio", path + "/" + name);
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
				RecordActivity.this.finish();
			});

			titleLayout.back.setOnClickListener(v -> {
				if (mediaRecorder != null) {
					Toast.makeText(this, "请结束录音再退出本页", Toast.LENGTH_SHORT).show();
					return;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(this)
						.setTitle("警告:")
						.setMessage("是否退出本页？");
				builder.setPositiveButton("是", (dialog, which) -> {
					if (name != null) {
						File f = new File(path, name);
						f.delete();
					}
					this.finish();
					dialog.dismiss();
				});
				builder.setNegativeButton("否", (dialog, which) -> dialog.dismiss());
				builder.create();
				builder.show();
			});
		} else if (mode == PLAY_MODE) {
			titleLayout.save.setVisibility(View.INVISIBLE);
			MediaPlayer.OnCompletionListener onCompletionListener = mp -> {
				handler.removeCallbacks(task);
				try {
					player.stop();
				}catch (Exception e) {
					player = null;
					player = new MediaPlayer();
				}
				player.release();
				player = null;
				whenFinish();
				Toast.makeText(RecordActivity.this, "播放完毕", Toast.LENGTH_SHORT).show();
			};

			start.setOnClickListener(v -> {
				player = new MediaPlayer();
				player.setOnCompletionListener(onCompletionListener);
				try {
					player.setDataSource(playPath);
					player.prepare();
					player.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
				whenStart();
				handler.post(task);
			});

			finish.setOnClickListener(v -> {
				handler.removeCallbacks(task);
				try {
					player.stop();
				}catch (Exception e) {
					player = null;
					player = new MediaPlayer();
				}
				player.release();
				player = null;
				whenFinish();
			});
		}
	}

	/**
	 * 更新录制/播放时间
	 */
	private final Runnable task = new Runnable() {
		@Override
		public void run() {
			String[] oldTime = time.getText().toString().split(":");
			int hour = Integer.parseInt(oldTime[0]);
			int minute = Integer.parseInt(oldTime[1]);
			int second = Integer.parseInt(oldTime[2]);
			if (second < 59) {
				second++;
			} else if (second == 59 && minute < 59) {
				minute++;
				second = 0;
			}
			if (second == 59 && minute == 59 && hour < 98) {
				hour++;
				minute = 0;
				second = 0;
			}
			oldTime[0] = hour + "";
			oldTime[1] = minute + "";
			oldTime[2] = second + "";
			if (second < 10)
				oldTime[2] = "0" + second;
			if (minute < 10)
				oldTime[1] = "0" + minute;
			if (hour < 10)
				oldTime[0] = "0" + hour;
			time.setText(oldTime[0] + ":" + oldTime[1] + ":" + oldTime[2]);
			handler.postDelayed(this,1000);
		}
	};

	private final Handler handler = new Handler(Looper.getMainLooper());

	/**
	 * 显示开始后的界面
	 */
	private void whenStart() {
		if (!time.getText().toString().equals(getResources().getString(R.string._00_00_00))) {
			time.setText(getResources().getString(R.string._00_00_00));
		}
		if (mode == RECORD_MODE) {
			titleLayout.title.setText(name);
		}
		start.setVisibility(View.INVISIBLE);
		start.setClickable(false);
		finish.setVisibility(View.VISIBLE);
		finish.setClickable(true);
	}

	/**
	 * 显示停止后的界面
	 */
	private void whenFinish() {
		finish.setClickable(false);
		finish.setVisibility(View.INVISIBLE);
		start.setClickable(true);
		start.setVisibility(View.VISIBLE);
	}

}
