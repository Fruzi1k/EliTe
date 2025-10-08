package com.example.elite.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elite.R;
import com.example.elite.apps.WorkerInfoActivity;
import com.example.elite.models.User;

import java.util.List;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {
    private static final String TAG = "WorkerAdapter";
    private Context context;
    private List<User> workersList;

    public WorkerAdapter(Context context, List<User> workersList) {
        this.context = context;
        this.workersList = workersList;
    }

    @NonNull
    @Override
    public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_worker, parent, false);
        return new WorkerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {
        User worker = workersList.get(position);
        Log.d(TAG, "Binding worker at position " + position + ": " + worker.getEmail());

        // Set worker name
        String fullName = worker.getFullName();
        if (fullName != null && !fullName.isEmpty()) {
            holder.workerName.setText(fullName);
            Log.d(TAG, "Worker name: " + fullName);
        } else {
            holder.workerName.setText("Unknown Worker");
            Log.d(TAG, "Worker name: Unknown");
        }

        // Set worker email
        if (worker.getEmail() != null && !worker.getEmail().isEmpty()) {
            holder.workerEmail.setText(worker.getEmail());
            holder.workerEmail.setVisibility(View.VISIBLE);
        } else {
            holder.workerEmail.setVisibility(View.GONE);
        }

        // Set worker phone if available
        if (worker.getPhone() != null && !worker.getPhone().isEmpty()) {
            holder.workerPhone.setText(worker.getPhone());
            holder.workerPhone.setVisibility(View.VISIBLE);
        } else {
            holder.workerPhone.setVisibility(View.GONE);
        }

        // Set position badge
        holder.positionBadge.setText(worker.getPosition() != null ? 
                worker.getPosition().toUpperCase() : "WORKER");

        // Set click listener to open WorkerInfoActivity
        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Worker clicked: " + worker.getEmail());
            Intent intent = new Intent(context, WorkerInfoActivity.class);
            intent.putExtra(WorkerInfoActivity.EXTRA_WORKER, worker);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + workersList.size());
        return workersList.size();
    }

    public static class WorkerViewHolder extends RecyclerView.ViewHolder {
        ImageView workerAvatar;
        TextView workerName;
        TextView workerEmail;
        TextView workerPhone;
        TextView positionBadge;

        public WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            workerAvatar = itemView.findViewById(R.id.worker_avatar);
            workerName = itemView.findViewById(R.id.worker_name);
            workerEmail = itemView.findViewById(R.id.worker_email);
            workerPhone = itemView.findViewById(R.id.worker_phone);
            positionBadge = itemView.findViewById(R.id.worker_position_badge);
        }
    }
}
