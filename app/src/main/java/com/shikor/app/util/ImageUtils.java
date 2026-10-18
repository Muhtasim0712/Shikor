package com.shikor.app.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * ImageUtils - Compresses profile photos to compact Base64 strings for Firestore
 * and decodes them back to local storage upon login or cloud sync.
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * Reads an image file from disk, resizes it so max dimension is <= maxDim,
     * compresses to JPEG with specified quality (e.g. 75), and returns a Base64 string.
     */
    @Nullable
    public static String fileToBase64Thumbnail(File file, int maxDim, int quality) {
        if (file == null || !file.exists()) return null;

        try {
            // First decode bounds
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            int width = options.outWidth;
            int height = options.outHeight;
            if (width <= 0 || height <= 0) return null;

            int sampleSize = 1;
            while (Math.max(width / sampleSize, height / sampleSize) > maxDim * 2) {
                sampleSize *= 2;
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            Bitmap raw = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            if (raw == null) return null;

            // Crop to center square
            int minDim = Math.min(raw.getWidth(), raw.getHeight());
            int x = (raw.getWidth() - minDim) / 2;
            int y = (raw.getHeight() - minDim) / 2;
            Bitmap square = Bitmap.createBitmap(raw, x, y, minDim, minDim);

            // Scale down to exact maxDim
            Bitmap scaled = Bitmap.createScaledBitmap(square, maxDim, maxDim, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] bytes = baos.toByteArray();

            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Error encoding image to Base64 thumbnail: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Decodes a Base64 string and writes it to destFile.
     */
    public static boolean base64ToFile(String base64Str, File destFile) {
        if (base64Str == null || base64Str.trim().isEmpty() || destFile == null) return false;

        try {
            byte[] bytes = Base64.decode(base64Str.trim(), Base64.DEFAULT);
            File parent = destFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                fos.write(bytes);
                fos.flush();
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error decoding Base64 to file: " + e.getMessage(), e);
            return false;
        }
    }
}
