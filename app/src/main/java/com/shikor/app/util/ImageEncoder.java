package com.shikor.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.shikor.app.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * ImageEncoder - Utility for scaling, compressing, Base64 encoding/decoding,
 * and loading story photos with 'ShikorPhotoSync' logging.
 */
public class ImageEncoder {

    public static class ProcessedPhotoResult {
        public final String localFilePath;
        public final String base64Data;
        public final int sizeKb;

        public ProcessedPhotoResult(String localFilePath, String base64Data, int sizeKb) {
            this.localFilePath = localFilePath;
            this.base64Data = base64Data;
            this.sizeKb = sizeKb;
        }
    }

    public static ProcessedPhotoResult preparePhotoForUpload(Context context, String sourceFilePath, String storyId) {
        if (sourceFilePath == null || sourceFilePath.trim().isEmpty()) return null;
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) return null;

        try {
            int rotation = 0;
            try {
                android.media.ExifInterface exif = new android.media.ExifInterface(sourceFilePath);
                int orientation = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL);
                if (orientation == android.media.ExifInterface.ORIENTATION_ROTATE_90) rotation = 90;
                else if (orientation == android.media.ExifInterface.ORIENTATION_ROTATE_180) rotation = 180;
                else if (orientation == android.media.ExifInterface.ORIENTATION_ROTATE_270) rotation = 270;
            } catch (Exception ignored) {}

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(sourceFilePath, options);
            int origW = options.outWidth;
            int origH = options.outHeight;

            int sampleSize = 1;
            while ((origW / sampleSize) > 1024 || (origH / sampleSize) > 1024) {
                sampleSize *= 2;
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFilePath, options);
            if (bitmap == null) return null;

            if (rotation != 0) {
                android.graphics.Matrix matrix = new android.graphics.Matrix();
                matrix.postRotate(rotation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }

            int quality = 75;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            while (baos.toByteArray().length > 300 * 1024 && quality > 25) {
                baos.reset();
                quality -= 10;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }

            byte[] bytes = baos.toByteArray();
            int sizeKb = bytes.length / 1024;
            Log.d("ShikorPhotoSync", "Upload start for story " + storyId + ", size: " + sizeKb + " KB (quality: " + quality + "%)");

            File dir = new File(context.getFilesDir(), "story_photos");
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created && !dir.exists()) {
                    Log.w("ShikorPhotoSync", "Failed to create directory " + dir.getAbsolutePath());
                }
            }

            File destFile = new File(dir, "story_" + storyId + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                fos.write(bytes);
            }

            Log.d("ShikorPhotoSync", "File written locally: " + destFile.getAbsolutePath());

            String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
            return new ProcessedPhotoResult(destFile.getAbsolutePath(), base64, sizeKb);
        } catch (Exception e) {
            Log.e("ShikorPhotoSync", "Decode failure for story " + storyId, e);
            return null;
        }
    }

    public static String saveBase64ToStoryFile(Context context, String storyId, String base64Str) {
        if (base64Str == null || base64Str.trim().isEmpty()) return "";
        try {
            File dir = new File(context.getFilesDir(), "story_photos");
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created && !dir.exists()) {
                    Log.w("ShikorPhotoSync", "Failed to create directory " + dir.getAbsolutePath());
                }
            }

            File destFile = new File(dir, "story_" + storyId + ".jpg");

            String cleanBase64 = base64Str.trim();
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
            }

            byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                fos.write(decodedBytes);
            }

            Log.d("ShikorPhotoSync", "Decode success & file written for story " + storyId + ": " + destFile.getAbsolutePath());
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("ShikorPhotoSync", "Decode failure for story " + storyId, e);
            return "";
        }
    }

    public static String fileToBase64(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) return "";
        try {
            File file = new File(filePath);
            if (!file.exists()) return filePath;

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;
            int scaleFactor = Math.max(1, Math.min(photoW / 800, photoH / 800));

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmOptions);

            if (bitmap == null) return filePath;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            return filePath;
        }
    }

    public static String base64ToFile(Context context, String base64Str) {
        if (base64Str == null || base64Str.trim().isEmpty()) return "";
        String trimmed = base64Str.trim();
        if (trimmed.startsWith("/") || trimmed.startsWith("file://") || trimmed.startsWith("content://") || trimmed.startsWith("http")) {
            return trimmed;
        }

        try {
            File dir = new File(context.getFilesDir(), "synced_photos");
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created && !dir.exists()) {
                    Log.w("ShikorPhotoSync", "Failed to create directory " + dir.getAbsolutePath());
                }
            }

            File destFile = new File(dir, "sync_photo_" + System.currentTimeMillis() + "_" + Math.abs(trimmed.hashCode()) + ".jpg");
            if (destFile.exists()) {
                return destFile.getAbsolutePath();
            }

            String cleanBase64 = trimmed;
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
            }

            byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                fos.write(decodedBytes);
            }
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            return trimmed;
        }
    }

    public static void loadImage(ImageView imageView, String path) {
        if (imageView == null || path == null || path.trim().isEmpty()) return;

        try {
            String trimmed = path.trim();
            File file = new File(trimmed);
            Glide.with(imageView.getContext())
                    .load(file.exists() ? file : trimmed)
                    .placeholder(R.color.surface_variant)
                    .error(R.color.surface_variant)
                    .centerCrop()
                    .into(imageView);
        } catch (Exception e) {
            Glide.with(imageView.getContext())
                    .load(path)
                    .into(imageView);
        }
    }
}
