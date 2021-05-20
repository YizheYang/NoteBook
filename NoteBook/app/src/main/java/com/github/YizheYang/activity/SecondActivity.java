package com.github.YizheYang.activity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.YizheYang.tools.MyAppCompatActivity;
import com.github.YizheYang.tools.MyLog;
import com.github.YizheYang.tools.MySQLiteOpenHelper;
import com.github.YizheYang.tools.MyTimer;
import com.github.YizheYang.recyclerview.Note;
import com.github.YizheYang.R;
import com.github.YizheYang.layout.Title;

import java.io.FileNotFoundException;

public class SecondActivity extends MyAppCompatActivity {

	private int mode;
	private String editId;

	private static final int NEW_MODE = 21;
	private static final int EDIT_MODE = 22;
	private static final int RECORD_MODE = 31;
	private static final int PLAY_MODE = 32;
	private static final int ADD_PICTURE = 1;
	private static final int CAMERA = 2;
	private static final int RECORD = 3;
	private static final int DRAW = 4;

	private EditText title;
	private EditText content;
	private MySQLiteOpenHelper helper;
	private int secret = 0;
	private CheckBox checkBox;
	private ImageView background;
	private String path;

	Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				Note note = (Note) msg.obj;
				editId = note.id;
				title.setText(note.title);
				loadEditData(note.content);
				secret = note.secret;
				if (secret == 1) {
					checkBox.setChecked(true);
				}
			}else if (msg.what == 2) {
				Bitmap bitmap = BitmapFactory.decodeFile(path);
				background.setImageBitmap(bitmap);
			}
		}
	};

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);

		helper = new MySQLiteOpenHelper(this, "NoteBook.db", null);
		helper.getWritableDatabase();
		title = findViewById(R.id.first_EditText);
		content = findViewById(R.id.second_EditText);
		Button testButton = findViewById(R.id.test2);
		checkBox = findViewById(R.id.checkbox);
		Title t = findViewById(R.id.second_title);

		Intent it = getIntent();
		int color = it.getIntExtra("color", R.color.white);
		getWindow().getDecorView().setBackgroundColor(getResources().getColor(color));
		path = it.getStringExtra("path");
		background = findViewById(R.id.second_background);
		if (path != null && !path.equals("")) {
			handler.sendEmptyMessage(2);
		}

		mode = it.getIntExtra("mode", NEW_MODE);
		if (mode == EDIT_MODE) {
			t.title.setText("编辑");
			Bundle bundle = it.getBundleExtra("data");
			Note note = new Note(bundle.getString("id"), bundle.getString("title")
					, bundle.getString("content"), bundle.getString("date"), bundle.getInt("secret"));
			Message message = new Message();
			message.what = 1;
			message.obj = note;
			handler.sendMessage(message);
		}else {
			t.title.setText("新建");
		}

		testButton.setOnClickListener(v -> Toast.makeText(SecondActivity.this, content.getText().toString(), Toast.LENGTH_LONG).show());

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
			intent.putExtra("color", color);
			startActivityForResult(intent, RECORD);
		});

		ImageButton ibt4 = findViewById(R.id.draw);
		ibt4.setOnClickListener(v -> {
			Intent intent = new Intent(SecondActivity.this, DrawActivity.class);
			startActivityForResult(intent, DRAW);
		});

		checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (isChecked) {
				secret = 1;
			}else {
				secret = 0;
			}
		});

		t.save.setOnClickListener(v -> {
			SQLiteDatabase db = helper.getWritableDatabase();
			if (mode == EDIT_MODE) {
				ContentValues values = new ContentValues();
				values.put("TITLE", title.getText().toString());
				values.put("CONTENT", content.getText().toString());
				values.put("SECRET", secret);
				db.update("Note", values, "ID=?", new String[]{editId});
			} else if (mode == NEW_MODE){
				ContentValues values = new ContentValues();
				values.put("TITLE", title.getText().toString());
				values.put("CONTENT", content.getText().toString());
				values.put("DATE", MyTimer.getDate());
				values.put("SECRET", secret);
				db.insert("Note", null, values);
			}
			Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
			setResult(RESULT_OK);
			SecondActivity.this.finish();
		});

		EditText content = findViewById(R.id.second_EditText);
		content.setOnClickListener(v -> {
			//让编辑框里的图片或者录音能够在被点击时正确地打开
//			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//			imm.hideSoftInputFromWindow(content.getWindowToken(), 0);
			Spanned s = content.getText();
			ImageSpan[] imageSpans = s.getSpans(0, s.length(), ImageSpan.class);
			int selectionStart = content.getSelectionStart();
			for (ImageSpan span : imageSpans) {
				int start = s.getSpanStart(span);
				int end = s.getSpanEnd(span);
				if (selectionStart >= start && selectionStart < end){//找到图片
					String lct = content.getText().toString().substring(start, end);
					String type = lct.substring(lct.length() - 4);
					Uri uri = Uri.parse(lct);
					Bitmap bitmap;
					if(type.equals(".amr")){
						playRecord(uri);
					} else if (type.equals(".jpg")){
						bitmap = BitmapFactory.decodeFile(lct);
						viewPicture(bitmap, uri);
					}
					return;
				}
			}
			//打开软键盘
//			imm.showSoftInput(content, 0);
		});

	}

	/**
	 * 对活动的返回值进行处理，具体为加上指定的图片
	 * @param requestCode 启动别的活动时的请求码
	 * @param resultCode 别的活动返回的结果码
	 * @param data 别的活动返回的数据
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			MyLog.d(this, "data: " + data);
			Uri uri = data.getData();
			ContentResolver cr = SecondActivity.this.getContentResolver();
			Bitmap bm = null;
			Bundle bd;
			String location = null;
			switch (requestCode) {
				case ADD_PICTURE:
					try {
						bm = BitmapFactory.decodeStream(cr.openInputStream(uri));
						MyLog.d(this, "uri: " + uri.getPath());
						location = getPath(SecondActivity.this, uri);
						MyLog.d(this, "location: " + location);
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
						String name = "temp_" + MyTimer.getTime();
						location = Environment.getExternalStorageDirectory() + "/Pictures/" + name + ".jpg";
						String result = MediaStore.Images.Media.insertImage(getContentResolver(), bm, name, null);
						MyLog.d(this, "Result: " + result);
						Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(result));
						sendBroadcast(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case RECORD:
					bd = data.getExtras();
					location = bd.getString("audio");
					bm = BitmapFactory.decodeResource(getResources(), R.drawable.baseline_mic_black_48);
					break;
				case DRAW:
					bd = data.getExtras();
					location = bd.getString("draw");
					bm = BitmapFactory.decodeFile(location);
					break;
				default:
			}
		scaleBitmap(bm,location);
		}
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

	/**
	 * 将选中的图片压缩并插入到编辑框中
	 * @param bm 已经选中的图片
	 * @param location 该图片在手机上的位置
	 */
	private void scaleBitmap(Bitmap bm, String location) {
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
		SpannableString spannableString = new SpannableString(location);
		spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_MARK_MARK);
		//光标移到下一行
		content.append("\n");
		Editable editable = content.getEditableText();
		int selectionIndex = content.getSelectionStart();
		spannableString.getSpans(0, spannableString.length(), ImageSpan.class);
		//将图片添加进EditText中
		editable.insert(selectionIndex, spannableString);
		//添加图片后自动空出两行
		content.append("\n");
	}

	/**
	 * 查看图片
	 * @param bitmap 想要查看的图片
	 * @param uri 该图片在手机上的地址
	 */
	private void viewPicture(Bitmap bitmap, Uri uri) {
		if (bitmap == null) {
			return;
		}
		Intent intent = new Intent(SecondActivity.this, ViewPictureActivity.class);
		intent.putExtra("path", uri.toString());
		startActivity(intent);
		//一开始用系统图库查看，后来决定自己写一个查看器
		//	 Intent intent = new Intent(Intent.ACTION_VIEW);
		//	 intent.setDataAndType(uri, "image/*");
		//	 startActivity(intent);
	}

	/**
	 * 播放选中的录音，具体为用播放模式启动RecordActivity
	 * @param uri 录音在本地的位置
	 */
	private void playRecord(Uri uri) {
		Intent intent = new Intent(SecondActivity.this, RecordActivity.class);
		intent.putExtra("mode", PLAY_MODE);
		Bundle bundle = new Bundle();
		bundle.putString("path", uri.toString());
		intent.putExtra("data", bundle);
		startActivity(intent);
	}

	/**
	 * 加载未处理过的含有路径的内容，将文件路径替换成对应的形式
	 * @param c 提取出来的字符串
	 */
	private void loadEditData(String c) {
		String[] strings = c.split("\n");
		for (String string : strings) {
			String type = null;
			if (string.length() > 4) {
				type = string.substring(string.length() - 4);
			}
			Bitmap bm;
			String location;
			if (type != null) {
				if (type.equals(".amr")) {
					location = string;
					bm = BitmapFactory.decodeResource(getResources(), R.drawable.baseline_mic_black_48);
					scaleBitmap(bm, location);
					continue;
				} else if (type.equals(".jpg") || type.equals("jpeg") || type.equals(".png") || type.equals(".bmp")
						|| type.equals(".ico") || type.equals("wbmp") || type.equals("webp") || type.equals(".gif")) {
					location = string;
					bm = BitmapFactory.decodeFile(location);
					scaleBitmap(bm, location);
					continue;
				}
			}
			content.append(string + "\n");
		}
	}

}
