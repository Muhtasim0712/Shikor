package com.shikor.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.daos.MemoryDao;
import com.shikor.app.data.local.entities.MemoryEntity;
import com.shikor.app.ui.memory.MemoryDetailActivity;
import com.shikor.app.util.AppExecutors;

/**
 * MemoryNotificationReceiver - Handles notification actions for new family memories (Phase 5).
 * CONCEPT: BroadcastReceiver (exported=false), notification action handling, marking as read.
 */
public class MemoryNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "ShikorMemory";
    public static final String ACTION_VIEW_MEMORY = "com.shikor.app.action.VIEW_MEMORY";
    public static final String EXTRA_MEMORY_ID = "extra_memory_id";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_VIEW_MEMORY.equals(intent.getAction())) return;

        String memoryId = intent.getStringExtra(EXTRA_MEMORY_ID);
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);

        if (notificationId != 0) {
            try {
                NotificationManagerCompat.from(context).cancel(notificationId);
            } catch (Exception ignored) {}
        }

        if (memoryId != null && !memoryId.isEmpty()) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                MemoryDao dao = AppDatabase.getInstance(context).memoryDao();
                MemoryEntity memory = dao.getMemoryByIdSync(memoryId);
                if (memory != null) {
                    memory.setNotified(true);
                    dao.update(memory);
                }
            });

            Intent detailIntent = new Intent(context, MemoryDetailActivity.class);
            detailIntent.putExtra(MemoryDetailActivity.EXTRA_MEMORY_ID, memoryId);
            detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(detailIntent);
            Log.d(TAG, "Notification action 'View' clicked for memory: " + memoryId);
        }
    }
}
