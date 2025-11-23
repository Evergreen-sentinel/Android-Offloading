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

public class ImageUploader {
    public interface UploadCallback {
        void onSuccess(byte[] responseBytes);
        void onFailure(String error);
    }

    private static final String TAG = "ImageUploader";
    private final OkHttpClient client;
    private final String serverUrl;

    public ImageUploader(String serverUrl) {
        this.client = new OkHttpClient();
        this.serverUrl = serverUrl;
    }

    /**
     * Uploads the selected image file to the server.
     * @param context The context, used to resolve the Uri into an InputStream.
     * @param fileUri The Uri of the image file to upload.
     * @param callback The OkHttp Callback to handle success/failure responses.
     */
    // CRITICAL: This signature must match the call in ImageActivity.java
    public void upload(Context context, Uri fileUri, Callback callback) {
        try {
            // Get the InputStream from the file Uri
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                // If stream is null, report failure immediately
                throw new IOException("Could not open input stream for file.");
            }

            // Create a RequestBody from the InputStream
            // We use a custom RequestBody to stream the data
            RequestBody requestBody = new  InputStreamRequestBody(
                    MediaType.parse("image/jpeg"), // Assumes JPEG, adjust as needed
                    inputStream
            );

            // Build the Multipart request body
            MultipartBody multipartBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image_file", "image.jpg", requestBody) // 'image_file' must match the server's expected field name
                    .build();

            // Build the final HTTP POST request
            Request request = new Request.Builder()
                    .url(serverUrl + "/upload_image") // Ensure your server route is correct
                    .post(multipartBody)
                    .build();

            // Enqueue the call (runs on a background thread)
            client.newCall(request).enqueue(callback);

        } catch (IOException e) {
            Log.e(TAG, "Error during file upload setup: " + e.getMessage());
            // Need to manually call failure since setup failed before enqueue
            callback.onFailure(null, e);
        }
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
        public void writeTo(okio.BufferedSink sink) throws IOException {
            try (okio.Source source = okio.Okio.source(inputStream)) {
                sink.writeAll(source);
            }
        }
    }
}