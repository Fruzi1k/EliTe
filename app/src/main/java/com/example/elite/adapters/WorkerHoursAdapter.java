package com.example.elite.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elite.R;
import com.example.elite.models.WorkEntry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WorkerHoursAdapter extends RecyclerView.Adapter<WorkerHoursAdapter.WorkerHoursViewHolder> {
    private static final String TAG = "WorkerHoursAdapter";
    private Context context;
    private List<WorkEntry> workEntries;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat dayFormat;

    public WorkerHoursAdapter(Context context, List<WorkEntry> workEntries) {
        this.context = context;
        this.workEntries = workEntries;
        this.dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        this.dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
    }

    @NonNull
    @Override
    public WorkerHoursViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_worker_hours, parent, false);
        return new WorkerHoursViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerHoursViewHolder holder, int position) {
        WorkEntry entry = workEntries.get(position);
        Log.d(TAG, "Binding work entry at position " + position);

        // Set date
        if (entry.getWorkDate() != null) {
            holder.textDate.setText(dateFormat.format(entry.getWorkDate()));
            
            // Set day of week
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(entry.getWorkDate());
            holder.textDayOfWeek.setText(dayFormat.format(entry.getWorkDate()));
        } else {
            holder.textDate.setText("N/A");
            holder.textDayOfWeek.setText("");
        }

        // Set project name
        if (entry.getProjectName() != null && !entry.getProjectName().isEmpty()) {
            holder.textProjectName.setText(entry.getProjectName());
        } else {
            holder.textProjectName.setText("No project");
        }

        // Set hours
        holder.textHours.setText(String.format(Locale.getDefault(), "%.1fh", entry.getHoursWorked()));
    }

    @Override
    public int getItemCount() {
        return workEntries.size();
    }

    public static class WorkerHoursViewHolder extends RecyclerView.ViewHolder {
        TextView textDate;
        TextView textDayOfWeek;
        TextView textProjectName;
        TextView textHours;

        public WorkerHoursViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.text_date);
            textDayOfWeek = itemView.findViewById(R.id.text_day_of_week);
            textProjectName = itemView.findViewById(R.id.text_project_name);
            textHours = itemView.findViewById(R.id.text_hours);
        }
    }
}
