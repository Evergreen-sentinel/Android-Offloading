package com.example.offloader;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class ImageUploader {
    public interface UploadCallback {
        void onSuccess(byte[] responseBytes);
        void onFailure(String error);
    }

    private static final String TAG = "ImageUploader";
    private final OkHttpClient client;
    private final String serverUrl;

    public ImageUploader(String serverUrl) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.serverUrl = serverUrl;
        Log.d(TAG, "ImageUploader initialized with server URL: " + serverUrl);
    }

    /**
     * Uploads the selected image file to the server.
     * @param context The context, used to resolve the Uri into an InputStream.
     * @param fileUri The Uri of the image file to upload.
     * @param callback The OkHttp Callback to handle success/failure responses.
     */
    // CRITICAL: This signature must match the call in ImageActivity.java
    public void upload(Context context, Uri fileUri, Callback callback) {
        Log.d(TAG, "Starting upload to: " + serverUrl + "upload");
        
        try {
            // Get the InputStream from the file Uri
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                // If stream is null, report failure immediately
                throw new IOException("Could not open input stream for file.");
            }

            Log.d(TAG, "Successfully opened input stream for file URI: " + fileUri);

            // Create a RequestBody from the InputStream
            // We use a custom RequestBody to stream the data
            RequestBody requestBody = new  InputStreamRequestBody(
                    MediaType.parse("image/jpeg"), // Assumes JPEG, adjust as needed
                    inputStream
            );

            // Build the Multipart request body
            MultipartBody multipartBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.jpg", requestBody) // 'file' matches Flask server's expected field name
                    .build();

            Log.d(TAG, "Built multipart request body with 'file' field");

            // Build the final HTTP POST request
            Request request = new Request.Builder()
                    .url(serverUrl + "upload") // Use the correct /upload endpoint
                    .post(multipartBody)
                    .addHeader("User-Agent", "AndroidApp/1.0")
                    .addHeader("Accept", "image/jpeg")
                    .addHeader("Connection", "close") // Help with connection management
                    .build();

            Log.d(TAG, "Sending POST request to: " + request.url());
            Log.d(TAG, "Request headers: " + request.headers());

            // Enqueue the call (runs on a background thread)
            client.newCall(request).enqueue(callback);

        } catch (IOException e) {
            Log.e(TAG, "Error during file upload setup: " + e.getMessage(), e);
            // Need to manually call failure since setup failed before enqueue
            callback.onFailure(null, e);
        }
    }

    /**
     * Test connectivity to the server using the /ping endpoint
     * @param callback The callback to handle the ping response
     */
    public void testConnectivity(Callback callback) {
        Log.d(TAG, "Testing connectivity to: " + serverUrl + "ping");
        
        Request request = new Request.Builder()
                .url(serverUrl + "ping")
                .get()
                .addHeader("User-Agent", "AndroidApp/1.0")
                .build();
                
        client.newCall(request).enqueue(callback);
    }

    // You will need a simple wrapper class to handle the InputStream for OkHttp
    // This is often needed when using InputStream directly in RequestBody
    private static class InputStreamRequestBody extends RequestBody {
        private final MediaType contentType;
        private final InputStream inputStream;

        public InputStreamRequestBody(MediaType contentType, InputStream inputStream) {
            this.contentType = contentType;
            this.inputStream = inputStream;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            try (Source source = Okio.source(inputStream)) {
                sink.writeAll(source);
            }
        }
    }
}
