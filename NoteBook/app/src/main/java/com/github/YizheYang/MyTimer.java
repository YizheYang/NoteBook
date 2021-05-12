package com.github.YizheYang;

import java.util.Calendar;

public class MyTimer {
	int year;
	int month;
	int day;
	int hour;
	int minute;
	int second;

	public MyTimer(){
		Calendar calendar = Calendar.getInstance();
		this.year = calendar.get(Calendar.YEAR);
		this.month = calendar.get(Calendar.MONTH);
		this.day = calendar.get(Calendar.DAY_OF_MONTH);
		this.hour = calendar.get(Calendar.HOUR_OF_DAY);
		this.minute = calendar.get(Calendar.MINUTE);
		this.second = calendar.get(Calendar.SECOND);
	}

	public String getTime() {
		Calendar calendar = Calendar.getInstance();
		this.year = calendar.get(Calendar.YEAR);
		this.month = calendar.get(Calendar.MONTH);
		this.day = calendar.get(Calendar.DAY_OF_MONTH);
		this.hour = calendar.get(Calendar.HOUR_OF_DAY);
		this.minute = calendar.get(Calendar.MINUTE);
		this.second = calendar.get(Calendar.SECOND);
		return String.valueOf(this.year) + String.valueOf(this.month) + String.valueOf(this.day) + String.valueOf(this.hour)
				+ String.valueOf(this.minute) + String.valueOf(this.second);
	}
}
