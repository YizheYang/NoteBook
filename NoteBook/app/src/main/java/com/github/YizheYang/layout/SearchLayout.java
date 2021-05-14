package com.github.YizheYang.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.github.YizheYang.R;

public class SearchLayout extends LinearLayout {

	public EditText editText;
	public Button button;

	public SearchLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		LayoutInflater.from(context).inflate(R.layout.search, this);
		button = findViewById(R.id.search_Button);
		editText = findViewById(R.id.search_EditText);
	}
}
