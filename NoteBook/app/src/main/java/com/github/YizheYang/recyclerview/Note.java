package com.github.YizheYang.recyclerview;

public class Note {

	public String id = "-1";
	public String title;
	public String content;
	public String date;
	public int secret;

	public Note(String id, String t, String c, String d) {
		this.id = id;
		this.title = t;
		this.content = c;
		this.date = d;
	}

	public Note(String id, String t, String c, String d, int s) {
		this.id = id;
		this.title = t;
		this.content = c;
		this.date = d;
		this.secret = s;
	}

}
