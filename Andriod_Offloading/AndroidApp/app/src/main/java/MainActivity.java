package com.example.offloader;

import androidx.appcompat.app.AppCompatActivity; // CRITICAL: Required for AppCompatActivity
import android.content.Intent;
import android.os.Bundle;
import android.view.View; // Required for findViewById and setOnClickListener
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Links to the dashboard menu layout

        // 1. Image Offloading Button
        // Launches ImageActivity
        findViewById(R.id.btn_task_image).setOnClickListener(v ->
                startActivity(new Intent(this, ImageActivity.class)));

        // 2. Video Uploading Button
        // Launches VideoActivity (placeholder)
        findViewById(R.id.btn_task_video_upload).setOnClickListener(v ->
                startActivity(new Intent(this, VideoActivity.class)));

        // 3. Video Editing Button
        // Launches VideoActivity (placeholder)
        findViewById(R.id.btn_task_video_edit).setOnClickListener(v ->
                startActivity(new Intent(this, VideoActivity.class)));

        // 4. Presentation Button (Placeholder)
        // Shows a Toast message
        findViewById(R.id.btn_task_presentation).setOnClickListener(v ->
                Toast.makeText(this, "Presentation Tool Not Yet Implemented", Toast.LENGTH_SHORT).show());
    }
}
