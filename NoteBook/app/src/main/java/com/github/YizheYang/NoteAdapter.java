package com.github.YizheYang;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<ViewHolder> {

	private Context mContext;
	private RecyclerView mRecyclerView;
	private List<Note> mNoteList;

	private OnItemClickListener mOnItemClickListener;

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.note_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Note note = mNoteList.get(position);
		holder.title.setText(note.title);
		holder.content.setText(note.content);
		if (mOnItemClickListener != null) {
			holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(v, position));
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

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.mOnItemClickListener = onItemClickListener;
	}

	public interface OnItemClickListener{
		void onItemClick(View view, int position);
	}
}
