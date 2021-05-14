package com.github.YizheYang;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {
	TextView title;
	TextView content;
	TextView date;

	public ViewHolder(@NonNull View itemView) {
		super(itemView);
		title = itemView.findViewById(R.id.recyclerView_title);
		content = itemView.findViewById(R.id.recyclerView_content);
		date = itemView.findViewById(R.id.recyclerView_date);
	}
}
