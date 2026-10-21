package com.example.madproject;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;

import androidx.core.graphics.Insets;

import androidx.core.view.ViewCompat;

import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

import java.util.List;

import java.util.Locale;

public class HistoryActivity extends AppCompatActivity implements LocationListener {

    DBHelper dbhelper;

    TextView tv_records, tv_loc;

    EditText et2, et3, et4;

    LocationManager locationManager;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_history);

        dbhelper = new DBHelper(getApplicationContext());

        tv_records = findViewById(R.id.tv_records);

        tv_loc = findViewById(R.id.tv_loc);

        et2 = findViewById(R.id.et2);

        et3 = findViewById(R.id.et3);

        et4 = findViewById(R.id.et4);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history_main), (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;

        });

    }

    public void readWorkouts(View view) {

        String data = dbhelper.readRecordHelper();

        if (data.isEmpty()) {

            tv_records.setText("No workout records found.");

        } else {

            tv_records.setText(data);

        }

    }

    public void deleteWorkout(View view) {

        String idStr = et2.getText().toString().trim();

        if (!idStr.isEmpty()) {

            dbhelper.deleterecordHelper(Integer.parseInt(idStr));

            Toast.makeText(this, "Record Deleted", Toast.LENGTH_SHORT).show();

            et2.setText("");

            readWorkouts(view);

        }

    }

    public void updateWorkout(View view) {

        String idStr = et3.getText().toString().trim();

        String newNotes = et4.getText().toString().trim();

        if (!idStr.isEmpty() && !newNotes.isEmpty()) {

            dbhelper.updateRecordHelper(Integer.parseInt(idStr), newNotes);

            Toast.makeText(this, "Record Updated", Toast.LENGTH_SHORT).show();

            et3.setText("");

            et4.setText("");

            readWorkouts(view);

        }

    }

    public void fetchLocation(View view) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);

        } else {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);

            Toast.makeText(this, "Fetching location name...", Toast.LENGTH_SHORT).show();

        }

    }

    @Override

    public void onLocationChanged(@NonNull Location location) {

        double lat = location.getLatitude();

        double lng = location.getLongitude();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {

            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses != null && !addresses.isEmpty()) {

                Address address = addresses.get(0);

                String locality = address.getLocality();

                String subAdmin = address.getSubAdminArea();

                String state = address.getAdminArea();

                String country = address.getCountryName();

                String locationName = "";

                if (locality != null) locationName += locality + ", ";

                if (subAdmin != null) locationName += subAdmin + ", ";

                if (state != null) locationName += state + ", ";

                if (country != null) locationName += country;

                tv_loc.setText("Current Location:\n" + locationName);

            } else {

                tv_loc.setText("Location:\nLat: " + lat + " | Lng: " + lng);

            }

        } catch (IOException e) {

            e.printStackTrace();

            tv_loc.setText("Location:\nLat: " + lat + " | Lng: " + lng);

        }

    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            fetchLocation(null);

        }

    }

}
