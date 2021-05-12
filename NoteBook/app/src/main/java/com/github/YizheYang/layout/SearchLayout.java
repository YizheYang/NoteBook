package com.github.YizheYang.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.github.YizheYang.R;

public class SearchLayout extends LinearLayout {

	public SearchLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		LayoutInflater.from(context).inflate(R.layout.search, this);
		Button search_Button = findViewById(R.id.search_Button);
		EditText search_EditText = findViewById(R.id.search_EditText);
		String text = search_EditText.getText().toString();
	}
}
