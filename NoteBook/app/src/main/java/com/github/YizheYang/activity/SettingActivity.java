package com.github.YizheYang.activity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.YizheYang.tools.MyAppCompatActivity;
import com.github.YizheYang.tools.MySQLiteOpenHelper;
import com.github.YizheYang.R;
import com.google.android.material.button.MaterialButton;

public class SettingActivity extends MyAppCompatActivity {

	private int color = R.color.white;
	private String path;
	private String password;
	private SQLiteDatabase db;
	private boolean colorChoose = false;
	private ImageView background;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this, "NoteBook.db", null);
		db = helper.getWritableDatabase();

		Intent intent = getIntent();
		password = intent.getStringExtra("password");
		color = intent.getIntExtra("color", R.color.white);
		path = intent.getStringExtra("path");
		getWindow().getDecorView().setBackgroundColor(getResources().getColor(color));
		background = findViewById(R.id.setting_background);
		if (path != null && !path.equals("")) {
			handler.sendEmptyMessage(1);
		}

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
			Intent it = new Intent();
			it.setType("image/*");
			it.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(it, 1);
		});

		MaterialButton button3 = findViewById(R.id.setting_button3);
		button3.setOnClickListener(v -> {
			Intent it = new Intent();
			it.putExtra("path", path);
			setResult(RESULT_OK, it);
			finish();
		});

		RadioGroup group1 = findViewById(R.id.radioGroup1);
		group1.setOnCheckedChangeListener((group, checkedId) -> {
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
			}
			colorChoose = true;
			getWindow().getDecorView().setBackgroundColor(getResources().getColor(color));
		});

		EditText oldPassword = findViewById(R.id.setting_oldPassword);
		EditText newPassword1 = findViewById(R.id.setting_newPassword1);
		EditText newPassword2 = findViewById(R.id.setting_newPassword2);
		MaterialButton button4 = findViewById(R.id.setting_button4);
		button4.setOnClickListener(v1 -> {//修改密码的逻辑
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
				path = getPath(this, uri);
				Toast.makeText(this, "图片加载成功", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private final Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				Bitmap bmp = BitmapFactory.decodeFile(path);
				background.setImageBitmap(bmp);
			}
		}
	};

	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @author paulburke
	 */
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] {
						split[1]
				};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}

}
