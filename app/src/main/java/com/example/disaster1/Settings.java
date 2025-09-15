package com.example.disaster1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Settings extends AppCompatActivity {

    private TextInputEditText newEmergencyNumber;
    private AutoCompleteTextView newLocation;
    private Button updateLocationBtn, updateEmergencyBtn;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        newLocation = findViewById(R.id.newLocation);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.town_city_list, android.R.layout.simple_dropdown_item_1line);
        newLocation.setAdapter(adapter);

        newEmergencyNumber = findViewById(R.id.newEmergencyNumber);
        updateLocationBtn = findViewById(R.id.updateLocationbtn);
        updateEmergencyBtn = findViewById(R.id.updateEmergencybtn);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        updateLocationBtn.setOnClickListener(v -> {
            String address = newLocation.getText().toString().trim();
            if (TextUtils.isEmpty(address)) {
                Toast.makeText(Settings.this, "Please enter a location", Toast.LENGTH_SHORT).show();
                return;
            }
            new FetchLocationCoordinates(Settings.this, address).execute();
            startActivity(new Intent(getApplicationContext(), splash.class));
            finish();
        });

        updateEmergencyBtn.setOnClickListener(v -> {
            String emergencyContact = newEmergencyNumber.getText().toString().trim();
            if (TextUtils.isEmpty(emergencyContact)) {
                Toast.makeText(Settings.this, "Please enter an emergency contact number", Toast.LENGTH_SHORT).show();
                return;
            }
            updateEmergencyContact(emergencyContact);
            startActivity(new Intent(getApplicationContext(), splash.class));
            finish();
        });
    }

    public void updateLocationInFirestore(String latitude, String longitude) {
        FirebaseUser user = fAuth.getCurrentUser();
        DocumentReference df = fStore.collection("Users").document(user.getUid());

        Map<String, Object> updates = new HashMap<>();
        updates.put("Latitude", latitude);
        updates.put("Longitude", longitude);

        df.update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(Settings.this, "Location Updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Settings.this, "Failed to Update Location", Toast.LENGTH_SHORT).show());
    }

    private void updateEmergencyContact(String emergencyContact) {
        FirebaseUser user = fAuth.getCurrentUser();
        DocumentReference df = fStore.collection("Users").document(user.getUid());

        Map<String, Object> updates = new HashMap<>();
        updates.put("EmergencyContact", emergencyContact);

        df.update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(Settings.this, "Emergency Contact Updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Settings.this, "Failed to Update Emergency Contact", Toast.LENGTH_SHORT).show());
    }
}
