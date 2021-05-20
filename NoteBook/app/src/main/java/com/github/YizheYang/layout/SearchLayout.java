package com.github.YizheYang.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.YizheYang.R;
import com.google.android.material.button.MaterialButton;

public class SearchLayout extends LinearLayout {

	public ImageView imageView;
	public EditText editText;
	public MaterialButton button;

	public SearchLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		LayoutInflater.from(context).inflate(R.layout.search, this);
		imageView = findViewById(R.id.search_ImageView);
		button = findViewById(R.id.search_Button);
		editText = findViewById(R.id.search_EditText);
	}
}
