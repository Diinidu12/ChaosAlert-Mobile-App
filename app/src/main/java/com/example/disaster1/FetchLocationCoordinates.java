package com.example.disaster1;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class FetchLocationCoordinates extends AsyncTask<String, Void, String> {

    private String locationName;
    private Settings activity; // Reference to the Settings activity

    public FetchLocationCoordinates(Settings activity, String locationName) {
        this.activity = activity;
        this.locationName = locationName;
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            String encodedLocationName = URLEncoder.encode(locationName, "UTF-8");
            String BASE_URL = "https://geocode.xyz/" + encodedLocationName + "?json=1&auth=Authcode";
            URL url = new URL(BASE_URL);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
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
            Log.e("FetchLocation", "Error", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("FetchLocation", "Error closing stream", e);
                }
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null || result.isEmpty()) {
            Toast.makeText(activity, "Error fetching location data", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(result);
            String fetchLatitude = jsonObject.getString("latt");
            String fetchLongitude = jsonObject.getString("longt");

            activity.updateLocationInFirestore(fetchLatitude, fetchLongitude);

        } catch (Exception e) {
            Log.e("FetchLocation", "JSON Parsing error", e);
            Toast.makeText(activity, "Data parsing error", Toast.LENGTH_SHORT).show();
        }
    }
}
