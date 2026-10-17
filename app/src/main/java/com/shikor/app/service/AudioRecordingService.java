package com.shikor.app.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.shikor.app.R;
import com.shikor.app.receiver.RecordingStateReceiver;
import com.shikor.app.ui.story.AddStoryActivity;
import com.shikor.app.util.NotificationHelper;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * AudioRecordingService - Foreground Service for recording elder oral histories and memories.
 *
 * CONCEPT: Supports BOTH started (startForegroundService) and bound (bindService) paradigms.
 * Manages MediaRecorder lifecycle, captures real-time microphone amplitudes, posts foreground
 * notifications with elapsed time and stop action, and broadcasts progress updates to RecordingStateReceiver.
 */
public class AudioRecordingService extends Service {

    private static final String TAG = "AudioRecordingService";
    private static final int NOTIFICATION_ID = 1971;

    public static final String ACTION_START_RECORDING = "com.shikor.app.action.START_RECORDING";
    public static final String ACTION_STOP_RECORDING = "com.shikor.app.action.STOP_RECORDING";
    public static final String ACTION_PAUSE_RECORDING = "com.shikor.app.action.PAUSE_RECORDING";
    public static final String ACTION_RESUME_RECORDING = "com.shikor.app.action.RESUME_RECORDING";

    public static final String EXTRA_OUTPUT_PATH = "extra_output_path";
    public static final String EXTRA_STORY_TITLE = "extra_story_title";

    public class LocalBinder extends Binder {
        public AudioRecordingService getService() {
            return AudioRecordingService.this;
        }
    }

    private final IBinder binder = new LocalBinder();
    private final Handler progressHandler = new Handler(Looper.getMainLooper());

    private MediaRecorder mediaRecorder;
    private String outputFilePath;
    private String storyTitle = "Elder Story";

    private boolean isRecording = false;
    private boolean isPaused = false;
    private long startTimeMs = 0;
    private long pauseStartTimeMs = 0;
    private long totalPausedMs = 0;
    private long lastNotificationUpdateSec = -1;

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording && !isPaused) {
                long elapsed = System.currentTimeMillis() - startTimeMs - totalPausedMs;
                int amplitude = 0;
                if (mediaRecorder != null) {
                    try {
                        amplitude = mediaRecorder.getMaxAmplitude();
                    } catch (Exception ignored) {}
                }

                // Broadcast live progress
                broadcastState(RecordingStateReceiver.STATE_RECORDING, elapsed, amplitude, null, 0, null);

                // Update notification text every second
                long currentSec = elapsed / 1000;
                if (currentSec != lastNotificationUpdateSec) {
                    lastNotificationUpdateSec = currentSec;
                    updateNotification(elapsed);
                }

                progressHandler.postDelayed(this, 300); // 300ms smooth updates
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createNotificationChannels(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_START_RECORDING:
                    String path = intent.getStringExtra(EXTRA_OUTPUT_PATH);
                    String title = intent.getStringExtra(EXTRA_STORY_TITLE);
                    if (title != null && !title.isEmpty()) {
                        this.storyTitle = title;
                    }
                    startRecording(path);
                    break;

                case ACTION_STOP_RECORDING:
                    stopRecording();
                    break;

                case ACTION_PAUSE_RECORDING:
                    pauseRecording();
                    break;

                case ACTION_RESUME_RECORDING:
                    resumeRecording();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    public void startRecording(@Nullable String customPath) {
        if (isRecording) {
            Log.w(TAG, "Already recording");
            return;
        }

        if (customPath == null || customPath.trim().isEmpty()) {
            File dir = new File(getFilesDir(), "stories_audio");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            outputFilePath = new File(dir, "story_" + System.currentTimeMillis() + ".m4a").getAbsolutePath();
        } else {
            outputFilePath = customPath;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mediaRecorder = new MediaRecorder(this);
            } else {
                mediaRecorder = new MediaRecorder();
            }

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setOutputFile(outputFilePath);

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            isPaused = false;
            startTimeMs = System.currentTimeMillis();
            totalPausedMs = 0;
            lastNotificationUpdateSec = -1;

            // Start foreground service with microphone type
            Notification notification = buildNotification(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }

            // Broadcast initial started event
            broadcastState(RecordingStateReceiver.STATE_RECORDING, 0, 0, null, 0, null);

            // Start periodic progress dispatcher
            progressHandler.post(progressRunnable);
            Log.d(TAG, "Audio recording started: " + outputFilePath);

        } catch (IOException | IllegalStateException e) {
            Log.e(TAG, "Failed to start audio recording", e);
            broadcastState(RecordingStateReceiver.STATE_ERROR, 0, 0, null, 0, e.getMessage());
            cleanupRecorder();
            stopSelf();
        }
    }

    public void pauseRecording() {
        if (isRecording && !isPaused && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder.pause();
                isPaused = true;
                pauseStartTimeMs = System.currentTimeMillis();
                progressHandler.removeCallbacks(progressRunnable);
                broadcastState(RecordingStateReceiver.STATE_PAUSED, getElapsedMs(), 0, null, 0, null);
                updateNotification(getElapsedMs());
            } catch (Exception e) {
                Log.e(TAG, "Failed to pause recording", e);
            }
        }
    }

    public void resumeRecording() {
        if (isRecording && isPaused && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder.resume();
                isPaused = false;
                totalPausedMs += (System.currentTimeMillis() - pauseStartTimeMs);
                broadcastState(RecordingStateReceiver.STATE_RECORDING, getElapsedMs(), 0, null, 0, null);
                progressHandler.post(progressRunnable);
            } catch (Exception e) {
                Log.e(TAG, "Failed to resume recording", e);
            }
        }
    }

    public void stopRecording() {
        if (!isRecording) {
            return;
        }

        long finalDurationMs = getElapsedMs();
        progressHandler.removeCallbacks(progressRunnable);

        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "MediaRecorder stop failed (possibly too short recording)", e);
        } finally {
            cleanupRecorder();
        }

        isRecording = false;
        isPaused = false;

        // Broadcast completion with final file path and duration
        broadcastState(RecordingStateReceiver.STATE_STOPPED, finalDurationMs, 0, outputFilePath, finalDurationMs, null);

        // Remove foreground notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }

        stopSelf();
        Log.d(TAG, "Audio recording stopped. Duration: " + finalDurationMs + "ms");
    }

    private void cleanupRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.reset();
                mediaRecorder.release();
            } catch (Exception ignored) {}
            mediaRecorder = null;
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public long getElapsedMs() {
        if (!isRecording) return 0;
        if (isPaused) {
            return pauseStartTimeMs - startTimeMs - totalPausedMs;
        }
        return System.currentTimeMillis() - startTimeMs - totalPausedMs;
    }

    public int getMaxAmplitude() {
        if (isRecording && !isPaused && mediaRecorder != null) {
            try {
                return mediaRecorder.getMaxAmplitude();
            } catch (Exception ignored) {}
        }
        return 0;
    }

    public String getOutputPath() {
        return outputFilePath;
    }

    private Notification buildNotification(long elapsedMs) {
        Intent openIntent = new Intent(this, AddStoryActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent(this, AudioRecordingService.class);
        stopIntent.setAction(ACTION_STOP_RECORDING);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String timeStr = formatDuration(elapsedMs);

        return new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_RECORDING_ID)
                .setContentTitle("Recording Memory: " + storyTitle)
                .setContentText("Recording in progress: " + timeStr)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setColor(androidx.core.content.ContextCompat.getColor(this, R.color.primary))
                .setOngoing(true)
                .setContentIntent(contentPendingIntent)
                .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void updateNotification(long elapsedMs) {
        Notification notification = buildNotification(elapsedMs);
        android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void broadcastState(int state, long elapsedMs, int amplitude, String path, long durationMs, String error) {
        Intent intent = new Intent(RecordingStateReceiver.ACTION_RECORDING_STATE);
        intent.setPackage(getPackageName());
        intent.putExtra(RecordingStateReceiver.EXTRA_STATE, state);
        intent.putExtra(RecordingStateReceiver.EXTRA_ELAPSED_MS, elapsedMs);
        intent.putExtra(RecordingStateReceiver.EXTRA_AMPLITUDE, amplitude);
        if (path != null) intent.putExtra(RecordingStateReceiver.EXTRA_FILE_PATH, path);
        if (durationMs > 0) intent.putExtra(RecordingStateReceiver.EXTRA_DURATION_MS, durationMs);
        if (error != null) intent.putExtra(RecordingStateReceiver.EXTRA_ERROR, error);
        sendBroadcast(intent);
    }

    private String formatDuration(long ms) {
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", min, sec);
    }

    @Override
    public void onDestroy() {
        stopRecording();
        super.onDestroy();
    }
}
