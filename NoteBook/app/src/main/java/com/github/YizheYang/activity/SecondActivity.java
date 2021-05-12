package com.github.YizheYang.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.YizheYang.MyTimer;
import com.github.YizheYang.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

public class SecondActivity extends AppCompatActivity {

	private static final String TAG = "SecondActivity";
	private final int ADD_PICTURE = 1;
	private final int CAMERA = 2;
	private final int RECORD = 3;
	private final int DRAW = 4;
	private EditText title;
	private EditText content;
	private Button testButton;
	private ImageView testView;

	Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				String data = (String) msg.obj;
				Uri uri = Uri.parse(data);
				testView.setImageURI(uri);
			}
		}
	};

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);

		requestPower();
		title = findViewById(R.id.first_EditText);
		content = findViewById(R.id.second_EditText);
		testButton = findViewById(R.id.test2);
		testView = findViewById(R.id.testView);
		testButton.setOnClickListener(v -> {
			Message message = new Message();
			message.what = 1;
			message.obj = content.getText().toString();
			handler.sendMessage(message);
			Toast.makeText(SecondActivity.this, content.getText().toString(), Toast.LENGTH_LONG).show();
		});

		ImageButton ibt1 = findViewById(R.id.add_picture);
		ibt1.setOnClickListener(v -> {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, ADD_PICTURE);
		});

		ImageButton ibt2 = findViewById(R.id.camera);
		ibt2.setOnClickListener(v -> {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(intent, CAMERA);
		});

		ImageButton ibt3 = findViewById(R.id.record);
		ibt3.setOnClickListener(v -> {
			Intent intent = new Intent(SecondActivity.this, RecordActivity.class);
			startActivityForResult(intent, RECORD);
		});

		ImageButton ibt4 = findViewById(R.id.draw);
		ibt4.setOnClickListener(v -> {
			Intent intent = new Intent(SecondActivity.this, DrawActivity.class);
			startActivityForResult(intent, DRAW);
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Uri uri = data.getData();
			ContentResolver cr = SecondActivity.this.getContentResolver();
			Bitmap bm = null;
			Bundle bd = null;
			String location = null;
			switch (requestCode) {
				case ADD_PICTURE:
					try {
						bm = BitmapFactory.decodeStream(cr.openInputStream(uri));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					break;
				case CAMERA:
					try {
						if (uri != null) {
							bm = MediaStore.Images.Media.getBitmap(cr, uri);
						} else {
							bd = data.getExtras();
							bm = bd.getParcelable("data");
						}
						MyTimer myTimer = new MyTimer();
						String name = "temp_" + myTimer.getTime();
						location = Environment.getExternalStorageDirectory() + "/Pictures/" + name + ".jpg";
						String result = MediaStore.Images.Media.insertImage(getContentResolver(), bm, name, null);
						Log.d(TAG, "onActivityResult: " + result);
						Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(result));
						sendBroadcast(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case RECORD:
					bd = data.getExtras();
					location = bd.getString("audio");
					bm = BitmapFactory.decodeResource(getResources(), R.drawable.record);
					break;
				case DRAW:
					bd = data.getExtras();
					location = bd.getString("draw");
					bm = BitmapFactory.decodeFile(location);
					break;
				default:
			}

			int imgWidth = bm.getWidth();
			int imgHeight = bm.getHeight();
			double partion = imgWidth*1.0/imgHeight;
			double sqrtLength = Math.sqrt(partion*partion + 1);
			//新的缩略图大小
			double newImgW = 480*(partion / sqrtLength);
			double newImgH = 480*(1 / sqrtLength);
			float scaleW = (float) (newImgW/imgWidth);
			float scaleH = (float) (newImgH/imgHeight);

			Matrix mx = new Matrix();
			//对原图片进行缩放
			mx.postScale(scaleW, scaleH);
			bm = Bitmap.createBitmap(bm, 0, 0, imgWidth, imgHeight, mx, true);
			final ImageSpan imageSpan = new ImageSpan(this, bm);
			if (uri != null) {
				location = uri.toString();
			}
			SpannableString spannableString = new SpannableString(location);
			spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_MARK_MARK);
			//光标移到下一行
//			content.append("\n");
			Editable editable = content.getEditableText();
			int selectionIndex = content.getSelectionStart();
			spannableString.getSpans(0, spannableString.length(), ImageSpan.class);

			//将图片添加进EditText中
			editable.insert(selectionIndex, spannableString);
			//添加图片后自动空出两行
//			content.append("\n");

		}
	}

	/**
	 * 请求所需的权限
	 */
	private void requestPower() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED) {
			//refuse == true
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				Toast.makeText(SecondActivity.this, "请同意授权以保证程序正常运行", Toast.LENGTH_SHORT).show();
			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
						Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 1);
			}
		}
	}


}
