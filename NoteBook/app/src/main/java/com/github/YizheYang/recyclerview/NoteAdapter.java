package com.github.YizheYang.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.YizheYang.R;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<ViewHolder> {

	private final Context mContext;
	private final List<Note> mNoteList;
	private OnItemClickListener mOnItemClickListener;
	private OnLongClickListener mOnLongClickListener;

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.note_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Note note = mNoteList.get(position);
		if (note.title != null && !note.title.equals("")) {
			holder.title.setText(note.title);
		}
		if (note.content != null && !note.content.equals("")) {
			holder.content.setText(replaceContent(note.content));
		}
		if (note.date != null && !note.date.equals("")) {
			holder.date.setText(note.date);
		}
		if (mOnItemClickListener != null) {
			holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(v, position));
		}
		if (mOnLongClickListener != null) {
			holder.itemView.setOnLongClickListener(v -> {
				mOnLongClickListener.OnLongClick(v, holder.getAdapterPosition());
				return true;
			});
		}
	}

	@Override
	public int getItemCount() {
		return mNoteList.size();
	}

	public NoteAdapter(Context context, List<Note> list) {
		this.mContext = context;
		this.mNoteList = list;
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.mOnItemClickListener = onItemClickListener;
	}

	/**
	 * 自定义单击事件
	 */
	public interface OnItemClickListener{
		void onItemClick(View view, int position);
	}

	public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
		this.mOnLongClickListener = onLongClickListener;
	}

	/**
	 * 自定义长按事件
	 */
	public interface OnLongClickListener{
		void OnLongClick(View view, int position);
	}

	/**
	 将图片或者录音地址改为[图片]或者[录音]，以便显示在主页
	 * @param c 存在地址的字符串
	 * @return 格式化完的字符串
	 */
	private String replaceContent(String c) {
		String[] strings = c.split("\n");
		for (int i = 0;i < strings.length;i++) {
			String type = null;
			if (strings[i].length() > 4) {
				type = strings[i].substring(strings[i].length() - 4);
			}
			if (type != null) {
				if(type.equals(".amr")){
					strings[i] = "[录音]";
				} else if (type.equals(".jpg") || type.equals("jpeg") || type.equals(".png") || type.equals(".bmp")
						|| type.equals(".ico") || type.equals("wbmp") || type.equals("webp") || type.equals(".gif")){
					strings[i] = "[图片]";
				}
			}
		}
		String result = "";
		for (String string : strings) {
			result += "\n" + string;
		}
		return result;
	}

}
