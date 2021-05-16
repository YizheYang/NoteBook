package com.github.YizheYang.activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.github.YizheYang.recyclerview.Note;
import com.github.YizheYang.recyclerview.NoteAdapter;
import com.github.YizheYang.R;
import com.github.YizheYang.layout.SearchLayout;
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
//				Bundle bundle = new Bundle();
//				bundle.putString("password", password);
//				intent.putExtras(bundle);
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
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);


		requestPower();
		helper = new MySQLiteOpenHelper(this, "NoteBook.db", null);
		db = helper.getWritableDatabase();
//		linearLayout = findViewById(R.id.main_linearLayout);
//		background = findViewById(R.id.background);
		loadNoteFromSQLite();
		loadPasswordFromSQLite();
		loadColorFromSQLite();


		recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
		adapter = new NoteAdapter(this, noteList);
		recyclerView.setAdapter(adapter);
		adapter.setOnItemClickListener((view, position) -> {
			startSecondActivityWithEditMode(noteList.get(position));
		});
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
				searchAdapter.setOnItemClickListener((view, position) -> {
					startSecondActivityWithEditMode(searchList.get(position));
				});
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
			startActivityForResult(intent, REQUEST_SECOND);
		});
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
				color = data.getIntExtra("color", 0);
				if (color != 0) {
					getWindow().getDecorView().setBackgroundColor(getResources().getColor(color));
					ContentValues values = new ContentValues();
					values.put("COLOR", color);
					db.update("Color", values, "COLOR=?", new String[]{String.valueOf(oldColor)});
				}

////				int color = data.getIntExtra("color", 0);
//				String path = data.getStringExtra("path");
//				Bitmap bitmap = BitmapFactory.decodeFile(path);
//
////				Drawable drawable = new BitmapDrawable(getResources(), bitmap);
////
//				Message message = new Message();
//				message.what = 1;
//				message.obj = bitmap;
//				handler.sendMessage(message);
////				linearLayout.setBackground(drawable);
			}
		}else if (requestCode == REQUEST_SECRET) {
			if (resultCode == RESULT_OK) {
				isSecret = data.getBooleanExtra("isCorrect", false);
				if (isSecret) {
					Toast.makeText(MainActivity.this, "进入隐私空间", Toast.LENGTH_SHORT).show();
					secretAdapter = new NoteAdapter(this, secretList);
					recyclerView.setAdapter(secretAdapter);
					secretAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
						@Override
						public void onItemClick(View view, int position) {
							startSecondActivityWithEditMode(secretList.get(position));
						}
					});
				}
			}
		}
	}

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
	 * 请求所需的权限
	 */
	public void requestPower() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			//refuse == true
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
				Toast.makeText(this, "请同意授权以保证程序正常运行", Toast.LENGTH_SHORT).show();
			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
								, Manifest.permission.WRITE_EXTERNAL_STORAGE
								, Manifest.permission.CAMERA
								, Manifest.permission.RECORD_AUDIO}
						, 1);
			}
		}
	}

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

	private boolean isRepeat(String l, String s) {
		for (int i = 0;i <= l.length() - s.length();i++) {
			String temp = l.substring(i, i + s.length());
			if (temp.equals(s)) {
				return true;
			}
		}
		return false;
	}

	private void startSecondActivityWithEditMode(Note note) {
		Intent intent = new Intent(MainActivity.this, SecondActivity.class);
		intent.putExtra("mode", EDIT_MODE);
		intent.putExtra("color", color);
		Bundle bundle = new Bundle();
		bundle.putString("id", note.id);
		bundle.putString("title", note.title);
		bundle.putString("content", note.content);
		intent.putExtra("data", bundle);
		startActivityForResult(intent, 1);
	}

}