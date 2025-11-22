package com.example.offloader;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements BatteryReceiver.BatteryListener {
    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final String TAG = "MainActivity";

    private ImageView imageView;
    private Button pickBtn, offloadBtn;
    private Uri selectedImageUri;
    private BatteryReceiver batteryReceiver;

    // TODO: put your server IP/hostname here (no trailing slash)
    private static final String SERVER_URL = "http://YOUR_SERVER_IP:5000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.resultImage);
        pickBtn = findViewById(R.id.pickBtn);
        offloadBtn = findViewById(R.id.offloadBtn);

        pickBtn.setOnClickListener(v -> openImagePicker());
        offloadBtn.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                // manual trigger
                tryOffload(selectedImageUri);
            } else {
                Toast.makeText(this, "Pick an image first", Toast.LENGTH_SHORT).show();
            }
        });

        // Register battery low receiver dynamically
        batteryReceiver = new BatteryReceiver(this);
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(batteryReceiver);
        } catch (IllegalArgumentException e) { /* ignore */ }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    byte[] bytes = Utils.readBytesFromUri(this, selectedImageUri);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageView.setImageBitmap(bmp);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void tryOffload(Uri uri) {
        // Check current battery percent before offloading (optional, extra check)
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
        float batteryPct = (level >= 0 && scale > 0) ? (level * 100f / scale) : 100f;

        // Offload if battery < 20% OR if manual triggered (we use manual trigger button too)
        if (batteryPct < 20.0f) {
            Toast.makeText(this, "Battery low — offloading to cloud...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Battery okay — still demonstrating offload...", Toast.LENGTH_SHORT).show();
        }

        // Read bytes and send to server
        try {
            byte[] bytes = Utils.readBytesFromUri(this, uri);
            ImageUploader uploader = new ImageUploader(SERVER_URL);
            uploader.uploadImage(bytes, new ImageUploader.UploadCallback() {
                @Override
                public void onSuccess(byte[] responseBytes) {
                    // run on UI thread
                    runOnUiThread(() -> {
                        Bitmap gray = BitmapFactory.decodeByteArray(responseBytes, 0, responseBytes.length);
                        imageView.setImageBitmap(gray);
                        Toast.makeText(MainActivity.this, "Received processed image", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Upload failed: " + error, Toast.LENGTH_LONG).show();
                    });
                    Log.e(TAG, "Upload failed: " + error);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read image bytes", Toast.LENGTH_SHORT).show();
        }
    }

    // BatteryReceiver.BatteryListener implementation:
    @Override
    public void onBatteryLow() {
        // Automatic offload when battery low — offload last selected image
        if (selectedImageUri != null) {
            tryOffload(selectedImageUri);
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Battery low but no image selected", Toast.LENGTH_SHORT).show());
        }
    }
}
