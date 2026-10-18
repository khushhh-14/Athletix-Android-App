package com.example.madproject;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int pic_id = 100;
    private static final int CAMERA_PERMISSION_CODE = 200;

    Spinner sp1;
    RadioGroup rg1;
    CheckBox cb1;
    EditText et1;
    TextView tv1, tv2;
    ImageView iv1;

    DBHelper dbhelper;
    String selectedDate = "";
    String selectedTime = "";
    BroadcastReceiver airplaneReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        sp1 = findViewById(R.id.sp1);
        rg1 = findViewById(R.id.rg1);
        cb1 = findViewById(R.id.cb1);
        et1 = findViewById(R.id.et1);
        iv1 = findViewById(R.id.iv1);

        dbhelper = new DBHelper(getApplicationContext());

        String[] categories = {"Running", "Cycling", "Weight Training", "HIIT", "Yoga"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        sp1.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        airplaneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
                if (isAirplaneModeOn) {
                    tv1.setText("Airplane Mode: ON (Offline Mode)");
                } else {
                    tv1.setText("Airplane Mode: OFF (Active)");
                }
            }
        };
        registerReceiver(airplaneReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
    }

    // Permission-checked camera launcher
    public void capturePhoto(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, pic_id);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to capture photos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            iv1.setImageBitmap(photo);
        }
    }

    public void openDatePicker(View view) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this, (v, y, m, d) -> {
            selectedDate = d + "/" + (m + 1) + "/" + y;
            tv2.setText("Date: " + selectedDate + " | Time: " + selectedTime);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    public void openTimePicker(View view) {
        Calendar c = Calendar.getInstance();
        TimePickerDialog tpd = new TimePickerDialog(this, (v, h, m) -> {
            selectedTime = String.format("%02d:%02d", h, m);
            tv2.setText("Date: " + selectedDate + " | Time: " + selectedTime);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        tpd.show();
    }

    public void saveWorkout(View view) {
        String category = sp1.getSelectedItem().toString();
        int selectedId = rg1.getCheckedRadioButtonId();
        RadioButton rb = findViewById(selectedId);
        String intensity = (rb != null) ? rb.getText().toString() : "Normal";
        String warmup = cb1.isChecked() ? "Yes" : "No";
        String notes = et1.getText().toString().trim();
        String dt = selectedDate + " " + selectedTime;

        if (notes.isEmpty()) {
            Toast.makeText(this, "Please enter notes", Toast.LENGTH_SHORT).show();
            return;
        }

        dbhelper.registerWorkoutHelper(category, intensity, warmup, notes, dt);
        Toast.makeText(this, "Workout Saved!", Toast.LENGTH_SHORT).show();
        et1.setText("");
    }

    public void openHistoryScreen(View view) {
        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (airplaneReceiver != null) {
            unregisterReceiver(airplaneReceiver);
        }
    }
}