package com.github.YizheYang.tools;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private final Context mContext;
	private final String path;

	public MyUncaughtExceptionHandler(Context context) {
		this.mContext = context;
		path = ContextCompat.getExternalFilesDirs(mContext.getApplicationContext(), null)[0].getAbsolutePath()
				+ "/crashLog";
	}

	@Override
	public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
		Log.e("程序出现异常了", "Thread = " + t.getName() + "\nThrowable = " + e.getMessage());
		String stackTraceInfo = getStackTraceInfo(e);
		Log.e("stackTraceInfo", stackTraceInfo);
		saveThrowableMessage(stackTraceInfo);
		Toast.makeText(mContext, "抱歉，我们发生了错误，需要退出应用", Toast.LENGTH_LONG).show();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);
	}

	/**
	 * 获取错误的信息
	 * @param throwable 抛出的异常信息
	 * @return 异常信息的字符串
	 */
	private String getStackTraceInfo(final Throwable throwable) {
		PrintWriter pw = null;
		Writer writer = new StringWriter();
		try {
			pw = new PrintWriter(writer);
			throwable.printStackTrace(pw);
		} catch (Exception e) {
			return "";
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
		return writer.toString();
	}

	/**
	 * 保存异常信息
	 * @param errorMessage 接收的异常信息的内容
	 */
	private void saveThrowableMessage(String errorMessage) {
		if (errorMessage.isEmpty()) {
			return;
		}
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		writeStringToFile(errorMessage, file);
	}

	/**
	 * 将字符串写入本地
	 * @param errorMessage 要写入的字符串
	 * @param file 写入的文件位置
	 */
	private void writeStringToFile(final String errorMessage, final File file) {
		new Thread(() -> {
			FileOutputStream outputStream = null;
			try {
				ByteArrayInputStream inputStream = new ByteArrayInputStream(errorMessage.getBytes());
				outputStream = new FileOutputStream(new File(file, MyTimer.getTime() + ".txt"));
				int len = 0;
				byte[] bytes = new byte[1024];
				while ((len = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, len);
				}
				outputStream.flush();
				Log.e("程序出异常了", "写入本地文件成功：" + file.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

}
