package com.shikor.app.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.view.TreeLayoutEngine;
import com.shikor.app.view.TreePainter;
import com.shikor.app.view.TreeUnit;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * TreeExporter - Generates high-resolution PNG image captures and multi-page archival PDF reports
 * of the family tree and directory, then safely shares them via Android FileProvider.
 *
 * CONCEPT: Android PdfDocument generation, Bitmap graphic rendering, and FileProvider sharing.
 */
public class TreeExporter {

    private static final String TAG = "TreeExporter";
    private static final String FILE_PROVIDER_AUTHORITY = "com.shikor.app.fileprovider";

    /**
     * Exports a family tree bitmap as a high-quality PNG image and opens the system share sheet.
     */
    public static void exportTreeAsPng(Context context, Bitmap treeBitmap, String familyName) {
        exportTreeAsPng(context, treeBitmap, familyName, null);
    }

    /**
     * Exports a family tree bitmap as a high-quality PNG image and opens the system share sheet.
     * If treeBitmap is null, automatically renders it headlessly from members list.
     */
    public static void exportTreeAsPng(Context context, Bitmap treeBitmap, String familyName, List<PersonEntity> members) {
        final Context appContext = context.getApplicationContext();
        AppExecutors.getInstance().mainThread().execute(() -> {
            Toast.makeText(appContext, "Generating PNG export...", Toast.LENGTH_SHORT).show();
        });

        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                List<PersonEntity> personList = members;
                if ((personList == null || personList.isEmpty()) && treeBitmap == null) {
                    personList = AppDatabase.getInstance(appContext).personDao().getAllPeopleGlobalSync();
                }

                Bitmap bmp = treeBitmap;
                if (bmp == null && personList != null && !personList.isEmpty()) {
                    bmp = renderTreeBitmapHeadless(appContext, personList, true, false);
                }

                if (bmp == null) {
                    AppExecutors.getInstance().mainThread().execute(() -> {
                        Toast.makeText(appContext, "No tree data to export. Please ensure family members are added.", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                File exportDir = new File(appContext.getCacheDir(), "exports");
                if (!exportDir.exists()) exportDir.mkdirs();

                String safeName = sanitizeFileName(familyName != null && !familyName.isEmpty() ? familyName : "family");
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                File pngFile = new File(exportDir, "shikor_" + safeName + "_tree_" + timeStamp + ".png");

                try (FileOutputStream fos = new FileOutputStream(pngFile)) {
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                }

                AppExecutors.getInstance().mainThread().execute(() -> {
                    shareFile(appContext, pngFile, "image/png", "Share Family Tree Image (PNG)");
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to export PNG", e);
                AppExecutors.getInstance().mainThread().execute(() -> {
                    Toast.makeText(appContext, "Failed to export PNG: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Exports an archival multi-page PDF document:
     * - Page 1: Family Tree Graphical Diagram
     * - Page 2+: Family Lineage & Contact Directory
     */
    public static void exportTreeAsPdf(Context context, Bitmap treeBitmap, String familyName, List<PersonEntity> members) {
        final Context appContext = context.getApplicationContext();
        AppExecutors.getInstance().mainThread().execute(() -> {
            Toast.makeText(appContext, "Generating PDF archive...", Toast.LENGTH_SHORT).show();
        });

        AppExecutors.getInstance().diskIO().execute(() -> {
            PdfDocument document = null;
            try {
                List<PersonEntity> personList = members;
                if ((personList == null || personList.isEmpty()) && treeBitmap == null) {
                    personList = AppDatabase.getInstance(appContext).personDao().getAllPeopleGlobalSync();
                }

                Bitmap bmp = treeBitmap;
                if (bmp == null && personList != null && !personList.isEmpty()) {
                    bmp = renderTreeBitmapHeadless(appContext, personList, true, false);
                }

                document = new PdfDocument();

                // Standard A4 dimensions in PostScript points: 842 pt width x 595 pt height (Landscape for tree)
                final int PAGE_WIDTH = 842;
                final int PAGE_HEIGHT = 595;

                // --- PAGE 1: GRAPHICAL FAMILY TREE ---
                PdfDocument.PageInfo pageInfo1 = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
                PdfDocument.Page page1 = document.startPage(pageInfo1);
                Canvas canvas1 = page1.getCanvas();

                drawPdfTreePage(canvas1, bmp, familyName, PAGE_WIDTH, PAGE_HEIGHT, personList != null ? personList.size() : 0);
                document.finishPage(page1);

                // --- PAGE 2+: FAMILY DIRECTORY REGISTRY ---
                if (personList != null && !personList.isEmpty()) {
                    final int ROWS_PER_PAGE = 12;
                    int totalPages = (int) Math.ceil((double) personList.size() / ROWS_PER_PAGE);

                    for (int p = 0; p < totalPages; p++) {
                        int pageNum = 2 + p;
                        PdfDocument.PageInfo pageInfoDir = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create();
                        PdfDocument.Page pageDir = document.startPage(pageInfoDir);
                        Canvas canvasDir = pageDir.getCanvas();

                        int startIdx = p * ROWS_PER_PAGE;
                        int endIdx = Math.min(startIdx + ROWS_PER_PAGE, personList.size());
                        List<PersonEntity> subList = personList.subList(startIdx, endIdx);

                        drawPdfDirectoryPage(canvasDir, subList, familyName, PAGE_WIDTH, PAGE_HEIGHT, startIdx, pageNum, 1 + totalPages);
                        document.finishPage(pageDir);
                    }
                }

                // Write PDF to cache
                File exportDir = new File(appContext.getCacheDir(), "exports");
                if (!exportDir.exists()) exportDir.mkdirs();

                String safeName = sanitizeFileName(familyName != null && !familyName.isEmpty() ? familyName : "family");
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                File pdfFile = new File(exportDir, "shikor_" + safeName + "_archive_" + timeStamp + ".pdf");

                try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                    document.writeTo(fos);
                }

                AppExecutors.getInstance().mainThread().execute(() -> {
                    shareFile(appContext, pdfFile, "application/pdf", "Share Family Archive Document (PDF)");
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to export PDF", e);
                AppExecutors.getInstance().mainThread().execute(() -> {
                    Toast.makeText(appContext, "Failed to export PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    /**
     * Renders complete graphical family tree into a high-resolution Bitmap headlessly
     * without requiring an attached View or main thread execution.
     */
    public static Bitmap renderTreeBitmapHeadless(Context context, List<PersonEntity> people, boolean natural, boolean nightMode) {
        if (people == null || people.isEmpty()) return null;

        try {
            TreePainter treePainter = new TreePainter(context);
            float density = context.getResources().getDisplayMetrics().density;

            TreeLayoutEngine.LayoutResult layout = TreeLayoutEngine.calculateLayout(
                    people,
                    null,
                    natural,
                    treePainter.NODE_WIDTH,
                    treePainter.NODE_HEIGHT,
                    treePainter.COUPLE_GAP,
                    treePainter.SIBLING_GAP,
                    treePainter.LEVEL_GAP,
                    density
            );

            if (layout == null || layout.allUnits.isEmpty() || layout.bounds.isEmpty()) {
                return null;
            }

            RectF bounds = new RectF(layout.bounds);
            float padding = 80f * density;
            bounds.inset(-padding, -padding);

            float w = bounds.width();
            float h = bounds.height();
            if (w <= 0 || h <= 0) return null;

            float maxDim = 2400f;
            float scale = Math.min(1.0f, Math.min(maxDim / w, maxDim / h));
            int bmpW = Math.max(400, (int) (w * scale));
            int bmpH = Math.max(400, (int) (h * scale));

            Bitmap bitmap = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            Matrix m = new Matrix();
            RectF destRect = new RectF(0, 0, bmpW, bmpH);
            m.setRectToRect(bounds, destRect, Matrix.ScaleToFit.CENTER);

            canvas.save();
            canvas.concat(m);

            if (natural) {
                treePainter.drawBackground(canvas, layout, nightMode);
                treePainter.drawNaturalTreeStaticStructure(canvas, layout, nightMode);
            } else {
                canvas.drawColor(nightMode ? 0xFF121A15 : 0xFFFBF9F3);
                treePainter.drawClassicChartStaticLayers(canvas, layout);
            }

            for (TreeUnit unit : layout.allUnits) {
                treePainter.drawPersonNode(canvas, unit.primary, unit.primaryRect, null, null, natural);
                if (unit.spouse != null) {
                    treePainter.drawPersonNode(canvas, unit.spouse, unit.spouseRect, null, null, natural);
                }
            }

            canvas.restore();
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error in renderTreeBitmapHeadless: " + e.getMessage(), e);
            return null;
        }
    }

    private static void drawPdfTreePage(Canvas canvas, Bitmap treeBitmap, String familyName, int width, int height, int totalMembers) {
        // Background: Soft archival cream
        canvas.drawColor(Color.parseColor("#FBF9F3"));

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Header Title
        paint.setColor(Color.parseColor("#3F6B4F")); // Primary Forest Green
        paint.setTextSize(26);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("SHIKOR — FAMILY ARCHIVE", 40, 48, paint);

        // Subheader metadata
        paint.setColor(Color.parseColor("#525850")); // Slate muted
        paint.setTextSize(12);
        paint.setTypeface(Typeface.DEFAULT);
        String dateStr = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
        String meta = "Family: " + (familyName != null ? familyName : "My Family") +
                "  •  Generated: " + dateStr +
                "  •  Total Documented Relatives: " + totalMembers;
        canvas.drawText(meta, 40, 68, paint);

        // Decorative separator line
        paint.setColor(Color.parseColor("#C9A24B")); // Soft Gold Accent
        paint.setStrokeWidth(2.0f);
        canvas.drawLine(40, 80, width - 40, 80, paint);

        // Tree Image Content
        if (treeBitmap != null) {
            float availableWidth = width - 80;
            float availableHeight = height - 130;

            float scale = Math.min(availableWidth / treeBitmap.getWidth(), availableHeight / treeBitmap.getHeight());
            float destW = treeBitmap.getWidth() * scale;
            float destH = treeBitmap.getHeight() * scale;

            float destX = 40 + (availableWidth - destW) / 2f;
            float destY = 92 + (availableHeight - destH) / 2f;

            Rect src = new Rect(0, 0, treeBitmap.getWidth(), treeBitmap.getHeight());
            RectF dest = new RectF(destX, destY, destX + destW, destY + destH);
            canvas.drawBitmap(treeBitmap, src, dest, paint);
        } else {
            paint.setColor(Color.parseColor("#8A6A4B"));
            paint.setTextSize(16);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
            canvas.drawText("No graphical tree structure recorded.", 40, 200, paint);
        }

        // Footer
        paint.setColor(Color.parseColor("#82887E"));
        paint.setTextSize(10);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("Preserving family roots, memories & stories  •  Page 1", 40, height - 20, paint);
        canvas.drawText("Shikor Android Application", width - 180, height - 20, paint);
    }

    private static void drawPdfDirectoryPage(
            Canvas canvas, List<PersonEntity> members, String familyName,
            int width, int height, int startNumber, int pageNum, int totalPages
    ) {
        canvas.drawColor(Color.parseColor("#FBF9F3"));

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Header
        paint.setColor(Color.parseColor("#3F6B4F"));
        paint.setTextSize(22);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("FAMILY MEMBER DIRECTORY", 40, 48, paint);

        paint.setColor(Color.parseColor("#525850"));
        paint.setTextSize(11);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("Lineage & Contacts Register  •  " + (familyName != null ? familyName : "My Family"), 40, 68, paint);

        paint.setColor(Color.parseColor("#3F6B4F"));
        paint.setStrokeWidth(1.5f);
        canvas.drawLine(40, 78, width - 40, 78, paint);

        // Table Header
        int y = 105;
        paint.setColor(Color.parseColor("#D7E8D5")); // Primary container
        canvas.drawRect(40, y - 18, width - 40, y + 8, paint);

        paint.setColor(Color.parseColor("#0C2614"));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(11);
        canvas.drawText("#", 50, y, paint);
        canvas.drawText("FULL NAME", 80, y, paint);
        canvas.drawText("GENDER", 270, y, paint);
        canvas.drawText("STATUS", 360, y, paint);
        canvas.drawText("BIRTH DATE", 460, y, paint);
        canvas.drawText("HOMETOWN", 580, y, paint);
        canvas.drawText("PHONE NUMBER", 690, y, paint);

        // Table Rows
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(10);
        int currentY = y + 30;

        for (int i = 0; i < members.size(); i++) {
            PersonEntity p = members.get(i);
            int itemNum = startNumber + i + 1;

            // Row alternate background
            if (i % 2 == 1) {
                paint.setColor(Color.parseColor("#F2EDE0"));
                canvas.drawRect(40, currentY - 16, width - 40, currentY + 12, paint);
            }

            paint.setColor(Color.parseColor("#2B2F2A"));
            canvas.drawText(String.valueOf(itemNum), 50, currentY, paint);

            String name = p.getFullName() != null ? p.getFullName() : "—";
            if (p.getNickName() != null && !p.getNickName().isEmpty()) {
                name += " (" + p.getNickName() + ")";
            }
            canvas.drawText(truncateText(name, 30), 80, currentY, paint);

            String gender = p.getGender() != null ? capitalize(p.getGender()) : "Other";
            canvas.drawText(gender, 270, currentY, paint);

            String status = p.isDeceased() ? "Late (Deceased)" : "Living";
            paint.setColor(p.isDeceased() ? Color.parseColor("#8A6A4B") : Color.parseColor("#2E533B"));
            canvas.drawText(status, 360, currentY, paint);

            paint.setColor(Color.parseColor("#2B2F2A"));
            String bday = p.getBirthDate() != null && !p.getBirthDate().isEmpty() ? p.getBirthDate() : "—";
            canvas.drawText(bday, 460, currentY, paint);

            String town = p.getHometown() != null && !p.getHometown().isEmpty() ? p.getHometown() : "—";
            canvas.drawText(truncateText(town, 16), 580, currentY, paint);

            String phone = p.getPhoneNumber() != null && !p.getPhoneNumber().isEmpty() ? p.getPhoneNumber() : "—";
            canvas.drawText(phone, 690, currentY, paint);

            currentY += 28;
        }

        // Footer
        paint.setColor(Color.parseColor("#82887E"));
        paint.setTextSize(10);
        canvas.drawText("Preserving family roots, memories & stories", 40, height - 20, paint);
        canvas.drawText("Page " + pageNum + " of " + totalPages, width - 120, height - 20, paint);
    }

    private static void shareFile(Context context, File file, String mimeType, String chooserTitle) {
        try {
            Uri contentUri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(mimeType);
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shikor Family Archive Export");
            shareIntent.setClipData(android.content.ClipData.newRawUri("", contentUri));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(shareIntent, chooserTitle);
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            List<android.content.pm.ResolveInfo> resInfoList = context.getPackageManager()
                    .queryIntentActivities(chooser, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
            for (android.content.pm.ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            context.startActivity(chooser);
        } catch (Exception e) {
            Log.e(TAG, "Failed to share export file", e);
            AppExecutors.getInstance().mainThread().execute(() -> {
                Toast.makeText(context, "Could not open share menu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private static String sanitizeFileName(String input) {
        return input.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase(Locale.US);
    }

    private static String truncateText(String text, int maxChars) {
        if (text == null) return "—";
        if (text.length() <= maxChars) return text;
        return text.substring(0, maxChars - 1) + "…";
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase(Locale.US) + text.substring(1).toLowerCase(Locale.US);
    }
}
