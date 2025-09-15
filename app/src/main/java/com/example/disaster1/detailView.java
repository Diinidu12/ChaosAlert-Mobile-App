package com.example.disaster1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class detailView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_view);

        Intent intent = getIntent();
        String date = intent.getStringExtra("date");
        String incidentType = intent.getStringExtra("incidentType");
        String location = intent.getStringExtra("location");
        String details = intent.getStringExtra("details");
        //int icon = intent.getIntExtra("icon", R.drawable.default_icon); // Provide a default icon if none is provided

        TextView dateTextView = findViewById(R.id.dateTextView);
        TextView incidentTypeTextView = findViewById(R.id.incidentTypeTextView);
        TextView locationTextView = findViewById(R.id.locationTextView);
        TextView detailsTextView = findViewById(R.id.detailsTextView);
        //ImageView iconImageView = findViewById(R.id.iconImageView);

        dateTextView.setText("Date : "+date);
        incidentTypeTextView.setText(incidentType);
        locationTextView.setText("Incident Location : " +location);
        detailsTextView.setText("Details: \n" + details);
        //iconImageView.setImageResource(icon);
    }
}