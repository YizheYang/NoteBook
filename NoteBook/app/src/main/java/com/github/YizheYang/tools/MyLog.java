package com.github.YizheYang.tools;

import android.content.Context;
import android.util.Log;

public class MyLog {

	private static final int VERBOSE = 1;
	private static final int DEBUG = 2;
	private static final int INFO = 3;
	private static final int WARN = 4;
	private static final int ERROR = 5;
	private static final int HIDE = 6;
	private static final int level = VERBOSE;

	public static void v(Context context, String message) {
		if (level <= VERBOSE) {
			Log.v(context.getClass().getSimpleName(), message);
		}
	}

	public static void d(Context context, String message) {
		if (level <= DEBUG) {
			Log.v(context.getClass().getSimpleName(), message);
		}
	}

	public static void i(Context context, String message) {
		if (level <= INFO) {
			Log.d(context.getClass().getSimpleName(), message);
		}
	}

	public static void w(Context context, String message) {
		if (level <= WARN) {
			Log.d(context.getClass().getSimpleName(), message);
		}
	}

	public static void e(Context context, String message) {
		if (level <= ERROR) {
			Log.d(context.getClass().getSimpleName(), message);
		}
	}

}
