package com.github.YizheYang.activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.YizheYang.MySQLiteOpenHelper;
import com.github.YizheYang.R;
import com.github.YizheYang.layout.SearchLayout;
import com.github.YizheYang.recyclerview.Note;
import com.github.YizheYang.recyclerview.NoteAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

	private static final int spanCount = 2;
	private static final int NEW_MODE = 21;
	private static final int EDIT_MODE = 22;
	private static final int REQUEST_SECOND = 1;
	private static final int REQUEST_SETTING = 2;
	private static final int REQUEST_SECRET = 3;
	private static final int REQUEST_PERMISSION = 4;
	private static final String TAG = "MainActivity";

	private RecyclerView recyclerView;
	private NoteAdapter adapter;
	private NoteAdapter searchAdapter;
	private NoteAdapter secretAdapter;
	private final List<Note> noteList = new ArrayList<>();
	private MySQLiteOpenHelper helper;
	private SQLiteDatabase db;

	private SearchLayout search;
	private final List<Note> searchList = new ArrayList<>();
	private ListPopupWindow listPopupWindow = null;

	private boolean isExit = false;

	private LinearLayout linearLayout;
	private ImageView background;

	private int secret = 0;
	private static final int SECRET_MODE = 3;
	private boolean isSecret = false;
	private final List<Note> secretList = new ArrayList<>();
	private String password;

	private int color = R.color.white;
	private String path;

	private static final String[] permissionList = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
			, Manifest.permission.WRITE_EXTERNAL_STORAGE
			, Manifest.permission.CAMERA
			, Manifest.permission.RECORD_AUDIO};
	private AlertDialog alertDialog;
	private AlertDialog mDialog;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.mainItem:
				Toast.makeText(MainActivity.this, "1", Toast.LENGTH_SHORT).show();
				break;
			case R.id.setting:
				Intent intent = new Intent(MainActivity.this, SettingActivity.class);
				intent.putExtra("color", color);
				intent.putExtra("password", password);
				intent.putExtra("path", path);
				startActivityForResult(intent, REQUEST_SETTING);
				break;
			default:
		}
		return true;
	}

	private final Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if (msg.what == 0) {
				isExit = false;
			}else if (msg.what == 1) {
				secret = 0;
			}else if (msg.what == 2) {
				Bitmap bitmap = BitmapFactory.decodeFile(msg.obj.toString());
				if (bitmap == null) {
					Toast.makeText(MainActivity.this, "图片不存在", Toast.LENGTH_SHORT).show();
					path = null;
				}else {
					background.setImageBitmap(bitmap);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		requestPermission();
		helper = new MySQLiteOpenHelper(this, "NoteBook.db", null);
		db = helper.getWritableDatabase();
//		linearLayout = findViewById(R.id.main_linearLayout);
		background = findViewById(R.id.main_background);
		loadNoteFromSQLite();
		loadPasswordFromSQLite();
		loadColorFromSQLite();
		loadBackgroundFromSQLite();


		recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
		adapter = new NoteAdapter(this, noteList);
		recyclerView.setAdapter(adapter);
		adapter.setOnItemClickListener((view, position) -> startSecondActivityWithEditMode(noteList.get(position)));
		adapter.setOnLongClickListener((view, position) -> {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).setTitle("警告").setMessage("是否删除此笔记？")
					.setPositiveButton("yes", (dialog, which) -> {
						Note note = noteList.get(position);
						db.delete("Note", "ID=?", new String[]{note.id});
						noteList.remove(position);
						adapter.notifyDataSetChanged();
						dialog.dismiss();
					});
			builder.setNegativeButton("no", (dialog, which) -> dialog.dismiss());
			builder.create().show();
		});

		search = findViewById(R.id.main_search);
		search.editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				if (listPopupWindow != null) {
					listPopupWindow.dismiss();
				}
				searchList.clear();
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().equals("")) {
					searchList.clear();
					return;
				}
				if (!isSecret) {
					for (int i = 0; i < noteList.size(); i++) {
						Note note = noteList.get(i);
						if (note.content.length() >= s.length()) {
							if (isRepeat(replaceContent(note.content), s.toString())) {
								searchList.add(note);
							}
						}
					}
				}else {
					for (int i = 0; i < secretList.size(); i++) {
						Note note = secretList.get(i);
						if (note.content.length() >= s.length()) {
							if (isRepeat(replaceContent(note.content), s.toString())) {
								searchList.add(note);
							}
						}
					}
				}

			}

			@Override
			public void afterTextChanged(Editable s) {
				showListPopupWindow();
				if (search.editText.getText().toString().equals("")) {
					if (!isSecret){
						recyclerView.setAdapter(adapter);
					}else {
						recyclerView.setAdapter(secretAdapter);
					}
				}
			}
		});

		search.button.setOnClickListener(v -> {
			if (!search.editText.getText().toString().equals("")) {
				searchAdapter = new NoteAdapter(this, searchList);
				recyclerView.setAdapter(searchAdapter);
				searchAdapter.setOnItemClickListener((view, position) -> startSecondActivityWithEditMode(searchList.get(position)));
			}else if (search.editText.getText().toString().equals("")) {
				recyclerView.setAdapter(adapter);
			}
		});

		search.imageView.setOnClickListener(v -> {
			secret++;
			handler.sendEmptyMessageDelayed(1, 1000);
			if (!isSecret && secret == SECRET_MODE) {
				secret = 0;
				Intent intent = new Intent(MainActivity.this, SecretActivity.class);
				intent.putExtra("password", password);
				startActivityForResult(intent, 3);
			}
		});

		FloatingActionButton fab = findViewById(R.id.floatButton);
		fab.setOnClickListener(v -> {
			if (isSecret) {
				Toast.makeText(this, "退出隐私空间", Toast.LENGTH_SHORT).show();
				recyclerView.setAdapter(adapter);
				isSecret = false;
				return;
			}
			Intent intent = new Intent(MainActivity.this, SecondActivity.class);
			intent.putExtra("color", color);
			intent.putExtra("path", path);
			startActivityForResult(intent, REQUEST_SECOND);
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		db = helper.getWritableDatabase();
	}

	@Override
	protected void onResume() {
		super.onResume();
		db = helper.getWritableDatabase();
	}

	@Override
	protected void onStop() {
		super.onStop();
		db.close();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		db = helper.getWritableDatabase();
	}

	/**
	 * 实现第二次返回才退出软件，防止误触
	 * @param keyCode
	 * @param event
	 * @return
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (!isExit) {
				isExit = true;
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				handler.sendEmptyMessageDelayed(0, 1000);
			} else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_SECOND) {
			if (resultCode == RESULT_OK) {
				noteList.clear();
				loadNoteFromSQLite();
				adapter.notifyDataSetChanged();
			}
		} else if (requestCode == REQUEST_SETTING) {
			if (resultCode == RESULT_OK) {
				loadPasswordFromSQLite();
				int oldColor = color;
				color = data.getIntExtra("color", R.color.white);
				if (color != 0) {
					getWindow().getDecorView().setBackgroundColor(getResources().getColor(color));
					ContentValues values = new ContentValues();
					values.put("COLOR", color);
					db.update("Color", values, "COLOR=?", new String[]{String.valueOf(oldColor)});
				}
				String oldPath = path;
				path = data.getStringExtra("path");
				if (path != null && !path.equals("")) {
					Message message = new Message();
					message.what = 2;
					message.obj = path;
					handler.sendMessage(message);
					ContentValues values = new ContentValues();
					values.put("PATH", path);
					db.update("Background", values, "PATH=?", new String[]{oldPath});
				}
			}
		}else if (requestCode == REQUEST_SECRET) {
			if (resultCode == RESULT_OK) {
				isSecret = data.getBooleanExtra("isCorrect", false);
				if (isSecret) {
					Toast.makeText(MainActivity.this, "进入隐私空间", Toast.LENGTH_SHORT).show();
					secretAdapter = new NoteAdapter(this, secretList);
					recyclerView.setAdapter(secretAdapter);
					secretAdapter.setOnItemClickListener((view, position) -> startSecondActivityWithEditMode(secretList.get(position)));
				}
			}
		}else if (requestCode == REQUEST_PERMISSION) {
			requestPermission();
		}
	}

	/**
	 * 从数据库中加载笔记，包括普通笔记和私密笔记
	 */
	private void loadNoteFromSQLite() {
		db = helper.getWritableDatabase();
		Cursor cursor1 = db.query("Note", null, "SECRET=?", new String[]{String.valueOf(0)}, null, null, null);
		if (cursor1.moveToFirst()) {
			do {
				Note note = new Note(cursor1.getString(cursor1.getColumnIndex("ID"))
						, cursor1.getString(cursor1.getColumnIndex("TITLE"))
						, cursor1.getString(cursor1.getColumnIndex("CONTENT"))
						, cursor1.getString(cursor1.getColumnIndex("DATE"))
						, cursor1.getInt(cursor1.getColumnIndex("SECRET")));
				noteList.add(note);
			}while (cursor1.moveToNext());
		}
		Cursor cursor2 = db.query("Note", null, "SECRET=?", new String[]{String.valueOf(1)}, null, null, null);
		if (cursor2.moveToFirst()) {
			do {
				Note note = new Note(cursor2.getString(cursor2.getColumnIndex("ID"))
						, cursor2.getString(cursor2.getColumnIndex("TITLE"))
						, cursor2.getString(cursor2.getColumnIndex("CONTENT"))
						, cursor2.getString(cursor2.getColumnIndex("DATE"))
						, cursor2.getInt(cursor2.getColumnIndex("SECRET")));
				secretList.add(note);
			}while (cursor2.moveToNext());
		}
		cursor1.close();
		cursor2.close();
	}

	/**
	 * 从数据库中加载隐私空间密码
	 */
	private void loadPasswordFromSQLite() {
		db = helper.getWritableDatabase();
		Cursor cursor = db.query("Password", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				password = cursor.getString(cursor.getColumnIndex("PASSWORD"));
			}while (cursor.moveToNext());
		}
		cursor.close();
	}

	/**
	 * 从数据库中加载背景颜色
	 */
	private void loadColorFromSQLite() {
		db = helper.getWritableDatabase();
		Cursor cursor = db.query("Color", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				color = cursor.getInt(cursor.getColumnIndex("COLOR"));
			}while (cursor.moveToNext());
		}
		cursor.close();
		getWindow().getDecorView().setBackgroundColor(getResources().getColor(color));
	}

	/**
	 * 从数据库中加载背景图片的地址
	 */
	private void loadBackgroundFromSQLite() {
		db = helper.getWritableDatabase();
		Cursor cursor = db.query("Background", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				path = cursor.getString(cursor.getColumnIndex("PATH"));
			}while (cursor.moveToNext());
		}
		if (!path.equals("")) {
			Message message = new Message();
			message.what = 2;
			message.obj = path;
			handler.sendMessage(message);
		}
		cursor.close();
	}

	/**
	 * 请求所需的权限
	 */
	public void requestPermission() {
		if (!isAllPermit(permissionList)) {
			ActivityCompat.requestPermissions(this, permissionList, 1);
		}else {
			Toast.makeText(this, "您已经申请啦", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 判断是否全部权限已经允许
	 * @param permissions 需要的权限
	 * @return true为全部已授权 false为至少有一个没授权
	 */
	public boolean isAllPermit(String[] permissions) {
		for (String permission : permissions) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 对请求的返回结果进行处理，此处是对未授权的权限进行二次请求
	 * @param requestCode 请求时的请求码
	 * @param permissions 请求的权限
	 * @param grantResults	请求的结果
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1) {
			for (int i = 0;i < permissions.length;i++) {
				if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(this, "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
				}else {
					if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle("permission")
								.setMessage("点击允许才可以使用我们的app哦")
								.setPositiveButton("去允许", (dialog, id) -> {
									if (mDialog != null && mDialog.isShowing()) {
										mDialog.dismiss();
									}
									Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
									Uri uri = Uri.fromParts("package", getPackageName(), null);//注意就是"package",不用改成自己的包名
									intent.setData(uri);
									startActivityForResult(intent, REQUEST_PERMISSION);
								});
						mDialog = builder.create();
						mDialog.setCanceledOnTouchOutside(false);
						mDialog.show();
					}else {
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle("permission")
								.setMessage("点击允许才可以使用我们的app哦")
								.setPositiveButton("去允许",  (dialog, id) -> {
									if (alertDialog != null && alertDialog.isShowing()) {
										alertDialog.dismiss();
									}
									ActivityCompat.requestPermissions(this, permissionList, 1);
								});
						alertDialog = builder.create();
						alertDialog.setCanceledOnTouchOutside(false);
						alertDialog.show();
					}
					break;
				}
			}
		}
	}

	/**
	 * 显示搜索框弹出的显示窗口
	 */
	private void showListPopupWindow() {
		listPopupWindow = new ListPopupWindow(this);
		SimpleAdapter adapter = new SimpleAdapter(this, getAdapterList(searchList), R.layout.search_item
				, new String[]{"title", "content", "date"}, new int[]{R.id.search_title, R.id.search_content, R.id.search_date});
		listPopupWindow.setAdapter(adapter);
		listPopupWindow.setAnchorView(search.editText);
//		listPopupWindow.setModal(true);
		listPopupWindow.setOnItemClickListener((adapterView, view, i, l) -> {
			startSecondActivityWithEditMode(searchList.get(i));
			listPopupWindow.dismiss();
		});
		listPopupWindow.show();
	}

	/**
	 * 返回合适的list，能把数据塞进搜索框弹出的窗口的adpter里
	 * @param l 放入的数据
	 * @return 经过格式化的数据
	 */
	public List<Map<String,Object>> getAdapterList(List<Note> l) {
		List<Map<String,Object>> list = new ArrayList<>();
		Map<String,Object> map;
		for(int i = 0;i < l.size();i++) {
			map= new HashMap<>();
			map.put("title", l.get(i).title);
			map.put("content", replaceContent(l.get(i).content));
			map.put("date", l.get(i).date);
			list.add(map);
		}
		return list;
	}

	/**
	 * 将图片或者录音地址改为[图片]或者[录音]，以便显示在主页
	 * @param c 存在地址的字符串
	 * @return 格式化完的字符串
	 */
	private String replaceContent(String c) {
		String[] strings = c.split("\n");
		for (int i = 0;i < strings.length;i++) {
			String type = null;
			if (strings[i].length() > 4) {
				type = strings[i].substring(strings[i].length() - 4);
			}
			if (type != null) {
				if(type.equals(".amr")){
					strings[i] = "[录音]";
				} else if (type.equals(".jpg")){
					strings[i] = "[图片]";
				}
			}
		}
		String result = "";
		for (String string : strings) {
			result += string;
		}
		return result;
	}

	/**
	 * 判断两个字符串是否有重复的部分，用来实现关键字搜索
	 * @param l	长的字符串，被比较的对象
	 * @param s	短的字符串，比较的对象
	 * @return	true l中有s	false l中没有s
	 */
	private boolean isRepeat(String l, String s) {
		for (int i = 0;i <= l.length() - s.length();i++) {
			String temp = l.substring(i, i + s.length());
			if (temp.equals(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 用编辑模式启动编辑器，即编辑已存在的数据
	 * @param note	被点击的数据
	 */
	private void startSecondActivityWithEditMode(Note note) {
		Intent intent = new Intent(MainActivity.this, SecondActivity.class);
		intent.putExtra("mode", EDIT_MODE);
		intent.putExtra("color", color);
		intent.putExtra("path", path);
		Bundle bundle = new Bundle();
		bundle.putString("id", note.id);
		bundle.putString("title", note.title);
		bundle.putString("content", note.content);
		intent.putExtra("data", bundle);
		startActivityForResult(intent, 1);
	}

}