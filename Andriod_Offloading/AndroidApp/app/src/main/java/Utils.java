package com.example.offloader;

import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {
    // Read all bytes from a Uri (works with content:// URIs returned by ACTION_GET_CONTENT)
    public static byte[] readBytesFromUri(Context ctx, Uri uri) throws IOException {
        InputStream in = ctx.getContentResolver().openInputStream(uri);
        if (in == null) throw new IOException("Unable to open input stream from URI");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int n;
        while ((n = in.read(data)) != -1) {
            buffer.write(data, 0, n);
        }
        in.close();
        return buffer.toByteArray();
    }
}
