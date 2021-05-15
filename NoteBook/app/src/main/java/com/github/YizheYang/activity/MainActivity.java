package com.github.YizheYang.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
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
import com.github.YizheYang.Note;
import com.github.YizheYang.NoteAdapter;
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

	private NoteAdapter adapter;
	private final List<Note> noteList = new ArrayList<>();
	private MySQLiteOpenHelper helper;
	private SQLiteDatabase db;

	private SearchLayout search;
	private final List<Note> searchList = new ArrayList<>();
	private ListPopupWindow listPopupWindow = null;

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
			default:
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		requestPower();
		helper = new MySQLiteOpenHelper(this, "NoteBook.db", null, 1);
		db = helper.getWritableDatabase();

		loadNoteFromSQLite();

		RecyclerView recyclerView = findViewById(R.id.recyclerView);
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

		search = findViewById(R.id.search);
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
				for (int i = 0; i < noteList.size(); i++) {
					Note note = noteList.get(i);
					if (note.content.length() >= s.length()) {
						if (isRepeat(replaceContent(note.content), s.toString())) {
							searchList.add(note);
						}
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				showListPopupWindow();
			}
		});

		FloatingActionButton fab = findViewById(R.id.floatButton);
		fab.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, SecondActivity.class);
			startActivityForResult(intent, 1);
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
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				noteList.clear();
				loadNoteFromSQLite();
				adapter.notifyDataSetChanged();
			}
		}
	}

	private void loadNoteFromSQLite() {
		db = helper.getWritableDatabase();
		Cursor cursor = db.query("Note", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Note note = new Note(cursor.getString(cursor.getColumnIndex("ID"))
						, cursor.getString(cursor.getColumnIndex("TITLE"))
						, cursor.getString(cursor.getColumnIndex("CONTENT"))
						, cursor.getString(cursor.getColumnIndex("DATE")));
				noteList.add(note);
			}while (cursor.moveToNext());
		}
		cursor.close();
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
		Bundle bundle = new Bundle();
		bundle.putString("id", note.id);
		bundle.putString("title", note.title);
		bundle.putString("content", note.content);
		intent.putExtra("data", bundle);
		startActivityForResult(intent, 1);
	}
}