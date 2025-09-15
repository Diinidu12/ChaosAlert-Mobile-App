package com.example.disaster1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Admin extends AppCompatActivity {

    private AutoCompleteTextView disasterType;
    private TextInputEditText date, time, latitude, longitude, description,locationCityTown;
    private Button saveButton, logout, viewReportsButton,GPSCordinatesbtn;
    private ImageButton pickTime, pickDate;
    private FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        logout = findViewById(R.id.logoutBtn);
        pickTime = findViewById(R.id.pickTime);
        pickDate = findViewById(R.id.pickDate);
        saveButton = findViewById(R.id.saveData);
        viewReportsButton = findViewById(R.id.viewReportsButton);

        GPSCordinatesbtn = findViewById(R.id.GPSCordinatesbtn);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        locationCityTown = findViewById(R.id.locationCityTown);


        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        description = findViewById(R.id.description);
        disasterType = findViewById(R.id.disType);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        String[] disasterArray = {"Flood", "Landslide", "Wildfire", "Fire"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, disasterArray);
        disasterType.setAdapter(adapter);

        disasterType.setOnTouchListener((v, event) -> {
            disasterType.showDropDown();
            return false;
        });

        pickTime.setOnClickListener(v -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            TimePickerFragment timePicker = new TimePickerFragment();
            timePicker.show(fragmentManager, "timePicker");
        });

        pickDate.setOnClickListener(v -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            DatePickerFragment datePicker = new DatePickerFragment();
            datePicker.show(fragmentManager, "datePicker");
        });

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), activity_login.class));
            finish();
        });

        getLastLocation();
        if (viewReportsButton != null) {
            viewReportsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), ViewReportsActivity.class));
                }
            });
        } else {
            Log.e("AdminActivity", "viewReportsButton is null");
        }

        GPSCordinatesbtn.setOnClickListener(view -> {
            String loc = locationCityTown.getText().toString();
            if (!loc.isEmpty()) {
                new FetchLocationCoordinates(loc).execute();
                Toast.makeText(Admin.this, "Location Coordinates Fetched", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
            }
        });
        saveButton.setOnClickListener(v -> {
            saveData();

        });

    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location != null) {
                            latitude.setText(String.valueOf(location.getLatitude()));
                            longitude.setText(String.valueOf(location.getLongitude()));
                        } else {
                            Toast.makeText(Admin.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(Admin.this, "Permission Denied", Toast.LENGTH_SHORT).show();

            }
        }
    }

    public class FetchLocationCoordinates extends AsyncTask<String, Void, String> {

        private String locationName;
        HttpURLConnection urlConnection;
        BufferedReader reader;

        public FetchLocationCoordinates(String locationName) {
            this.locationName = locationName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Perform any setup or initial steps here if necessary
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {

                String encodedLocationName = URLEncoder.encode(locationName, "UTF-8");
                String BASE_URL = "https://geocode.xyz/" + encodedLocationName + "?json=1&auth=270066452520134173698x111429";
                URL url = new URL(BASE_URL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) { // HTTP 200 is OK
                    return null;
                }

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                return buffer.toString();

            } catch (IOException e) {
                Log.e("MainActivity", "Error", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("Admin", "Error closing stream", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null || result.isEmpty()) {
                Toast.makeText(Admin.this, "Error fetching location data", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                String fetchlatitude = jsonObject.getString("latt");
                String fetchlongitude = jsonObject.getString("longt");

                latitude.setText(fetchlatitude);
                longitude.setText(fetchlongitude);

            } catch (JSONException e) {
                Log.e("MainActivity", "JSON Parsing error", e);
                Toast.makeText(Admin.this, "Data parsing error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void saveData() {
        String loc = locationCityTown.getText().toString();
        String lat = latitude.getText().toString();
        String lon = longitude.getText().toString();
        String desc = description.getText().toString();
        String incType = disasterType.getText().toString();
        String incDate = date.getText().toString();
        String incTime = time.getText().toString();

        if (lat.isEmpty() || lon.isEmpty() || desc.isEmpty() || incType.isEmpty() || incDate.isEmpty() || incTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch the current user's ID (reporter ID)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String reporterId = (currentUser != null) ? currentUser.getUid() : "Unknown";

        // Create a map for the incident data
        Map<String, Object> incidentData = new HashMap<>();
        incidentData.put("Reporter ID", reporterId);
        incidentData.put("Incident Type", incType);
        incidentData.put("Date", incDate);
        incidentData.put("Time", incTime);
        incidentData.put("LocationName", loc);
        incidentData.put("Latitude", Double.parseDouble(lat));
        incidentData.put("Longitude", Double.parseDouble(lon));
        incidentData.put("Description", desc);

        // Save data directly to Firestore under "incident_reports" collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("incident_reports")
                .add(incidentData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(Admin.this, "Incident report saved successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), Admin.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Admin.this, "Failed to save report", Toast.LENGTH_SHORT).show();
                });
    }




    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String formattedTime = String.format("%02d:%02d", hourOfDay, minute);
            ((TextInputEditText) getActivity().findViewById(R.id.time)).setText(formattedTime);
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(requireContext(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            String formattedDate = String.format("%02d/%02d/%04d", day, month + 1, year);
            ((TextInputEditText) getActivity().findViewById(R.id.date)).setText(formattedDate);
        }

    }
}


