package com.github.YizheYang;

public class Note {

	public String id = "-1";
	public String title;
	public String content;

	public Note(String t, String c) {
		this.title = t;
		this.content = c;
	}

	public Note(String id, String t, String c) {
		this.id = id;
		this.title = t;
		this.content = c;
	}

//	public void setTitle(String title) {
//		this.title = title;
//	}
//
//	public void setContent(String content) {
//		this.content = content;
//	}
}
