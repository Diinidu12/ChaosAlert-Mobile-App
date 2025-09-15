package com.example.disaster1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button SOSButton;
    NotificationHelper notificationHelper;
    private SOSHandler sosHandler;
    FusedLocationProviderClient fusedLocationClient;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int SMS_PERMISSION_REQUEST_CODE = 1;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2;
    String[] incidentTypeList = new String[20];
    String[] locationList = new String[20];
    String[] dateList = new String[20];
    String[] timeList = new String[20];
    String[] descriptionList = new String[20];
    Integer[] iconList = new Integer[20]; // You might need to add icons if applicable


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
        }

        // Existing initializations
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SOSButton = findViewById(R.id.SOSButton);
        Button logout = findViewById(R.id.logoutBtn);

        FetchEarthquakeData FetchData = new FetchEarthquakeData();
        FetchData.execute();

        FetchTsunamiData FetchData1 = new FetchTsunamiData();
        FetchData1.execute();

        fetchIncidentReports();
        //TextView reportDataTextView = findViewById(R.id.ReportData);
        //fetchIncidentReports(reportDataTextView);

        sosHandler = new SOSHandler(this);

        SOSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirm Action")
                        .setMessage("Are you sure you want to send an SOS?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // If the user confirms, proceed with the action
                                checkLocationPermissionAndSendSOS();
                            }
                        })
                        .setNegativeButton("No", null) // Do nothing if the user cancels
                        .show();

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirm Action")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User confirmed the action, proceed with logout
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(getApplicationContext(), activity_login.class));
                                finish();
                            }
                        })
                        .setNegativeButton("No", null) // Do nothing if the user cancels
                        .show();
            }
        });

        ImageView menuIcon = findViewById(R.id.imgmenu);
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });

    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.toolbar_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_settings) {
                    startActivity(new Intent(MainActivity.this, Settings.class));
                    return true;
                } else if (id == R.id.action_about_us) {
                    startActivity(new Intent(MainActivity.this,AboutUs.class));
                    return true;
                } else {
                    return false;
                }
            }
        });
        popup.show();
    }

    private boolean isWithinLastTwoDays(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -2);
        Date twoDaysAgo = calendar.getTime();
        Date eventDate = new Date(timeInMillis);

        return eventDate.after(twoDaysAgo);
    }

    public class FetchEarthquakeData extends AsyncTask<String, Void, String> {

        private NotificationHelper notificationHelper;

        HttpURLConnection urlConnection;
        BufferedReader reader;
        String earthquakeJsonStr;
        String errorMessage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            notificationHelper = new MyNotificationHelper(); // Instantiate the concrete class
        }

        @Override
        protected void onPostExecute(String earthquakeJsonStr) {
            if (earthquakeJsonStr == null || earthquakeJsonStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Error fetching earthquake data", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject earthquakeObject = new JSONObject(earthquakeJsonStr);
                JSONArray featuresArray = earthquakeObject.getJSONArray("features");
                Log.d("FetchEarthquakeData", "Number of earthquakes: " + featuresArray.length());

                JSONObject prioritizedEarthquake = null;

                // Loop through all earthquakes to find one with tsunami warning, magnitude >= 5, and within the last two days
                for (int i = 0; i < featuresArray.length(); i++) {
                    JSONObject earthquake = featuresArray.getJSONObject(i);
                    JSONObject properties = earthquake.getJSONObject("properties");

                    double magnitude = properties.getDouble("mag");
                    int tsunamiWarning = properties.getInt("tsunami");
                    long time = properties.getLong("time");

                    Log.d("FetchEarthquakeData", "Earthquake " + i + ": Magnitude=" + magnitude + ", Time=" + time);

                    if (magnitude >= 4 && isWithinLastTwoDays(time)) {
                        Log.d("FetchEarthquakeData", "Earthquake " + i + " matches criteria.");

                        prioritizedEarthquake = earthquake;
                        break;
                    }
                }

                TextView earthquakeDataTextView = findViewById(R.id.earthqurckData);

                if (prioritizedEarthquake != null) {
                    JSONObject properties = prioritizedEarthquake.getJSONObject("properties");

                    String magnitude = properties.getString("mag");
                    String place = properties.getString("place");
                    long time = properties.getLong("time");
                    String url = properties.getString("url");
                    String title = "Earthquake Alert";
                    String message = "Magnitude: " + magnitude + ", Location: " + place;


                    // Send notification
                    notificationHelper.sendNotification(MainActivity.this, title, message);
                    // Convert time to human-readable format
                    Date date = new Date(time);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                    String dateString = dateFormat.format(date);

                    // Create a string with earthquake details
                    String earthquakeDetails = "Magnitude: " + magnitude + "\n" +
                            "Location: " + place + "\n" +
                            "Time: " + dateString + "\n" +
                            "More Info: ";

                    // Set the earthquake details to the TextView, including clickable link
                    String earthquakeLinkText = "<a href=\"" + url + "\">Earthquake Details</a>";
                    earthquakeDataTextView.setText(Html.fromHtml(earthquakeDetails + earthquakeLinkText));
                    earthquakeDataTextView.setMovementMethod(LinkMovementMethod.getInstance());

                } else {
                    earthquakeDataTextView.setText("No recent major earthquake reported within the last 2 days.");
                }

            } catch (JSONException e) {
                Log.e("MainActivity", "JSON Parsing error", e);
                Toast.makeText(MainActivity.this, "Data parsing error", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected String doInBackground(String... strings) {
            try {
                final String BASE_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&latitude=7.2906&longitude=80.6337&maxradiuskm=380";
                URL url = new URL(BASE_URL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) { // HTTP 200 is OK
                    errorMessage = "Error fetching data from server. Please try again.";
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
                errorMessage = "Error fetching data. Please check your connection.";
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("MainActivity", "Error closing stream", e);
                    }
                }
            }
        }
    }

    public class FetchTsunamiData extends AsyncTask<String, Void, String> {

        private NotificationHelper notificationHelper;
        HttpURLConnection urlConnection;
        BufferedReader reader;
        String tsunamiJsonStr;
        String errorMessage;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            notificationHelper = new MyNotificationHelper(); // Instantiate the concrete class
        }
        protected void onPostExecute(String tsunamiJsonStr) {
            if (tsunamiJsonStr == null || tsunamiJsonStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Error fetching tsunami data", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject earthquakeObject = new JSONObject(tsunamiJsonStr);
                JSONArray featuresArray = earthquakeObject.getJSONArray("features");

                JSONObject prioritizedEarthquake = null;

                // Loop through all earthquakes to find one with tsunami warning, magnitude >= 5, and within the last two days
                for (int i = 0; i < featuresArray.length(); i++) {
                    JSONObject earthquake = featuresArray.getJSONObject(i);
                    JSONObject properties = earthquake.getJSONObject("properties");

                    double magnitude = properties.getDouble("mag");
                    int tsunamiWarning = properties.getInt("tsunami");
                    long time = properties.getLong("time");

                    String title = "Tsunami Alert";
                    String message = "Magnitude: " + magnitude; //add pager warning size if possible


                    if (magnitude >= 5 && tsunamiWarning == 1 && isWithinLastTwoDays(time)) {
                        prioritizedEarthquake = earthquake;

                        notificationHelper.sendNotification(MainActivity.this, title, message);

                        break;
                    }
                }

                TextView tsunamiDataTextView = findViewById(R.id.tsunamiData);

                if (prioritizedEarthquake != null) {
                    JSONObject properties = prioritizedEarthquake.getJSONObject("properties");

                    if (properties.getInt("tsunami") == 1) {
                        String tsunamiWarningText = "Tsunami Warning Issued - ";
                        String tsunamiLinkText = "<a href=\"http://www.tsunami.gov/\">Further Details</a>";
                        tsunamiDataTextView.setText(Html.fromHtml(tsunamiWarningText + tsunamiLinkText));
                        tsunamiDataTextView.setMovementMethod(LinkMovementMethod.getInstance());

                        // Send notification
                    }
                } else {
                    tsunamiDataTextView.setText("No Tsunami Warning within the last 2 days.");
                }

            } catch (JSONException e) {
                Log.e("MainActivity", "JSON Parsing error", e);
                Toast.makeText(MainActivity.this, "Data parsing error", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected String doInBackground(String... strings) {
            try {
                final String BASE_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&latitude=6.9271&longitude=79.8612&maxradiuskm=2000";
                URL url = new URL(BASE_URL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) { // HTTP 200 is OK
                    errorMessage = "Error fetching data from server. Please try again.";
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
                errorMessage = "Error fetching data. Please check your connection.";
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("MainActivity", "Error closing stream", e);
                    }
                }
            }
        }
    }


    private void fetchIncidentReports() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(MainActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("Users").document(currentUser.getUid());
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                double userLat = Double.parseDouble(documentSnapshot.getString("Latitude"));
                double userLon = Double.parseDouble(documentSnapshot.getString("Longitude"));

                CollectionReference reportsRef = FirebaseFirestore.getInstance().collection("incident_reports");

                reportsRef.addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(MainActivity.this, "Error fetching reports: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot == null || snapshot.isEmpty()) {
                        Toast.makeText(MainActivity.this, "No reports available.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int reportCount = 0;

                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        String incidentType = document.getString("Incident Type");
                        String locationName = document.getString("LocationName");
                        String dateStr = document.getString("Date");
                        String description = document.getString("Description");
                        String time = document.getString("Time");
                        double reportLat = document.getDouble("Latitude");
                        double reportLon = document.getDouble("Longitude");

                        double distance = calculateHaversineDistance(userLat, userLon, reportLat, reportLon);

                        if (distance <= 50.0 && isWithinLastTwoDays(dateStr)) {
                            if (reportCount < 20) { // Ensure we don't exceed array size
                                incidentTypeList[reportCount] = incidentType;
                                locationList[reportCount] = locationName;
                                dateList[reportCount] = dateStr;
                                descriptionList[reportCount] = description;
                                timeList[reportCount] = time;
                                //iconList[reportCount] = R.drawable.default_icon; // Replace with actual icons if available
                                reportCount++;

                                // Send real-time notification
                                String title = "Incident Report";
                                String message = "Type: " + incidentType + ", Location: " + locationName + ", Date: " + dateStr;
                                MyNotificationHelper notificationHelper = new MyNotificationHelper();
                                notificationHelper.sendNotification(MainActivity.this, title, message);
                            }
                        }
                    }

                    // Initialize and set the adapter
                    IncidentListAdapter adapter = new IncidentListAdapter(MainActivity.this, incidentTypeList, locationList, dateList,timeList,descriptionList, iconList);
                    ListView listView = findViewById(R.id.list_view);
                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        Intent detailActivity = new Intent(MainActivity.this, detailView.class);
                        detailActivity.putExtra("date", dateList[position]);
                        detailActivity.putExtra("time", timeList[position]);
                        detailActivity.putExtra("incidentType", incidentTypeList[position]);
                        detailActivity.putExtra("location", locationList[position]);
                        detailActivity.putExtra("details",descriptionList[position]);
                        detailActivity.putExtra("icon", iconList[position]);
                       startActivity(detailActivity);
                    });

                });
            } else {
                Toast.makeText(MainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(MainActivity.this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show()
        );
    }



    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // convert to kilometers
        return distance;
    }

    private boolean isWithinLastTwoDays(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date reportDate = sdf.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -2);
            Date twoDaysAgo = calendar.getTime();
            return reportDate != null && reportDate.after(twoDaysAgo);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Check for SMS permission after location permission is granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.SEND_SMS},
                                SMS_PERMISSION_REQUEST_CODE);
                    } else {
                        sosHandler.onSOSButtonClick();
                    }
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case SMS_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sosHandler.onSOSButtonClick();
                } else {
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }



    private void checkLocationPermissionAndSendSOS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            sosHandler.onSOSButtonClick();
        }
    }




}