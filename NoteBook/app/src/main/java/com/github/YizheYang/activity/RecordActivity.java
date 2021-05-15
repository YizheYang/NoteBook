package com.github.YizheYang.activity;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.VolumeShaper;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.YizheYang.MyTimer;
import com.github.YizheYang.R;
import com.github.YizheYang.layout.Title;

import java.io.File;
import java.io.IOException;
import java.util.Timer;

public class RecordActivity extends AppCompatActivity {

	private static final String TAG = "RecordActivity";

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
	private Timer timer;
	private ImageButton save;
	private boolean recording = false;

	@RequiresApi(api = Build.VERSION_CODES.Q)
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);

		time = findViewById(R.id.record_time);
		start = findViewById(R.id.record_start);
		finish = findViewById(R.id.record_finish);
		titleLayout = findViewById(R.id.record_title);

		Intent it = getIntent();
		mode = it.getIntExtra("mode", RECORD_MODE);
		if (mode == PLAY_MODE) {
			Bundle bundle = it.getBundleExtra("data");
			playPath = bundle.getString("path");
			titleLayout.title.setText(playPath.substring(60));
		}

		path = ContextCompat.getExternalFilesDirs(getApplicationContext(), null)[0].getAbsolutePath();
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		if (mode == RECORD_MODE) {
			start.setOnClickListener(v1 -> {
				if (name != null) {
					File f = new File(path, name);
					f.delete();
				}
				MyTimer mt = new MyTimer();
				name = "record_" + mt.getTime() + ".amr";
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
				//隐藏开始按钮，显示结束按钮
				Message message1 = new Message();
				message1.what = 0;
				handler.sendMessage(message1);
				//开始计时
				refreshTime.post(task);

				finish.setOnClickListener(v2 -> {
					refreshTime.removeCallbacks(task);
					try {
						mediaRecorder.stop();
					} catch (Exception e) {
						mediaRecorder = null;
						mediaRecorder = new MediaRecorder();
					}
					mediaRecorder.release();
					mediaRecorder = null;
					Message message2 = new Message();
					message2.what = 2;
					handler.sendMessage(message2);
					Toast.makeText(RecordActivity.this, "录制成功", Toast.LENGTH_SHORT).show();
				});

				titleLayout.save.setOnClickListener(v3 -> {
					if (mediaRecorder != null) {
						Toast.makeText(RecordActivity.this, "正在录音，请结束后再保存", Toast.LENGTH_SHORT).show();
						return;
					}
					Intent intent = getIntent();
					Bundle bundle = new Bundle();
					bundle.putString("audio", path + "/" + name);
					intent.putExtras(bundle);
					setResult(RESULT_OK, intent);
					RecordActivity.this.finish();
				});
			});
		} else if (mode == PLAY_MODE) {
			titleLayout.save.setVisibility(View.INVISIBLE);
			titleLayout.save.setClickable(false);

			MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					refreshTime.removeCallbacks(task);

					try {
						player.stop();
					}catch (Exception e) {
						player = null;
						player = new MediaPlayer();
					}
					player.release();
					player = null;
					Message message2 = new Message();
					message2.what = 2;
					handler.sendMessage(message2);
					Toast.makeText(RecordActivity.this, "播放完毕", Toast.LENGTH_SHORT).show();
				}
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

				Message message1 = new Message();
				message1.what = 0;
				handler.sendMessage(message1);

				refreshTime.post(task);
			});

			finish.setOnClickListener(v -> {
				refreshTime.removeCallbacks(task);
				try {
					player.stop();
				}catch (Exception e) {
					player = null;
					player = new MediaPlayer();
				}
				player.release();
				player = null;
				Message message2 = new Message();
				message2.what = 2;
				handler.sendMessage(message2);
			});
		}
	}

	private final Handler refreshTime = new Handler();
	private final Runnable task = new Runnable() {
		@Override
		public void run() {
			refreshTime.postDelayed(this,1000);
			Message message = new Message();
			message.what = 1;
			handler.sendMessage(message);
		}
	};

	private final Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 0:
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
					break;
				case 1:
					String[] oldTime = time.getText().toString().split(":");
					int hour = Integer.parseInt(oldTime[0]);
					int minute = Integer.parseInt(oldTime[1]);
					int second = Integer.parseInt(oldTime[2]);
					if(second < 59){
						second++;
					}
					else if(second == 59 && minute < 59){
						minute++;
						second = 0;
					}
					if(second == 59 && minute == 59 && hour < 98){
						hour++;
						minute = 0;
						second = 0;
					}
					oldTime[0] = hour + "";
					oldTime[1] = minute + "";
					oldTime[2] = second + "";
					if(second < 10)
						oldTime[2] = "0" + second;
					if(minute < 10)
						oldTime[1] = "0" + minute;
					if(hour < 10)
						oldTime[0] = "0" + hour;
					time.setText(oldTime[0] + ":" + oldTime[1] + ":" + oldTime[2]);
					break;
				case 2:
					finish.setClickable(false);
					finish.setVisibility(View.INVISIBLE);
					start.setClickable(true);
					start.setVisibility(View.VISIBLE);
					break;
			}
		}
	};

}
