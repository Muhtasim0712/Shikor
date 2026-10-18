package com.shikor.app.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.net.Uri;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shikor.app.R;
import com.shikor.app.data.repository.FamilyRepository;
import com.shikor.app.databinding.ActivityMainBinding;
import com.shikor.app.sync.SyncManager;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.ui.tools.ExportActivity;
import com.shikor.app.util.AlarmScheduler;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.FamilyImporter;
import com.shikor.app.util.NotificationHelper;
import com.shikor.app.util.Prefs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * MainActivity - Primary container activity for Shikor.
 * Holds BottomNavigationView and manages Fragments (Tree, Members, Timeline, Settings).
 * Handles Android 13 notification permissions, starts daily reminder alarms, and supports contact export/import.
 *
 * CONCEPT: Android 13 POST_NOTIFICATIONS flow, AlarmScheduler daily scheduling, and Fragment switching.
 */
public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                NotificationHelper.createNotificationChannels(this);
                if (isGranted) {
                    AlarmScheduler.scheduleDailyReminder(this);
                }
            });

    private final ActivityResultLauncher<String[]> mainImportLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    importMembers(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        String familyName = Prefs.getInstance(this).getFamilyName();
        if (familyName != null && !familyName.isEmpty()) {
            binding.toolbar.setTitle(familyName);
        }

        // Trigger background cloud sync-down to restore/update family data from Firestore
        String familyId = Prefs.getInstance(this).getFamilyId();
        if (familyId != null && !familyId.isEmpty()) {
            FamilyRepository.getInstance(this).syncDownFamilyData(familyId, null);
        }

        // Initialize Notification Channels & Android 13 Permission Flow
        NotificationHelper.createNotificationChannels(this);
        checkNotificationPermissionAndSchedule();

        // Setup real-time Cloud Sync & Status Chip monitoring
        setupSyncMonitoring();

        setupBottomNavigation();

        // Default screen is MembersFragment (or TreeFragment)
        if (savedInstanceState == null) {
            loadFragment(new MembersFragment());
            ((com.google.android.material.navigation.NavigationBarView) binding.bottomNavigation).setSelectedItemId(R.id.nav_members);
        }
    }

    private void setupSyncMonitoring() {
        SyncManager syncManager = SyncManager.getInstance(this);
        String familyId = Prefs.getInstance(this).getFamilyId();
        if (familyId != null && !familyId.isEmpty()) {
            syncManager.attachSnapshotListeners(familyId);
        }

        syncManager.getSyncStatusLiveData().observe(this, this::updateSyncChip);
        binding.chipSyncStatus.setOnClickListener(v -> showSyncDetailsDialog());
    }

    private void updateSyncChip(SyncManager.SyncStatus status) {
        if (status == null || binding == null) return;

        switch (status.state) {
            case SYNCED:
                binding.chipSyncStatus.setText("Synced");
                binding.chipSyncStatus.setChipIconResource(android.R.drawable.stat_sys_upload_done);
                binding.chipSyncStatus.setChipIconTint(ContextCompat.getColorStateList(this, R.color.primary));
                break;
            case SYNCING:
                binding.chipSyncStatus.setText("Syncing...");
                binding.chipSyncStatus.setChipIconResource(android.R.drawable.stat_notify_sync);
                binding.chipSyncStatus.setChipIconTint(ContextCompat.getColorStateList(this, R.color.tertiary));
                break;
            case PENDING_CHANGES:
                binding.chipSyncStatus.setText(status.pendingCount + " Pending");
                binding.chipSyncStatus.setChipIconResource(android.R.drawable.stat_sys_upload);
                binding.chipSyncStatus.setChipIconTint(ContextCompat.getColorStateList(this, R.color.tertiary));
                break;
            case OFFLINE:
                binding.chipSyncStatus.setText("Offline");
                binding.chipSyncStatus.setChipIconResource(android.R.drawable.stat_sys_warning);
                binding.chipSyncStatus.setChipIconTint(ContextCompat.getColorStateList(this, R.color.outline));
                break;
            case ERROR:
                binding.chipSyncStatus.setText("Sync Issue");
                binding.chipSyncStatus.setChipIconResource(android.R.drawable.stat_notify_error);
                binding.chipSyncStatus.setChipIconTint(ContextCompat.getColorStateList(this, R.color.error));
                break;
        }
    }

    private void showSyncDetailsDialog() {
        SyncManager syncManager = SyncManager.getInstance(this);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Cloud Synchronization")
                .setMessage(formatSyncDialogBody(syncManager, syncManager.getSyncStatusLiveData().getValue()))
                .setPositiveButton("Sync Now", null)
                .setNegativeButton("Close", null)
                .create();

        androidx.lifecycle.Observer<SyncManager.SyncStatus> statusObserver = status -> {
            if (dialog.isShowing()) {
                dialog.setMessage(formatSyncDialogBody(syncManager, status));
            }
        };

        syncManager.getSyncStatusLiveData().observe(this, statusObserver);
        dialog.setOnDismissListener(d -> syncManager.getSyncStatusLiveData().removeObserver(statusObserver));

        dialog.show();

        // Custom click listener on "Sync Now" to show error if offline
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (!syncManager.isNetworkOnline()) {
                Toast.makeText(this, "Cannot sync while offline. Please connect to the internet.", Toast.LENGTH_LONG).show();
            } else {
                syncManager.triggerManualSync();
                Toast.makeText(this, "Manual cloud sync triggered", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private String formatSyncDialogBody(SyncManager syncManager, SyncManager.SyncStatus status) {
        String stateName = status != null ? status.state.name() : "UNKNOWN";
        String message = status != null ? status.statusMessage : "Checking sync status...";
        String timeStr = (status != null && status.lastSyncedTimestamp > 0) ?
                new SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(new Date(status.lastSyncedTimestamp)) :
                "Pending initial sync";

        return "Connection: " + (syncManager.isNetworkOnline() ? "Online 🟢" : "Offline ⚪ (Airplane mode)") + "\n\n" +
                "Cloud State: " + stateName + "\n" +
                "Details: " + message + "\n" +
                "Last Synced: " + timeStr;
    }

    private void checkNotificationPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                AlarmScheduler.scheduleDailyReminder(this);
            }
        } else {
            AlarmScheduler.scheduleDailyReminder(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String familyId = Prefs.getInstance(this).getFamilyId();
        if (familyId != null && !familyId.isEmpty()) {
            SyncManager.getInstance(this).attachSnapshotListeners(familyId);
            FamilyRepository.getInstance(this).syncDownFamilyData(familyId, null);
        }
    }

    private void setupBottomNavigation() {
        ((com.google.android.material.navigation.NavigationBarView) binding.bottomNavigation).setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_members) {
                loadFragment(new MembersFragment());
                return true;
            } else if (itemId == R.id.nav_tree) {
                loadFragment(new TreeFragment());
                return true;
            } else if (itemId == R.id.nav_timeline) {
                loadFragment(new TimelineFragment());
                return true;
            } else if (itemId == R.id.nav_settings) {
                loadFragment(new SettingsFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.navHostContainer, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export_contacts) {
            startActivity(new Intent(this, ExportActivity.class));
            return true;
        } else if (id == R.id.action_import_members) {
            mainImportLauncher.launch(new String[]{
                    "text/*",
                    "text/csv",
                    "text/comma-separated-values",
                    "text/vcard",
                    "text/x-vcard",
                    "*/*"
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void importMembers(Uri uri) {
        Toast.makeText(this, "Importing family members from file...", Toast.LENGTH_SHORT).show();

        AppExecutors.getInstance().diskIO().execute(() -> {
            FamilyImporter.ImportResult result = FamilyImporter.importFromUri(this, uri);

            AppExecutors.getInstance().mainThread().execute(() -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(result.success ? "Import Complete" : "Import Failed")
                        .setMessage(result.message)
                        .setPositiveButton("OK", null)
                        .setIcon(result.success ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert)
                        .show();
            });
        });
    }
}
