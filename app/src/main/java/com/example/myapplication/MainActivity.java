package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button buttonGetLocation  ,submitButton ;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText inputFullName, inputAge, inputDescription, inputPhone, inputPrice, inputLocation, inputUsername;
    private Spinner spinnerStatus, spinnerCars;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String USERNAME_KEY = "username";
    private static final String DIALOG_SHOWN_KEY = "username_dialog_shown";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

//        was the user warned
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(DIALOG_SHOWN_KEY, false)) {
            showUsernameImportanceDialog();
        }


        inputFullName = findViewById(R.id.inputFullName);
        inputAge = findViewById(R.id.inputAge);
        spinnerCars = findViewById(R.id.inputCar);
        inputDescription = findViewById(R.id.inputDescription);
        inputLocation = findViewById(R.id.inputLocation);
        inputPhone = findViewById(R.id.inputPhone);
        inputPrice = findViewById(R.id.inputPrice);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        buttonGetLocation = findViewById(R.id.buttonGetLocation);
        submitButton = findViewById(R.id.submitButton);
        inputUsername = findViewById(R.id.inputUsername);



        // the last session inputs
        initializeFields();
        loadDataFromSharedPreferences();




//      filling the box of cars ans status
        String[] statusOptions = {"Busy", "Free", "Out of Service"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        String[] carsOptions = {"BMW", "RANGE ROVER", "Ferrari"};
        ArrayAdapter<String> carsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carsOptions);
        carsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCars.setAdapter(carsAdapter);


        buttonGetLocation.setOnClickListener(v -> {
//            ask for permission else (permission given) go fetch
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                fetchLocation();
            }
        });

        submitButton.setOnClickListener(v -> {
            saveUserData();
        });

    }

    private void initializeFields() {
        inputFullName = findViewById(R.id.inputFullName);
        inputAge = findViewById(R.id.inputAge);
        spinnerCars = findViewById(R.id.inputCar);
        inputDescription = findViewById(R.id.inputDescription);
        inputLocation = findViewById(R.id.inputLocation);
        inputPhone = findViewById(R.id.inputPhone);
        inputPrice = findViewById(R.id.inputPrice);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        buttonGetLocation = findViewById(R.id.buttonGetLocation);
        submitButton = findViewById(R.id.submitButton);
        inputUsername = findViewById(R.id.inputUsername);

        // Initialize Spinner adapters
        String[] statusOptions = {"Busy", "Free", "Out of Service"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        String[] carsOptions = {"BMW", "RANGE ROVER", "Ferrari"};
        ArrayAdapter<String> carsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carsOptions);
        carsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCars.setAdapter(carsAdapter);
    }


    private void loadDataFromSharedPreferences() {

        String[] statusOptions = {"Busy", "Free", "Out of Service"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        String[] carsOptions = {"BMW", "RANGE ROVER", "Ferrari"};
        ArrayAdapter<String> carsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carsOptions);
        carsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCars.setAdapter(carsAdapter);

        // Use ternary operations to handle null values
        inputFullName.setText(sharedPreferences.getString("full_name", "") != null ? sharedPreferences.getString("full_name", "") : "");
        inputAge.setText(sharedPreferences.getString("age", "") != null ? sharedPreferences.getString("age", "") : "");
        inputDescription.setText(sharedPreferences.getString("description", "") != null ? sharedPreferences.getString("description", "") : "");
        inputLocation.setText(sharedPreferences.getString("location", "") != null ? sharedPreferences.getString("location", "") : "");
        inputPhone.setText(sharedPreferences.getString("phone", "") != null ? sharedPreferences.getString("phone", "") : "");
        inputPrice.setText(sharedPreferences.getString("price", "") != null ? sharedPreferences.getString("price", "") : "");
        inputUsername.setText(sharedPreferences.getString("username", "") != null ? sharedPreferences.getString("username", "") : "");

        // Load Spinner values with ternary operations
        String savedCar = sharedPreferences.getString("car", "");
        if (savedCar != null && !savedCar.isEmpty()) {
            int carPosition = carsAdapter.getPosition(savedCar);
            spinnerCars.setSelection(carPosition);
        }

        String savedStatus = sharedPreferences.getString("status", "");
        if (savedStatus != null && !savedStatus.isEmpty()) {
            int statusPosition = statusAdapter.getPosition(savedStatus);
            spinnerStatus.setSelection(statusPosition);
        }
    }

    private void saveUserData() {
        String fullName = inputFullName.getText().toString().trim();
        String age = inputAge.getText().toString().trim();
        String car = spinnerCars.getSelectedItem().toString().trim();
        String description = inputDescription.getText().toString().trim();
        String location = inputLocation.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String price = inputPrice.getText().toString().trim();
        String username = inputUsername.getText().toString().trim();

        // Save each input to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("full_name", fullName);
        editor.putString("age", age);
        editor.putString("car", car);
        editor.putString("description", description);
        editor.putString("location", location);
        editor.putString("phone", phone);
        editor.putString("status", status);
        editor.putString("price", price);
        editor.putString("username", username);
        editor.apply();

        // Prepare userData map
        Map<String, Object> userData = new HashMap<>();
        userData.put("full_name", fullName);
        userData.put("age", Integer.parseInt(age));
        userData.put("car", car);
        userData.put("description", description);
        userData.put("location", location);
        userData.put("phone", phone);
        userData.put("status", status);
        userData.put("price", Double.parseDouble(price));
        userData.put("username", username);
        userData.put("isAvailable", true);

        // Upload data to Firestore
        uploadDataToFirestore(username, userData);
    }

    private void showUsernameImportanceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Important: Username")
                .setMessage("It's crucial to keep your username consistent. Your username is the only unique identifier you have, so make sure to keep it unique and don't change it. Changing your username may result in loss of account integrity and data.")
                .setPositiveButton("I Understand", (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(DIALOG_SHOWN_KEY, true);
                    editor.apply();
                })
                .setCancelable(false)
                .show();
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(MainActivity.this);
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                String city = address.getLocality();
                                String country = address.getCountryName();
                                String fullAddress = city + ", " + country;

                                // Fill the location EditText with the fetched location
                                inputLocation.setText(fullAddress);
                            } else {
                                Toast.makeText(MainActivity.this, "Unable to determine location.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error fetching location.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Location not available.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadDataToFirestore(String username, Map<String, Object> userData) {
        db.collection("allUsers").document(username)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Data added successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to add data", Toast.LENGTH_SHORT).show();
                });
    }

}
