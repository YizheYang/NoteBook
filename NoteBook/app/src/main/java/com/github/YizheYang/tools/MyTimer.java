package com.github.YizheYang.tools;

import java.util.Calendar;

public class MyTimer {

	public static int year;
	public static int month;
	public static int day;
	public static int hour;
	public static int minute;
	public static int second;

//	public MyTimer(){
//		Calendar calendar = Calendar.getInstance();
//		this.year = calendar.get(Calendar.YEAR);
//		this.month = calendar.get(Calendar.MONTH);
//		this.day = calendar.get(Calendar.DAY_OF_MONTH);
//		this.hour = calendar.get(Calendar.HOUR_OF_DAY);
//		this.minute = calendar.get(Calendar.MINUTE);
//		this.second = calendar.get(Calendar.SECOND);
//	}

	/**
	 * 获取当前时间
	 * @return 当前时间，格式是全部为数字，可用来命名
	 */
	public static String getTime() {
		Calendar calendar = Calendar.getInstance();
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH) + 1;
		day = calendar.get(Calendar.DAY_OF_MONTH);
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		minute = calendar.get(Calendar.MINUTE);
		second = calendar.get(Calendar.SECOND);
		return year + String.valueOf(month) + day + String.valueOf(hour) + minute + String.valueOf(second);
	}

	/**
	 * 获取当前日期
	 * @return 当前日期，格式是年月日时分秒，可用来显示日期
	 */
	public static String getDate() {
		Calendar calendar = Calendar.getInstance();
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH) + 1;
		day = calendar.get(Calendar.DAY_OF_MONTH);
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		minute = calendar.get(Calendar.MINUTE);
		second = calendar.get(Calendar.SECOND);
		return year + "年" + month + "月" + day + "日" + hour + ":" + minute + ":" + second;
	}

}
