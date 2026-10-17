package com.shikor.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * RecordingStateReceiver - Dynamically registered BroadcastReceiver to monitor
 * real-time status, timer, audio amplitude, and completion of elder voice recordings.
 *
 * CONCEPT: Dynamic BroadcastReceiver communication between AudioRecordingService and UI.
 */
public class RecordingStateReceiver extends BroadcastReceiver {

    public static final String ACTION_RECORDING_STATE = "com.shikor.app.action.RECORDING_STATE";

    public static final String EXTRA_STATE = "extra_state";
    public static final String EXTRA_ELAPSED_MS = "extra_elapsed_ms";
    public static final String EXTRA_AMPLITUDE = "extra_amplitude";
    public static final String EXTRA_FILE_PATH = "extra_file_path";
    public static final String EXTRA_DURATION_MS = "extra_duration_ms";
    public static final String EXTRA_ERROR = "extra_error";

    public static final int STATE_IDLE = 0;
    public static final int STATE_RECORDING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_STOPPED = 3;
    public static final int STATE_ERROR = 4;

    public interface OnRecordingStateChangedListener {
        void onRecordingStarted();
        void onRecordingProgress(long elapsedMs, int amplitude);
        void onRecordingPaused();
        void onRecordingResumed();
        void onRecordingStopped(String filePath, long durationMs);
        void onRecordingError(String error);
    }

    private OnRecordingStateChangedListener listener;

    public RecordingStateReceiver() {
    }

    public RecordingStateReceiver(OnRecordingStateChangedListener listener) {
        this.listener = listener;
    }

    public void setListener(OnRecordingStateChangedListener listener) {
        this.listener = listener;
    }

    public static IntentFilter createIntentFilter() {
        return new IntentFilter(ACTION_RECORDING_STATE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_RECORDING_STATE.equals(intent.getAction()) || listener == null) {
            return;
        }

        int state = intent.getIntExtra(EXTRA_STATE, STATE_IDLE);
        switch (state) {
            case STATE_RECORDING:
                long elapsed = intent.getLongExtra(EXTRA_ELAPSED_MS, 0);
                int amp = intent.getIntExtra(EXTRA_AMPLITUDE, 0);
                if (elapsed == 0) {
                    listener.onRecordingStarted();
                } else {
                    listener.onRecordingProgress(elapsed, amp);
                }
                break;

            case STATE_PAUSED:
                listener.onRecordingPaused();
                break;

            case STATE_STOPPED:
                String path = intent.getStringExtra(EXTRA_FILE_PATH);
                long duration = intent.getLongExtra(EXTRA_DURATION_MS, 0);
                listener.onRecordingStopped(path, duration);
                break;

            case STATE_ERROR:
                String err = intent.getStringExtra(EXTRA_ERROR);
                listener.onRecordingError(err != null ? err : "Unknown recording error");
                break;

            default:
                break;
        }
    }
}
