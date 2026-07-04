package com.bloodbank_2026.bloodbank;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmergencyRequestAdapter extends RecyclerView.Adapter<EmergencyRequestAdapter.ViewHolder> {

    private Context context;
    private List<EmergencyRequest> requestList;
    private List<String> requestIds;

    public EmergencyRequestAdapter(Context context, List<EmergencyRequest> requestList, List<String> requestIds) {
        this.context = context;
        this.requestList = requestList;
        this.requestIds = requestIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_emergency_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyRequest request = requestList.get(position);
        String requestId = requestIds.get(position);

        holder.itemBlood.setText("Blood: " + request.bloodGroup);
        holder.itemLocation.setText("Location: " + request.location);
        holder.itemPhone.setText("Phone: " + request.phone);
        holder.itemMessage.setText("Message: " + (request.message.isEmpty() ? "Urgent help needed!" : request.message));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EmergencyDetailActivity.class);
            intent.putExtra("requestId", requestId);
            intent.putExtra("blood", request.bloodGroup);
            intent.putExtra("location", request.location);
            intent.putExtra("phone", request.phone);
            intent.putExtra("message", request.message);
            intent.putExtra("time", request.timestamp);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemBlood, itemLocation, itemPhone, itemMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemBlood = itemView.findViewById(R.id.itemBlood);
            itemLocation = itemView.findViewById(R.id.itemLocation);
            itemPhone = itemView.findViewById(R.id.itemPhone);
            itemMessage = itemView.findViewById(R.id.itemMessage);
        }
    }
}
