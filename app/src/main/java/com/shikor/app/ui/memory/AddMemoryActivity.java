package com.shikor.app.ui.memory;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shikor.app.R;
import com.shikor.app.data.local.entities.MemoryEntity;
import com.shikor.app.data.local.entities.MemoryPhotoEntity;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.data.repository.MemoryRepository;
import com.shikor.app.databinding.ActivityAddMemoryBinding;
import com.shikor.app.receiver.RecordingStateReceiver;
import com.shikor.app.service.AudioRecordingService;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.ui.person.PersonViewModel;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.Prefs;
import com.shikor.app.worker.UploadMemoryWorker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * AddMemoryActivity - Activity to create and record new family memories (Phase 4).
 * CONCEPT: Material 3 forms, MaterialDatePicker, multi-select dialogs, photo preview grid, WorkManager background enqueue.
 */
public class AddMemoryActivity extends BaseActivity implements RecordingStateReceiver.OnRecordingStateChangedListener {

    private ActivityAddMemoryBinding binding;
    private PersonViewModel personViewModel;

    private List<PersonEntity> familyMembers = new ArrayList<>();
    private final List<String> selectedTaggedPersonIds = new ArrayList<>();
    private final List<String> attachedPhotoPaths = new ArrayList<>();

    private long selectedEventDateMillis = System.currentTimeMillis();
    private boolean isSaving = false;

    // Audio recording state
    private AudioRecordingService audioService;
    private boolean isServiceBound = false;
    private RecordingStateReceiver recordingReceiver;
    private String recordedAudioPath = null;
    private long recordedAudioDurationMs = 0;
    private MediaPlayer previewPlayer;

    private Uri cameraImageUri = null;
    private String cameraPhotoPath = null;

    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    savePhotoFromUri(uri);
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraPhotoPath != null) {
                    addPhotoPath(cameraPhotoPath);
                }
            });

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean recordGranted = result.get(Manifest.permission.RECORD_AUDIO);
                if (Boolean.TRUE.equals(recordGranted)) {
                    startAudioRecordingService();
                } else {
                    showSnackbar("Microphone permission is required to record voice memories.");
                }
            });

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioRecordingService.LocalBinder binder = (AudioRecordingService.LocalBinder) service;
            audioService = binder.getService();
            isServiceBound = true;
            updateRecordingUI(audioService.isRecording());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            audioService = null;
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMemoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        personViewModel = new ViewModelProvider(this).get(PersonViewModel.class);
        recordingReceiver = new RecordingStateReceiver(this);

        setupUploaderLine();
        setupControls();
        observeFamilyMembers();
    }

    private void setupUploaderLine() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty())
                ? user.getDisplayName()
                : (user != null && user.getEmail() != null ? user.getEmail() : "Family Member");
        binding.tvUploaderLine.setText("Adding as " + name);
    }

    private void setupControls() {
        binding.cardDatePicker.setOnClickListener(v -> showDatePickerDialog());
        binding.btnAddPhoto.setOnClickListener(v -> showPhotoSourceDialog());

        binding.btnStartRecord.setOnClickListener(v -> checkPermissionsAndStartRecording());
        binding.btnStopRecord.setOnClickListener(v -> stopAudioRecordingService());
        binding.btnPlayPreview.setOnClickListener(v -> toggleAudioPreview());
        binding.btnDeleteAudio.setOnClickListener(v -> deleteRecordedAudio());

        binding.btnSaveMemory.setOnClickListener(v -> saveMemory());

        updateDateDisplay(selectedEventDateMillis);
        updateTaggedChipsUI();
    }

    private void showDatePickerDialog() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Memory Date")
                .setSelection(selectedEventDateMillis)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                selectedEventDateMillis = selection;
                updateDateDisplay(selection);
            }
        });
        datePicker.show(getSupportFragmentManager(), "MEMORY_DATE_PICKER");
    }

    private void updateDateDisplay(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        binding.tvSelectedDate.setText(sdf.format(new Date(millis)));
    }

    private void observeFamilyMembers() {
        personViewModel.getPeople().observe(this, people -> {
            if (people != null) {
                familyMembers = people;
                updateTaggedChipsUI();
            }
        });
    }

    private void showTagMembersDialog() {
        if (familyMembers.isEmpty()) {
            Toast.makeText(this, "No family members found to tag.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[familyMembers.size()];
        boolean[] checked = new boolean[familyMembers.size()];
        for (int i = 0; i < familyMembers.size(); i++) {
            PersonEntity p = familyMembers.get(i);
            names[i] = p.getFullName();
            checked[i] = selectedTaggedPersonIds.contains(p.getId());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Tag Family Members")
                .setMultiChoiceItems(names, checked, (dialog, which, isChecked) -> {
                    String pId = familyMembers.get(which).getId();
                    if (isChecked) {
                        if (!selectedTaggedPersonIds.contains(pId)) selectedTaggedPersonIds.add(pId);
                    } else {
                        selectedTaggedPersonIds.remove(pId);
                    }
                })
                .setPositiveButton("Done", (dialog, which) -> updateTaggedChipsUI())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateTaggedChipsUI() {
        binding.chipGroupTaggedMembers.removeAllViews();
        if (selectedTaggedPersonIds.isEmpty()) {
            Chip promptChip = new Chip(this);
            promptChip.setText("+ Tag Relatives");
            promptChip.setCheckable(false);
            promptChip.setOnClickListener(v -> showTagMembersDialog());
            binding.chipGroupTaggedMembers.addView(promptChip);
        } else {
            for (String pId : selectedTaggedPersonIds) {
                for (PersonEntity p : familyMembers) {
                    if (p.getId().equals(pId)) {
                        Chip chip = new Chip(this);
                        chip.setText(p.getFullName());
                        chip.setCloseIconVisible(true);
                        chip.setOnCloseIconClickListener(v -> {
                            selectedTaggedPersonIds.remove(pId);
                            updateTaggedChipsUI();
                        });
                        binding.chipGroupTaggedMembers.addView(chip);
                        break;
                    }
                }
            }
            Chip addMoreChip = new Chip(this);
            addMoreChip.setText("+");
            addMoreChip.setOnClickListener(v -> showTagMembersDialog());
            binding.chipGroupTaggedMembers.addView(addMoreChip);
        }
    }

    private void showPhotoSourceDialog() {
        if (attachedPhotoPaths.size() >= 4) {
            Toast.makeText(this, "Maximum 4 photos allowed per memory.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {"Choose from Gallery", "Take a Photo"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        photoPickerLauncher.launch("image/*");
                    } else {
                        launchCamera();
                    }
                })
                .show();
    }

    private void launchCamera() {
        try {
            File dir = new File(getFilesDir(), "camera_temp");
            if (!dir.exists()) dir.mkdirs();
            File imageFile = new File(dir, "cam_" + System.currentTimeMillis() + ".jpg");
            cameraPhotoPath = imageFile.getAbsolutePath();
            cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

            cameraLauncher.launch(cameraImageUri);
        } catch (Exception e) {
            showSnackbar("Failed to launch camera: " + e.getMessage());
        }
    }

    private void savePhotoFromUri(Uri uri) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                File dir = new File(getFilesDir(), "memories_temp");
                if (!dir.exists()) dir.mkdirs();

                File destFile = new File(dir, "temp_" + System.currentTimeMillis() + ".jpg");
                try (InputStream in = getContentResolver().openInputStream(uri);
                     FileOutputStream out = new FileOutputStream(destFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while (in != null && (len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }

                AppExecutors.getInstance().mainThread().execute(() -> addPhotoPath(destFile.getAbsolutePath()));
            } catch (Exception e) {
                AppExecutors.getInstance().mainThread().execute(() ->
                        showSnackbar("Failed to attach photo: " + e.getMessage())
                );
            }
        });
    }

    private void addPhotoPath(String path) {
        if (path == null || path.trim().isEmpty()) return;
        if (attachedPhotoPaths.size() >= 4) {
            Toast.makeText(this, "Maximum 4 photos allowed", Toast.LENGTH_SHORT).show();
            return;
        }
        attachedPhotoPaths.add(path);
        renderPhotoPreviews();
    }

    private void renderPhotoPreviews() {
        binding.layoutPhotoPreviewsContainer.removeAllViews();
        for (int i = 0; i < attachedPhotoPaths.size(); i++) {
            final int index = i;
            String path = attachedPhotoPaths.get(i);

            View thumbView = getLayoutInflater().inflate(R.layout.item_photo_preview_thumb, binding.layoutPhotoPreviewsContainer, false);
            ImageView ivThumb = thumbView.findViewById(R.id.ivPhotoThumb);
            ImageView btnRemove = thumbView.findViewById(R.id.btnRemoveThumb);

            Glide.with(this).load(new File(path)).centerCrop().into(ivThumb);
            btnRemove.setOnClickListener(v -> {
                attachedPhotoPaths.remove(index);
                renderPhotoPreviews();
            });

            binding.layoutPhotoPreviewsContainer.addView(thumbView);
        }
    }

    // --- Audio Recording ---

    private void checkPermissionsAndStartRecording() {
        List<String> needed = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.RECORD_AUDIO);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!needed.isEmpty()) {
            permissionLauncher.launch(needed.toArray(new String[0]));
        } else {
            startAudioRecordingService();
        }
    }

    private void startAudioRecordingService() {
        Intent intent = new Intent(this, AudioRecordingService.class);
        intent.setAction(AudioRecordingService.ACTION_START_RECORDING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        bindService(new Intent(this, AudioRecordingService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void stopAudioRecordingService() {
        if (audioService != null) {
            audioService.stopRecording();
        }
    }

    private void updateRecordingUI(boolean isRecording) {
        if (isRecording) {
            binding.btnStartRecord.setEnabled(false);
            binding.btnStopRecord.setEnabled(true);
            binding.tvRecordingTimer.setVisibility(View.VISIBLE);
        } else {
            binding.btnStartRecord.setEnabled(true);
            binding.btnStopRecord.setEnabled(false);
            binding.tvRecordingTimer.setVisibility(View.GONE);
        }
    }

    private void toggleAudioPreview() {
        if (recordedAudioPath == null) return;
        try {
            if (previewPlayer != null && previewPlayer.isPlaying()) {
                previewPlayer.stop();
                previewPlayer.release();
                previewPlayer = null;
                binding.btnPlayPreview.setIconResource(android.R.drawable.ic_media_play);
            } else {
                previewPlayer = new MediaPlayer();
                previewPlayer.setDataSource(recordedAudioPath);
                previewPlayer.prepare();
                previewPlayer.start();
                binding.btnPlayPreview.setIconResource(android.R.drawable.ic_media_pause);
                previewPlayer.setOnCompletionListener(mp -> binding.btnPlayPreview.setIconResource(android.R.drawable.ic_media_play));
            }
        } catch (Exception ignored) {}
    }

    private void deleteRecordedAudio() {
        if (previewPlayer != null) {
            try { previewPlayer.release(); } catch (Exception ignored) {}
            previewPlayer = null;
        }
        if (recordedAudioPath != null) {
            File f = new File(recordedAudioPath);
            if (f.exists()) f.delete();
            recordedAudioPath = null;
            recordedAudioDurationMs = 0;
        }
        binding.layoutAudioPreview.setVisibility(View.GONE);
        binding.btnStartRecord.setEnabled(true);
    }

    @Override
    public void onRecordingStarted() {
        updateRecordingUI(true);
    }

    @Override
    public void onRecordingProgress(long elapsedMs, int amplitude) {
        updateRecordingUI(true);
        long sec = elapsedMs / 1000;
        binding.tvRecordingTimer.setText(String.format(Locale.getDefault(), "%02d:%02d / 01:30", sec / 60, sec % 60));
    }

    @Override
    public void onRecordingPaused() {}

    @Override
    public void onRecordingResumed() {}

    @Override
    public void onRecordingStopped(String filePath, long durationMs) {
        updateRecordingUI(false);
        if (filePath != null) {
            recordedAudioPath = filePath;
            recordedAudioDurationMs = durationMs;
            binding.layoutAudioPreview.setVisibility(View.VISIBLE);
            binding.tvAudioDuration.setText(String.format(Locale.getDefault(), "Voice Recording Ready (%02d:%02d)", durationMs / 60000, (durationMs / 1000) % 60));
            if (isServiceBound) {
                unbindService(serviceConnection);
                isServiceBound = false;
            }
        }
    }

    @Override
    public void onRecordingError(String error) {
        updateRecordingUI(false);
        showSnackbar("Recording error: " + error);
    }

    // --- Save Memory ---

    private void saveMemory() {
        if (isSaving) return;

        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String description = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            binding.tilTitle.setError("Please enter a memory title");
            return;
        } else {
            binding.tilTitle.setError(null);
        }

        isSaving = true;
        binding.btnSaveMemory.setEnabled(false);

        String memoryId = UUID.randomUUID().toString();
        String familyId = Prefs.getInstance(this).getFamilyId();
        if (familyId == null || familyId.trim().isEmpty()) familyId = "default_family";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uploaderUid = user != null ? user.getUid() : "anonymous";
        String uploaderName = (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty())
                ? user.getDisplayName()
                : (user != null && user.getEmail() != null ? user.getEmail() : "Family Member");

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedEventDateMillis);
        int year = cal.get(Calendar.YEAR);
        long now = System.currentTimeMillis();

        MemoryEntity memory = new MemoryEntity(
                memoryId,
                familyId,
                title,
                description,
                selectedEventDateMillis,
                year,
                new ArrayList<>(selectedTaggedPersonIds),
                attachedPhotoPaths.size(),
                recordedAudioPath != null && !recordedAudioPath.isEmpty(),
                recordedAudioDurationMs,
                recordedAudioPath,
                uploaderUid,
                uploaderName,
                now,
                now,
                MemoryEntity.STATE_LOCAL_PENDING,
                null,
                false
        );

        List<MemoryPhotoEntity> photoEntities = new ArrayList<>();
        for (int i = 0; i < attachedPhotoPaths.size(); i++) {
            MemoryPhotoEntity pe = new MemoryPhotoEntity(
                    memoryId + "_p_" + i,
                    memoryId,
                    i,
                    attachedPhotoPaths.get(i),
                    now,
                    MemoryPhotoEntity.STATE_NONE
            );
            photoEntities.add(pe);
        }

        MemoryRepository.getInstance(this).insertMemory(memory, photoEntities, new MemoryRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Constraints constraints = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build();

                OneTimeWorkRequest uploadWork = new OneTimeWorkRequest.Builder(UploadMemoryWorker.class)
                        .setInputData(new androidx.work.Data.Builder().putString(UploadMemoryWorker.KEY_MEMORY_ID, memoryId).build())
                        .setConstraints(constraints)
                        .setBackoffCriteria(androidx.work.BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                        .build();

                WorkManager.getInstance(AddMemoryActivity.this).enqueueUniqueWork(
                        "upload_memory_" + memoryId,
                        ExistingWorkPolicy.REPLACE,
                        uploadWork
                );

                Toast.makeText(AddMemoryActivity.this, "Memory saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                isSaving = false;
                binding.btnSaveMemory.setEnabled(true);
                showSnackbar("Failed to save memory: " + error);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = RecordingStateReceiver.createIntentFilter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(recordingReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(recordingReceiver, filter);
        }
        recordingReceiver.setListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        recordingReceiver.setListener(null);
        try { unregisterReceiver(recordingReceiver); } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        if (isServiceBound) {
            try { unbindService(serviceConnection); } catch (Exception ignored) {}
        }
        if (previewPlayer != null) {
            try { previewPlayer.release(); } catch (Exception ignored) {}
            previewPlayer = null;
        }
        super.onDestroy();
    }
}
