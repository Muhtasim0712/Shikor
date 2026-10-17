package com.shikor.app.ui.story;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shikor.app.R;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.data.local.entities.StoryEntity;
import com.shikor.app.databinding.ActivityStoryDetailBinding;
import com.shikor.app.ui.base.BaseActivity;
import com.shikor.app.ui.person.PersonDetailActivity;
import com.shikor.app.ui.person.PersonViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * StoryDetailActivity - Displays full oral story, photo, and interactive MediaPlayer with SeekBar.
 *
 * CONCEPT: MediaPlayer playback with SeekBar tracking and scrubbing, lifecycle cleanup, and share/delete actions.
 */
public class StoryDetailActivity extends BaseActivity {

    public static final String EXTRA_STORY_ID = "extra_story_id";

    private ActivityStoryDetailBinding binding;
    private StoryViewModel storyViewModel;
    private PersonViewModel personViewModel;

    private String storyId;
    private StoryEntity currentStory;
    private PersonEntity currentPerson;

    private MediaPlayer mediaPlayer;
    private boolean isUserSeeking = false;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying() && !isUserSeeking) {
                int currentPos = mediaPlayer.getCurrentPosition();
                binding.seekBarAudio.setProgress(currentPos);
                binding.tvAudioCurrentTime.setText(formatDuration(currentPos));
                progressHandler.postDelayed(this, 100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemWindowInsets(binding.getRoot());

        storyId = getIntent().getStringExtra(EXTRA_STORY_ID);
        if (storyId == null || storyId.isEmpty()) {
            Toast.makeText(this, "Story ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        storyViewModel = new ViewModelProvider(this).get(StoryViewModel.class);
        personViewModel = new ViewModelProvider(this).get(PersonViewModel.class);

        setupClickListeners();
        observeStory();
    }

    private void setupClickListeners() {
        // Tagged person chip opens PersonDetailActivity
        binding.cardPersonTag.setOnClickListener(v -> {
            if (currentPerson != null) {
                Intent intent = new Intent(this, PersonDetailActivity.class);
                intent.putExtra(PersonDetailActivity.EXTRA_PERSON_ID, currentPerson.getId());
                startActivity(intent);
            }
        });

        // Audio Play / Pause
        binding.btnPlayPauseAudio.setOnClickListener(v -> toggleAudioPlayback());

        // Disallow NestedScrollView from intercepting SeekBar touch gestures
        binding.seekBarAudio.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false; // let SeekBar handle the actual touch/scrub
        });

        // Interactive SeekBar
        binding.seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    binding.tvAudioCurrentTime.setText(formatDuration(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
                progressHandler.removeCallbacks(updateProgressRunnable);
                seekBar.getParent().requestDisallowInterceptTouchEvent(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.getParent().requestDisallowInterceptTouchEvent(false);
                int targetProgress = seekBar.getProgress();

                if (mediaPlayer == null) {
                    initializeMediaPlayer();
                }

                if (mediaPlayer != null) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            mediaPlayer.seekTo(targetProgress, MediaPlayer.SEEK_CLOSEST);
                        } else {
                            mediaPlayer.seekTo(targetProgress);
                        }
                    } catch (Exception e) {
                        isUserSeeking = false;
                    }

                    mediaPlayer.setOnSeekCompleteListener(mp -> {
                        isUserSeeking = false;
                        try {
                            binding.tvAudioCurrentTime.setText(formatDuration(mp.getCurrentPosition()));
                            binding.seekBarAudio.setProgress(mp.getCurrentPosition());
                            if (mp.isPlaying()) {
                                progressHandler.removeCallbacks(updateProgressRunnable);
                                progressHandler.post(updateProgressRunnable);
                            }
                        } catch (Exception ignored) {}
                    });

                    // Safety timeout in case onSeekComplete is delayed or skipped
                    progressHandler.postDelayed(() -> {
                        if (isUserSeeking) {
                            isUserSeeking = false;
                            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                progressHandler.removeCallbacks(updateProgressRunnable);
                                progressHandler.post(updateProgressRunnable);
                            }
                        }
                    }, 500);
                } else {
                    isUserSeeking = false;
                }
            }
        });

        // Share Story
        binding.btnShareStory.setOnClickListener(v -> shareStory());

        // Delete Story
        binding.btnDeleteStory.setOnClickListener(v -> confirmDeleteStory());
    }

    private void observeStory() {
        storyViewModel.getStoryById(storyId).observe(this, story -> {
            if (story != null) {
                currentStory = story;
                populateUI(story);
                observePerson(story.getPersonId());
            }
        });
    }

    private void observePerson(String personId) {
        personViewModel.getPerson(personId).observe(this, person -> {
            if (person != null) {
                currentPerson = person;
                binding.tvPersonName.setText(person.getFullName());
                binding.avatarPerson.setGender(person.getGender());
                binding.avatarPerson.setDeceased(person.isDeceased());
                binding.avatarPerson.setInitials(person.getInitials());
                binding.avatarPerson.setPhotoPath(person.getPhotoPath());
                binding.cardPersonTag.setVisibility(View.VISIBLE);
            }
        });
    }

    private void populateUI(StoryEntity story) {
        binding.toolbar.setTitle(story.getTitle());
        binding.tvStoryTitle.setText(story.getTitle());
        binding.tvStoryDate.setText(dateFormat.format(new Date(story.getCreatedAt())));

        // Body
        if (story.getBody() != null && !story.getBody().trim().isEmpty()) {
            binding.tvStoryBody.setText(story.getBody());
            binding.cardStoryBody.setVisibility(View.VISIBLE);
        } else {
            binding.cardStoryBody.setVisibility(View.GONE);
        }

        // Photo
        if (story.getPhotoPath() != null && !story.getPhotoPath().trim().isEmpty()) {
            File photoFile = new File(story.getPhotoPath());
            binding.cardStoryPhoto.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(photoFile.exists() ? photoFile : story.getPhotoPath())
                    .into(binding.ivStoryPhoto);
        } else {
            binding.cardStoryPhoto.setVisibility(View.GONE);
        }

        // Audio
        if (story.getAudioPath() != null && !story.getAudioPath().trim().isEmpty()) {
            binding.cardAudioPlayer.setVisibility(View.VISIBLE);
            long duration = story.getAudioDurationMs();
            binding.tvAudioTotalTime.setText(formatDuration(duration));
            binding.seekBarAudio.setMax((int) duration);
            binding.seekBarAudio.setProgress(0);
            binding.tvAudioStatus.setText("Ready to play");
            initializeMediaPlayer();
        } else {
            binding.cardAudioPlayer.setVisibility(View.GONE);
        }
    }

    private void initializeMediaPlayer() {
        if (currentStory == null || currentStory.getAudioPath() == null) return;
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(currentStory.getAudioPath());
                mediaPlayer.prepare();

                int totalDuration = mediaPlayer.getDuration();
                binding.seekBarAudio.setMax(totalDuration);
                binding.tvAudioTotalTime.setText(formatDuration(totalDuration));

                mediaPlayer.setOnCompletionListener(mp -> {
                    binding.btnPlayPauseAudio.setImageResource(android.R.drawable.ic_media_play);
                    binding.tvAudioStatus.setText("Finished");
                    binding.seekBarAudio.setProgress(0);
                    binding.tvAudioCurrentTime.setText("00:00");
                    progressHandler.removeCallbacks(updateProgressRunnable);
                });
            }
        } catch (Exception e) {
            android.util.Log.e("StoryDetailActivity", "Failed to initialize media player", e);
        }
    }

    private void toggleAudioPlayback() {
        if (currentStory == null || currentStory.getAudioPath() == null) return;

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            pauseAudio();
        } else {
            playAudio();
        }
    }

    private void playAudio() {
        try {
            if (mediaPlayer == null) {
                initializeMediaPlayer();
            }

            if (mediaPlayer != null) {
                mediaPlayer.start();
                binding.btnPlayPauseAudio.setImageResource(android.R.drawable.ic_media_pause);
                binding.tvAudioStatus.setText("Playing...");
                progressHandler.removeCallbacks(updateProgressRunnable);
                progressHandler.post(updateProgressRunnable);
            }
        } catch (Exception e) {
            showSnackbar("Failed to play audio: " + e.getMessage());
            releaseMediaPlayer();
        }
    }

    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            binding.btnPlayPauseAudio.setImageResource(android.R.drawable.ic_media_play);
            binding.tvAudioStatus.setText("Paused");
            progressHandler.removeCallbacks(updateProgressRunnable);
        }
    }

    private void releaseMediaPlayer() {
        progressHandler.removeCallbacks(updateProgressRunnable);
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        if (binding != null) {
            binding.btnPlayPauseAudio.setImageResource(android.R.drawable.ic_media_play);
            binding.tvAudioStatus.setText("Ready");
        }
    }

    private void shareStory() {
        if (currentStory == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append(currentStory.getTitle()).append("\n");
        if (currentPerson != null) {
            sb.append("A memory of: ").append(currentPerson.getFullName()).append("\n\n");
        }
        if (currentStory.getBody() != null) {
            sb.append(currentStory.getBody()).append("\n\n");
        }
        sb.append("— Preserved in Shikor Family Archive");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentStory.getTitle() + " - Shikor");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Story"));
    }

    private void confirmDeleteStory() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Story")
                .setMessage("Are you sure you want to delete this family memory? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (currentStory != null) {
                        releaseMediaPlayer();
                        storyViewModel.deleteStory(currentStory);
                        Toast.makeText(this, "Story deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String formatDuration(long ms) {
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", min, sec);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseAudio();
    }

    @Override
    protected void onDestroy() {
        releaseMediaPlayer();
        super.onDestroy();
    }
}
