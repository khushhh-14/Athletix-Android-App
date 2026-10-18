package com.example.madproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

    public void exportCsvFile(View view) {
        File folder = getExternalFilesDir(null);
        File file = new File(folder, "workout_logs.csv");
        try {
            FileWriter fw = new FileWriter(file);
            fw.append("ID,Category,Intensity,Warmup,Notes,DateTime\n");
            Cursor cursor = dbhelper.getAllDataCursor();
            while (cursor.moveToNext()) {
                fw.append(String.valueOf(cursor.getInt(0))).append(",")
                        .append(cursor.getString(1)).append(",")
                        .append(cursor.getString(2)).append(",")
                        .append(cursor.getString(3)).append(",")
                        .append(cursor.getString(4)).append(",")
                        .append(cursor.getString(5)).append("\n");
            }
            cursor.close();
            fw.flush();
            fw.close();
            Toast.makeText(this, "Saved to: " + file.getName(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void fetchLocation(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
            Toast.makeText(this, "Fetching coordinates...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        tv_loc.setText("Lat: " + location.getLatitude() + " | Lng: " + location.getLongitude());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation(null);
        }
    }
}