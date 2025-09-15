package com.example.disaster1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.disaster1.MainActivity;
import com.example.disaster1.R;
import com.example.disaster1.activity_login;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.location.Address;
import android.location.Geocoder;

public class activity_register extends AppCompatActivity {
    EditText fullName, email, password, phone, emergencyContact;
    Button registerBtn, goToLogin;
    AutoCompleteTextView locationInput;
    CheckBox isReporterBox, isUserBox;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        geocoder = new Geocoder(this);

        fullName = findViewById(R.id.registerName);
        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        phone = findViewById(R.id.registerPhone);
        emergencyContact = findViewById(R.id.Emergency_Contact);
        locationInput = findViewById(R.id.location_input);
        registerBtn = findViewById(R.id.registerBtn);
        goToLogin = findViewById(R.id.gotoLogin);
        isUserBox = findViewById(R.id.isUser);
        isReporterBox = findViewById(R.id.isReporter);

        // Setup AutoCompleteTextView with predefined list of towns/cities
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.town_city_list, android.R.layout.simple_dropdown_item_1line);
        locationInput.setAdapter(adapter);

        locationInput.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = (String) parent.getItemAtPosition(position);
            getCoordinatesFromCity(selectedCity);
        });

        // Checkbox one check
        isUserBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                isReporterBox.setChecked(false);
            }
        });

        isReporterBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                isUserBox.setChecked(false);
            }
        });

        registerBtn.setOnClickListener(v -> {
            if (checkField(fullName) && checkField(email) && checkField(password) && checkField(phone)) {
                if (!(isReporterBox.isChecked() || isUserBox.isChecked())) {
                    Toast.makeText(activity_register.this, "Select the Account Type", Toast.LENGTH_SHORT).show();
                    return;
                }

                fAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = fAuth.getCurrentUser();
                            Toast.makeText(activity_register.this, "Account Created", Toast.LENGTH_SHORT).show();

                            DocumentReference df = fStore.collection("Users").document(user.getUid());
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("FullName", fullName.getText().toString());
                            userInfo.put("UserEmail", email.getText().toString());

                            Map<String, String> coordinates = (Map<String, String>) locationInput.getTag();
                            userInfo.put("Latitude", coordinates != null ? coordinates.get("latitude") : "N/A");
                            userInfo.put("Longitude", coordinates != null ? coordinates.get("longitude") : "N/A");
                            userInfo.put("PhoneNumber", phone.getText().toString());
                            userInfo.put("EmergencyContact", emergencyContact.getText().toString());
                            if (isReporterBox.isChecked()) {
                                userInfo.put("isAdmin", "1");
                            }
                            if (isUserBox.isChecked()) {
                                userInfo.put("isUser", "1");
                            }
                            df.set(userInfo);
                            if (isUserBox.isChecked()) {
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else if (isReporterBox.isChecked()) {
                                startActivity(new Intent(getApplicationContext(), Admin.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(activity_register.this, "Failed to Create an Account", Toast.LENGTH_SHORT).show());
            }
        });

        goToLogin.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), activity_login.class)));
    }

    private void getCoordinatesFromCity(String city) {
        try {
            List<Address> addresses = geocoder.getFromLocationName(city, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                Map<String, String> coordinates = new HashMap<>();
                coordinates.put("latitude", String.valueOf(address.getLatitude()));
                coordinates.put("longitude", String.valueOf(address.getLongitude()));
                locationInput.setTag(coordinates);
            } else {
                Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Geocoder service not available", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean checkField(EditText textField) {
        if (textField.getText().toString().isEmpty()) {
            textField.setError("Field cannot be empty");
            return false;
        }
        return true;
    }
}
