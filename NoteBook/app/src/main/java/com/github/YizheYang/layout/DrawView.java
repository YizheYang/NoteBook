package com.github.YizheYang.layout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.github.YizheYang.DrawPath;
import com.github.YizheYang.R;

import java.util.ArrayList;
import java.util.Iterator;

public class DrawView extends View {

	private final int width;
	private final int height;
	private Paint paint;
	private Paint bitmapPaint;
	private final ArrayList<DrawPath> savePath;
	private final ArrayList<DrawPath> deletePath;
	private DrawPath dp;
	private Bitmap bitmap;
	private Canvas canvas;
	private Path path;
	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;

	private int currentColor = getResources().getColor(R.color.black);
	private int currentSize = 10;
	private int currentStyle = PEN;
	private static final int PEN = 1;
	private static final int ERASER = 2;

	private final int[] paintColor = {getResources().getColor(R.color.black)
			, getResources().getColor(R.color.purple_200)
			, getResources().getColor(R.color.purple_500)
			, getResources().getColor(R.color.purple_700)
			, getResources().getColor(R.color.teal_200)
			, getResources().getColor(R.color.teal_700)};

	public DrawView(Context context) {
		super(context);
		DisplayMetrics dm = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels - 2 * 45;
		initBoard();
		savePath = new ArrayList<>();
		deletePath = new ArrayList<>();
	}

	public DrawView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		DisplayMetrics dm = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels - 2 * 45;
		initBoard();
		savePath = new ArrayList<>();
		deletePath = new ArrayList<>();
	}

	public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		DisplayMetrics dm = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels - 2 * 45;
		initBoard();
		savePath = new ArrayList<>();
		deletePath = new ArrayList<>();
	}

	/**
	 * 初始化画板
	 */
	public void initBoard() {
		setPaintStyle();
		bitmapPaint = new Paint(Paint.DITHER_FLAG);
		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		canvas = new Canvas(bitmap);
		canvas.drawColor(getResources().getColor(R.color.white));
		path = new Path();
		bitmapPaint = new Paint(Paint.DITHER_FLAG);
	}

	/**
	 * 设置画下来的颜色
	 */
	public void setPaintStyle() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(currentSize);
		if (currentStyle == PEN) {
			paint.setColor(currentColor);
		}else {
			paint.setColor(getResources().getColor(R.color.white));
		}
	}

	/**
	 * 选择是画笔还是橡皮擦
	 * @param choose 选择的选项
	 */
	public void selectPaintStyle(int choose) {
		if (choose == 0) {
			currentStyle = PEN;
			setPaintStyle();
		}else if (choose == 1) {
			currentStyle = ERASER;
			setPaintStyle();
			paint.setStrokeWidth(20);
		}
	}

	/**
	 * 设置画笔大小
	 * @param choose 选择的选项
	 */
	public void setPaintSize(int choose) {
		currentSize = Integer.parseInt(this.getResources().getStringArray(R.array.paintSize)[choose]);
		setPaintStyle();
	}

	/**
	 * 设置画笔颜色
	 * @param choose 选择的选项
	 */
	public void setPaintColor(int choose) {
		currentColor = paintColor[choose];
		setPaintStyle();
	}

	/**
	 * 撤回上一步
	 */
	public void revoke() {
		if (savePath != null && savePath.size() > 0) {
			initBoard();
			DrawPath drawPath = savePath.get(savePath.size() - 1);
			deletePath.add(drawPath);
			savePath.remove(savePath.size() - 1);
			Iterator<DrawPath> it = savePath.iterator();
			while (it.hasNext()) {
				DrawPath dp = it.next();
				canvas.drawPath(dp.getPath(), dp.getPaint());
			}
			invalidate();
		}
	}

	/**
	 * 恢复下一步
	 */
	public void resume() {
		if (deletePath.size() > 0) {
			DrawPath drawPath = deletePath.get(deletePath.size() - 1);
			savePath.add(drawPath);
			canvas.drawPath(drawPath.getPath(), drawPath.getPaint());
			deletePath.remove(deletePath.size() - 1);
			invalidate();
		}
	}

	/**
	 * 清除整个画板
	 */
	public void remove() {
		initBoard();
		invalidate();
		savePath.clear();
		deletePath.clear();
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				path = new Path();
				dp = new DrawPath();
				dp.path = path;
				dp.paint = paint;
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				break;
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
		if (path != null) {
			canvas.drawPath(path, paint);
		}
	}

	private void touch_start(float x, float y) {
		path.reset();
		path.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			path.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
			mX = x;
			mY = y;
		}
	}

	private void touch_up() {
		path.lineTo(mX, mY);
		canvas.drawPath(path, paint);
		savePath.add(dp);
		path = null;
	}

}
