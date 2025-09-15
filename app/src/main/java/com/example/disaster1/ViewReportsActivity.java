package com.example.disaster1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;

public class ViewReportsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ReportsAdapter adapter;
    private List<Report> reportList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reports);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        reportList = new ArrayList<Report>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportsAdapter(reportList);
        recyclerView.setAdapter(adapter);

        fetchReports();
    }
    private void fetchReports() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (userId == null) {
            Toast.makeText(ViewReportsActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query Firestore to get incident reports for the current user
        db.collection("incident_reports")
                .whereEqualTo("Reporter ID", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Report> filteredReports = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String incidentType = document.getString("Incident Type");
                        String date = document.getString("Date");
                        String time = document.getString("Time");
                        String location = document.getString("LocationName");
                        String reportId = document.getId();  // Use document ID as the report name

                        if (incidentType != null && date != null && time != null) {
                            filteredReports.add(new Report(reportId, incidentType, date, time, location));
                        }
                    }

                    // Update UI with the fetched reports
                    reportList.clear();
                    reportList.addAll(filteredReports);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (filteredReports.isEmpty()) {
                        Toast.makeText(ViewReportsActivity.this, "No reports found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FetchReports", "Failed to fetch reports", e);
                    Toast.makeText(ViewReportsActivity.this, "Failed to fetch reports", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }


}
