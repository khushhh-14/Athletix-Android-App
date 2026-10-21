package com.example.madproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
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
        // 1. Check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            return;
        }

        // 2. Check if device location/GPS is toggled on in phone settings
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(this, "Please enable Location / GPS in your phone settings", Toast.LENGTH_LONG).show();
            tv_loc.setText("Location is disabled in phone settings.");
            return;
        }

        tv_loc.setText("Fetching address...");

        // 3. Try to get instant last known location from network or GPS
        Location lastKnown = null;
        if (isNetworkEnabled) {
            lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (lastKnown == null && isGpsEnabled) {
            lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (lastKnown != null) {
            displayAddressFromLocation(lastKnown);
        }

        // 4. Request live updates from both providers
        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 5, this);
        }
        if (isGpsEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        displayAddressFromLocation(location);
    }

    private void displayAddressFromLocation(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Android 13+ (API 33) compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, lng, 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(@NonNull List<Address> addresses) {
                    runOnUiThread(() -> processAddressResult(addresses, lat, lng));
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> tv_loc.setText("Lat: " + lat + " | Lng: " + lng));
                }
            });
        } else {
            // Older Android versions
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                processAddressResult(addresses, lat, lng);
            } catch (IOException e) {
                tv_loc.setText("Lat: " + lat + " | Lng: " + lng);
            }
        }
    }

    private void processAddressResult(List<Address> addresses, double lat, double lng) {
        if (addresses != null && !addresses.isEmpty()) {
            Address address = addresses.get(0);
            String locality = address.getLocality(); // City
            String subAdmin = address.getSubAdminArea(); // Area / Taluka / District
            String state = address.getAdminArea(); // State
            String country = address.getCountryName(); // Country

            StringBuilder sb = new StringBuilder();
            if (locality != null) sb.append(locality).append(", ");
            if (subAdmin != null) sb.append(subAdmin).append(", ");
            if (state != null) sb.append(state).append(", ");
            if (country != null) sb.append(country);

            String result = sb.toString().trim();
            if (result.endsWith(",")) {
                result = result.substring(0, result.length() - 1);
            }

            tv_loc.setText("Current Location:\n" + result);
        } else {
            tv_loc.setText("Lat: " + lat + " | Lng: " + lng);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation(null);
        } else {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}