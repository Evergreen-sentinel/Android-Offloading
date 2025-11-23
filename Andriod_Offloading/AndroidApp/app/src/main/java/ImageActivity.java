package com.example.offloader;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;

// Imports for OkHttp networking
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

// ImageActivity implements the BatteryListener interface, requiring the onBatteryLow() method.
public class ImageActivity extends AppCompatActivity implements BatteryReceiver.BatteryListener {

    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final String TAG = "ImageActivity";

    private ImageView imageView;
    private Button pickBtn, offloadBtn;
    private Uri selectedImageUri;
    private BatteryReceiver batteryReceiver;

    // TODO: Update with your server endpoint URL (should end with /)
    private static final String SERVER_URL = "https://android-offloading.onrender.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image); // Use the image layout

        imageView = findViewById(R.id.resultImage);
        pickBtn = findViewById(R.id.pickBtn);
        offloadBtn = findViewById(R.id.offloadBtn);

        // --- 1. Setup Battery Monitoring ---
        batteryReceiver = new BatteryReceiver(this); // 'this' implements BatteryListener
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
        registerReceiver(batteryReceiver, filter);
        Log.d(TAG, "BatteryReceiver registered.");

        // --- 2. Setup Image Picker ---
        pickBtn.setOnClickListener(v -> openImagePicker());

        // --- 3. Setup Manual Offload ---
        offloadBtn.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                tryOffload(selectedImageUri);
            } else {
                Toast.makeText(this, "Please select an image first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Core Logic Methods ---

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            // Display the selected image thumbnail
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found.", e);
                Toast.makeText(this, "Error reading image file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // This method handles the actual HTTP networking with OkHttp
    private void tryOffload(Uri uri) {
        // Display a message to the user
        Toast.makeText(this, "Attempting offload to server...", Toast.LENGTH_LONG).show();
        offloadBtn.setEnabled(false); // Prevent multiple clicks

        ImageUploader uploader = new ImageUploader(SERVER_URL);

        uploader.upload(this, uri, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network request failed: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(ImageActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    offloadBtn.setEnabled(true);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response finalResponse = response) {
                    Log.d(TAG, "Server response code: " + finalResponse.code());
                    
                    if (!finalResponse.isSuccessful()) {
                        String errorBody = "";
                        try {
                            if (finalResponse.body() != null) {
                                errorBody = finalResponse.body().string();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error response body", e);
                        }
                        
                        Log.e(TAG, "Server error: " + finalResponse.code() + " - " + errorBody);
                        runOnUiThread(() -> {
                            String errorMsg = "Server Error: " + finalResponse.code();
                            if (!errorBody.isEmpty()) {
                                errorMsg += " - " + errorBody;
                            }
                            Toast.makeText(ImageActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            offloadBtn.setEnabled(true);
                        });
                        return;
                    }

                    // Handle successful response - server returns processed image as JPEG
                    if (finalResponse.body() != null) {
                        byte[] imageBytes = finalResponse.body().bytes();
                        Log.d(TAG, "Received processed image, size: " + imageBytes.length + " bytes");
                        
                        runOnUiThread(() -> {
                            // Convert bytes to bitmap and display the processed image
                            Bitmap processedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            if (processedBitmap != null) {
                                imageView.setImageBitmap(processedBitmap);
                                Toast.makeText(ImageActivity.this, "Image processed successfully!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ImageActivity.this, "Error: Could not decode processed image", Toast.LENGTH_LONG).show();
                            }
                            offloadBtn.setEnabled(true);
                        });
                    } else {
                        Log.e(TAG, "Response body is null");
                        runOnUiThread(() -> {
                            Toast.makeText(ImageActivity.this, "Error: Empty response from server", Toast.LENGTH_LONG).show();
                            offloadBtn.setEnabled(true);
                        });
                    }
                }
            }
        });
    }

    // --- BatteryListener Implementation ---

    // THIS METHOD IS REQUIRED by the BatteryReceiver.BatteryListener interface
    @Override
    public void onBatteryLow() {
        Log.w(TAG, "Battery LOW signal received. Auto-offloading.");
        if (selectedImageUri != null) {
            tryOffload(selectedImageUri);
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Battery low but no image selected for auto-offload.", Toast.LENGTH_SHORT).show());
        }
    }

    // --- Lifecycle Cleanup ---

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the receiver to prevent memory leaks
        try {
            unregisterReceiver(batteryReceiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Receiver was not registered, skipping unregister.");
        }
    }
}