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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.YizheYang.MySQLiteOpenHelper;
import com.github.YizheYang.MyTimer;
import com.github.YizheYang.recyclerview.Note;
import com.github.YizheYang.R;
import com.github.YizheYang.layout.Title;

import java.io.FileNotFoundException;

public class SecondActivity extends AppCompatActivity {

	private static final String TAG = "SecondActivity";
	private int mode;
	private String editId;

	private static final int NEW_MODE = 21;
	private static final int EDIT_MODE = 22;
	private static final int RECORD_MODE = 31;
	private static final int PLAY_MODE = 32;

	private final int ADD_PICTURE = 1;
	private final int CAMERA = 2;
	private final int RECORD = 3;
	private final int DRAW = 4;

	private EditText title;
	private EditText content;

	private MyTimer myTimer;
	private MySQLiteOpenHelper helper;

	private int secret = 0;
	private CheckBox checkBox;

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
			}
		}
	};

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);

		helper = new MySQLiteOpenHelper(this, "NoteBook.db", null);
		helper.getWritableDatabase();
		myTimer = new MyTimer();
		title = findViewById(R.id.first_EditText);
		content = findViewById(R.id.second_EditText);
		Button testButton = findViewById(R.id.test2);
		checkBox = findViewById(R.id.checkbox);
		Title t = findViewById(R.id.second_title);

		Intent it = getIntent();
		int color = it.getIntExtra("color", R.color.white);
		getWindow().getDecorView().setBackgroundColor(getResources().getColor(color));
		mode = it.getIntExtra("mode", NEW_MODE);
		if (mode == EDIT_MODE) {
			t.title.setText("编辑");
			Bundle bundle = it.getBundleExtra("data");
			Note note = new Note(bundle.getString("id"), bundle.getString("title"), bundle.getString("content"), bundle.getString("date"));
			Message message = new Message();
			message.what = 1;
			message.obj = note;
			handler.sendMessage(message);
		}else {
			t.title.setText("新建");
		}

		testButton.setOnClickListener(v -> {
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
			intent.putExtra("color", color);
			startActivityForResult(intent, RECORD);
		});

		ImageButton ibt4 = findViewById(R.id.draw);
		ibt4.setOnClickListener(v -> {
			Intent intent = new Intent(SecondActivity.this, DrawActivity.class);
			startActivityForResult(intent, DRAW);
		});

		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					secret = 1;
				}else if (!isChecked) {
					secret = 0;
				}
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
				values.put("DATE", myTimer.getDate());
				values.put("SECRET", secret);
				db.insert("Note", null, values);
			}
			Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
			setResult(RESULT_OK);
			SecondActivity.this.finish();
		});

		EditText content = findViewById(R.id.second_EditText);
		content.setOnClickListener(v -> {
//				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(content.getWindowToken(), 0);
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
//				imm.showSoftInput(content, 0);
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Log.d(TAG, "data: " + data);
			Uri uri = data.getData();
			ContentResolver cr = SecondActivity.this.getContentResolver();
			Bitmap bm = null;
			Bundle bd = null;
			String location = null;
			switch (requestCode) {
				case ADD_PICTURE:
					try {
						bm = BitmapFactory.decodeStream(cr.openInputStream(uri));
						Log.d(TAG, "uri: " + uri.getPath());
						location = getPath(SecondActivity.this, uri);
						Log.d(TAG, "location: " + location);
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

	private void viewPicture(Bitmap bitmap, Uri uri) {
	 if (bitmap == null) {
		 return;
	 }
	 Intent intent = new Intent(SecondActivity.this, ViewPictureActivity.class);
	 intent.putExtra("path", uri.toString());
	 startActivity(intent);
//	 Intent intent = new Intent(Intent.ACTION_VIEW);
//	 intent.setDataAndType(uri, "image/*");
//	 startActivity(intent);
	}

	private void playRecord(Uri uri) {
		Intent intent = new Intent(SecondActivity.this, RecordActivity.class);
		intent.putExtra("mode", PLAY_MODE);
		Bundle bundle = new Bundle();
		bundle.putString("path", uri.toString());
		intent.putExtra("data", bundle);
		startActivity(intent);
	}

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
					bm = BitmapFactory.decodeResource(getResources(), R.drawable.record);
					scaleBitmap(bm, location);
					continue;
				} else if (type.equals(".jpg")) {
					location = string;
					bm = BitmapFactory.decodeFile(location);
					scaleBitmap(bm, location);
					continue;
				}
			}
			content.append(string);
		}
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
}
