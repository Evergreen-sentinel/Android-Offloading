package com.example.offloader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class ImageUploader {
    private final String baseUrl;
    private final OkHttpClient client;

    public interface UploadCallback {
        void onSuccess(byte[] responseBytes);
        void onFailure(String error);
    }

    public ImageUploader(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = new OkHttpClient();
    }

    public void uploadImage(byte[] imageBytes, UploadCallback callback) {
        MediaType MEDIA_JPEG = MediaType.parse("image/jpeg");

        RequestBody fileBody = RequestBody.create(imageBytes, MEDIA_JPEG);
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", fileBody)
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "/upload")  // Flask endpoint
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("HTTP " + response.code());
                    return;
                }
                byte[] respBytes = response.body().bytes();
                callback.onSuccess(respBytes);
            }
        });
    }
}
