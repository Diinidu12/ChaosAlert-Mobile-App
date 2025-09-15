package com.example.disaster1;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    private List<Report> reports;

    public ReportsAdapter(List<Report> reports) {
        this.reports = reports;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.reportType.setText("Disaster Type: "+report.getIncidentType());
        holder.reportDate.setText("Reported Date: "+report.getDate());
        holder.reportTime.setText("Reported Time: "+report.getTime());
        holder.reportLocation.setText("Incident's Location: "+report.getLocation());
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {

        TextView reportType;
        TextView reportDate;
        TextView reportTime;
        TextView reportLocation;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reportType = itemView.findViewById(R.id.reportType);
            reportDate = itemView.findViewById(R.id.reportDate);
            reportTime = itemView.findViewById(R.id.reportTime);
            reportLocation = itemView.findViewById(R.id.reportLocation);
        }
    }
}
