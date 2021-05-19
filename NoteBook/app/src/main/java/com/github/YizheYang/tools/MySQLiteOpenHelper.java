package com.github.YizheYang.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
	protected Context mContext;
	private static final int VERSION = 5;
	public static final String CREATE = "create table Note ("
			+ "ID integer primary key autoincrement, "
			+ "TITLE text, "
			+ "CONTENT text, "
			+ "DATE integer)";

	public MySQLiteOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory) {
		super(context, name, factory, VERSION);
		this.mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE);
		db.execSQL("ALTER TABLE Note ADD COLUMN SECRET integer");
		db.execSQL("CREATE table Password (ID integer primary key autoincrement, PASSWORD text)");
		ContentValues values2 = new ContentValues();
		values2.put("PASSWORD", "000");
		db.insert("Password", null, values2);
		db.execSQL("CREATE TABLE Color (ID integer primary key autoincrement, COLOR INTEGER)");
		ContentValues values3 = new ContentValues();
		values3.put("COLOR", 2131099845);
		db.insert("Color", null, values3);
		db.execSQL("CREATE TABLE Background (ID integer primary key autoincrement, PATH TEXT)");
		ContentValues values4 = new ContentValues();
		values4.put("PATH", "");
		db.insert("Background", null, values4);
		Toast.makeText(mContext, "数据库创建成功", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (oldVersion){
			case 1:
				db.execSQL("ALTER TABLE Note ADD COLUMN SECRET integer");
				ContentValues values1 = new ContentValues();
				values1.put("SECRET", 0);
				db.update("Note", values1, null, null);
			case 2:
				db.execSQL("CREATE table Password (ID integer primary key autoincrement, PASSWORD text)");
				ContentValues values2 = new ContentValues();
				values2.put("PASSWORD", "000");
				db.insert("Password", null, values2);
			case 3:
				db.execSQL("CREATE TABLE Color (ID integer primary key autoincrement, COLOR INTEGER)");
				ContentValues values3 = new ContentValues();
				values3.put("COLOR", 2131099845);
				db.insert("Color", null, values3);
			case 4:
				db.execSQL("CREATE TABLE Background (ID integer primary key autoincrement, PATH TEXT)");
				ContentValues values4 = new ContentValues();
				values4.put("PATH", "");
				db.insert("Background", null, values4);
			default:
		}
	}
}
