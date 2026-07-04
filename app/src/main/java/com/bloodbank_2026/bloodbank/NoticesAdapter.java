package com.bloodbank_2026.bloodbank;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NoticesAdapter extends RecyclerView.Adapter<NoticesAdapter.NoticeViewHolder> {

    public interface OnNoticeLongClickListener {
        void onNoticeLongClick(Notice notice);
    }

    private List<Notice> noticeList;
    private OnNoticeLongClickListener longClickListener;

    public NoticesAdapter(List<Notice> noticeList) {
        this.noticeList = noticeList;
    }

    public NoticesAdapter(List<Notice> noticeList, OnNoticeLongClickListener longClickListener) {
        this.noticeList = noticeList;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = noticeList.get(position);
        holder.title.setText(notice.title);
        holder.message.setText(notice.message);
        holder.time.setText(notice.timestamp);

        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onNoticeLongClick(notice);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    public static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, time;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.itemNoticeTitle);
            message = itemView.findViewById(R.id.itemNoticeMessage);
            time = itemView.findViewById(R.id.itemNoticeTime);
        }
    }
}
