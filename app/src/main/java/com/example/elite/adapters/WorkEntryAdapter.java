package com.example.elite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WorkEntryAdapter extends RecyclerView.Adapter<WorkEntryAdapter.WorkEntryViewHolder> {

    private List<WorkEntry> workEntries;
    private Context context;
    private OnWorkEntryActionListener listener;

    public interface OnWorkEntryActionListener {
        void onWorkEntryEdit(WorkEntry workEntry);
        void onWorkEntryClick(WorkEntry workEntry);
    }

    public WorkEntryAdapter(List<WorkEntry> workEntries, Context context, OnWorkEntryActionListener listener) {
        this.workEntries = workEntries;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_work_entry, parent, false);
        return new WorkEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkEntryViewHolder holder, int position) {
        WorkEntry workEntry = workEntries.get(position);
        
        holder.textUserName.setText(workEntry.getUserName());
        holder.textHours.setText(String.format(Locale.getDefault(), "%.1f hrs", workEntry.getHoursWorked()));
        holder.textProjectName.setText(workEntry.getProjectName());
        
        // Description
        if (workEntry.getDescription() != null && !workEntry.getDescription().trim().isEmpty()) {
            holder.textDescription.setText(workEntry.getDescription());
            holder.textDescription.setVisibility(View.VISIBLE);
        } else {
            holder.textDescription.setVisibility(View.GONE);
        }
        
        // Created at
        SimpleDateFormat sdf = new SimpleDateFormat("'Reported' MMM dd 'at' HH:mm", Locale.getDefault());
        holder.textCreatedAt.setText(sdf.format(workEntry.getCreatedAt()));
        
        // Click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorkEntryClick(workEntry);
            }
        });
        
        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorkEntryEdit(workEntry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workEntries.size();
    }

    public void updateWorkEntries(List<WorkEntry> newWorkEntries) {
        this.workEntries = newWorkEntries;
        notifyDataSetChanged();
    }

    static class WorkEntryViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView textUserName, textHours, textProjectName, textDescription, textCreatedAt;
        MaterialButton buttonEdit;

        public WorkEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_work_entry);
            textUserName = itemView.findViewById(R.id.text_user_name);
            textHours = itemView.findViewById(R.id.text_hours);
            textProjectName = itemView.findViewById(R.id.text_project_name);
            textDescription = itemView.findViewById(R.id.text_description);
            textCreatedAt = itemView.findViewById(R.id.text_created_at);
            buttonEdit = itemView.findViewById(R.id.button_edit);
        }
    }
}