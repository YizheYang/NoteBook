package com.github.YizheYang.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.YizheYang.Note;
import com.github.YizheYang.NoteAdapter;
import com.github.YizheYang.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

	private static final int spanCount = 2;

	private List<Note> noteList;

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

		RecyclerView recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
		NoteAdapter adapter = new NoteAdapter(this, noteList);
		recyclerView.setAdapter(adapter);
		adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {

			}
		});


		FloatingActionButton fab = findViewById(R.id.floatButton);
		fab.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, SecondActivity.class);
			startActivity(intent);
		});


	}
}