package com.shikor.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * MemoryImageCompressor - Compresses memory photos: corrects EXIF rotation, scales max side <= 1280px,
 * and compresses to JPEG under ~250KB with Base64 NO_WRAP encoding.
 * CONCEPT: Image processing, EXIF handling, bitmap scaling, compression.
 */
public class MemoryImageCompressor {

    private static final String TAG = "ShikorMemory";
    private static final int MAX_DIMENSION = 1280;
    private static final int TARGET_SIZE_BYTES = 250 * 1024;

    public static class ProcessedPhoto {
        public final String localPath;
        public final String base64Data;
        public final int sizeKb;

        public ProcessedPhoto(String localPath, String base64Data, int sizeKb) {
            this.localPath = localPath;
            this.base64Data = base64Data;
            this.sizeKb = sizeKb;
        }
    }

    @Nullable
    public static ProcessedPhoto compressAndEncode(Context context, String sourcePath, String memoryId, int index) {
        if (sourcePath == null || sourcePath.trim().isEmpty()) return null;
        File file = new File(sourcePath);
        if (!file.exists()) return null;

        try {
            // 1. Read EXIF rotation
            int rotation = 0;
            try {
                ExifInterface exif = new ExifInterface(sourcePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotation = 90;
                else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotation = 180;
                else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotation = 270;
            } catch (Exception ignored) {}

            // 2. Decode bounds & scale down max side <= 1280px
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(sourcePath, options);
            int origW = options.outWidth;
            int origH = options.outHeight;

            int sampleSize = 1;
            while ((origW / sampleSize) > MAX_DIMENSION || (origH / sampleSize) > MAX_DIMENSION) {
                sampleSize *= 2;
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            Bitmap bitmap = BitmapFactory.decodeFile(sourcePath, options);
            if (bitmap == null) return null;

            if (rotation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }

            // 3. Compress JPEG starting at quality 80, lowering until under ~250 KB
            int quality = 80;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            while (baos.toByteArray().length > TARGET_SIZE_BYTES && quality > 25) {
                baos.reset();
                quality -= 10;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }

            byte[] bytes = baos.toByteArray();
            int sizeKb = bytes.length / 1024;
            Log.d(TAG, "Photo compression for memory " + memoryId + " [idx " + index + "]: " + sizeKb + " KB (quality: " + quality + "%)");

            // 4. Save to app-specific storage (files dir, memories/<memoryId>/photo_<n>.jpg)
            File dir = new File(context.getFilesDir(), "memories/" + memoryId);
            if (!dir.exists()) dir.mkdirs();

            File destFile = new File(dir, "photo_" + index + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                fos.write(bytes);
            }

            String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
            return new ProcessedPhoto(destFile.getAbsolutePath(), base64, sizeKb);
        } catch (Exception e) {
            Log.e(TAG, "Error compressing photo for memory " + memoryId + ": " + e.getMessage(), e);
            return null;
        }
    }
}
