package com.example.collectme;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

public class MainActivity extends WearableActivity {

    private static final String TAG = "Login";

    private EditText subjectID;
    private Button btLogin;

    public static final String EXTRA_SUBJECT_ID = "SUBJECT_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subjectID = findViewById(R.id.subjectID);

        btLogin = findViewById(R.id.btLogin);
        btLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login(v);
            }
        });

        // Enables Always-on
        setAmbientEnabled();
    }

    public void login(View view) {
        String id = subjectID.getText().toString().trim();
        File file = new File(Environment.getExternalStorageDirectory() + "/Data", id);
        if (!file.exists())
            file.mkdirs();

        Intent intent = new Intent(this, Section1.class);
        intent.putExtra(EXTRA_SUBJECT_ID, id);
        startActivity(intent);
    }
}