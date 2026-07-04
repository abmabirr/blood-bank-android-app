package com.bloodbank_2026.bloodbank;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Object item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Object item);
    }

    private List<Object> notificationList;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public NotificationsAdapter(List<Object> notificationList, OnItemClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    public NotificationsAdapter(List<Object> notificationList, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
        this.notificationList = notificationList;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = notificationList.get(position);

        if (item instanceof EmergencyRequest) {
            EmergencyRequest request = (EmergencyRequest) item;
            holder.sosTitle.setText("📢 Urgent Blood Needed");
            holder.sosBloodGroup.setText(request.bloodGroup);
            holder.sosLocation.setText(request.location);
            holder.sosTime.setText(request.timestamp);
        } else if (item instanceof Notice) {
            Notice notice = (Notice) item;
            holder.sosTitle.setText(notice.title);
            holder.sosBloodGroup.setText("N");
            holder.sosLocation.setText(notice.message);
            holder.sosTime.setText(notice.timestamp);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(item);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sosTitle, sosBloodGroup, sosLocation, sosTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sosTitle = itemView.findViewById(R.id.sosTitle);
            sosBloodGroup = itemView.findViewById(R.id.sosBloodGroup);
            sosLocation = itemView.findViewById(R.id.sosLocation);
            sosTime = itemView.findViewById(R.id.sosTime);
        }
    }
}
