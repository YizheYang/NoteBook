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

	private Context mContext;
	private RecyclerView mRecyclerView;
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
			holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					mOnLongClickListener.OnLongClick(v, holder.getAdapterPosition());
					return true;
				}
			});
		}
	}

	@Override
	public int getItemCount() {
		return mNoteList.size();
	}

//	@Override
//	public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
//		try {
//			if (mRecyclerView == null) {
//				mRecyclerView = recyclerView;
//			}
//			ifGridLayoutManager();
//		} catch (Exception e){
//			e.printStackTrace();
//		}
//	}

	public NoteAdapter(Context context, List<Note> list) {
		this.mContext = context;
		this.mNoteList = list;
	}

//	/**
//	 * 在网格流的显示下让头部和尾部自成一行
//	 */
//	private void ifGridLayoutManager() {
//		if (mRecyclerView == null) {
//			return;
//		}
//		final RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
//		if (layoutManager instanceof GridLayoutManager) {
//			((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//				@Override
//				public int getSpanSize(int position) {
//					return (isHeaderView(position) || isFooterView(position))
//							? ((GridLayoutManager) layoutManager).getSpanCount() : 1;
//				}
//			});
//		}
//	}

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
				} else if (type.equals(".jpg")){
					strings[i] = "[图片]";
				}
			}
		}
		String result = "";
		for (String string : strings) {
			result += string;
		}
		return result;
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.mOnItemClickListener = onItemClickListener;
	}

	public interface OnItemClickListener{
		void onItemClick(View view, int position);
	}

	public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
		this.mOnLongClickListener = onLongClickListener;
	}

	public interface OnLongClickListener{
		void OnLongClick(View view, int position);
	}
}
