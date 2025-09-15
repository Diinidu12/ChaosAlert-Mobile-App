package com.example.disaster1;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SOSHandler {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int SMS_PERMISSION_REQUEST_CODE = 2;
    private Activity activity;
    private FusedLocationProviderClient fusedLocationClient;

    public SOSHandler(Activity activity) {
        this.activity = activity;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public void onSOSButtonClick() {
        // Check for SMS permission
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
            return;
        }

        // Check for location permission
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation(location -> {
                if (location != null) {
                    retrieveEmergencyContactAndSendSOS(location);
                } else {
                    Toast.makeText(activity, "Failed to get location. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.d("SOSHandler", "Location is null.");
                }
            });
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation(final LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissions not granted, handle the case here if needed
            Toast.makeText(activity, "Location permission is required", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location location = task.getResult();
                        callback.onLocationReceived(location);
                    } else {
                        Log.d("SOSHandler", "Failed to get location: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        callback.onLocationReceived(null);
                    }
                });
    }

    private void retrieveEmergencyContactAndSendSOS(Location location) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid(); // Get the user ID of the currently logged-in user

        if (userId == null) {
            Toast.makeText(activity, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.d("SOSHandler", "User ID is null.");
            return;
        }

        DocumentReference docRef = db.collection("Users").document(userId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String emergencyContact = document.getString("EmergencyContact");
                    Log.d("SOSHandler", "Emergency Contact: " + emergencyContact);
                    if (emergencyContact != null) {
                        sendSOSMessage(emergencyContact, location);
                    } else {
                        Toast.makeText(activity, "Emergency contact not found", Toast.LENGTH_SHORT).show();
                        Log.d("SOSHandler", "Emergency contact is null.");
                    }
                } else {
                    Toast.makeText(activity, "No such document", Toast.LENGTH_SHORT).show();
                    Log.d("SOSHandler", "Document does not exist for user ID: " + userId);
                }
            } else {
                Toast.makeText(activity, "Failed to retrieve emergency contact", Toast.LENGTH_SHORT).show();
                Log.e("SOSHandler", "Error retrieving document: ", task.getException());
            }
        });
    }

    private void sendSOSMessage(String phoneNumber, Location location) {
        String message = "SOS: Emergency situation. Please help!";
        if (location != null) {
            message += " Location: Lat=" + location.getLatitude() + ", Lng=" + location.getLongitude();
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(activity, "SOS message sent", Toast.LENGTH_SHORT).show();
            Log.d("SOSHandler", "SOS message sent to " + phoneNumber + ": " + message);
        } catch (Exception e) {
            Toast.makeText(activity, "Failed to send SOS message", Toast.LENGTH_SHORT).show();
            Log.e("SOSHandler", "Error sending SOS: ", e);
        }
    }

    // Handle permission results
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onSOSButtonClick();
                } else {
                    Toast.makeText(activity, "Location permission denied. Cannot send SOS.", Toast.LENGTH_SHORT).show();
                    Log.d("SOSHandler", "Location permission denied.");
                }
                break;
            case SMS_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onSOSButtonClick();
                } else {
                    Toast.makeText(activity, "SMS permission denied. Cannot send SOS.", Toast.LENGTH_SHORT).show();
                    Log.d("SOSHandler", "SMS permission denied.");
                }
                break;
        }
    }

    interface LocationCallback {
        void onLocationReceived(Location location);
    }
}
