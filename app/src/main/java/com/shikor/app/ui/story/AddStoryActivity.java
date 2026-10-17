package com.shikor.app.ui.story;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.shikor.app.R;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.data.local.entities.StoryEntity;
import com.shikor.app.databinding.ActivityAddStoryBinding;
import com.shikor.app.receiver.RecordingStateReceiver;
import com.shikor.app.service.AudioRecordingService;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.ui.person.PersonViewModel;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.Prefs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * AddStoryActivity - Creates a family story/memory with text, photo attachment,
 * and live oral audio recording using AudioRecordingService and RecordingStateReceiver.
 *
 * CONCEPT: AudioRecordingService (foreground, started and bound) + dynamic RecordingStateReceiver.
 */
public class AddStoryActivity extends BaseActivity implements RecordingStateReceiver.OnRecordingStateChangedListener {

    public static final String EXTRA_PERSON_ID = "extra_person_id";

    private ActivityAddStoryBinding binding;
    private StoryViewModel storyViewModel;
    private PersonViewModel personViewModel;

    private List<PersonEntity> familyMembers = new ArrayList<>();
    private String preselectedPersonId;

    private String attachedPhotoPath = null;
    private String recordedAudioPath = null;
    private long recordedAudioDurationMs = 0;

    // Audio Service Connection & Receiver
    private AudioRecordingService audioService;
    private boolean isServiceBound = false;
    private RecordingStateReceiver recordingReceiver;

    // Audio Preview MediaPlayer
    private MediaPlayer previewPlayer;
    private boolean isPreviewPlaying = false;

    // ServiceConnection for bound AudioRecordingService
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

    // Photo Picker Launcher
    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    savePhotoFromUri(uri);
                }
            });

    // Permission Launcher
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean recordGranted = result.get(Manifest.permission.RECORD_AUDIO);
                if (Boolean.TRUE.equals(recordGranted)) {
                    startAudioRecordingService();
                } else {
                    showSnackbar("Microphone permission is required to record voice stories.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        storyViewModel = new ViewModelProvider(this).get(StoryViewModel.class);
        personViewModel = new ViewModelProvider(this).get(PersonViewModel.class);

        preselectedPersonId = getIntent().getStringExtra(EXTRA_PERSON_ID);

        recordingReceiver = new RecordingStateReceiver(this);

        setupControls();
        observeFamilyMembers();
        observeViewModel();
    }

    private void setupControls() {
        // Photo actions
        binding.btnAddPhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        binding.btnRemovePhoto.setOnClickListener(v -> removeAttachedPhoto());

        // Audio recording actions
        binding.btnStartRecord.setOnClickListener(v -> checkPermissionsAndStartRecording());
        binding.btnStopRecord.setOnClickListener(v -> stopAudioRecordingService());

        // Audio preview playback
        binding.btnPlayPreview.setOnClickListener(v -> toggleAudioPreview());
        binding.btnDeleteAudio.setOnClickListener(v -> deleteRecordedAudio());

        // Save story
        binding.btnSaveStory.setOnClickListener(v -> saveStory());
    }

    private void observeFamilyMembers() {
        personViewModel.getPeople().observe(this, people -> {
            if (people != null && !people.isEmpty()) {
                familyMembers = people;
                List<String> names = new ArrayList<>();
                int selectedIndex = 0;

                for (int i = 0; i < people.size(); i++) {
                    PersonEntity p = people.get(i);
                    names.add(p.getFullName());
                    if (preselectedPersonId != null && preselectedPersonId.equals(p.getId())) {
                        selectedIndex = i;
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_dropdown_item, names
                );
                binding.spinnerPerson.setAdapter(adapter);
                binding.spinnerPerson.setSelection(selectedIndex);
            }
        });
    }

    private void observeViewModel() {
        storyViewModel.getOperationSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Story saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        storyViewModel.getOperationError().observe(this, error -> {
            if (error != null) {
                showSnackbar(error);
            }
        });
    }

    private void checkPermissionsAndStartRecording() {
        List<String> perms = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.RECORD_AUDIO);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!perms.isEmpty()) {
            permissionLauncher.launch(perms.toArray(new String[0]));
        } else {
            startAudioRecordingService();
        }
    }

    /**
     * Starts and binds AudioRecordingService (Foreground Service).
     */
    private void startAudioRecordingService() {
        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "Story";

        Intent serviceIntent = new Intent(this, AudioRecordingService.class);
        serviceIntent.setAction(AudioRecordingService.ACTION_START_RECORDING);
        serviceIntent.putExtra(AudioRecordingService.EXTRA_STORY_TITLE, title);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        updateRecordingUI(true);
    }

    private void stopAudioRecordingService() {
        if (isServiceBound && audioService != null) {
            audioService.stopRecording();
        } else {
            Intent serviceIntent = new Intent(this, AudioRecordingService.class);
            serviceIntent.setAction(AudioRecordingService.ACTION_STOP_RECORDING);
            startService(serviceIntent);
        }
    }

    private void updateRecordingUI(boolean isRecording) {
        if (isRecording) {
            binding.cardRecordingLive.setVisibility(View.VISIBLE);
            binding.btnStartRecord.setVisibility(View.GONE);
            binding.cardAudioPreview.setVisibility(View.GONE);
        } else {
            binding.cardRecordingLive.setVisibility(View.GONE);
            if (recordedAudioPath != null) {
                binding.btnStartRecord.setVisibility(View.GONE);
                binding.cardAudioPreview.setVisibility(View.VISIBLE);
                binding.tvAudioPreviewDuration.setText(formatDuration(recordedAudioDurationMs));
            } else {
                binding.btnStartRecord.setVisibility(View.VISIBLE);
                binding.cardAudioPreview.setVisibility(View.GONE);
            }
        }
    }

    // --- RecordingStateReceiver Callbacks ---

    @Override
    public void onRecordingStarted() {
        updateRecordingUI(true);
        binding.tvRecordingTimer.setText("Recording: 00:00");
    }

    @Override
    public void onRecordingProgress(long elapsedMs, int amplitude) {
        binding.tvRecordingTimer.setText("Recording: " + formatDuration(elapsedMs));
    }

    @Override
    public void onRecordingPaused() {
        binding.tvRecordingTimer.setText("Recording Paused");
    }

    @Override
    public void onRecordingResumed() {
        // Handled in onRecordingProgress
    }

    @Override
    public void onRecordingStopped(String filePath, long durationMs) {
        this.recordedAudioPath = filePath;
        this.recordedAudioDurationMs = durationMs;
        updateRecordingUI(false);
    }

    @Override
    public void onRecordingError(String error) {
        updateRecordingUI(false);
        showSnackbar("Recording error: " + error);
    }

    // --- Audio Preview Playback ---

    private void toggleAudioPreview() {
        if (recordedAudioPath == null) return;

        if (isPreviewPlaying) {
            stopPreviewPlayer();
        } else {
            startPreviewPlayer();
        }
    }

    private void startPreviewPlayer() {
        try {
            stopPreviewPlayer();
            previewPlayer = new MediaPlayer();
            previewPlayer.setDataSource(recordedAudioPath);
            previewPlayer.prepare();
            previewPlayer.start();
            isPreviewPlaying = true;
            binding.btnPlayPreview.setIconResource(android.R.drawable.ic_media_pause);

            previewPlayer.setOnCompletionListener(mp -> stopPreviewPlayer());
        } catch (Exception e) {
            showSnackbar("Failed to play preview: " + e.getMessage());
            stopPreviewPlayer();
        }
    }

    private void stopPreviewPlayer() {
        if (previewPlayer != null) {
            try {
                if (previewPlayer.isPlaying()) {
                    previewPlayer.stop();
                }
                previewPlayer.release();
            } catch (Exception ignored) {}
            previewPlayer = null;
        }
        isPreviewPlaying = false;
        binding.btnPlayPreview.setIconResource(android.R.drawable.ic_media_play);
    }

    private void deleteRecordedAudio() {
        stopPreviewPlayer();
        if (recordedAudioPath != null) {
            File f = new File(recordedAudioPath);
            if (f.exists()) f.delete();
            recordedAudioPath = null;
            recordedAudioDurationMs = 0;
        }
        updateRecordingUI(false);
    }

    // --- Photo Handling ---

    private void savePhotoFromUri(Uri uri) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                File dir = new File(getFilesDir(), "stories_photos");
                if (!dir.exists()) dir.mkdirs();

                File destFile = new File(dir, "photo_" + System.currentTimeMillis() + ".jpg");
                try (InputStream in = getContentResolver().openInputStream(uri);
                     FileOutputStream out = new FileOutputStream(destFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }

                AppExecutors.getInstance().mainThread().execute(() -> {
                    attachedPhotoPath = destFile.getAbsolutePath();
                    binding.cardPhotoPreview.setVisibility(View.VISIBLE);
                    binding.btnAddPhoto.setVisibility(View.GONE);
                    Glide.with(this).load(destFile).into(binding.ivPhotoPreview);
                });
            } catch (Exception e) {
                AppExecutors.getInstance().mainThread().execute(() ->
                        showSnackbar("Failed to attach photo: " + e.getMessage())
                );
            }
        });
    }

    private void removeAttachedPhoto() {
        if (attachedPhotoPath != null) {
            File f = new File(attachedPhotoPath);
            if (f.exists()) f.delete();
            attachedPhotoPath = null;
        }
        binding.cardPhotoPreview.setVisibility(View.GONE);
        binding.btnAddPhoto.setVisibility(View.VISIBLE);
    }

    // --- Story Validation & Saving ---

    private void saveStory() {
        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String body = binding.etBody.getText() != null ? binding.etBody.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            binding.tilTitle.setError("Please enter a story title");
            return;
        } else {
            binding.tilTitle.setError(null);
        }

        if (familyMembers.isEmpty()) {
            showSnackbar("No family members found. Add family members first.");
            return;
        }

        int selectedPersonIndex = binding.spinnerPerson.getSelectedItemPosition();
        if (selectedPersonIndex < 0 || selectedPersonIndex >= familyMembers.size()) {
            showSnackbar("Please select a family member.");
            return;
        }

        PersonEntity selectedPerson = familyMembers.get(selectedPersonIndex);

        // Validation: must have either body, photo, or audio
        if (TextUtils.isEmpty(body) && TextUtils.isEmpty(attachedPhotoPath) && TextUtils.isEmpty(recordedAudioPath)) {
            showSnackbar("Please add story text, a photo, or an audio recording.");
            return;
        }

        String storyId = UUID.randomUUID().toString();
        String familyId = Prefs.getInstance(this).getFamilyId();
        String authorUid = Prefs.getInstance(this).getUserId();
        long now = System.currentTimeMillis();

        StoryEntity story = new StoryEntity(
                storyId,
                selectedPerson.getId(),
                familyId != null ? familyId : "",
                title,
                body,
                recordedAudioPath,
                recordedAudioDurationMs,
                attachedPhotoPath,
                now,
                authorUid,
                now,
                StoryEntity.SYNC_STATE_PENDING
        );

        storyViewModel.saveStory(story);
    }

    private String formatDuration(long ms) {
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", min, sec);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Dynamically register RecordingStateReceiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(recordingReceiver, RecordingStateReceiver.createIntentFilter(), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(recordingReceiver, RecordingStateReceiver.createIntentFilter());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(recordingReceiver);
        } catch (Exception ignored) {}

        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        stopPreviewPlayer();
    }
}
