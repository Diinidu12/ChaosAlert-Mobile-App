package com.example.disaster1;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class IncidentListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] incidentTypes;
    private final String[] locations;
    private final String[] dates;
    private final String[] times;
    private final String[] description;
    private final Integer[] icons; // If you have icons for incidents

    public IncidentListAdapter(Activity context, String[] incidentTypes, String[] locations, String[] dates, String[] times, String[] description, Integer[] icons) {
        super(context, R.layout.my_list, incidentTypes);
        this.context = context;
        this.incidentTypes = incidentTypes;
        this.locations = locations;
        this.dates = dates;
        this.times = times;
        this.description = description;
        this.icons = icons; // Pass icons if available
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.my_list, null, true);
        TextView txtIncidentType = (TextView) rowView.findViewById(R.id.txt_date);
        TextView txtLocation = (TextView) rowView.findViewById(R.id.txt_location);
        TextView txtDate = (TextView) rowView.findViewById(R.id.txt_temp);
        TextView txtTime = (TextView) rowView.findViewById(R.id.txt_time);
        //ImageView imgIcon = (ImageView) rowView.findViewById(R.id.icon);

        txtIncidentType.setText(incidentTypes[position]);
        txtLocation.setText("Location: "+locations[position]);
        txtDate.setText("Date: "+dates[position]);
        txtTime.setText("Time: " +times[position]);
        //if (icons != null) {
         //   imgIcon.setImageResource(icons[position]); // Use icons if applicable
       // }

        return rowView;
    }
}