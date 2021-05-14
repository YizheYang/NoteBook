package com.github.YizheYang.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private static final int spanCount = 2;
	private static final int NEW_MODE = 1;
	private static final int EDIT_MODE = 2;

	private NoteAdapter adapter;
	private List<Note> noteList = new ArrayList<>();
	private MySQLiteOpenHelper helper;
	private SQLiteDatabase db;

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
			Note note = noteList.get(position);
			Intent intent = new Intent(MainActivity.this, SecondActivity.class);
			intent.putExtra("mode", EDIT_MODE);
			Bundle bundle = new Bundle();
			bundle.putString("id", note.id);
			bundle.putString("title", note.title);
			bundle.putString("content", note.content);
			intent.putExtra("data", bundle);
			startActivityForResult(intent, 1);
		});
		adapter.setOnLongClickListener(new NoteAdapter.OnLongClickListener() {
			@Override
			public void OnLongClick(View view, int position) {
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
			}
		});

		FloatingActionButton fab = findViewById(R.id.floatButton);
		fab.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, SecondActivity.class);
			startActivityForResult(intent, 1);
		});
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
		Cursor cursor = db.query("Note", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Note note = new Note(cursor.getString(cursor.getColumnIndex("ID"))
						, cursor.getString(cursor.getColumnIndex("TITLE"))
						, cursor.getString(cursor.getColumnIndex("CONTENT")));
				noteList.add(note);
			}while (cursor.moveToNext());
		}
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

}