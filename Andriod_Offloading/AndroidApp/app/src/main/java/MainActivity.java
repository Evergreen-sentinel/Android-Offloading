package com.example.offloader;

import androidx.appcompat.app.AppCompatActivity; // CRITICAL: Required for AppCompatActivity
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // For simplicity, directly launch the ImageActivity
        // You can later add a dashboard layout with multiple options
        startActivity(new Intent(this, ImageActivity.class));
        finish(); // Close MainActivity so back button doesn't return here
    }
}
