package com.example.collectme;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Section2 extends WearableActivity implements SensorEventListener {

    private static final String TAG = "Section2";
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;

    private Button btRecord;
    private Spinner spinnerAct;
    private EditText repetition;

    private String[] actions;
    private String action, rep;
    private String ax, ay, az, gx, gy, gz;
    private String filePath, fileName;
    private String subjectID;
    private FileWriter writer;

    private boolean record = false;
    private boolean flagA = false;
    private boolean flagG = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent myIntent = getIntent();
        subjectID = myIntent.getStringExtra("SUBJECT_ID");
        Log.d(TAG, subjectID);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, 10000);
            Log.d(TAG, "onCreate: Registered accelerometer listener");
        }

        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, 10000);
            Log.d(TAG, "onCreate: Registered gyroscope listener");
        }

        spinnerAct = findViewById(R.id.spinner);
        populateSpinnerAct();

        btRecord = findViewById(R.id.btRecord);
        btRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dataCollector(arg0);
            }
        });

        repetition = findViewById(R.id.rep);

        filePath = Environment.getExternalStorageDirectory() + "/Data/" + subjectID + "/2";
        File file = new File(Environment.getExternalStorageDirectory()
                + "/Data/" + subjectID + "/2");

        if (!file.exists())
            file.mkdirs();

        setAmbientEnabled();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            flagA = true;
            ax = String.format("%.4f", event.values[0]);
            ay = String.format("%.4f", event.values[1]);
            az = String.format("%.4f", event.values[2]);
        }

        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            flagG = true;
            gx = String.format("%.4f", event.values[0]);
            gy = String.format("%.4f", event.values[1]);
            gz = String.format("%.4f", event.values[2]);
        }

        if (flagA && flagG) {
            SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date date = new Date(System.currentTimeMillis());
            String data = formatter.format(date);
            data +=  "," + ax + "," + ay + "," + az + "," + gx + "," + gy + "," + gz + "\n";

            if (!data.isEmpty() && record)
                dataReader(data);

            flagA = false;
            flagG = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void dataCollector(View arg0) {
        String buttonText = btRecord.getText().toString();

        if (buttonText.equals("Start")) {
            Toast.makeText(getApplicationContext(), "Recording",
                    Toast.LENGTH_SHORT).show();

            action = spinnerAct.getSelectedItem().toString().trim().replaceAll(" ", "");

            Date date = new Date(System.currentTimeMillis());
            String timeMilli = "" + date.getTime();

            fileName =  action + "_" + timeMilli + ".csv";
            fileName = timeMilli + "_" + subjectID + "_" + action + ".csv";
            record = true;

            try {
                writer = new FileWriter(new File(filePath, fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
            btRecord.setText("Stop");
        }

        else if (buttonText.equals("Stop")){
            record = false;
            repetition.setEnabled(true);
            btRecord.setText("Save");
        }

        else {
            rep = repetition.getText().toString().trim();
            if (rep.equals(""))
                Toast.makeText(getApplicationContext(), "Missing repetition",
                        Toast.LENGTH_SHORT).show();
            else {
                File from = new File(filePath, fileName);
                fileName = fileName.substring(0, fileName.indexOf(".")) + "_" + rep + ".csv";
                File to = new File(filePath, fileName);
                from.renameTo(to);

                btRecord.setText("Start");
                Toast.makeText(getApplicationContext(), "Saving",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dataReader(String data) {
        try {
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateSpinnerAct() {
        actions = new String[]{"Push up", "Skipping", "Boxing", "Chopping", "Peeling", "Stirring"};
        ArrayAdapter actAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, actions);
        actAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerAct.setAdapter(actAdapter);
    }
}