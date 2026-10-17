package com.shikor.app.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.net.Uri;
import android.provider.Settings;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.shikor.app.R;
import com.shikor.app.databinding.FragmentSettingsBinding;
import com.shikor.app.ui.tools.ExportActivity;
import com.shikor.app.util.AlarmScheduler;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.FamilyImporter;
import com.shikor.app.util.NotificationHelper;
import com.shikor.app.util.Prefs;

import java.util.Calendar;
import java.util.Locale;

/**
 * SettingsFragment - Controls family reminders, daily alert scheduling,
 * Android 13 notification permissions, exact alarm capability, and contacts import & export.
 *
 * CONCEPT: MaterialTimePicker, MaterialSwitch, ActivityResultLauncher for POST_NOTIFICATIONS,
 * AlarmScheduler, and file import via OpenDocument.
 */
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private Prefs prefs;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    NotificationHelper.createNotificationChannels(requireContext());
                    prefs.setNotificationsEnabled(true);
                    if (binding != null) binding.switchDailyReminders.setChecked(true);
                    AlarmScheduler.scheduleDailyReminder(requireContext());
                    updateTimeDisplay(prefs.getReminderHour(), prefs.getReminderMinute());
                    Toast.makeText(requireContext(), "Notification permission granted. Reminders scheduled!", Toast.LENGTH_SHORT).show();
                } else {
                    prefs.setNotificationsEnabled(false);
                    if (binding != null) binding.switchDailyReminders.setChecked(false);
                    updateTimeDisplay(prefs.getReminderHour(), prefs.getReminderMinute());
                    Toast.makeText(requireContext(), "Permission denied. Reminders cannot show notifications.", Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<String[]> importFilePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    importFamilyMembers(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = Prefs.getInstance(requireContext());

        populateFamilyInfo();
        setupRemindersSection();
        setupToolsSection();
    }

    private void populateFamilyInfo() {
        String familyName = prefs.getFamilyName();
        String userEmail = prefs.getUserEmail();
        String familyCode = prefs.getFamilyCode();

        binding.tvSettingsFamilyName.setText(familyName != null && !familyName.isEmpty() ? familyName : "My Family");
        binding.tvSettingsUserEmail.setText(userEmail != null && !userEmail.isEmpty() ? userEmail : "");
        binding.tvSettingsFamilyCode.setText(familyCode != null && !familyCode.isEmpty() ? familyCode : "------");

        binding.btnCopyCode.setOnClickListener(v -> {
            String message = getString(R.string.family_code_invite_message, familyCode);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Family Code");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(shareIntent, "Share Family Code"));
        });
    }

    private void setupRemindersSection() {
        // Daily Reminders Toggle
        binding.switchDailyReminders.setChecked(prefs.isNotificationsEnabled());
        binding.switchDailyReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    NotificationHelper.createNotificationChannels(requireContext());
                    prefs.setNotificationsEnabled(true);
                    AlarmScheduler.scheduleDailyReminder(requireContext());
                    updateTimeDisplay(prefs.getReminderHour(), prefs.getReminderMinute());
                    Toast.makeText(requireContext(), "Daily reminders active at " + formatTime(prefs.getReminderHour(), prefs.getReminderMinute()), Toast.LENGTH_SHORT).show();
                }
            } else {
                prefs.setNotificationsEnabled(false);
                AlarmScheduler.cancelDailyReminder(requireContext());
                updateTimeDisplay(prefs.getReminderHour(), prefs.getReminderMinute());
                Toast.makeText(requireContext(), "Daily reminders paused", Toast.LENGTH_SHORT).show();
            }
        });

        // Exact alarm capability check (Android 12+)
        checkExactAlarmPermission();

        // Current alert time display
        updateTimeDisplay(prefs.getReminderHour(), prefs.getReminderMinute());

        // MaterialTimePicker for selecting alert time
        binding.layoutReminderTime.setOnClickListener(v -> showTimePicker());

        // Immediate test reminder button
        binding.btnTestReminder.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                NotificationHelper.createNotificationChannels(requireContext());
                AlarmScheduler.triggerImmediateCheck(requireContext());
                Toast.makeText(requireContext(), "Test reminder sent! Check your notification shade.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean canExact = AlarmScheduler.canScheduleExact(requireContext());
            binding.layoutExactAlarmWarning.setVisibility(canExact ? View.GONE : View.VISIBLE);
            binding.btnGrantExactAlarm.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Could not open alarm settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            binding.layoutExactAlarmWarning.setVisibility(View.GONE);
        }
    }

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(prefs.getReminderHour())
                .setMinute(prefs.getReminderMinute())
                .setTitleText("Select Daily Reminder Time")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int hour = picker.getHour();
            int minute = picker.getMinute();
            prefs.setReminderTime(hour, minute);

            if (prefs.isNotificationsEnabled()) {
                AlarmScheduler.scheduleDailyReminder(requireContext());
            }

            updateTimeDisplay(hour, minute);

            long nextMillis = AlarmScheduler.getNextDailyReminderMillis(requireContext());
            Calendar now = Calendar.getInstance();
            Calendar target = Calendar.getInstance();
            target.setTimeInMillis(nextMillis);
            boolean isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
            long diffMin = Math.max(0, (nextMillis - System.currentTimeMillis()) / 60000);

            String scheduledInfo;
            if (isToday) {
                scheduledInfo = "Reminder set! Scheduled for today at " + formatTime(hour, minute) + " (in " + diffMin + " min)";
            } else {
                scheduledInfo = "Scheduled for tomorrow at " + formatTime(hour, minute) + ". (Tip: to test today, pick a minute ahead of your clock)";
            }
            Toast.makeText(requireContext(), scheduledInfo, Toast.LENGTH_LONG).show();
        });

        picker.show(getParentFragmentManager(), "reminder_time_picker");
    }

    private void updateTimeDisplay(int hour, int minute) {
        binding.tvReminderTimeValue.setText(formatTime(hour, minute));

        if (!prefs.isNotificationsEnabled()) {
            binding.tvNextAlertStatus.setText("Daily reminders are currently paused");
            return;
        }

        long nextMillis = AlarmScheduler.getNextDailyReminderMillis(requireContext());
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(nextMillis);
        boolean isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
        long diffMin = Math.max(0, (nextMillis - System.currentTimeMillis()) / 60000);

        if (isToday) {
            binding.tvNextAlertStatus.setText("Next alert: Today at " + formatTime(hour, minute) + " (in " + diffMin + "m)");
        } else {
            binding.tvNextAlertStatus.setText("Next alert: Tomorrow at " + formatTime(hour, minute));
        }
    }

    private String formatTime(int hour, int minute) {
        String amPm = hour >= 12 ? "PM" : "AM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm);
    }

    private void setupToolsSection() {
        // Open Contacts Export Screen
        binding.layoutExportContacts.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ExportActivity.class);
            startActivity(intent);
        });

        // Import Family Members from CSV or vCard (.vcf)
        binding.layoutImportMembers.setOnClickListener(v -> {
            importFilePickerLauncher.launch(new String[]{
                    "text/*",
                    "text/csv",
                    "text/comma-separated-values",
                    "text/vcard",
                    "text/x-vcard",
                    "*/*"
            });
        });
    }

    private void importFamilyMembers(Uri uri) {
        Toast.makeText(requireContext(), "Importing relatives from file...", Toast.LENGTH_SHORT).show();

        AppExecutors.getInstance().diskIO().execute(() -> {
            FamilyImporter.ImportResult result = FamilyImporter.importFromUri(requireContext(), uri);

            AppExecutors.getInstance().mainThread().execute(() -> {
                if (!isAdded()) return;

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(result.success ? "Import Complete" : "Import Failed")
                        .setMessage(result.message)
                        .setPositiveButton("OK", null)
                        .setIcon(result.success ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert)
                        .show();
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
