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
import com.shikor.app.ui.tools.RelationshipFinderActivity;
import com.shikor.app.util.AlarmScheduler;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.FamilyImporter;
import com.shikor.app.util.NotificationHelper;
import com.shikor.app.util.Prefs;
import com.shikor.app.util.TreeExporter;
import com.shikor.app.view.FamilyTreeView;
import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import android.graphics.Bitmap;
import java.util.List;

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
        setupPreferencesSection();
        setupAccountSection();
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
        int currentHour = prefs.getReminderHour();
        int currentMinute = prefs.getReminderMinute();

        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    prefs.setReminderTime(hourOfDay, minute);

                    if (prefs.isNotificationsEnabled()) {
                        AlarmScheduler.scheduleDailyReminder(requireContext());
                    }

                    updateTimeDisplay(hourOfDay, minute);

                    long nextMillis = AlarmScheduler.getNextDailyReminderMillis(requireContext());
                    Calendar now = Calendar.getInstance();
                    Calendar target = Calendar.getInstance();
                    target.setTimeInMillis(nextMillis);
                    boolean isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                            now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
                    long diffMin = Math.max(0, (nextMillis - System.currentTimeMillis()) / 60000);

                    String scheduledInfo;
                    if (isToday) {
                        scheduledInfo = "Reminder set for today at " + formatTime(hourOfDay, minute) + " (in " + diffMin + " min)";
                    } else {
                        scheduledInfo = "Scheduled for tomorrow at " + formatTime(hourOfDay, minute) + ". (Tip: to test today, pick a minute ahead of your clock)";
                    }
                    Toast.makeText(requireContext(), scheduledInfo, Toast.LENGTH_LONG).show();
                },
                currentHour,
                currentMinute,
                false
        );
        timePickerDialog.setTitle("Select Daily Reminder Time");
        timePickerDialog.show();
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

        // Open Relationship Finder
        binding.layoutRelationshipFinder.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RelationshipFinderActivity.class);
            startActivity(intent);
        });

        // Export Family Tree (PDF & PNG)
        binding.layoutExportTree.setOnClickListener(v -> showExportTreeDialog());
    }

    private void showExportTreeDialog() {
        String[] options = {"Export PDF Document (Archive)", "Export PNG Image (Diagram)"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Export Family Tree")
                .setItems(options, (dialog, which) -> {
                    String familyId = prefs.getFamilyId();
                    String familyName = prefs.getFamilyName();

                    AppExecutors.getInstance().diskIO().execute(() -> {
                        List<PersonEntity> people = AppDatabase.getInstance(requireContext()).personDao().getAllPeopleSync(familyId);
                        if (people == null || people.isEmpty()) {
                            AppExecutors.getInstance().mainThread().execute(() ->
                                    Toast.makeText(requireContext(), "No relatives found to export.", Toast.LENGTH_SHORT).show()
                            );
                            return;
                        }

                        if (which == 0) {
                            TreeExporter.exportTreeAsPdf(requireContext(), null, familyName, people);
                        } else {
                            TreeExporter.exportTreeAsPng(requireContext(), null, familyName, people);
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupPreferencesSection() {
        updateThemeDisplay();
        updateLanguageDisplay();

        binding.layoutThemeSetting.setOnClickListener(v -> showThemeDialog());
        binding.layoutLanguageSetting.setOnClickListener(v -> showLanguageDialog());
    }

    private void updateThemeDisplay() {
        String mode = prefs.getThemeMode();
        if (Prefs.THEME_LIGHT.equals(mode)) {
            binding.tvCurrentThemeValue.setText("Light Theme (উজ্জ্বল)");
        } else if (Prefs.THEME_DARK.equals(mode)) {
            binding.tvCurrentThemeValue.setText("Dark Theme (অন্ধকার / নাইট)");
        } else {
            binding.tvCurrentThemeValue.setText("System Default (সিস্টেম ডিফল্ট)");
        }
    }

    private void showThemeDialog() {
        String[] options = {"System Default", "Light Theme", "Dark Theme"};
        int currentIdx = 0;
        String mode = prefs.getThemeMode();
        if (Prefs.THEME_LIGHT.equals(mode)) currentIdx = 1;
        else if (Prefs.THEME_DARK.equals(mode)) currentIdx = 2;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select App Theme")
                .setSingleChoiceItems(options, currentIdx, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == 1) {
                        prefs.setThemeMode(Prefs.THEME_LIGHT);
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else if (which == 2) {
                        prefs.setThemeMode(Prefs.THEME_DARK);
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        prefs.setThemeMode(Prefs.THEME_SYSTEM);
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                    updateThemeDisplay();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateLanguageDisplay() {
        String lang = prefs.getLanguage();
        if ("bn".equals(lang)) {
            binding.tvCurrentLanguageValue.setText("বাংলা (Bengali)");
        } else {
            binding.tvCurrentLanguageValue.setText("English (ইংরেজি)");
        }
    }

    private void showLanguageDialog() {
        String[] options = {"English", "বাংলা (Bengali)"};
        int currentIdx = "bn".equals(prefs.getLanguage()) ? 1 : 0;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Language")
                .setSingleChoiceItems(options, currentIdx, (dialog, which) -> {
                    dialog.dismiss();
                    String langTag = which == 1 ? "bn" : "en";
                    prefs.setLanguage(langTag);
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langTag));
                    updateLanguageDisplay();
                    if (isAdded()) {
                        requireActivity().recreate();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupAccountSection() {
        binding.btnSettingsLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out of your family space? You can log back in with your email and password at any time.")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        prefs.clearSession();
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
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
